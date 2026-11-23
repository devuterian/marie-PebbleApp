package coredevices.pebble.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coredevices.pebble.rememberLibPebble
import io.rebble.libpebblecommon.permissions.PluginDefinedPermission
import io.rebble.libpebblecommon.permissions.RequestedPermissions
import io.rebble.libpebblecommon.permissions.permissionKind
import coredevices.util.relativeTime
import io.rebble.libpebblecommon.permissions.AppPermissionStatus
import io.rebble.libpebblecommon.permissions.InstallReview
import io.rebble.libpebblecommon.permissions.PermissionKind
import io.rebble.libpebblecommon.permissions.PermissionPolicy
import io.rebble.libpebblecommon.permissions.PermissionStatus
import io.ktor.http.decodeURLPart
import io.rebble.libpebblecommon.permissions.domainOf
import io.rebble.libpebblecommon.permissions.declarationOf
import io.rebble.libpebblecommon.permissions.describesRequired
import io.rebble.libpebblecommon.permissions.fileScopeOf
import io.rebble.libpebblecommon.permissions.isRequired
import io.rebble.libpebblecommon.permissions.packageOf
import io.rebble.libpebblecommon.permissions.permissionOf
import io.rebble.libpebblecommon.plugin.PluginPermissions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid
import localization.isKoreanUi
import localization.localized

@Composable
fun ReplaceAppDialog(replaced: ReplacedApp, onAnswer: (replace: Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAnswer(false) },
        title = { Text(localized("Replace ${replaced.title}?", "${replaced.title} 앱을 바꿀까요?")) },
        text = {
            Text(
                localized(
"${replaced.title} is installed from ${replaced.installedFrom}. This copy is from " +
                    "${replaced.newFrom}, and may not be from the same developer.\n\n" +
                    "Replacing it erases everything the installed app has stored, including its settings, " +
                    "sign-ins and permissions, on this phone and on connected watches.",
"${replaced.title} 앱은 ${replaced.installedFrom}에서 설치했어요. 이 사본은 ${replaced.newFrom}에서 받은 것이라 같은 개발자가 만든 게 아닐 수도 있어요.\n\n바꾸면 설치된 앱이 저장한 설정, 로그인 정보, 권한 등 모든 데이터가 이 휴대폰과 연결된 시계에서 지워져요.",
)
            )
        },
        confirmButton = { TextButton(onClick = { onAnswer(true) }) { Text(localized("Replace", "바꾸기")) } },
        dismissButton = { TextButton(onClick = { onAnswer(false) }) { Text(localized("Cancel", "취소")) } },
    )
}

