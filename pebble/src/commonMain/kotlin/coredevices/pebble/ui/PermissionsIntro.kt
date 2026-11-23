package coredevices.pebble.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import coredevices.pebble.rememberLibPebble
import coredevices.util.CoreConfigHolder
import io.rebble.libpebblecommon.permissions.PermissionPolicy
import localization.localized
import org.koin.compose.koinInject

private const val HAS_SEEN_PERMISSIONS_INTRO_SETTINGS_KEY = "hasSeenPermissionsIntro"

fun Settings.hasSeenPermissionsIntro() = getBoolean(HAS_SEEN_PERMISSIONS_INTRO_SETTINGS_KEY, false)
fun Settings.setHasSeenPermissionsIntro(seen: Boolean) = set(HAS_SEEN_PERMISSIONS_INTRO_SETTINGS_KEY, seen)

fun PermissionPolicy.displayName(): String = when (this) {
    PermissionPolicy.AskIfExfiltrating -> localized("When needed", "필요할 때")
    PermissionPolicy.AlwaysAsk -> localized("Always", "항상")
    PermissionPolicy.NeverAsk -> localized("Never", "안 함")
}

fun PermissionPolicy.description(): String = when (this) {
    PermissionPolicy.AskIfExfiltrating ->
        localized("Apps ask before connecting to the internet or acting for you (e.g. controlling your music or ringing your phone)", "앱이 인터넷에 연결하거나 음악 제어, 휴대폰 벨 울리기처럼 대신 동작하기 전에 물어봐요")
    PermissionPolicy.AlwaysAsk -> localized("Apps ask before using any permission.", "앱이 권한을 쓰기 전에 항상 물어봐요.")
    PermissionPolicy.NeverAsk -> localized("Apps can use any permission except reading notifications, unless you explicitly turn it off on the app's page", "앱이 알림 읽기를 뺀 모든 권한을 묻지 않고 써요. 앱 화면에서 직접 끈 권한은 쓰지 않아요")
}

private fun PermissionPolicy.details(): String = when (this) {
    PermissionPolicy.AskIfExfiltrating ->
        localized(
"Apps that can't send anything off your phone (no internet access and no phone companion app) can read your data, " +
            "like your location, calendar or music, without asking, because it can't go anywhere. " +
            "Apps that could send it off your phone ask first.\n\n" +
            "Every app asks before going online, and before acting for you: adding timeline pins, controlling " +
            "music, launching apps, changing watch settings, ringing your phone, or using a plugin's actions. " +
            "Every app also asks before reading your notifications.",
"휴대폰 밖으로 아무것도 보낼 수 없는 앱(인터넷 접근도, 휴대폰 컴패니언 앱도 없는 앱)은 위치, 캘린더, 음악 같은 데이터를 묻지 않고 읽어요. 데이터가 밖으로 나갈 수 없기 때문이에요. 휴대폰 밖으로 보낼 수 있는 앱은 먼저 물어봐요.\n\n모든 앱은 인터넷에 연결하기 전, 그리고 타임라인 핀 추가, 음악 제어, 앱 실행, 시계 설정 변경, 휴대폰 벨 울리기, 플러그인 동작 사용처럼 대신 동작하기 전에 물어봐요. 알림을 읽기 전에도 항상 물어봐요.",
)
    PermissionPolicy.AlwaysAsk ->
        localized("Apps ask the first time they use any permission, including reading data they couldn't send anywhere.", "앱이 어떤 권한이든 처음 쓸 때 물어봐요. 밖으로 보낼 수 없는 데이터를 읽을 때도 마찬가지예요.")
    PermissionPolicy.NeverAsk ->
        localized(
"Apps get any permission they ask for without asking you, including going online, except reading your " +
            "notifications, which every app still asks for. Anything you've " +
            "turned off on an app's page stays off. Not recommended!",
"앱이 요청한 권한을 묻지 않고 모두 받아요. 인터넷 연결도 포함돼요. 단, 알림 읽기는 모든 앱이 여전히 물어봐요. 앱 화면에서 끈 권한은 계속 꺼져 있어요. 추천하지 않아요.",
)
}

/** Every mode, for the setting's info popup. */
@Composable
fun PermissionPolicyDetails() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            localized(
"Whichever you choose, you can review and change each app's permissions on its page. Apps built before " +
                    "permissions existed have their own setting, \"Ask before older apps use permissions\".",
"무엇을 고르든 앱 화면에서 앱별 권한을 확인하고 바꿀 수 있어요. 권한 시스템이 생기기 전에 만든 앱은 \"오래된 앱이 권한을 쓰기 전에 묻기\" 설정을 따로 써요.",
),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PermissionPolicy.entries.forEach { policy ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (policy == PermissionPolicy.AskIfExfiltrating) localized("${policy.displayName()} (default)", "${policy.displayName()}(기본값)") else policy.displayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(policy.details(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** What permissions are, and when apps ask; shown once, in a dialog or in onboarding. */
@Composable
fun PermissionsIntroContent(settings: SettingsItemsState?, modifier: Modifier = Modifier) {
    val libPebble = rememberLibPebble()
    val hasWatch = libPebble.watches.collectAsState().value.isNotEmpty()
    val indexEnabled = koinInject<CoreConfigHolder>().config.collectAsState().value.enableIndex
    val askers = when {
        hasWatch && indexEnabled ->
            localized("Watchfaces, watchapps and plugins (including the ones Index uses to save your notes and reminders)", "워치페이스, 워치앱, 플러그인(Index가 메모와 리마인더를 저장할 때 쓰는 플러그인 포함)")
        indexEnabled -> localized("Plugins, including the ones Index uses to save your notes and reminders,", "플러그인(Index가 메모와 리마인더를 저장할 때 쓰는 플러그인 포함)")
        else -> localized("Watchfaces, watchapps and plugins", "워치페이스, 워치앱, 플러그인")
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            localized(
"$askers now ask before using your phone's features: going online, your location, " +
                "notifications, calendar, contacts, music, files, watch settings and timeline.",
"이제 ${askers}에서 휴대폰 기능을 쓰기 전에 물어봐요: 인터넷 연결, 위치, 알림, 캘린더, 연락처, 음악, 파일, 시계 설정, 타임라인.",
),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            localized(
"Change the permissions mode for all apps below (also in Settings/Apps), and configure " +
                    "each app's individual settings on its own screen",
"아래에서 모든 앱의 권한 모드를 바꿀 수 있어요(설정 › 앱에도 있어요). 앱별 설정은 각 앱 화면에서 바꿔요.",
),
            style = MaterialTheme.typography.bodySmall,
        )
        if (hasWatch) {
            Text(
                localized(
"Older apps which weren't built for the Permissions system are not affected, " +
                        "unless you choose to enforce permissions for them in Settings -> Apps.",
"권한 시스템에 맞게 만들어지지 않은 오래된 앱은 영향을 받지 않아요. 적용하려면 설정 › 앱에서 켜 주세요.",
),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        settings.Show(SettingsIds.PermissionPolicy)
    }
}

@Composable
fun PermissionsIntroDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        // The settings row needs about the width of the settings screen.
        modifier = Modifier.padding(horizontal = 16.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(localized("App permissions", "앱 권한")) },
        text = {
            val settings = rememberSettingsItemsState(navBarNav = null, snackbarDisplay = {})
            PermissionsIntroContent(settings, Modifier.verticalScroll(rememberScrollState()))
        },
        confirmButton = { TextButton(onClick = onDismissRequest) { Text(localized("OK", "확인")) } },
    )
}
