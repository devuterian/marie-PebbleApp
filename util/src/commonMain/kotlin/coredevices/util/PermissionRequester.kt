package coredevices.util

import localization.localized

import PlatformUiContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class PermissionResult {
    Granted,
    Rejected,
    RejectedForever,
    Error,
    Unknown,
}

fun Permission.name(): String = when (this) {
    Permission.Location -> localized("Location")
    Permission.BackgroundLocation -> localized("Background Location", "백그라운드 위치")
    Permission.PostNotifications -> localized("Show Notifications", "알림 표시")
    Permission.Bluetooth -> localized("Bluetooth", "블루투스")
    Permission.ReadCallLog -> localized("Missed Calls", "부재중 전화")
    Permission.Calendar -> localized("Calendar")
    Permission.Contacts -> localized("Contacts")
    Permission.ReadPhoneState -> localized("Phone Calls", "전화")
    Permission.ReadNotifications -> localized("Read Notifications", "알림 읽기")
    Permission.RecordAudio -> localized("Record Audio", "오디오 녹음")
    Permission.SpeechRecognizer -> localized("Speech Recognizer", "음성 인식")
    Permission.ExternalStorage -> localized("External Storage", "외부 저장 공간")
    Permission.SetAlarms -> localized("Set Alarms", "알람 설정")
    Permission.BatteryOptimization -> localized("Exempt from Battery Optimizations", "배터리 최적화 제외")
    Permission.Beeper -> localized("Access Beeper", "Beeper 접근")
    Permission.Reminders -> localized("Access Reminders", "미리 알림 접근")
}

fun Permission.description(): String = when (this) {
    Permission.Location -> localized("To power watchfaces using location for e.g. weather", "날씨 등 위치를 사용하는 워치페이스에 필요합니다")
    Permission.BackgroundLocation -> localized("To power watchfaces using location for e.g. weather, while the Pebble app is running in the background", "앱이 백그라운드에 있을 때도 날씨 등 위치를 사용하는 워치페이스에 필요합니다")
    Permission.PostNotifications -> localized("Get a notification when there is a new software update for your Pebble and show connection status", "시계 업데이트 소식과 연결 상태를 알림으로 표시합니다")
    Permission.Bluetooth -> localized("Connect to your Pebble", "페블과 연결할 때 필요합니다")
    Permission.ReadCallLog -> localized("To access the call log - to show missed calls on the watch", "통화 기록을 읽어 시계에 부재중 전화를 표시합니다")
    Permission.Calendar -> localized("To show calendar events on the watch, and respond to invitations", "시계에 일정을 보여주고 초대에 응답할 때 필요합니다")
    Permission.Contacts -> localized("To show who is calling, and filter notifications by contact", "발신자를 표시하고 연락처별로 알림을 설정할 때 필요합니다")
    Permission.ReadPhoneState -> localized("To show phone calls on the watch", "시계에 걸려 온 전화를 표시합니다")
    Permission.ReadNotifications -> localized("To display notifications on the watch", "시계에 휴대폰 알림을 표시합니다")
    Permission.RecordAudio -> localized("To record manual local notes", "기기에 음성 메모를 녹음합니다")
    Permission.SpeechRecognizer -> localized("To transcribe recordings on this device", "이 기기에서 녹음을 글자로 바꿉니다")
    Permission.ExternalStorage -> localized("To store recordings", "녹음 파일을 저장합니다")
    Permission.SetAlarms -> localized("To notify when a reminder is triggered", "미리 알림 시간이 되면 알려 줍니다")
    Permission.BatteryOptimization -> localized("To handle recordings in the background", "백그라운드에서도 녹음을 처리합니다")
    Permission.Beeper -> localized("To send messages via Beeper", "Beeper로 메시지를 보냅니다")
    Permission.Reminders -> localized("To create reminders", "미리 알림을 만듭니다")
}

abstract class PermissionRequester(
    private val requiredPermissions: RequiredPermissions,
    protected val appResumed: AppResumed,
) {
    protected val logger = Logger.withTag("PermissionRequester")
    private val _missingPermissions = MutableStateFlow<Set<Permission>>(emptySet())
    val missingPermissions: StateFlow<Set<Permission>> = _missingPermissions.asStateFlow()
    private val _grantedPermissions = MutableStateFlow<Set<Permission>>(emptySet())
    val grantedPermissions: StateFlow<Set<Permission>> = _grantedPermissions
    private val permissionRefreshRequests = MutableSharedFlow<Unit>(replay = 1)

    fun init() {
        logger.v { "init()" }
        GlobalScope.launch {
            logger.v { "globalscope" }
            requiredPermissions.requiredPermissions
                .distinctUntilChanged()
                .combine(permissionRefreshRequests) { requiredPermissions, _ ->
                    logger.d { "refreshing permissions..." }
                    val allGrantedPermissions = Permission.entries.filter {
                        try { hasPermission(it) } catch (_: Exception) { false }
                    }
                    _grantedPermissions.value = allGrantedPermissions.toSet()
                    _missingPermissions.value = requiredPermissions.filter { permission -> !allGrantedPermissions.contains(permission) }.toSet()
                    logger.d { "missingPermissions: ${_missingPermissions.value}" }
                }.collect { }
        }
        GlobalScope.launch {
            appResumed.appResumed.collect {
                refreshPermissions()
            }
        }
        refreshPermissions()
    }

    suspend fun requestPermission(
        permission: Permission,
        uiContext: PlatformUiContext
    ): PermissionResult {
        val result = requestPlatformPermission(permission, uiContext)
        logger.d { "requestPermission($permission) = $result" }
        if (result == PermissionResult.Granted) {
            refreshPermissions()
        }
        return result
    }

    protected abstract suspend fun requestPlatformPermission(
        permission: Permission,
        uiContext: PlatformUiContext
    ): PermissionResult

    abstract suspend fun hasPermission(permission: Permission): Boolean
    abstract fun openPermissionsScreen(uiContext: PlatformUiContext)

    private fun refreshPermissions() {
        permissionRefreshRequests.tryEmit(Unit)
    }
}

fun PermissionRequester.granted(permission: Permission): Flow<Boolean> = grantedPermissions.map { it.contains(permission) }

expect fun Permission.requestIsFullScreen(): Boolean

data class RequiredPermissions(
    val requiredPermissions: Flow<Set<Permission>>
)

fun Boolean.asPermissionResult(): PermissionResult = if (this) {
    PermissionResult.Granted
} else {
    PermissionResult.Rejected
}