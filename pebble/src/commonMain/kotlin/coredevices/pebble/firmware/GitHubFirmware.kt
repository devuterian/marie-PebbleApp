package coredevices.pebble.firmware

import coredevices.pebble.services.HttpClientAuthType
import coredevices.pebble.services.PebbleHttpClient
import coredevices.pebble.services.PebbleHttpClient.Companion.get
import io.rebble.libpebblecommon.connection.FirmwareUpdateCheckResult
import io.rebble.libpebblecommon.services.FirmwareVersion
import io.rebble.libpebblecommon.services.WatchInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

class GitHubFirmware(private val httpClient: PebbleHttpClient) {
    suspend fun getLatestFirmware(watch: WatchInfo): FirmwareUpdateCheckResult {
        val release: GitHubFirmwareRelease? = httpClient.get(
            "https://api.github.com/repos/devuterian/PebbleOAO/releases/latest",
            auth = HttpClientAuthType.None,
        )
        return release?.updateFor(watch.platform.revision, watch.runningFwVersion)
            ?: FirmwareUpdateCheckResult.UpdateCheckFailed("깃허브에서 펌웨어 업데이트를 확인하지 못했어요. 잠시 후 다시 시도해주세요.")
    }
}

@Serializable
internal data class GitHubFirmwareRelease(
    @SerialName("tag_name") val tag: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val body: String? = null,
    val assets: List<GitHubFirmwareAsset>,
) {
    fun updateFor(hardware: String, running: FirmwareVersion): FirmwareUpdateCheckResult {
        if (draft || prerelease) return FirmwareUpdateCheckResult.FoundNoUpdate
        val revision = marieRevision(tag)
            ?: return FirmwareUpdateCheckResult.UpdateCheckFailed("릴리즈의 펌웨어 버전을 읽지 못했어요.")
        val asset = assets.singleOrNull { it.name == "normal_${hardware}_${tag}.pbz" }
            ?: return FirmwareUpdateCheckResult.UpdateCheckFailed("최신 정식 릴리즈에 이 시계용 펌웨어가 없어요.")
        val version = FirmwareVersion.from(
            tag = tag,
            isRecovery = false,
            gitHash = "",
            timestamp = Instant.fromEpochSeconds(0),
            isDualSlot = false,
            isSlot0 = false,
        ) ?: return FirmwareUpdateCheckResult.UpdateCheckFailed("릴리즈의 펌웨어 버전을 읽지 못했어요.")
        val baseComparison = compareValuesBy(version, running, { it.major }, { it.minor }, { it.patch })
        val newer = baseComparison > 0 ||
            (baseComparison == 0 && revision > (marieRevision(running.stringVersion) ?: 0))
        return if (running.isRecovery || newer) {
            FirmwareUpdateCheckResult.FoundUpdate(version, asset.url, body.orEmpty())
        } else {
            FirmwareUpdateCheckResult.FoundNoUpdate
        }
    }
}

@Serializable
internal data class GitHubFirmwareAsset(
    val name: String,
    @SerialName("browser_download_url") val url: String,
)

private val MARIE_VERSION = Regex("""^v?\d+\.\d+\.\d+-ver(\d+)-[a-z][a-z0-9-]*$""")

private fun marieRevision(tag: String): Int? =
    MARIE_VERSION.matchEntire(tag)?.groupValues?.get(1)?.toIntOrNull()