/** [onAnswer] gets null if the install was cancelled. */
@Composable
fun InstallReviewDialog(review: InstallReview, onAnswer: (Map<String, Boolean>?) -> Unit) {
    val defined = rememberPluginPermissions()
    val decisions = remember(review) { mutableStateMapOf<String, Boolean>() }
    val policy = rememberLibPebble().config.collectAsState().value.watchConfig.permissionPolicy
    // Allowing WriteFiles includes reading, so ReadFiles follows it.
    fun decisionOf(key: String): Boolean? =
        if (key == PluginPermissions.READ_FILES && decisions[PluginPermissions.WRITE_FILES] == true) true else decisions[key]
    AlertDialog(
        onDismissRequest = { onAnswer(null) },
        // The default width leaves a permission row too little room.
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp),
        title = { Text(localized("Install ${review.appName}?", "${review.appName} 앱을 설치할까요?")) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                // The dialog is surfaceContainerHigh, a group's usual colour.
                val cardColor = MaterialTheme.colorScheme.surfaceContainerLowest
                @Composable
                fun Group(title: String?, keys: List<String>, automatic: Boolean, subtitle: String? = null) {
                    CardGroup(
                        title,
                        keys,
                        subtitle = subtitle,
                        containerColor = cardColor,
                        dividerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) { key ->
                        PermissionRow(
                            key,
                            defined,
                            reason = review.reasonFor(key),
                            required = automatic && review.isRequired(key),
                            modifier = Modifier.padding(horizontal = 12.dp),
                        ) {
                            val decision = decisionOf(key)
                            DecisionChoice(decision, enabled = decision == decisions[key], automatic = automatic) { decisions[key] = it }
                        }
                    }
                }
                // At the end of what it allows, so it's only reached after scrolling past every permission.
                @Composable
                fun AllowAll(label: String, keys: List<String>) {
                    TextButton(
                        onClick = { keys.forEach { decisions[it] = true } },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text(label) }
                }
                val toReview = review.toReview.byKind(defined)
                if (review.describesRequired) {
                    val (required, optional) = toReview.partition(review::isRequired)
                    if (required.isNotEmpty()) {
                        Group(localized("Required", "필수"), required, automatic = false)
                        if (optional.isNotEmpty()) AllowAll(localized("Allow all required", "필수 권한 모두 허용"), required)
                    }
                    if (optional.isNotEmpty()) {
                        Group(localized("Optional", "선택"), optional, automatic = false, subtitle = localized("Decide now, or when ${review.appName} first uses them.", "지금 정하거나 ${review.appName} 앱이 처음 쓸 때 정할 수 있어요."))
                    }
                } else if (toReview.isNotEmpty()) {
                    Group(localized("It wants to", "요청한 권한"), toReview, automatic = false)
                }
                if (toReview.isNotEmpty()) AllowAll(localized("Allow all", "모두 허용"), toReview)
                if (review.automatic.isNotEmpty()) {
                    var expanded by remember(review) { mutableStateOf(false) }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(start = 4.dp),
                        ) {
                            Text(
                                localized("Allowed automatically (${review.automatic.size})", "자동 허용됨(${review.automatic.size})"),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (expanded) localized("Collapse", "접기") else localized("Expand", "펼치기"),
                            )
                        }
                        if (expanded) {
                            Group(
                                null,
                                review.automatic.requiredFirst(defined, { it }, review::isRequired),
                                automatic = true,
                                subtitle = automaticExplanation(review.appName, policy, review.automatic),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Optional ones left undecided are asked about when the app uses them.
                    onAnswer((review.toReview + review.automatic).mapNotNull { key -> decisionOf(key)?.let { key to it } }.toMap())
                },
                enabled = review.toReview.filter(review::needsAnswer).all { decisionOf(it) != null },
            ) { Text(localized("Install", "설치")) }
        },
        dismissButton = { TextButton(onClick = { onAnswer(null) }) { Text(localized("Cancel", "취소")) } },
    )
}

// Health can also be allowed because the app was built before the SDK checked it.
private fun automaticExplanation(appName: String, policy: PermissionPolicy, keys: List<String>) = when {
    policy == PermissionPolicy.NeverAsk || PluginPermissions.HEALTH in keys ->
        localized("You don't need to do anything here: your settings allow these. Deny any you'd rather $appName didn't use.", "여기서는 따로 할 일이 없어요. 지금 설정에서 이 권한들을 허용해요. ${appName} 앱이 쓰지 않았으면 하는 권한은 거부할 수 있어요.")
    else ->
        localized("You don't need to do anything here. $appName can't send data off your phone, so these are allowed under your current settings. Deny any you'd rather it didn't use.", "여기서는 따로 할 일이 없어요. ${appName} 앱은 휴대폰 밖으로 데이터를 보낼 수 없어서 지금 설정에서 이 권한들을 허용해요. 쓰지 않았으면 하는 권한은 거부할 수 있어요.")
}

