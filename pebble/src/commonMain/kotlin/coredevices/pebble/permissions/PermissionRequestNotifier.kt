package coredevices.pebble.permissions

import coredevices.pebble.RealPebbleDeepLinkHandler
import coredevices.pebble.config.VisibleConfigPage
import coredevices.pebble.ui.requestLabels
import coredevices.pebble.ui.requestSummary
import coredevices.util.WatchNotificationAction
import coredevices.util.WatchNotifier
import coredevices.util.setWatchNotificationActionHandler
import io.rebble.libpebblecommon.connection.AppContext
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.packets.blobdb.TimelineAttribute
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid
import localization.isKoreanUi
import localization.localized

/** A running app's runtime requests, answered together by the notification's actions. */
data class PermissionRequestNotification(
    val key: Int,
    val appUuid: Uuid,
    val permissions: List<String>,
    val title: String,
    val body: String,
    /** False when [body] can't list them all, so they can only be allowed after reviewing them in the app. */
    val canAllow: Boolean,
    /** Opens the app's page, where its requests are answered. */
    val deepLink: String,
)

expect fun notifyPermissionRequest(appContext: AppContext, notification: PermissionRequestNotification)

expect fun removePermissionRequestNotification(appContext: AppContext, key: Int)

/**
 * One notification per running app with requests waiting on the user, removed once they're
 * answered (here or elsewhere in the app) or the app stops.
 */
class PermissionRequestNotifier(
    private val libPebble: LibPebble,
    private val appContext: AppContext,
    private val watchNotifier: WatchNotifier,
    private val visibleConfigPage: VisibleConfigPage,
) {
    /** Before the watch connects, so it can answer notifications sent before an app restart. */
    fun registerWatchActions() {
        listOf(true, false).forEach { granted ->
            libPebble.setWatchNotificationActionHandler(watchActionType(granted)) { data ->
                val (appUuid, permissions) = decodeWatchActionData(requireNotNull(data))
                answerPermissionRequests(libPebble, appUuid, permissions, granted)
            }
        }
    }

    fun init() {
        GlobalScope.launch {
            var shown = emptyMap<Uuid, List<String>>()
            libPebble.activePermissionRequests().collect { active ->
                val current = active.groupBy({ it.appUuid }, { it.permission })
                // Only something new is worth a notification: answering one of several shouldn't re-alert.
                // The app's open config page asks instead.
                current.filter { (appUuid, keys) ->
                    !shown[appUuid].orEmpty().containsAll(keys) && appUuid != visibleConfigPage.app.value
                }
                    .forEach { (appUuid, keys) -> notify(notificationFor(appUuid, keys)) }
                (shown.keys - current.keys).forEach { appUuid ->
                    removePermissionRequestNotification(appContext, keyOf(appUuid))
                    watchNotifier.remove(watchKey(appUuid))
                }
                shown = current
            }
        }
    }

    private fun notify(notification: PermissionRequestNotification) {
        notifyPermissionRequest(appContext, notification)
        val all = notification.permissions.size > 1
        val data = Json.encodeToString(listOf(notification.appUuid.toString()) + notification.permissions)
        watchNotifier.send(
            key = watchKey(notification.appUuid),
            title = notification.title,
            body = notification.body,
            actions = listOfNotNull(
                WatchNotificationAction(if (all) localized("Allow all", "모두 허용") else localized("Allow", "허용"), watchActionType(true), data).takeIf { notification.canAllow },
                WatchNotificationAction(if (all) localized("Deny all", "모두 거부") else localized("Deny", "거부"), watchActionType(false), data),
            ),
        )
    }

    private suspend fun notificationFor(appUuid: Uuid, keys: List<String>): PermissionRequestNotification {
        val appName = libPebble.getLockerApp(appUuid).first()?.properties?.title ?: localized("An app", "앱")
        val labels = requestLabels(keys, libPebble.pluginPermissions().first())
        val (body, complete) = requestBody(labels)
        return PermissionRequestNotification(
            key = keyOf(appUuid),
            appUuid = appUuid,
            permissions = keys,
            title = appName,
            body = body,
            canAllow = complete,
            deepLink = RealPebbleDeepLinkHandler.permissionRequestsUri(appUuid),
        )
    }

    companion object {
        fun keyOf(appUuid: Uuid) = "permissions/$appUuid".hashCode()

        /** As many of [labels] as the watch can show, then how many more; and whether that's all of them. */
        internal fun requestBody(labels: List<String>, korean: Boolean = isKoreanUi()): Pair<String, Boolean> {
            val summary = requestSummary(labels, korean)
            val full = if (korean) "권한 요청: $summary" else "Wants to $summary"
            return if (full.fitsOnWatch()) full to true else shortenedRequest(labels, korean) to false
        }

        private fun shortenedRequest(labels: List<String>, korean: Boolean): String =
            (labels.size - 1 downTo 1).asSequence()
                .map { shown ->
                    if (korean) {
                        "권한 요청: ${requestSummary(labels.take(shown), korean)} 외 ${labels.size - shown}개. Pebble 앱에서 확인해 주세요."
                    } else {
                        val listed = labels.take(shown) + "${labels.size - shown} more"
                        "Wants to ${requestSummary(listed, korean)}. Review them in the Pebble app."
                    }
                }
                .firstOrNull { it.fitsOnWatch() }
                ?: when {
                    korean -> "권한 ${labels.size}개를 요청해요. Pebble 앱에서 확인해 주세요."
                    labels.size == 1 -> "Wants a permission. Review it in the Pebble app."
                    else -> "Wants ${labels.size} permissions. Review them in the Pebble app."
                }

        private fun String.fitsOnWatch() = encodeToByteArray().size <= TimelineAttribute.Body.maxLength

        private fun watchKey(appUuid: Uuid) = "permission-request/$appUuid"

        private fun watchActionType(granted: Boolean) = if (granted) "permission_allow" else "permission_deny"

        private fun decodeWatchActionData(data: String): Pair<Uuid, List<String>> {
            val parts = Json.decodeFromString<List<String>>(data)
            return Uuid.parse(parts.first()) to parts.drop(1)
        }
    }
}

/** Answers those of [permissions] still waiting: the notification may be older than an answer given in the app. */
suspend fun answerPermissionRequests(libPebble: LibPebble, appUuid: Uuid, permissions: List<String>, granted: Boolean) {
    val waiting = libPebble.activePermissionRequests().first()
        .filter { it.appUuid == appUuid && it.permission in permissions }
        .map { it.permission }
    libPebble.setPermissionDecisions(appUuid, waiting.associateWith { granted })
}
