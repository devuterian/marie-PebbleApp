package coredevices.coreapp.util

import PlatformUiContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import coredevices.util.CoreConfigFlow
import coredevices.util.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

// Asset names carry the independent app revision, not the firmware release revision.
data class GitHubAppAsset(val name: String, val url: String, val digest: String, val revision: Int)

class GitHubAppUpdate(
    private val settings: Settings,
    private val context: Context,
    private val config: CoreConfigFlow,
) : AppUpdate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val checkLock = Mutex()
    private val lastChecks = mutableMapOf<String, Long>()
    private val installLock = Mutex()
    private val state = MutableStateFlow<AppUpdateState>(AppUpdateState.NoUpdateAvailable)
    override val updateAvailable = state.asStateFlow()

    private fun message(english: String, korean: String): String =
        if (context.resources.configuration.locales[0].language == "ko") korean else english

    private fun toast(english: String, korean: String) {
        Toast.makeText(context, message(english, korean), Toast.LENGTH_LONG).show()
    }

    override suspend fun checkForUpdates(force: Boolean) = checkLock.withLock {
        val now = System.currentTimeMillis()
        val includePrereleases = config.value.prereleaseFirmwareWatches.isNotEmpty()
        val channel = if (includePrereleases) "preview" else "stable"
        if (!force && now - (lastChecks[channel] ?: 0L) < 6 * 60 * 60 * 1000L) return@withLock
        try {
            val installed = context.packageManager.getPackageInfo(context.packageName, 0)
            val revision = marieAppRevision(installed.versionName.orEmpty()) ?: 0
            val asset = withContext(Dispatchers.IO) {
                val connection = URL("https://api.github.com/repos/devuterian/PebbleOAO/releases?per_page=100").openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("User-Agent", "PebbleOAO-Android")
                try {
                    check(connection.responseCode == 200)
                    val releases = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
                    buildList {
                        for (i in 0 until releases.length()) {
                            val release = releases.getJSONObject(i)
                            if (release.optBoolean("draft") || (release.optBoolean("prerelease") && !includePrereleases)) continue
                            val assets = release.getJSONArray("assets")
                            for (j in 0 until assets.length()) {
                                val item = assets.getJSONObject(j)
                                val name = item.getString("name")
                                if (!name.startsWith("Pebble-") || !name.endsWith(".apk")) continue
                                val version = marieAppRevision(name) ?: continue
                                val url = item.getString("browser_download_url")
                                val digest = item.optString("digest")
                                if (version > revision && validAppAsset(url, digest)) add(GitHubAppAsset(name, url, digest, version))
                            }
                        }
                    }.maxByOrNull { it.revision }
                } finally { connection.disconnect() }
            }
            lastChecks[channel] = now
            state.value = asset?.let { AppUpdateState.UpdateAvailable(AppUpdatePlatformContent(githubAsset = it)) }
                ?: AppUpdateState.NoUpdateAvailable
            withContext(Dispatchers.Main) {
                if (asset != null) {
                    notifyUpdate(asset)
                    if (force) toast("Update found. Tap App Updates to install.", "새 버전을 찾았습니다. 앱 업데이트를 눌러 설치하십시오.")
                } else if (force) toast("You're using the latest app.", "최신 앱을 사용하고 있습니다.")
            }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            Logger.w(e) { "Could not check GitHub app updates" }
            if (force) withContext(Dispatchers.Main) { toast("Couldn't check for updates. Try again later.", "업데이트를 확인하지 못했습니다. 잠시 후 다시 시도하십시오.") }
        }
    }

    override fun startUpdateFlow(uiContext: PlatformUiContext, update: AppUpdatePlatformContent) {
        val asset = update.githubAsset ?: return
        if (installLock.isLocked) return
        scope.launch {
            installLock.withLock {
                if (Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
                    toast("Allow this app to install updates, then tap App Updates again.", "이 앱의 설치 권한을 허용한 뒤 앱 업데이트를 다시 누르십시오.")
                    uiContext.activity.startActivity(Intent(AndroidSettings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
                    return@withLock
                }
                toast("Downloading app update…", "앱 업데이트를 내려받는 중입니다…")
                val file = File(context.cacheDir, "app-updates/update.apk")
                try {
                    withContext(Dispatchers.IO) {
                        file.parentFile!!.mkdirs()
                        val connection = URL(asset.url).openConnection() as HttpURLConnection
                        connection.connectTimeout = 15000
                        connection.readTimeout = 60000
                        try {
                            check(connection.responseCode == 200)
                            connection.inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                        } finally { connection.disconnect() }
                        val hash = MessageDigest.getInstance("SHA-256")
                        file.inputStream().use { input ->
                            val buffer = ByteArray(65536)
                            while (true) { val n = input.read(buffer); if (n < 0) break; hash.update(buffer, 0, n) }
                        }
                        check("sha256:" + hash.digest().joinToString("") { "%02x".format(it) } == asset.digest)
                        val pm = context.packageManager
                        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
                        val current = pm.getPackageInfo(context.packageName, flags)
                        val candidate = checkNotNull(pm.getPackageArchiveInfo(file.path, flags))
                        check(candidate.packageName == current.packageName && PackageInfoCompat.getLongVersionCode(candidate) > PackageInfoCompat.getLongVersionCode(current))
                        val currentSigners = signingCertificates(current)
                        val candidateSigners = signingCertificates(candidate)
                        check(candidateSigners.isNotEmpty() && candidateSigners == currentSigners)
                    }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    uiContext.activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    })
                } catch (e: CancellationException) { file.delete(); throw e
                } catch (e: Exception) {
                    file.delete()
                    Logger.w(e) { "App update download or validation failed" }
                    toast("Update couldn't be installed. Check your connection and app signing key.", "업데이트를 설치하지 못했습니다. 인터넷 연결이나 앱 서명이 맞는지 확인하십시오.")
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun signingCertificates(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
        return signatures.orEmpty().map { it.toCharsString() }.toSet()
    }

    private fun notifyUpdate(asset: GitHubAppAsset) {
        if (settings.getInt("app_update_notified_revision", 0) >= asset.revision) return
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, message("App updates", "앱 업데이트"), NotificationManager.IMPORTANCE_DEFAULT))
        val pending = PendingIntent.getActivity(context, 12346, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            manager.notify(3006089, NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(message("Pebble app update available", "페블 앱 새 버전이 나왔습니다"))
                .setContentText(message("Open Settings → App Updates to install.", "설정 → 앱 업데이트에서 설치하십시오."))
                .setContentIntent(pending).setAutoCancel(true).build())
            settings.putInt("app_update_notified_revision", asset.revision)
        } catch (e: SecurityException) { Logger.w(e) { "App update notification permission unavailable" } }
    }

    companion object { private const val CHANNEL = "app_update_channel" }
}