/** The label shares its line with the control; the reason and [status] get the width below it. */
@Composable
internal fun PermissionRow(
    key: String,
    defined: Map<String, PluginDefinedPermission>,
    status: String? = null,
    label: String = permissionLabel(key, defined),
    reason: String? = null,
    required: Boolean = false,
    modifier: Modifier = Modifier,
    control: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                permissionIcon(key, defined),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (required) RequiredTag()
                when (permissionKind(key, defined)) {
                    PermissionKind.Exfiltrating ->
                        KindInfo(Icons.Outlined.Info, localized("Sends data off your phone", "휴대폰 밖으로 데이터 전송"), exfiltratingExplanation(key))
                    PermissionKind.Dangerous ->
                        KindInfo(Icons.Filled.Warning, localized("Sensitive", "민감한 권한"), localized("Sensitive permissions always need your approval, whatever your settings.", "민감한 권한은 설정과 관계없이 항상 승인이 필요해요."))
                    else -> {}
                }
                if (permissionOf(key) == PluginPermissions.HEALTH) {
                    lastConnectedWatch()?.takeUnless { enforcesHealth(it.runningFwVersion) }?.let { watch ->
                        KindInfo(
                            Icons.Filled.Warning,
                            localized("Not enforced on ${watch.displayName()}", "${watch.displayName()}에서는 적용 안 됨"),
                            localized("${watch.displayName()} can't restrict health data, so apps can read it whatever you choose here.", "${watch.displayName()} 시계는 건강 데이터 접근을 막을 수 없어서, 여기서 어떻게 정하든 앱이 건강 데이터를 읽을 수 있어요."),
                        )
                    }
                }
            }
            control()
        }
        if (reason != null || status != null) {
            // Lined up with the label, past the icon.
            Column(modifier = Modifier.padding(start = 32.dp)) {
                reason?.let { ReasonText(label, it) }
                status?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(end = 4.dp).size(12.dp),
                        )
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequiredTag() {
    Text(
        localized("Required", "필수"),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}

/** A developer's reason can be any length: two lines here, tapped for the rest. */
@Composable
internal fun ReasonText(title: String, reason: String) {
    var truncated by remember(reason) { mutableStateOf(false) }
    var showing by remember(reason) { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (truncated) Modifier.clickable { showing = true } else Modifier,
    ) {
        Text(
            reason,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { truncated = it.hasVisualOverflow },
            modifier = Modifier.weight(1f, fill = false),
        )
        if (truncated) ReasonIcon(title, Modifier.padding(start = 4.dp).size(16.dp))
    }
    if (showing) ReasonDialog(title, reason) { showing = false }
}

/** Shows [reason] in full when tapped. */
@Composable
internal fun ReasonInfoButton(title: String, reason: String, modifier: Modifier = Modifier) {
    var showing by remember(reason) { mutableStateOf(false) }
    ReasonIcon(title, modifier.clickable { showing = true })
    if (showing) ReasonDialog(title, reason) { showing = false }
}

/** "?", so it isn't confused with the kind's info icon. */
@Composable
private fun ReasonIcon(title: String, modifier: Modifier) {
    Icon(
        Icons.AutoMirrored.Outlined.HelpOutline,
        contentDescription = localized("Why $title", "${title} 요청 이유"),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun ReasonDialog(title: String, reason: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(reason, modifier = Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(localized("OK", "확인")) } },
    )
}

private val FIRMWARE_MAJOR_MINOR = Regex("v?(\\d+)\\.(\\d+)")

/** Broadly, the watches that can run newer firmware. */
private fun enforcesHealth(firmware: String): Boolean {
    val (major, minor) = FIRMWARE_MAJOR_MINOR.find(firmware)?.destructured ?: return true
    return major.toInt() > 4 || (major.toInt() == 4 && minor.toInt() >= 40)
}

@Composable
private fun KindInfo(icon: ImageVector, title: String, explanation: String) {
    var explaining by remember { mutableStateOf(false) }
    Icon(
        icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = 6.dp).size(18.dp).clickable { explaining = true },
    )
    if (explaining) {
        AlertDialog(
            onDismissRequest = { explaining = false },
            title = { Text(title) },
            text = { Text(explanation) },
            confirmButton = { TextButton(onClick = { explaining = false }) { Text(localized("OK", "확인")) } },
        )
    }
}

internal fun kindRank(key: String, defined: Map<String, PluginDefinedPermission>) = when (permissionKind(key, defined)) {
    PermissionKind.Dangerous -> 0
    PermissionKind.Exfiltrating -> 1
    PermissionKind.Write -> 2
    PermissionKind.Read -> 3
    null -> 4
}

internal fun List<String>.byKind(defined: Map<String, PluginDefinedPermission>) = sortedBy { kindRank(it, defined) }

/** Required ones first: the user can't usefully skip them. */
internal fun <T> List<T>.requiredFirst(
    defined: Map<String, PluginDefinedPermission>,
    key: (T) -> String,
    required: (T) -> Boolean,
) = sortedWith(compareByDescending<T> { required(it) }.thenBy { kindRank(key(it), defined) })

private fun exfiltratingExplanation(key: String) = when (permissionOf(key)) {
    PluginPermissions.GLOBAL_INTERNET ->
        localized("Allow this app to connect to any site on the internet, because it was built before the permissions system was created.", "권한 시스템이 생기기 전에 만든 앱이라 인터넷의 모든 사이트에 연결하도록 허용해요.")
    PluginPermissions.LEGACY_PEBBLEKIT, PluginPermissions.PEBBLEKIT_2 ->
        localized("This allows the app to send data to its phone app, which can send it anywhere.", "허용하면 앱이 휴대폰 앱으로 데이터를 보낼 수 있고, 휴대폰 앱은 그 데이터를 어디로든 보낼 수 있어요.")
    else -> localized("This allows the app to send data off your device - limited to the specified location", "허용하면 앱이 기기 밖으로 데이터를 보낼 수 있어요. 보낼 수 있는 곳은 지정된 위치로 제한돼요")
}

private fun permissionIcon(key: String, defined: Map<String, PluginDefinedPermission>): ImageVector = when (permissionOf(key)) {
    PluginPermissions.INTERNET, PluginPermissions.GLOBAL_INTERNET -> Icons.Filled.Public
    PluginPermissions.LOCAL_NETWORK -> Icons.Filled.Router
    PluginPermissions.LOCATION -> Icons.Filled.LocationOn
    PluginPermissions.WATCH_MICROPHONE -> Icons.Filled.Mic
    PluginPermissions.HEALTH -> Icons.Filled.MonitorHeart
    PluginPermissions.NOTIFICATIONS -> Icons.Filled.Notifications
    PluginPermissions.CALENDAR -> Icons.Filled.CalendarMonth
    PluginPermissions.CONTACTS -> Icons.Filled.Contacts
    PluginPermissions.TIMELINE -> Icons.Filled.ViewTimeline
    PluginPermissions.SHOW_NOTIFICATIONS -> Icons.Filled.NotificationsActive
    PluginPermissions.READ_WATCH_SETTINGS, PluginPermissions.WRITE_WATCH_SETTINGS -> Icons.Filled.Watch
    PluginPermissions.READ_MUSIC, PluginPermissions.CONTROL_MUSIC -> Icons.Filled.MusicNote
    PluginPermissions.READ_APPS, PluginPermissions.LAUNCH_APPS -> Icons.Filled.Apps
    PluginPermissions.FIND_MY_PHONE -> Icons.Filled.PhonelinkRing
    PluginPermissions.SET_INDEX_ALARMS -> Icons.Filled.Alarm
    PluginPermissions.SEND_MESSAGES_BEEPER -> Icons.AutoMirrored.Filled.Message
    PluginPermissions.WRITE_INDEX_NOTES -> Icons.AutoMirrored.Filled.Note
    PluginPermissions.WRITE_INDEX_REMINDERS -> Icons.Filled.Checklist
    PluginPermissions.READ_FILES, PluginPermissions.WRITE_FILES -> Icons.Filled.Folder
    PluginPermissions.LEGACY_PEBBLEKIT -> Icons.Filled.Campaign
    PluginPermissions.PEBBLEKIT_2 -> Icons.Filled.SwapHoriz
    else -> if (permissionKind(key, defined) == PermissionKind.Exfiltrating) Icons.Filled.CloudUpload else Icons.Filled.Extension
}

/** Completes "<app> wants to". */
fun permissionLabel(key: String, defined: Map<String, PluginDefinedPermission>): String = when (permissionOf(key)) {
    PluginPermissions.INTERNET -> domainOf(key)?.let { localized("Connect to $it", "${it}에 연결") } ?: localized("Connect to any website", "모든 웹사이트에 연결")
    PluginPermissions.LOCAL_NETWORK -> localized("Connect to devices on your local network", "로컬 네트워크의 기기에 연결")
    PluginPermissions.GLOBAL_INTERNET -> localized("Connect to any website or local device", "모든 웹사이트나 로컬 기기에 연결")
    PluginPermissions.LOCATION -> localized("Use your location", "위치 사용")
    PluginPermissions.WATCH_MICROPHONE -> localized("Use your watch microphone", "시계 마이크 사용")
    PluginPermissions.HEALTH -> localized("Read your health data", "건강 데이터 읽기")
    PluginPermissions.NOTIFICATIONS -> packageOf(key)?.let { localized("Read your notifications from $it", "${it} 알림 읽기") } ?: localized("Read all your notifications", "모든 알림 읽기")
    PluginPermissions.CALENDAR -> localized("Read your calendar", "캘린더 읽기")
    PluginPermissions.CONTACTS -> localized("Read your contacts", "연락처 읽기")
    PluginPermissions.TIMELINE -> localized("Add pins to your timeline", "타임라인에 핀 추가")
    PluginPermissions.SHOW_NOTIFICATIONS -> localized("Show notifications on your watch", "시계에 알림 표시")
    PluginPermissions.READ_WATCH_SETTINGS -> localized("Read your watch settings", "시계 설정 읽기")
    PluginPermissions.WRITE_WATCH_SETTINGS -> localized("Change your watch settings", "시계 설정 변경")
    PluginPermissions.READ_MUSIC -> localized("See what music is playing", "재생 중인 음악 보기")
    PluginPermissions.CONTROL_MUSIC -> localized("Control music playback", "음악 재생 제어")
    PluginPermissions.READ_APPS -> localized("See which apps and watchfaces you have", "설치된 앱과 워치페이스 보기")
    PluginPermissions.LAUNCH_APPS -> localized("Launch apps on your watch", "시계에서 앱 실행")
    PluginPermissions.FIND_MY_PHONE -> localized("Ring your phone to find it", "벨을 울려 휴대폰 찾기")
    PluginPermissions.SET_INDEX_ALARMS -> localized("Set alarms and timers on your phone via Index", "Index로 휴대폰 알람과 타이머 설정")
    PluginPermissions.SEND_MESSAGES_BEEPER -> localized("Send messages to your contacts through Index & Beeper", "Index와 Beeper로 연락처에 메시지 보내기")
    PluginPermissions.WRITE_INDEX_NOTES -> localized("Save Index notes for you", "Index 메모 저장")
    PluginPermissions.WRITE_INDEX_REMINDERS -> localized("Add Index reminders and list items for you", "Index 리마인더와 목록 항목 추가")
    PluginPermissions.READ_FILES -> fileScopeOf(key)?.let { localized("Read '${fileScopeLabel(it)}' in device storage", "기기 저장공간의 '${fileScopeLabel(it)}' 읽기") } ?: localized("Read files from device storage you permit", "허용한 기기 저장공간 파일 읽기")
    PluginPermissions.WRITE_FILES -> fileScopeOf(key)?.let { localized("Read & write to '${fileScopeLabel(it)}' in device storage", "기기 저장공간의 '${fileScopeLabel(it)}' 읽기 및 쓰기") } ?: localized("Read & write to files in device storage you permit", "허용한 기기 저장공간 파일 읽기 및 쓰기")
    PluginPermissions.LEGACY_PEBBLEKIT -> localized("Broadcast messages to every app on your phone (legacy PebbleKit)", "휴대폰의 모든 앱에 메시지 보내기(이전 PebbleKit)")
    PluginPermissions.PEBBLEKIT_2 -> localized("Exchange messages with ${packageOf(key)}", "${packageOf(key)} 앱과 메시지 주고받기")
    else -> defined[key]?.let { localized("${it.label} (via ${it.pluginName})", "${it.label}(${it.pluginName} 제공)") } ?: key
}

/** The tail of a picked entry: `Documents/Notes` out of a SAF uri or an iOS path. */
internal fun fileScopeLabel(scope: String): String {
    val decoded = scope.decodeURLPart()
    return decoded.substringAfterLast(':').trimEnd('/').split('/').filter { it.isNotEmpty() }
        .takeLast(2).joinToString("/").ifEmpty { decoded }
}

@Composable
internal fun rememberPluginPermissions(): Map<String, PluginDefinedPermission> {
    val libPebble = rememberLibPebble()
    return remember { libPebble.pluginPermissions() }.collectAsState(emptyMap()).value
}

fun requestLabels(keys: List<String>, defined: Map<String, PluginDefinedPermission>): List<String> =
    keys.map { permissionLabel(it, defined).replaceFirstChar { c -> c.lowercase() } }

fun requestSummary(labels: List<String>, korean: Boolean = isKoreanUi()): String =
    if (labels.size <= 1) labels.joinToString() else labels.dropLast(1).joinToString() + (if (korean) ", " else " and ") + labels.last()

fun requestSummary(keys: List<String>, defined: Map<String, PluginDefinedPermission>): String =
    requestSummary(requestLabels(keys, defined))

@Composable
fun PendingPermissionRequests(onReview: (Uuid) -> Unit) {
    val libPebble = rememberLibPebble()
    val defined = rememberPluginPermissions()
    val active by remember {
        libPebble.activePermissionRequests().map { requests -> requests.filter { it.seenAtMs == null } }
    }.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        active.groupBy { it.appUuid }.forEach { (appUuid, requests) ->
            val keys = requests.map { it.permission }
            val appName by remember(appUuid) {
                libPebble.getLockerApp(appUuid).map { it?.properties?.title }
            }.collectAsState(null)
            val reason by remember(appUuid, keys) {
                libPebble.appPermissionStatuses(appUuid).map { statuses ->
                    keys.singleOrNull()?.let { key -> statuses?.firstOrNull { it.permission == key }?.reason }
                }
            }.collectAsState(null)
            Card(onClick = { onReview(appUuid) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp)) {
                    Text(
                        localized("${appName ?: "An app"} wants to ${requestSummary(keys, defined)}", "${appName ?: "앱"}에서 권한을 요청해요: ${requestSummary(keys, defined)}"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    reason?.let { ReasonText(appName ?: localized("Why", "이유"), it) }
                    Row(modifier = Modifier.align(Alignment.End)) {
                        if (keys.size == 1) {
                            fun answer(granted: Boolean) = scope.launch {
                                libPebble.setPermissionDecisions(appUuid, mapOf(keys.single() to granted))
                            }
                            TextButton(onClick = { answer(false) }) { Text(localized("Deny", "거부")) }
                            TextButton(onClick = { answer(true) }) { Text(localized("Allow", "허용")) }
                        } else {
                            TextButton(onClick = { onReview(appUuid) }) { Text(localized("Review", "검토")) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppPermissionsSection(appUuid: Uuid, collapsible: Boolean = true) {
    val libPebble = rememberLibPebble()
    val defined = rememberPluginPermissions()
    // Null while loading.
    val loaded by remember(appUuid) {
        libPebble.appPermissionStatuses(appUuid).map { all -> LoadedStatuses(all?.sortedBy { it.status != PermissionStatus.Waiting }) }
    }.collectAsState(null)
    // Leaving the page stops asking about anything still waiting.
    val current by rememberUpdatedState(loaded?.statuses)
    DisposableEffect(appUuid) {
        onDispose {
            val unanswered = current.orEmpty().filter { it.status == PermissionStatus.Waiting }.map { it.permission }
            if (unanswered.isNotEmpty()) {
                GlobalScope.launch { libPebble.markPermissionRequestsSeen(appUuid, unanswered) }
            }
        }
    }
    val legacy by remember(appUuid) {
        libPebble.getLockerApp(appUuid).map { it?.properties?.legacy == true }
    }.collectAsState(false)
    // Null until the user toggles it: open, except for a legacy app's.
    var expandedByUser by remember(appUuid) { mutableStateOf<Boolean?>(null) }
    val statuses = (loaded ?: return).statuses
    if (statuses.isNullOrEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(localized("Permissions", "권한"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                if (statuses == null) localized("Shown once the app has been installed on your watch.", "시계에 앱을 설치하면 표시돼요.")
                else localized("This app doesn't use any permissions.", "이 앱은 권한을 사용하지 않아요."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // A legacy app's are collapsed, and never hidden while a request waits for an answer.
        val title = if (legacy) localized("Permissions (Legacy App)", "권한(이전 앱)") else localized("Permissions", "권한")
        if (collapsible) {
            val open = (legacy && statuses.any { it.status == PermissionStatus.Waiting }) || (expandedByUser ?: !legacy)
            SectionHeader(title, open) { expandedByUser = !open }
            if (!open) return@Column
        } else {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        }
        if (legacy) {
            Text(
                localized(
"Apps built before permissions existed can use these by default. To be asked first, turn on " +
                    "\"Ask before older apps use permissions\" in Settings.",
"권한 시스템이 생기기 전에 만든 앱은 기본적으로 이 권한을 쓸 수 있어요. 먼저 물어보게 하려면 설정에서 \"오래된 앱이 권한을 쓰기 전에 묻기\"를 켜 주세요.",
),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        @Composable
        fun StatusRow(row: AppPermissionStatus, inGroups: Boolean) {
            val lastUsedAtMs = row.lastUsedAtMs
            val used = if (lastUsedAtMs == null) localized("Not used yet", "아직 사용 안 함") else localized("Used ${relativeTime(Instant.fromEpochMilliseconds(lastUsedAtMs))}", "${relativeTime(Instant.fromEpochMilliseconds(lastUsedAtMs))} 사용")
            val automatic = row.status == PermissionStatus.AllowedAutomatically
            // Grouped, localized("Allowed automatically", "자동 허용됨") is the group's title.
            val note = if (automatic && !inGroups) localized("Allowed automatically · $used", "자동 허용됨 · ${used}") else used
            val decide = { granted: Boolean ->
                val decisions = buildMap {
                    put(row.permission, granted)
                    // Allowing WriteFiles includes reading, so ReadFiles follows it.
                    if (granted && row.permission == PluginPermissions.WRITE_FILES &&
                        statuses.any { it.permission == PluginPermissions.READ_FILES }
                    ) put(PluginPermissions.READ_FILES, true)
                }
                scope.launch { libPebble.setPermissionDecisions(appUuid, decisions) }
                Unit
            }
            val impliedByWrite = row.permission == PluginPermissions.READ_FILES &&
                statuses.any { it.permission == PluginPermissions.WRITE_FILES && it.status == PermissionStatus.Granted }
            // A picked entry follows its bare permission: off while that is denied, back when it's allowed again.
            val bareDenied = fileScopeOf(row.permission) != null && statuses.deniesBareFilePermission(permissionOf(row.permission))
            PermissionRow(
                row.permission,
                defined,
                note,
                reason = row.reason,
                required = automatic && inGroups && row.required == true,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                when {
                    impliedByWrite -> DecisionChoice(true, enabled = false) {}
                    bareDenied -> DecisionChoice(false, enabled = false) {}
                    else -> DecisionChoice(
                        when (row.status) {
                            PermissionStatus.Granted -> true
                            PermissionStatus.Denied -> false
                            else -> null
                        },
                        automatic = automatic,
                        onDecide = decide,
                    )
                }
            }
        }
        // Grouped only once the app says which are required.
        if (statuses.any { it.required != null }) {
            val (automatic, asked) = statuses.partition { it.status == PermissionStatus.AllowedAutomatically }
            val (required, optional) = asked.partition { it.required == true }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (required.isNotEmpty()) {
                    CardGroup(localized("Required", "필수"), required.sortedBy { kindRank(it.permission, defined) }) { StatusRow(it, inGroups = true) }
                }
                if (optional.isNotEmpty()) {
                    CardGroup(localized("Optional", "선택"), optional.sortedBy { kindRank(it.permission, defined) }) { StatusRow(it, inGroups = true) }
                }
                if (automatic.isNotEmpty()) {
                    CardGroup(
                        localized("Allowed automatically", "자동 허용됨"),
                        automatic.requiredFirst(defined, { it.permission }, { it.required == true }),
                    ) { StatusRow(it, inGroups = true) }
                }
            }
        } else {
            CardGroup(null, statuses.sortedBy { kindRank(it.permission, defined) }) { StatusRow(it, inGroups = false) }
        }
    }
}

/** A section's title, which collapses and expands it. */
@Composable
internal fun SectionHeader(title: String, open: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(bottom = 4.dp))
        Icon(
            if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (open) localized("Collapse", "접기") else localized("Expand", "펼치기"),
        )
    }
}

/** [statuses] is null until the app's permissions are known. */
private class LoadedStatuses(val statuses: List<AppPermissionStatus>?)

/** Whether the bare file permission reads as denied, with `WriteFiles` standing in for a `ReadFiles` the app never declared. */
private fun List<AppPermissionStatus>.deniesBareFilePermission(bare: String): Boolean {
    val own = firstOrNull { it.permission == bare }
    if (own != null) return own.status == PermissionStatus.Denied
    return bare == PluginPermissions.READ_FILES &&
        any { it.permission == PluginPermissions.WRITE_FILES && it.status == PermissionStatus.Denied }
}

/** Nothing is selected while [granted] is null, except that an [automatic] grant shows Allow faded. */
@Composable
private fun DecisionChoice(granted: Boolean?, enabled: Boolean = true, automatic: Boolean = false, onDecide: (Boolean) -> Unit) {
    val options = listOf(Triple(false, Icons.Filled.Close, localized("Deny", "거부")), Triple(true, Icons.Filled.Check, localized("Allow", "허용")))
    val implied = granted == null && automatic
    val impliedColors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
    )
    // Short, so the reason below sits close to the label: without the 48dp touch target's padding.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, (value, icon, label) ->
                val impliedAllow = implied && value
                SegmentedButton(
                    selected = granted == value || impliedAllow,
                    onClick = { onDecide(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    enabled = enabled,
                    colors = if (impliedAllow) impliedColors else SegmentedButtonDefaults.colors(),
                    icon = {},
                    modifier = Modifier.width(40.dp).height(32.dp),
                ) {
                    Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** What a store app asks for, before it's in the locker. */
@Composable
fun StorePermissionsSection(requested: RequestedPermissions) {
    val config by rememberLibPebble().config.collectAsState()
    val defined = rememberPluginPermissions()
    val keys = requested.all
    if (keys.isEmpty()) return
    var open by remember(requested) { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionHeader(localized("Permissions", "권한"), open) { open = !open }
        if (!open) return@Column
        if (requested.declared == null) {
            val access = keys.map(::legacyAccessName).distinct().joinToString()
            val text = if (config.watchConfig.askLegacyAppsForPermissions) {
                localized("Built before permissions: asks before using $access", "권한 시스템 이전 앱: ${access} 사용 전에 물어봐요")
            } else {
                localized("Built before permissions: has access to $access", "권한 시스템 이전 앱: ${access} 사용 가능")
            }
            Text(text, style = MaterialTheme.typography.bodyMedium)
        } else {
            val declared = requested.declared.orEmpty()
            val sorted = keys.byKind(defined)
            // Reasons are behind an info button here: this is an overview, not where the user decides.
            @Composable
            fun ListingRow(key: String) {
                val label = permissionLabel(key, defined)
                PermissionRow(key, defined, label = label, modifier = Modifier.padding(horizontal = 12.dp)) {
                    declared.declarationOf(key)?.reason?.let { ReasonInfoButton(label, it, Modifier.padding(start = 8.dp).size(20.dp)) }
                }
            }
            if (declared.describesRequired()) {
                val (required, optional) = sorted.partition(declared::isRequired)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (required.isNotEmpty()) CardGroup(localized("Required", "필수"), required) { ListingRow(it) }
                    if (optional.isNotEmpty()) CardGroup(localized("Optional", "선택"), optional) { ListingRow(it) }
                }
            } else {
                CardGroup(null, sorted) { ListingRow(it) }
            }
        }
    }
}

private fun legacyAccessName(key: String) = when (permissionOf(key)) {
    PluginPermissions.GLOBAL_INTERNET -> localized("the internet", "인터넷")
    PluginPermissions.LOCATION -> localized("location", "위치")
    PluginPermissions.TIMELINE -> localized("timeline pins", "타임라인 핀")
    PluginPermissions.SHOW_NOTIFICATIONS -> localized("watch notifications", "시계 알림")
    PluginPermissions.HEALTH -> localized("health data", "건강 데이터")
    else -> localized("phone apps", "휴대폰 앱")
}

/** A dot for anything waiting for an answer; "!" if something the app needs is among it. */
@Composable
fun PermissionRequestBadge(appUuid: Uuid, modifier: Modifier = Modifier) {
    val libPebble = rememberLibPebble()
    val pending by libPebble.appsWithPendingPermissionRequests.collectAsState()
    if (appUuid !in pending) return
    val requiredWaiting by remember(appUuid) {
        libPebble.appPermissionStatuses(appUuid).map { statuses ->
            statuses.orEmpty().any { it.required == true && it.status == PermissionStatus.Waiting }
        }
    }.collectAsState(false)
    if (requiredWaiting) {
        Badge(modifier = modifier) { Text("!") }
    } else {
        Badge(modifier = modifier)
    }
}
