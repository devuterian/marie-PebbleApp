package coredevices.pebble.ui

import localization.localized

import AppUpdateTracker
import CommonRoutes
import CoreAppVersion
import NextBugReportContext
import PlatformUiContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.cactus.isCactusSupported
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import coreapp.pebble.generated.resources.Res
import coreapp.pebble.generated.resources.wispr_flow_logo_black
import coreapp.pebble.generated.resources.wispr_flow_logo_white
import coredevices.CoreBackgroundSync
import coredevices.EnableExperimentalDevices
import coredevices.analytics.AnalyticsBackend
import coredevices.analytics.CoreAnalytics
import coredevices.analytics.setUser
import coredevices.coreapp.util.AppUpdate
import coredevices.coreapp.util.AppUpdateState
import coredevices.firestore.UsersDao
import coredevices.pebble.PebbleFeatures
import coredevices.pebble.Platform
import coredevices.pebble.account.BootConfigProvider
import coredevices.pebble.account.PebbleAccount
import coredevices.pebble.health.HealthSyncTracker
import coredevices.pebble.health.PlatformHealthSync
import coredevices.pebble.rememberLibPebble
import coredevices.pebble.ui.SettingsIds.EnableActivityInsights
import coredevices.pebble.ui.SettingsIds.EnableHealthPlatformSync
import coredevices.pebble.ui.SettingsIds.EnableHealthTracking
import coredevices.pebble.ui.SettingsIds.EnableSleepInsights
import coredevices.pebble.ui.SettingsIds.OfflineSpeechRecognition
import coredevices.pebble.ui.SettingsKeys.KEY_ENABLE_FIREBASE_UPLOADS
import coredevices.pebble.ui.SettingsKeys.KEY_ENABLE_MEMFAULT_UPLOADS
import coredevices.pebble.ui.SettingsKeys.KEY_ENABLE_MIXPANEL_UPLOADS
import coredevices.pebble.weather.WeatherFetcher
import coredevices.ui.ConfirmDialog
import coredevices.ui.CoreLinearProgressIndicator
import coredevices.ui.M3Dialog
import coredevices.ui.SignInDialog
import coredevices.util.CoreConfig
import coredevices.util.CoreConfigHolder
import coredevices.util.Permission
import coredevices.util.PermissionRequester
import coredevices.util.STTConfig
import coredevices.util.emailOrNull
import coredevices.util.models.CactusSTTMode
import coredevices.util.models.ModelDownloadStatus
import coredevices.util.models.ModelInfo
import coredevices.util.models.ModelManager
import coredevices.util.models.RecommendedModel
import coredevices.util.models.inProgress
import coredevices.util.rememberUiContext
import coredevices.util.transcription.PlatformSpeechRecognizer
import coredevices.util.transcription.SpeechModelAvailability
import coredevices.util.transcription.SpokenLanguageOptions
import coredevices.util.transcription.platformModelNeedsDownload
import coredevices.util.transcription.platformModelState
import coredevices.util.transcription.spokenLanguageLabel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.crashlytics.crashlytics
import io.rebble.libpebblecommon.connection.AppContext
import io.rebble.libpebblecommon.connection.ConnectedPebble
import io.rebble.libpebblecommon.connection.KnownPebbleDevice
import io.rebble.libpebblecommon.database.entity.HRMonitoringInterval
import io.rebble.libpebblecommon.database.entity.HealthGender
import io.rebble.libpebblecommon.js.PKJSApp
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.packets.ProtocolCapsFlag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import theme.CoreAppTheme
import theme.ThemeProvider
import theme.currentColorScheme
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class TopLevelType(val displayName: String) {
    Phone(localized("Phone")),
    Watch(localized("Watch")),
    All(localized("All")),
    Notifications(localized("Notifications")),
    ;

    fun icon(platform: Platform) = when (this) {
        Phone -> when (platform) {
            Platform.Android -> Icons.Default.PhoneAndroid
            Platform.IOS -> Icons.Default.PhoneIphone
        }
        Watch -> Icons.Default.Watch
        All -> Icons.AutoMirrored.Filled.List
        Notifications -> Icons.Default.Notifications
    }

    fun show(type: TopLevelType): Boolean = when (this) {
        All -> true
        else -> this == type
    }
}

enum class Section(val title: String, val icon: ImageVector) {
    About(localized("About"), Icons.Default.Info),
    Support(localized("Get Help"), Icons.Default.SupportAgent),
    Defaults(localized("Defaults"), Icons.Default.Tune),
    QuickLaunch(localized("Quick Launch"), Icons.Default.RocketLaunch), // watch only
    NotificationsWatch(localized("Notifications"), Icons.Default.Notifications), // watch only
    General(localized("General"), Icons.Default.Settings),
    Apps(localized("Apps"), Icons.Default.Apps),
    Battery(localized("Battery"), Icons.Default.BatteryFull),
    Calendar(localized("Calendar"), Icons.Default.CalendarMonth),
    Health(localized("Health"), Icons.AutoMirrored.Filled.DirectionsRun),
    Speech(localized("Speech Recognition"), Icons.Default.Mic),
    Display(localized("Display"), Icons.Default.DarkMode), // watch only
    Weather(localized("Weather"), Icons.Default.Cloud),
    Notifications(localized("Notifications"), Icons.Default.Notifications),
    Time(localized("Time"), Icons.Default.Schedule), // watch only
    Timeline(localized("Timeline"), Icons.Default.Timeline), // watch only
    QuietTime(localized("Quiet Time"), Icons.Default.DoNotDisturb),
    Connectivity(localized("Connectivity"), Icons.Default.Wifi),
    Music(localized("Music"), Icons.Default.MusicNote),
    Other(localized("Other"), Icons.Default.MoreHoriz), // watch only
    Diagnostics(localized("Diagnostics"), Icons.Default.Timeline),
    Debug(localized("Debug"), Icons.Default.BugReport),
    BundledPlugins(localized("Bundled Plugins"), Icons.Default.Extension), // TODO to be removed when we have a better solution
}

fun Section.navigatesDirectlyTo(): NavBarRoute? = when (this) {
    Section.Battery -> PebbleNavBarRoutes.BatterySettingsRoute
    else -> null
}

object SettingsIds {
    const val OfflineSpeechRecognition = "OfflineSpeechRecognition"
    const val EnableHealthTracking = "EnableHealthTracking"
    const val EnableActivityInsights = "EnableActivityInsights"
    const val EnableSleepInsights = "EnableSleepInsights"
    const val EnableHealthPlatformSync = "EnableHealthPlatformSync"
    const val HealthHeight = "HealthHeight"
    const val HealthWeight = "HealthWeight"
    const val HealthAge = "HealthAge"
    const val HealthGenderId = "HealthGender"
    const val HealthImperialUnits = "HealthImperialUnits"
    const val HrmEnabled = "HrmEnabled"
    const val HrmMeasurementInterval = "HrmMeasurementInterval"
    const val HrmActivityTracking = "HrmActivityTracking"
}

data class SettingsItem(
    val id: String? = null,
    val title: String,
    val topLevelType: TopLevelType,
    val section: Section,
    val keywords: String = "",
    val show: () -> Boolean = { true },
    val badge: String? = null,
    private val item: @Composable () -> Unit,
    val isDebugSetting: Boolean,
    val onDisplayed: (@Composable () -> Unit)? = null,
) {
    @Composable
    fun Item() {
        onDisplayed?.invoke()
        item()
    }
}

private val ELEVATION = 0.dp

@Composable
fun settingsBadgeTotal(): Int {
    val permissionRequester: PermissionRequester = koinInject()
    val missingPermissions by permissionRequester.missingPermissions.collectAsState()
    val coreConfigHolder: CoreConfigHolder = koinInject()
    val coreConfig by coreConfigHolder.config.collectAsState()
    val permissionBadgeCount = if (coreConfig.hidePermissionWarningBadges) 0 else missingPermissions.size
    val appUpdate: AppUpdate = koinInject()
    val updateState by appUpdate.updateAvailable.collectAsState()
    val updatesAvailable = when (updateState) {
        AppUpdateState.NoUpdateAvailable -> 0
        is AppUpdateState.UpdateAvailable -> 1
    }
    val appUpdateTracker: AppUpdateTracker = koinInject()
    val appWasUpdated by appUpdateTracker.appWasUpdated.collectAsState()
    val appUpdated = when (appWasUpdated) {
        true -> 1
        false -> 0
    }
    return permissionBadgeCount + updatesAvailable + appUpdated
}

private val logger = Logger.withTag("WatchSettingsScreen")

sealed interface RequestedLocalSTTMode {
    val mode: CactusSTTMode

    object Disabled : RequestedLocalSTTMode {
        override val mode = CactusSTTMode.RemoteOnly
    }

    data class Enabled(
        override val mode: CactusSTTMode,
        val modelName: String
    ) : RequestedLocalSTTMode
}

class WatchSettingsScreenViewModel : ViewModel() {
    val searchState = SearchState()
    var selectedTopLevelType by mutableStateOf(TopLevelType.Phone)
}

data class SettingsItemsState(
    val rawSettingsItems: List<SettingsItem>,
    val debugOptionsEnabled: Boolean,
    val anyWatchSupportsSettingsSync: Boolean,
    val coreConfig: CoreConfig,
    val healthTrackingEnabled: Boolean,
)

@Composable
fun rememberSettingsItemsState(navBarNav: NavBarNav?, snackbarDisplay: SnackbarDisplay): SettingsItemsState? {
    val libPebble = rememberLibPebble()
    val libPebbleConfig by libPebble.config.collectAsState()
    val coreConfigHolder: CoreConfigHolder = koinInject()
    val coreConfig by coreConfigHolder.config.collectAsState()
    val themeProvider: ThemeProvider = koinInject()
    val settings: Settings = koinInject()
    val currentTheme by themeProvider.theme.collectAsState()
    val pebbleFeatures = koinInject<PebbleFeatures>()
    val pebbleAccount = koinInject<PebbleAccount>()
    val loggedIn by pebbleAccount.loggedIn.collectAsState()
    val coreUser by Firebase.auth.authStateChanged.map {
        it?.emailOrNull
    }.distinctUntilChanged()
        .collectAsState(Firebase.auth.currentUser?.emailOrNull)
    val scope = rememberCoroutineScope()
    val appContext = koinInject<AppContext>()
    val appVersion = koinInject<CoreAppVersion>()
    val platform = koinInject<Platform>()
    val modelManager: ModelManager = koinInject()
    val nextBugReportContext: NextBugReportContext = koinInject()
    val appUpdate: AppUpdate = koinInject()
    val updateState by appUpdate.updateAvailable.collectAsState()
    val (showCopyTokenDialog, setShowCopyTokenDialog) = remember { mutableStateOf(false) }
    val coreBackgroundSync: CoreBackgroundSync = koinInject()
    if (showCopyTokenDialog) {
        PKJSCopyTokenDialog(onDismissRequest = { setShowCopyTokenDialog(false) })
    }
    var showHealthStatsDialog by remember { mutableStateOf(false) }
    val showFakeHealthDataDialog = remember { mutableStateOf(false) }
    var showSignInDialog by remember { mutableStateOf(false) }
    var debugOptionsEnabled by remember { mutableStateOf(settings.showDebugOptions()) }
    var pendingSTTModeDialog by remember { mutableStateOf<CactusSTTMode?>(null) }
    var showSpokenLanguageDialog by remember { mutableStateOf(false) }
    val recommendedSTTModel = modelManager.getRecommendedSTTModel()
    val modelDownloadState by modelManager.modelDownloadStatus.collectAsState()
    val platformSpeechRecognizer: PlatformSpeechRecognizer = koinInject()
    val platformSttAvailable by produceState(false) {
        value = withContext(Dispatchers.Default) { platformSpeechRecognizer.isAvailable() }
    }
    val platformDownloadStatus by platformSpeechRecognizer.downloadStatus.collectAsState()
    val platformModelAvailability by produceState(
        SpeechModelAvailability.Unsupported,
        coreConfig.sttConfig.spokenLanguage,
        platformDownloadStatus,
        platformSttAvailable,
    ) {
        value = if (platformSttAvailable) {
            withContext(Dispatchers.Default) {
                platformSpeechRecognizer.modelAvailability(coreConfig.sttConfig.spokenLanguage)
            }
        } else {
            SpeechModelAvailability.Unsupported
        }
    }
    val platformNeedsDownload = platformModelNeedsDownload(platformModelAvailability, platformDownloadStatus)
    if (showSpokenLanguageDialog) {
        SpokenLanguagePickerDialog(
            selectedCode = coreConfig.sttConfig.spokenLanguage,
            onLanguageSelected = { code ->
                coreConfigHolder.update(
                    coreConfig.copy(
                        sttConfig = coreConfig.sttConfig.copy(spokenLanguage = code)
                    )
                )
                showSpokenLanguageDialog = false
                if (coreConfig.sttConfig.mode == CactusSTTMode.PlatformOnly) {
                    scope.launch {
                        val availability = platformSpeechRecognizer.modelAvailability(code)
                        if (platformModelNeedsDownload(availability, platformDownloadStatus)) {
                            navBarNav?.navigateTo(CommonRoutes.SpeechModelDownloadDialog)
                        }
                    }
                }
            },
            onDismissRequest = { showSpokenLanguageDialog = false },
        )
    }
    pendingSTTModeDialog?.let { pendingSTTMode ->
        val recommendedModel by produceState<ModelInfo?>(null) {
            withContext(Dispatchers.Default) {
                val models = modelManager.getAvailableSTTModels()
                value = models.firstOrNull { it.slug == recommendedSTTModel.modelSlug }
                    ?: run {
                        snackbarDisplay.showSnackbar(localized("Error occurred. Please try again later."))
                        logger.e { "Recommended model $recommendedSTTModel not found in available models: ${models.map { it.slug }}" }
                        pendingSTTModeDialog = null
                        null
                    }
            }
        }
        val recommendedModelFinal = recommendedModel
        if (recommendedModelFinal == null) {
            return@let
        }
        ModelDownloadPromptDialog(
            isLite = recommendedSTTModel is RecommendedModel.Lite,
            downloadSizeInMb = recommendedModelFinal.sizeInMB,
            onGetRecommended = {
                scope.launch {
                    if (!modelManager.downloadSTTModel(recommendedModelFinal, allowMetered = true)) {
                        snackbarDisplay.showSnackbar(localized("Error starting download. Please try again later."))
                        logger.e { "Failed to start download for recommended model ${recommendedModelFinal.slug}" }
                    } else {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                sttConfig = STTConfig(
                                    mode = pendingSTTMode,
                                    modelName = recommendedModelFinal.slug
                                )
                            )
                        )
                    }
                    pendingSTTModeDialog = null
                }
            },
            onDismiss = {
                pendingSTTModeDialog = null
            }
        )
    }
    if (showHealthStatsDialog) {
        HealthStatsDialog(
            libPebble = libPebble,
            onDismissRequest = { showHealthStatsDialog = false },
        )
    }
    ConfirmDialog(
        show = showFakeHealthDataDialog,
        title = localized("Populate fake health data"),
        text = localized("This will wipe all existing health data and replace it with 30 days of fake data. This cannot be undone."),
        confirmText = localized("Wipe & Populate"),
        onConfirm = {
            scope.launch { libPebble.populateDebugHealthData() }
        },
    )
    if (showSignInDialog) {
        SignInDialog(
            onDismiss = { showSignInDialog = false }
        )
    }
    val modelDownloadStatus by modelManager.modelDownloadStatus.collectAsState()
    LaunchedEffect(modelDownloadStatus) {
        when (modelDownloadStatus) {
            is ModelDownloadStatus.Failed -> {
                snackbarDisplay.showSnackbar("Failed to download model")
            }
            else -> {}
        }
    }
    var themeDropdownExpanded by remember { mutableStateOf(false) }
    val permissionRequester: PermissionRequester = koinInject()
    val missingPermissions by permissionRequester.missingPermissions.collectAsState()
    val uiContext = rememberUiContext()
    val analyticsBackend: AnalyticsBackend = koinInject()
    val usersDao: UsersDao = koinInject()
    val enableFirebase = remember { mutableStateOf(settings.getBoolean(KEY_ENABLE_FIREBASE_UPLOADS, true)) }
    val enableMemfault = remember { mutableStateOf(settings.getBoolean(KEY_ENABLE_MEMFAULT_UPLOADS, true)) }
    val enableMixpanel = remember { mutableStateOf(settings.getBoolean(KEY_ENABLE_MIXPANEL_UPLOADS, true)) }
    val enableExperimentalDevices: EnableExperimentalDevices = koinInject()
    val experimentalDevices by enableExperimentalDevices.enabled.collectAsState()
    val appUpdateTracker: AppUpdateTracker = koinInject()
    val showChangelogBadge = remember { appUpdateTracker.appWasUpdated.value }
    val hasOfflineModels by produceState(false) {
        withContext(Dispatchers.Default) {
            value = modelManager.getDownloadedModelSlugs().any { it.startsWith("parakeet", false) }
        }
    }
    val cactusSupported = remember { isCactusSupported() }
    val bootConfigProvider: BootConfigProvider = koinInject()
    val rebbleVoiceAvailable by produceState(false, loggedIn) {
        value = withContext(Dispatchers.Default) {
            loggedIn != null && (bootConfigProvider.getBootConfig()?.config?.voice?.languages?.isNotEmpty() == true)
        }
    }
    val healthSettingsNullable by libPebble.healthSettings.collectAsState(null)
    val healthSettings = healthSettingsNullable ?: return null
    val weatherFetcher: WeatherFetcher = koinInject()
    val watches by libPebble.watches.collectAsState(null)
    val watchesCastable = watches ?: return null
    val anyWatchSupportsSettingsSync = remember(watchesCastable) {
        watchesCastable.any {
            it is KnownPebbleDevice && it.capabilities.contains(
                ProtocolCapsFlag.SupportsBlobDbVersion
            )
        }
    }
    val watchPrefs = watchPrefs()
    val coreAnalytics: CoreAnalytics = koinInject()
    val platformHealthSync: PlatformHealthSync = koinInject()
    val healthSyncTracker: HealthSyncTracker = koinInject()
    val healthPlatformSyncEnabled by healthSyncTracker.enabled.collectAsState()
    val healthIsSyncing by platformHealthSync.syncing.collectAsState()

    val rawSettingsItems = remember(
            libPebbleConfig,
            debugOptionsEnabled,
            missingPermissions,
            updateState,
            enableFirebase,
            enableMemfault,
            enableMixpanel,
            coreConfig,
            experimentalDevices,
            loggedIn,
            watchPrefs,
            rebbleVoiceAvailable,
            platformSttAvailable,
        ) {
            listOfNotNull(
                basicSettingsActionItem(
                    title = localized("App Updates"),
                    description = if (updateState is AppUpdateState.UpdateAvailable) localized("Update available — tap to install") else localized("Check for app updates"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.About,
                    action = {
                        val update = updateState as? AppUpdateState.UpdateAvailable
                        if (uiContext != null && update != null) {
                            appUpdate.startUpdateFlow(uiContext, update.update)
                        } else {
                            scope.launch { appUpdate.checkForUpdates(force = true) }
                        }
                    },
                    badge = when (updateState) {
                        AppUpdateState.NoUpdateAvailable -> null
                        is AppUpdateState.UpdateAvailable -> "1"
                    },
                    show = { platform == Platform.Android || updateState is AppUpdateState.UpdateAvailable },
                ),
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Permissions"),
                    description = if (missingPermissions.isEmpty()) {
                        localized("All permissions granted!")
                    } else if (missingPermissions.size == 1) {
                        "${missingPermissions.first()} permission missing!"
                    } else {
                        "${missingPermissions.size} permissions missing!"
                    },
                    topLevelType = TopLevelType.Phone,
                    section = Section.About,
                    action = if (missingPermissions.isNotEmpty()) {
                        {
                            nav.navigateTo(PebbleNavBarRoutes.PermissionsRoute)
                        }
                    } else null,
                    badge = if (missingPermissions.isEmpty() || coreConfig.hidePermissionWarningBadges) null else "${missingPermissions.size}",
                ) },
                SettingsItem(
                    title = localized("App Version"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.About,
                    item = {
                        ListItem(
                            headlineContent = {
                                Text(localized("App Version: ${appVersion.version}", "앱 버전: ${appVersion.version}"))
                            },
                            shadowElevation = ELEVATION,
                        )
                    },
                    isDebugSetting = false,
                ),
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("What’s new in the app"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.About,
                    action = {
                        nav.navigateTo(CommonRoutes.RoadmapChangelogRoute)
                    },
                    actionIcon = Icons.AutoMirrored.Default.Launch,
                    badge = if (showChangelogBadge) "1" else null,
                    onDisplayed = {
                        LaunchedEffect(Unit) {
                            appUpdateTracker.acknowledgeCurrentVersion()
                        }
                    },
                ) },
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("What’s new in PebbleOS"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.About,
                    action = {
                        nav.navigateTo(CommonRoutes.PebbleOsChangelogRoute)
                    },
                    actionIcon = Icons.AutoMirrored.Default.Launch,
                ) },
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Getting Started & Troubleshooting"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Support,
                    action = {
                        nav.navigateTo(CommonRoutes.TroubleshootingRoute)
                    },
                    actionIcon = Icons.AutoMirrored.Default.Launch,
                ) },
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("New Bug Report"),
                    description = localized("Please report a bug if anything went wrong!"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Support,
                    action = {
                        nextBugReportContext.nextContext = null
                        nav.navigateTo(
                            CommonRoutes.BugReport(
                                pebble = true,
                            )
                        )
                    },
                ) },
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("View My Bug Reports"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Support,
                    action = {
                        nav.navigateTo(CommonRoutes.ViewMyBugReportsRoute)
                    },
                ) },
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Configure Appstore Sources"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Apps,
                    action = { nav.navigateTo(PebbleNavBarRoutes.AppstoreSettingsRoute()) },
                ) },
                basicSettingsDropdownItem(
                    title = localized("App Theme"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    keywords = "dark light system",
                    selectedItem = currentTheme,
                    items = CoreAppTheme.entries,
                    onItemSelected = {
                        themeProvider.setTheme(it)
                    },
                    itemText = {
                        stringResource(it.resource)
                    },
                ),
                basicSettingsToggleItem(
                    id = SettingsIds.HealthImperialUnits,
                    title = localized("Imperial Units"),
                    description = localized("Use miles/feet/inches/lb and Fahrenheit instead of metric units"),
                    keywords = "weather health degrees celsius temperature miles",
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    checked = healthSettings.imperialUnits,
                    onCheckChanged = {
                        GlobalScope.launch {
                            libPebble.updateImperialUnits(it)
                            weatherFetcher.fetchWeather(this)
                        }
                    },
                ),
                basicSettingsActionItem(
                    title = localized("Restore System app positions"),
                    description = localized("Restore system apps to their usual position at the top of the menu"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Apps,
                    action = {
                        libPebble.restoreSystemAppOrder()
                    },
                ),
                basicSettingsDropdownItem(
                    title = localized("Background Refresh Interval"),
                    description = localized("How often to check for updates, update apps from store, etc"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    selectedItem = RegularSyncInterval.from(coreConfig.regularSyncInterval),
                    items = RegularSyncInterval.entries,
                    onItemSelected = {
                        coreBackgroundSync.updateFullSyncPeriod(it.period)
                    },
                    itemText = {
                        it.displayName
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Enable Index Feed"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    checked = coreConfig.enableIndex,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                enableIndex = it,
                                indexPermissionsConfirmed = if (it) coreConfig.indexPermissionsConfirmed else false,
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Foreground Service"),
                    description = localized("Show foreground service notification to keep app alive in background"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    checked = coreConfig.androidForegroundServiceForWatchConnectionV2,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                androidForegroundServiceForWatchConnectionV2 = it,
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsForegroundService() },
                ),
                basicSettingsToggleItem(
                    title = localized("Watch fully charged"),
                    description = localized("Notify on this phone when a watch finishes charging"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    checked = coreConfig.notifyWatchFullyCharged,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                notifyWatchFullyCharged = it
                            )
                        )
                    },
                ),
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Quick replies"),
                    description = localized("Preset messages for notification replies on the watch (canned messages)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    action = {
                        nav.navigateTo(PebbleNavBarRoutes.CannedRepliesRoute)
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                ) },
                basicSettingsToggleItem(
                    title = localized("Always send notifications"),
                    description = localized("Send notifications to the watch even when the phone screen is on"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.alwaysSendNotifications,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    alwaysSendNotifications = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                ),
                basicSettingsToggleItem(
                    title = localized("Respect Phone Do Not Disturb"),
                    description = localized("Notifications won't be sent to watch if phone is in Do Not Disturb mode (unless configured for that app/person in phone settings)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.respectDoNotDisturb,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    respectDoNotDisturb = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                ),
                basicSettingsToggleItem(
                    title = localized("Mute phone notification effects"),
                    description = localized("Mutes the phone's own notification vibration/sound while the watch is connected"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.mutePhoneNotificationSoundsWhenConnected,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    mutePhoneNotificationSoundsWhenConnected = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationHints() },
                ),
                basicSettingsToggleItem(
                    title = localized("Mute phone call effects"),
                    description = localized("Mutes the phone's own call vibration/sound while the watch is connected"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.mutePhoneCallSoundsWhenConnected,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    mutePhoneCallSoundsWhenConnected = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationHints() },
                ),
                SettingsItem(
                    title = localized("Vibration Pattern"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    show = { pebbleFeatures.supportsVibePatterns() },
                    item = {
                        SelectVibePatternOrNone(
                            currentPattern = libPebbleConfig.notificationConfig.overrideDefaultVibePattern,
                            onChangePattern = { pattern ->
                                libPebble.updateConfig(
                                    libPebbleConfig.copy(
                                        notificationConfig = libPebbleConfig.notificationConfig.copy(
                                            overrideDefaultVibePattern = pattern?.name
                                        )
                                    )
                                )
                            },
                            subtext = localized("Override the default on the watch"),
                        )
                    },
                    isDebugSetting = false,
                ),
                basicSettingsToggleItem(
                    title = localized("Use vibration patterns from OS"),
                    description = localized("If there is a vibration pattern defined by the app which created a notification, use it on the watch (unless overridden)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.useAndroidVibePatterns,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    useAndroidVibePatterns = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsVibePatterns() },
                ),
                basicSettingsToggleItem(
                    title = localized("Send notification images"),
                    description = localized("Show photos sent in messages on the watch. Can also be turned off per app on the Notifications tab."),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.sendNotificationImages,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    sendNotificationImages = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationImages() },
                ),
                basicSettingsToggleItem(
                    title = localized("Send local-only notifications to watch"),
                    description = localized("Android recommends not forwarding notifications marked as local-only to external devices - check to override this"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.sendLocalOnlyNotifications,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    sendLocalOnlyNotifications = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                ),
                basicSettingsToggleItem(
                    title = localized("Enable showsUserInterface actions"),
                    description = localized("Include notification actions which are marked as opening a user interface on the phone"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.addShowsUserInterfaceActions,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    addShowsUserInterfaceActions = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                ),
                basicSettingsNumberItem(
                    title = localized("Store notifications for"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    description = localized("How long notifications are stored for. This enabled better deduplicating, and powers the notification history view"),
                    value = libPebbleConfig.notificationConfig.storeNotifiationsForDays.toLong(),
                    onValueChange = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    storeNotifiationsForDays = it.toInt()
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                    min = 0,
                    max = 7,
                    unit = localized("Days")
                ),
                basicSettingsToggleItem(
                    title = localized("Store disabled notifications"),
                    description = localized("Store notifications from disabled apps/channels, to allow viewing them in history"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Notifications,
                    checked = libPebbleConfig.notificationConfig.storeDisabledNotifications,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    storeDisabledNotifications = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationFiltering() },
                ),
                basicSettingsToggleItem(
                    title = "다른 구형 페블도 BLE 검색에 표시",
                    description = "페블·페블 스틸·타임 라운드를 BLE 검색에도 표시합니다. 타임과 타임 스틸은 이 옵션과 관계없이 표시됩니다.",
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.watchConfig.allowLegacyWatchesInBleScan,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    allowLegacyWatchesInBleScan = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                    show = { pebbleFeatures.supportsBtClassic() },
                ),
                basicSettingsToggleItem(
                    title = localized("Disable Companion Device Manager"),
                    description = localized("Don't use Android's Companion Device Manager to connect. Only use this option if the app crashes every time you press 'connect' and you cannot get past this step. This will disable certain features (including Notification Channels), and certain permissions will need to be granted manually."),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = coreConfig.disableCompanionDeviceManager,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                disableCompanionDeviceManager = it,
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsCompanionDeviceManager() },
                ),

                basicSettingsToggleItem(
                    title = localized("Ignore Missing PRF"),
                    description = localized("Ignore missing PRF when connecting to development watches"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.watchConfig.ignoreMissingPrf,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    ignoreMissingPrf = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Use reversed PPoG"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.bleConfig.legacyReversedPPoG,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    legacyReversedPPoG = it
                                )
                            )
                        )
                    },
                    show = { false },
                ),
                basicSettingsToggleItem(
                    title = localized("Enable Calendar"),
                    description = localized("Show calendar pins on timeline"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Calendar,
                    checked = libPebbleConfig.watchConfig.calendarPins,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    calendarPins = it
                                )
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Calendar Reminders"),
                    description = localized("Alerts before calendar events"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Calendar,
                    checked = libPebbleConfig.watchConfig.calendarReminders,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    calendarReminders = it
                                )
                            )
                        )
                    },
                    show = { libPebbleConfig.watchConfig.calendarPins },
                ),
                SettingsItem(
                    title = localized("Reminder Vibration Pattern"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Calendar,
                    show = {
                        libPebbleConfig.watchConfig.calendarPins &&
                                libPebbleConfig.watchConfig.calendarReminders &&
                                pebbleFeatures.supportsVibePatterns()
                    },
                    item = {
                        SelectVibePatternOrNone(
                            currentPattern = libPebbleConfig.watchConfig.overrideCalendarVibePattern,
                            onChangePattern = { pattern ->
                                libPebble.updateConfig(
                                    libPebbleConfig.copy(
                                        watchConfig = libPebbleConfig.watchConfig.copy(
                                            overrideCalendarVibePattern = pattern?.name
                                        )
                                    )
                                )
                            },
                            subtext = localized("Override the default on the watch"),
                            title = localized("Reminder Vibration Pattern"),
                        )
                    },
                    isDebugSetting = false,
                ),
                basicSettingsToggleItem(
                    title = localized("Declined Events"),
                    description = localized("Display declined calendar events"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Calendar,
                    checked = libPebbleConfig.watchConfig.calendarShowDeclinedEvents,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    calendarShowDeclinedEvents = it
                                )
                            )
                        )
                    },
                    show = { libPebbleConfig.watchConfig.calendarPins },
                ),
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Calendars"),
                    description = localized("Configure which calendars to display"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Calendar,
                    action = {
                        nav.navigateTo(PebbleNavBarRoutes.CalendarsRoute)
                    },
                    show = { libPebbleConfig.watchConfig.calendarPins },
                ) },
                basicSettingsToggleItem(
                    id = EnableHealthTracking,
                    title = localized("Enable Health Tracking"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = healthSettings.trackingEnabled,
                    onCheckChanged = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(
                                trackingEnabled = it
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    id = SettingsIds.HrmEnabled,
                    title = localized("Heart Rate Monitor"),
                    description = localized("Allow the watch to measure heart rate"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = healthSettings.hrmEnabled,
                    show = { healthSettings.trackingEnabled },
                    onCheckChanged = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(hrmEnabled = it)
                        )
                    },
                ),
                basicSettingsDropdownItem(
                    id = SettingsIds.HrmMeasurementInterval,
                    title = localized("Background Sampling"),
                    description = localized("How often the watch checks your heart rate when you're not in a workout. This can have an impact on battery life."),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    selectedItem = healthSettings.hrmMeasurementInterval,
                    items = HRMonitoringInterval.entries,
                    onItemSelected = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(hrmMeasurementInterval = it)
                        )
                    },
                    itemText = {
                        when (it) {
                            HRMonitoringInterval.TenMin -> localized("Every 10 minutes")
                            HRMonitoringInterval.ThirtyMin -> localized("Every 30 minutes")
                            HRMonitoringInterval.OneHour -> localized("Every hour")
                            HRMonitoringInterval.Disabled -> localized("Off")
                        }
                    },
                    show = { healthSettings.trackingEnabled && healthSettings.hrmEnabled },
                ),
                basicSettingsToggleItem(
                    id = SettingsIds.HrmActivityTracking,
                    title = localized("HR During Activities"),
                    description = localized("Continuously track heart rate during detected walks and runs. This can have an impact on battery life."),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = healthSettings.hrmActivityTrackingEnabled,
                    show = { healthSettings.trackingEnabled && healthSettings.hrmEnabled },
                    onCheckChanged = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(hrmActivityTrackingEnabled = it)
                        )
                    },
                ),
                basicSettingsToggleItem(
                    id = EnableActivityInsights,
                    title = localized("Activity Insights"),
                    description = localized("Receive notifications with insights about your activity"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = healthSettings.activityInsightsEnabled,
                    show = { healthSettings.trackingEnabled },
                    onCheckChanged = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(
                                activityInsightsEnabled = it
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    id = EnableSleepInsights,
                    title = localized("Sleep Insights"),
                    description = localized("Receive notifications with insights about your sleep"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = healthSettings.sleepInsightsEnabled,
                    show = { healthSettings.trackingEnabled },
                    onCheckChanged = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(
                                sleepInsightsEnabled = it
                            )
                        )
                    },
                ),
                basicSettingsNumberFieldItem(
                    id = SettingsIds.HealthHeight,
                    title = localized("Height"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    description = localized("Used for accurate calorie and distance estimates"),
                    value = if (healthSettings.imperialUnits) (healthSettings.heightMm / 25.4).roundToLong()
                            else (healthSettings.heightMm / 10).toLong(),
                    onValueChange = { v ->
                        val mm = if (healthSettings.imperialUnits) (v * 25.4).roundToLong().toShort()
                                 else (v * 10).toShort()
                        libPebble.updateHealthSettings(healthSettings.copy(heightMm = mm))
                    },
                    show = { healthSettings.trackingEnabled },
                    min = if (healthSettings.imperialUnits) 39 else 100,
                    max = if (healthSettings.imperialUnits) 87 else 220,
                    unit = if (healthSettings.imperialUnits) "in" else "cm",
                    valueFormatter = if (healthSettings.imperialUnits) {
                        { v -> "${v / 12}' ${v % 12}\"" }
                    } else null,
                ),
                basicSettingsNumberFieldItem(
                    id = SettingsIds.HealthWeight,
                    title = localized("Weight"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    description = localized("Used for accurate calorie estimates"),
                    value = if (healthSettings.imperialUnits) (healthSettings.weightDag / 45.359).roundToLong()
                            else (healthSettings.weightDag / 100).toLong(),
                    onValueChange = { v ->
                        val dag = if (healthSettings.imperialUnits) (v * 45.359).roundToLong().toShort()
                                  else (v * 100).toShort()
                        libPebble.updateHealthSettings(healthSettings.copy(weightDag = dag))
                    },
                    show = { healthSettings.trackingEnabled },
                    min = if (healthSettings.imperialUnits) 66 else 30,
                    max = if (healthSettings.imperialUnits) 441 else 200,
                    unit = if (healthSettings.imperialUnits) "lb" else "kg",
                ),
                basicSettingsNumberFieldItem(
                    id = SettingsIds.HealthAge,
                    title = localized("Age"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    description = localized("Used for heart-rate zone calculations"),
                    value = healthSettings.ageYears.toLong(),
                    onValueChange = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(ageYears = it.toInt())
                        )
                    },
                    show = { healthSettings.trackingEnabled },
                    min = 1,
                    max = 120,
                    unit = "years",
                ),
                basicSettingsDropdownItem(
                    id = SettingsIds.HealthGenderId,
                    title = localized("Gender"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    selectedItem = healthSettings.gender,
                    items = HealthGender.entries,
                    onItemSelected = {
                        libPebble.updateHealthSettings(
                            healthSettings.copy(gender = it)
                        )
                    },
                    itemText = { it.name },
                    show = { healthSettings.trackingEnabled },
                ),
                basicSettingsToggleItem(
                    id = EnableHealthPlatformSync,
                    title = if (platform == Platform.IOS) localized("Sync to Apple Health") else localized("Sync to Health Connect"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = healthPlatformSyncEnabled,
                    description = localized("Write steps, heart rate, sleep, and workouts to your phone's health platform"),
                    show = { healthSettings.trackingEnabled && platformHealthSync.isAvailable() },
                    onCheckChanged = { enabled ->
                        scope.launch {
                            if (enabled) {
                                val granted = platformHealthSync.requestPermissions()
                                if (!granted) {
                                    logger.w { "Health platform permissions not granted" }
                                    return@launch
                                }
                            } else {
                                healthSyncTracker.setEnabled(false)
                            }
                        }
                    },
                ),
                basicSettingsActionItem(
                    title = localized("Sync Now"),
                    description = if (healthIsSyncing) localized("Syncing...") else localized("Sync Pebble health data to phone"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    show = { healthPlatformSyncEnabled },
                    action = if (healthIsSyncing) {
                        null
                    } else {
                        {
                            scope.launch {
                                platformHealthSync.sync()
                            }
                        }
                    },
                ),
                basicSettingsActionItem(
                    title = localized("Open Google Fit"),
                    description = localized("View your synced health data in Google Fit"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    show = { healthPlatformSyncEnabled && platform == Platform.Android },
                    action = {
                        openGoogleFitApp(uiContext)
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Show Health Tab"),
                    description = localized("Show Health instead of Notifications in the bottom bar"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    checked = coreConfig.preferHealthTab,
                    show = { coreConfig.enableIndex && healthSettings.trackingEnabled },
                    onCheckChanged = {
                        coreConfigHolder.update(coreConfig.copy(preferHealthTab = it))
                    },
                ),
                basicSettingsActionItem(
                    title = localized("View debug stats"),
                    description = localized("Health statistics and averages"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    keywords = "health steps sleep stats debug",
                    action = {
                        showHealthStatsDialog = true
                    },
                    show = { debugOptionsEnabled },
                ),
                basicSettingsActionItem(
                    title = localized("Populate fake health data"),
                    description = localized("Warning: this will wipe all existing health data!"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Health,
                    keywords = "health debug fake data populate",
                    action = {
                        showFakeHealthDataDialog.value = true
                    },
                    show = { debugOptionsEnabled },
                ),
                basicSettingsToggleItem(
                    title = localized("Enable Weather"),
                    description = localized("Fetch weather for the current location, for the Weather App (requires location permission)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Weather,
                    checked = coreConfig.fetchWeather,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                fetchWeather = it,
                            )
                        )
                        GlobalScope.launch { weatherFetcher.fetchWeather(this) }
                    },
                ),
                basicSettingsDropdownItem(
                    title = localized("Weather Refresh Interval"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Weather,
                    selectedItem = WeatherSyncInterval.from(coreConfig.weatherSyncInterval),
                    items = WeatherSyncInterval.entries,
                    onItemSelected = {
                        coreBackgroundSync.updateWeatherSyncPeriod(it.period)
                    },
                    itemText = {
                        it.displayName
                    },
                    show = { coreConfig.fetchWeather }
                ),
                basicSettingsToggleItem(
                    title = localized("Weather Pins"),
                    description = localized("Add weather pins to timeline"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Weather,
                    checked = coreConfig.weatherPinsV2,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                weatherPinsV2 = it,
                            )
                        )
                        GlobalScope.launch { weatherFetcher.fetchWeather(this) }
                    },
                    show = { coreConfig.fetchWeather }
                ),
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Locations"),
                    description = localized("Configure weather locations"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Weather,
                    action = {
                        nav.navigateTo(PebbleNavBarRoutes.WeatherRoute)
                    },
                    show = { coreConfig.fetchWeather }
                ) },
                basicSettingsToggleItem(
                    title = localized("Use LAN developer connection"),
                    description = localized("Allow connecting to developer connection over LAN, this is not secure and should only be used on trusted networks"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.watchConfig.lanDevConnection,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    lanDevConnection = it
                                )
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Show debug options"),
                    description = localized("Show some extra debug options around the app - not useful for most users (contains some options which might break your watch)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    checked = debugOptionsEnabled,
                    onCheckChanged = {
                        settings.set(SHOW_DEBUG_OPTIONS_SETTINGS_KEY, it)
                        debugOptionsEnabled = it
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("PKJS Debugger"),
                    description = localized("Allow connection via the ${if (platform == Platform.Android) "Chrome" else "Safari"} remote inspector to debug PKJS apps. Restart watchapp after changing.", "PKJS 앱을 디버깅할 수 있도록 ${if (platform == Platform.Android) "Chrome" else "Safari"} 원격 검사기 연결을 허용합니다. 변경한 뒤 시계 앱을 다시 실행해 주십시오."),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    checked = libPebbleConfig.watchConfig.pkjsInspectable,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    pkjsInspectable = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Watch settings sync"),
                    description = localized("Only for debugging - disables syncing settings with watch when disabled"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    checked = libPebbleConfig.watchConfig.enableWatchSettingsSync,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    enableWatchSettingsSync = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsDropdownItem(
                    id = OfflineSpeechRecognition,
                    title = localized("Offline Speech Recognition"),
                    keywords = "cactus stt speech recognition offline rebble",
                    topLevelType = TopLevelType.Phone,
                    section = Section.Speech,
                    items = CactusSTTMode.entries.filter { mode ->
                        when (mode) {
                            CactusSTTMode.RebbleOnly,
                            CactusSTTMode.RebbleFirst,
                            CactusSTTMode.RebbleFallback -> rebbleVoiceAvailable
                            CactusSTTMode.PlatformOnly -> platformSttAvailable
                            else -> true
                        }
                    },
                    selectedItem = coreConfig.sttConfig.mode,
                    onItemSelected = {
                        val isRebble = it == CactusSTTMode.RebbleOnly ||
                                it == CactusSTTMode.RebbleFirst ||
                                it == CactusSTTMode.RebbleFallback
                        val isPlatform = it == CactusSTTMode.PlatformOnly
                        val needsLocal = it == CactusSTTMode.LocalOnly ||
                                it == CactusSTTMode.LocalFirst ||
                                it == CactusSTTMode.RebbleFirst ||
                                it == CactusSTTMode.RebbleFallback
                        if (isRebble && !rebbleVoiceAvailable) {
                            snackbarDisplay.showSnackbar(localized("Rebble speech recognition requires a Rebble subscription"))
                            showSignInDialog = true
                        } else if (isPlatform && !platformSttAvailable) {
                            snackbarDisplay.showSnackbar(localized("This device doesn't support system speech recognition"))
                        } else if (it != CactusSTTMode.RemoteOnly && !isPlatform && !cactusSupported) {
                            snackbarDisplay.showSnackbar(localized("This device doesn't support local speech recognition"))
                        } else if (it != CactusSTTMode.LocalOnly && !isPlatform && !isRebble && coreUser == null) {
                            snackbarDisplay.showSnackbar(localized("You need to be signed in to use cloud speech recognition"))
                            showSignInDialog = true
                        } else if (needsLocal && !hasOfflineModels) {
                            pendingSTTModeDialog = it
                        } else {
                            coreConfigHolder.update(
                                coreConfig.copy(
                                    sttConfig = coreConfig.sttConfig.copy(
                                        mode = it
                                    )
                                )
                            )
                            if (isPlatform && uiContext != null) {
                                scope.launch {
                                    permissionRequester.requestPermission(
                                        Permission.SpeechRecognizer,
                                        uiContext,
                                    )
                                }
                            }
                            if (isPlatform && platformNeedsDownload) {
                                navBarNav?.navigateTo(CommonRoutes.SpeechModelDownloadDialog)
                            }
                        }
                    },
                    itemText = { mode ->
                        when (mode) {
                            CactusSTTMode.RemoteOnly -> localized("Cloud Only")
                            CactusSTTMode.RemoteFirst -> localized("Cloud (with Local Fallback)")
                            CactusSTTMode.LocalOnly -> localized("Local Only")
                            CactusSTTMode.LocalFirst -> localized("Local (with Cloud Fallback)")
                            CactusSTTMode.RebbleOnly -> localized("Rebble Only")
                            CactusSTTMode.RebbleFirst -> localized("Rebble (with Local Fallback)")
                            CactusSTTMode.RebbleFallback -> localized("Local (with Rebble Fallback)")
                            CactusSTTMode.PlatformOnly -> localized("System (On-Device)")
                        }
                    },
                    extraSupportingContent = {
                        modelDownloadState.takeIf { it.inProgress }?.let { state ->
                            Column {
                                Text(
                                    text = localized("Downloading in the background..."),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                (state as? ModelDownloadStatus.Downloading)?.progress?.let { progress ->
                                    CoreLinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                                    )
                                } ?: CoreLinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                                )
                            }
                        }
                    },
                ),
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = localized("Manage Offline Models"),
                    description = if (coreConfig.sttConfig.mode == CactusSTTMode.LocalOnly ||
                        coreConfig.sttConfig.mode == CactusSTTMode.RebbleFallback) {
                        localized("Note: Offline speech recognition is lower accuracy, consider using") +
                                localized("'Fallback only' mode to improve results when online")
                    } else {
                        null
                    },
                    keywords = "cactus stt speech recognition offline",
                    topLevelType = TopLevelType.Phone,
                    section = Section.Speech,
                    show = {
                        coreConfig.sttConfig.mode !in setOf(
                            CactusSTTMode.RemoteOnly,
                            CactusSTTMode.RebbleOnly,
                            CactusSTTMode.PlatformOnly,
                        ) || hasOfflineModels
                    },
                    action = {
                        nav.navigateTo(PebbleNavBarRoutes.OfflineModelsRoute)
                    },
                ) },
                navBarNav?.let { nav -> basicSettingsActionItem(
                    title = "System Speech Model",
                    description = "${spokenLanguageLabel(coreConfig.sttConfig.spokenLanguage)} · " +
                        platformModelState(platformModelAvailability, platformDownloadStatus),
                    keywords = "system stt speech recognition model download language",
                    topLevelType = TopLevelType.Phone,
                    section = Section.Speech,
                    show = { coreConfig.sttConfig.mode == CactusSTTMode.PlatformOnly },
                    action = { nav.navigateTo(CommonRoutes.SpeechModelDownloadDialog) }
                        .takeIf { platformNeedsDownload },
                ) },
                basicSettingsActionItem(
                    title = localized("Spoken Language"),
                    description = spokenLanguageLabel(coreConfig.sttConfig.spokenLanguage),
                    keywords = "language stt speech recognition locale iso",
                    topLevelType = TopLevelType.Phone,
                    section = Section.Speech,
                    action = { showSpokenLanguageDialog = true },
                ),
                SettingsItem(
                    title = localized("Cloud Recognition Provider"),
                    isDebugSetting = false,
                    topLevelType = TopLevelType.Phone,
                    section = Section.Speech,
                    keywords = "",
                    item = {
                        val logo = if (currentColorScheme().isDark) {
                            Res.drawable.wispr_flow_logo_white
                        } else {
                            Res.drawable.wispr_flow_logo_black
                        }
                        ListItem(
                            headlineContent = { Text(localized("Cloud Recognition Provider")) },
                            trailingContent = {
                                Image(
                                    painter = painterResource(logo),
                                    contentDescription = "Wispr Flow",
                                    modifier = Modifier.height(20.dp),
                                )
                            },
                            shadowElevation = ELEVATION,
                        )
                    }
                ),
                basicSettingsToggleItem(
                    title = localized("Ignore other Pebble apps"),
                    description = localized("Allow connection even when there are other Pebble apps installed on this phone. Warning: this will likely make the connection unreliable if you are using BLE! We don't recommend enabling this"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = coreConfig.ignoreOtherPebbleApps,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                ignoreOtherPebbleApps = !coreConfig.ignoreOtherPebbleApps,
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsDetectingOtherPebbleApps() },
                ),
                basicSettingsToggleItem(
                    title = localized("Send app crashes"),
                    description = localized("This allows us to fix crashes in the mobile app - otherwise we don't know how often they are happening, or how to fix them"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = enableFirebase.value,
                    onCheckChanged = {
                        enableFirebase.value = it
                        settings.set(KEY_ENABLE_FIREBASE_UPLOADS, it)
                        if (!it) {
                            coreAnalytics.logEvent("crashlytics_collection_disabled")
                        }
                        Firebase.crashlytics.setCrashlyticsCollectionEnabled(it)
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Send watch analytics"),
                    description = localized("Only for Core Devices watches. This allows us to measure metrics e.g. battery life, and debug watch crashes (otherwise we do not know whether they are regressions in reliability or performance)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = enableMemfault.value,
                    onCheckChanged = {
                        enableMemfault.value = it
                        if (!it) {
                            coreAnalytics.logEvent("memfault_collection_disabled")
                        }
                        settings.set(KEY_ENABLE_MEMFAULT_UPLOADS, it)
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Send app analytics"),
                    description = localized("This allows us to track metrics e.g. connectivity, so that we can track different types of error and improve reliability"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = enableMixpanel.value,
                    onCheckChanged = {
                        enableMixpanel.value = it
                        settings.set(KEY_ENABLE_MIXPANEL_UPLOADS, it)
                        if (!it) {
                            coreAnalytics.logEvent("mixpanel_collection_disabled")
                        }
                        analyticsBackend.setEnabled(it)
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Show notifications in phone logs"),
                    description = localized("Notification logging, to diagnose processing/deduplication issues (does not include any content/app name/personal information unless separately enabled below)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = libPebbleConfig.notificationConfig.dumpNotificationContent,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    dumpNotificationContent = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsNotificationLogging() },
                ),
                basicSettingsToggleItem(
                    title = localized("Show sensitive content in phone logs"),
                    description = localized("Include unredacted personal information (notification content, calendar events, app names, etc) in logs"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = !libPebbleConfig.notificationConfig.obfuscateContent,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                notificationConfig = libPebbleConfig.notificationConfig.copy(
                                    obfuscateContent = !it
                                )
                            )
                        )
                        coreConfigHolder.update(
                            coreConfig.copy(
                                obfuscateSensitiveLogs = !it
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Verbose connection logging"),
                    description = localized("Detailed connectivity state machine logging (please don't enable this unless we ask you to)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = libPebbleConfig.watchConfig.verboseWatchManagerLogging,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    verboseWatchManagerLogging = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Verbose PPoG logging"),
                    description = localized("Detailed Pebble Protocol over GATT logging (please don't enable this unless we ask you to)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Diagnostics,
                    checked = libPebbleConfig.bleConfig.verbosePpogLogging,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    verbosePpogLogging = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Re-publish GATT services after BT restore"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.bleConfig.republishGattServicesOnRestore,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    republishGattServicesOnRestore = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsRestartingGattServerAfterBtPowerOn() }
                ),
                basicSettingsToggleItem(
                    title = localized("Reversed PPoG"),
                    description = localized("Let the watch host the data connection when it supports it. Turn off to use the older phone-hosted mode. Reconnect for this to take effect"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.bleConfig.useReversedPpogV2,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    useReversedPpogV2 = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Bluetooth state restoration"),
                    description = localized("Let iOS relaunch the app to restore the watch connection. Takes effect on next app launch"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.bleConfig.centralStateRestoration,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    centralStateRestoration = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsCentralStateRestoration() },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Passive reconnection mode"),
                    description = localized("After a failed connection, wait for the watch to become available instead of retrying repeatedly"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.bleConfig.autoConnectAfterFailure,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    autoConnectAfterFailure = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsBleAutoConnect() },
                ),
                basicSettingsToggleItem(
                    title = localized("Filter BLE watch scans by UUID"),
                    description = localized("Disable this only if BLE scans are not finding your Core watch or Pebble 2"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = libPebbleConfig.bleConfig.filterScanResultsByUuid,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                bleConfig = libPebbleConfig.bleConfig.copy(
                                    filterScanResultsByUuid = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsActionItem(
                    title = localized("Post test notification"),
                    description = localized("Create a test notification, with actions"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    action = { postTestNotification(appContext) },
                    show = { pebbleFeatures.supportsPostTestNotification() },
                ),
                basicSettingsDropdownItem(
                    title = localized("Watch type for unknown devices"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    items = WatchType.entries,
                    selectedItem = libPebbleConfig.watchConfig.unknownWatchTypePlatform,
                    onItemSelected = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    unknownWatchTypePlatform = it
                                )
                            )
                        )
                    },
                    itemText = { it.name },
                    isDebugSetting = true,
                ),
                basicSettingsActionItem(
                    title = localized("Force JSCore GC"),
                    description = "",
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    action = {
                        libPebble.watches.value.filterIsInstance<ConnectedPebble.CompanionAppControl>()
                            .forEach {
                                it.currentCompanionAppSessions.value.filterIsInstance<PKJSApp>().firstOrNull()?.debugForceGC()
                            }
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Disable FW update notifications"),
                    description = localized("Ignore notifications for users who sideload their own firmware"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    checked = coreConfig.disableFirmwareUpdateNotifications,
                    onCheckChanged = {
                        coreConfigHolder.update(
                            coreConfig.copy(
                                disableFirmwareUpdateNotifications = it
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                basicSettingsActionItem(
                    title = localized("Do immediate background sync"),
                    description = localized("Sync firmware updates, locker, etc manually now (happens regularly automatically)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    action = {
                        GlobalScope.launch {
                            coreBackgroundSync.doBackgroundSync(this, force = true)
                        }
                    },
                    isDebugSetting = true,
                ),
                basicSettingsActionItem(
                    title = localized("Copy PKJS account token"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    action = { setShowCopyTokenDialog(true) },
                    show = { loggedIn != null },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Use experimental plugins"),
                    description = localized("Enable the new plugins API. This is an experimental feature under development - not recommended unless you know what you are doing (API is unstable, and can expose private data until a permission system is implemented)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Debug,
                    checked = libPebbleConfig.watchConfig.enablePlugins,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    enablePlugins = it
                                )
                            )
                        )
                    },
                    isDebugSetting = true,
                ),
                *libPebble.configurablePlugins().map { plugin ->
                    basicSettingsActionItem(
                        title = localized("Configure ${plugin.name}", "${plugin.name} 설정"),
                        description = localized("Settings for the ${plugin.name} plugin", "${plugin.name} 플러그인 설정"),
                        topLevelType = TopLevelType.Phone,
                        section = Section.BundledPlugins,
                        action = {
                            WatchappSettingsUrlCache.put(plugin.uuid, plugin.configPageUrl)
                            navBarNav?.navigateTo(
                                PebbleRoutes.WatchappSettingsRoute(
                                    uuid = plugin.uuid,
                                    title = plugin.name,
                                )
                            )
                        },
                        show = { libPebbleConfig.watchConfig.enablePlugins },
                        isDebugSetting = true,
                    )
                }.toTypedArray(),
                basicSettingsActionItem(
                    title = localized("Sign Out - Pebble Account"),
                    description = localized("Sign out of your Pebble account ($coreUser)", "Pebble 계정에서 로그아웃 ($coreUser)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    action = {
                        scope.launch {
                            try {
                                usersDao.signOut()
                                libPebble.requestLockerSync()
                                analyticsBackend.setUser(email = null)
                                logger.d { "User signed out" }
                            } catch (e: Exception) {
                                logger.e(e) { "Failed to sign out" }
                            }
                        }
                    },
                    show = { coreUser != null },
                ),
                basicSettingsActionItem(
                    title = localized("Sign In - Pebble Account"),
                    description = localized("Sign in to backup your Pebble account to backup apps, settings, etc"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    action = { showSignInDialog = true },
                    show = { coreUser == null },
                ),
                basicSettingsActionItem(
                    title = localized("Sign Out - Rebble"),
                    description = localized("Sign out of your Rebble account"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    action = {
                        scope.launch {
                            pebbleAccount.setToken(null, null)
                        }
                    },
                    show = { loggedIn != null },
                ),
                basicSettingsToggleItem(
                    title = localized("Auto-Resume Firmware Updates"),
                    description = localized("Automatically continue an interrupted firmware update when the watch reconnects"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    checked = libPebbleConfig.watchConfig.autoResumeFirmwareUpdate,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    autoResumeFirmwareUpdate = it
                                )
                            )
                        )
                    },
                ),
                navBarNav?.let {basicSettingsActionItem(
                    title = localized("Show Watch Onboarding"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    action = {
                        navBarNav.navigateTo(CommonRoutes.WatchOnboardingRoute)
                    },
                    show = { debugOptionsEnabled },
                ) },
                navBarNav?.let {basicSettingsActionItem(
                    title = localized("Show Ring Onboarding"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.General,
                    action = {
                        navBarNav.navigateTo(CommonRoutes.RingOnboardingRoute)
                    },
                    show = { debugOptionsEnabled },
                ) },
                basicSettingsToggleItem(
                    title = localized("Emulate Timeline Webservice"),
                    description = localized("Intercept calls to Timeline webservice, instead inserting pins locally, immediately"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Apps,
                    checked = libPebbleConfig.watchConfig.emulateRemoteTimeline,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    emulateRemoteTimeline = it
                                )
                            )
                        )
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Use Pebble Weather Service when apps are broken"),
                    description = localized("If old apps are using a broken weather API, attempt to use the Pebble Weather Service instead (will only work for some apps which use OpenWeather API)"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Apps,
                    checked = coreConfig.interceptPKJSWeather,
                    onCheckChanged = {
                        coreConfigHolder.update(coreConfig.copy(interceptPKJSWeather = it))
                    },
                ),
                basicSettingsToggleItem(
                    title = localized("Show watch connection debug info"),
                    description = localized("Extra debug info on devices tab"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Connectivity,
                    checked = coreConfig.showWatchConnectionDebugInfo,
                    onCheckChanged = {
                        coreConfigHolder.update(coreConfig.copy(showWatchConnectionDebugInfo = it))
                    },
                    isDebugSetting = true,
                ),
                basicSettingsToggleItem(
                    title = localized("Seek instead of Skip for Podcasts"),
                    description = localized("Icons will only update on updated PebbleOS version"),
                    topLevelType = TopLevelType.Phone,
                    section = Section.Music,
                    checked = libPebbleConfig.watchConfig.musicSeekWhenAvailable,
                    onCheckChanged = {
                        libPebble.updateConfig(
                            libPebbleConfig.copy(
                                watchConfig = libPebbleConfig.watchConfig.copy(
                                    musicSeekWhenAvailable = it
                                )
                            )
                        )
                    },
                    show = { pebbleFeatures.supportsMusic() },
                ),
            ) + watchPrefs
        }

    return SettingsItemsState(
        rawSettingsItems = rawSettingsItems,
        debugOptionsEnabled = debugOptionsEnabled,
        anyWatchSupportsSettingsSync = anyWatchSupportsSettingsSync,
        coreConfig = coreConfig,
        healthTrackingEnabled = healthSettings.trackingEnabled,
    )
}

@Composable
fun WatchSettingsScreen(navBarNav: NavBarNav, topBarParams: TopBarParams) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        val viewModel = koinViewModel<WatchSettingsScreenViewModel>()
        val platform = koinInject<Platform>()
        val state = rememberSettingsItemsState(navBarNav, topBarParams) ?: return

        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            topBarParams.searchAvailable(viewModel.searchState)
            topBarParams.actions { }
            topBarParams.title(localized("Settings"))
            launch {
                topBarParams.scrollToTop.collect {
                    if (listState.firstVisibleItemIndex > 0) {
                        listState.animateScrollToItem(0)
                    } else {
                        viewModel.selectedTopLevelType = TopLevelType.Phone
                    }
                }
            }
        }
        val libPebble = rememberLibPebble()
        val libPebbleConfig by libPebble.config.collectAsState()
        val settingsSyncEnabled = libPebbleConfig.watchConfig.enableWatchSettingsSync

        val availableTopLevelTypes = remember(state.anyWatchSupportsSettingsSync, state.coreConfig, settingsSyncEnabled) {
            TopLevelType.entries.filter {
                when (it) {
                    TopLevelType.Phone -> true
                    TopLevelType.Watch -> state.anyWatchSupportsSettingsSync && settingsSyncEnabled
                    TopLevelType.All -> state.coreConfig.showAllSettingsTab
                    TopLevelType.Notifications -> false
                }
            }
        }

        val validSettingsItems =
            remember(state.rawSettingsItems, viewModel.selectedTopLevelType, state.debugOptionsEnabled) {
                state.rawSettingsItems.filter {
                    viewModel.selectedTopLevelType.show(it.topLevelType) &&
                            (state.debugOptionsEnabled || !it.isDebugSetting)
                }
            }

        val searchQuery = viewModel.searchState.query

        val filteredItems by remember(
            validSettingsItems,
            viewModel.searchState.query,
        ) {
            derivedStateOf {
                if (searchQuery.isEmpty()) {
                    validSettingsItems.filter { item -> item.show() }
                } else {
                    validSettingsItems.filter {
                        (it.title.contains(searchQuery, ignoreCase = true) ||
                                it.keywords.contains(
                                    searchQuery,
                                    ignoreCase = true
                                )) && it.show()
                    }
                }
            }
        }

        val sectionsToShowInList = remember(filteredItems) {
            Section.entries.filter { section ->
                filteredItems.any { it.section == section } || section.navigatesDirectlyTo() != null
            }
        }
        val groupedItemsToDisplay = remember(filteredItems) {
            filteredItems.groupBy { it.section }.entries.sortedBy { it.key.ordinal }
        }

        val isSearching = searchQuery.isNotEmpty()

        Scaffold {
            Column {
                // Only show tab buttons at top if there is more than one
                if (availableTopLevelTypes.size > 1) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        SingleChoiceSegmentedButtonRow {
                            availableTopLevelTypes.forEachIndexed { index, type ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = availableTopLevelTypes.size
                                    ),
                                    selected = viewModel.selectedTopLevelType == type,
                                    onClick = { viewModel.selectedTopLevelType = type },
                                    icon = { },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                type.icon(platform),
                                                contentDescription = type.name,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                type.displayName,
                                                fontSize = 10.sp,
                                                lineHeight = 13.sp,
                                                modifier = Modifier.padding(top = 3.dp).widthIn(min = 55.dp),
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (viewModel.selectedTopLevelType == TopLevelType.Notifications) {
                    NotificationsScreenContent(topBarParams, navBarNav)
                    return@Column
                }

                if (isSearching) {
                    // When searching, show all matching settings grouped by section (like before)
                    LazyColumn(state = listState) {
                        groupedItemsToDisplay.forEach { (section, items) ->
                            stickyHeader {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    Text(
                                        section.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp,
                                        )
                                    )
                                }
                            }
                            items(
                                items = items,
                                key = { it.title },
                            ) { item ->
                                item.Item()
                            }
                            item {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                } else {
                    val notificationsDisplaced = state.coreConfig.enableIndex &&
                        state.healthTrackingEnabled && state.coreConfig.preferHealthTab
                    val healthDisplaced = state.coreConfig.enableIndex &&
                        state.healthTrackingEnabled && !state.coreConfig.preferHealthTab

                    // When not searching, show category list
                    LazyColumn(state = listState) {
                        if (notificationsDisplaced) {
                            item(key = "displaced_notifications") {
                                ListItem(
                                    leadingContent = {
                                        Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
                                    },
                                    headlineContent = { Text(localized("Notifications")) },
                                    shadowElevation = ELEVATION,
                                    modifier = Modifier.clickable {
                                        navBarNav.navigateTo(PebbleNavBarRoutes.NotificationsRoute)
                                    },
                                )
                            }
                        }
                        if (healthDisplaced) {
                            item(key = "displaced_health") {
                                ListItem(
                                    leadingContent = {
                                        Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
                                    },
                                    headlineContent = { Text(localized("Health")) },
                                    shadowElevation = ELEVATION,
                                    modifier = Modifier.clickable {
                                        navBarNav.navigateTo(PebbleNavBarRoutes.HealthRoute)
                                    },
                                )
                            }
                        }
                        items(
                            items = sectionsToShowInList,
                            key = { it.name },
                        ) { section ->
                            val sectionBadgeCount = filteredItems
                                .filter { it.section == section && it.badge != null && it.show() }
                                .sumOf { it.badge?.toIntOrNull() ?: 0 }
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        section.icon,
                                        contentDescription = null,
                                    )
                                },
                                headlineContent = { Text(section.title) },
                                trailingContent = if (sectionBadgeCount > 0) {
                                    {
                                        Badge {
                                            Text("$sectionBadgeCount")
                                        }
                                    }
                                } else null,
                                shadowElevation = ELEVATION,
                                modifier = Modifier.clickable {
                                    val navigateDirectlyTo = section.navigatesDirectlyTo()
                                    if (navigateDirectlyTo != null) {
                                        navBarNav.navigateTo(navigateDirectlyTo)
                                    } else {
                                        navBarNav.navigateTo(
                                            PebbleNavBarRoutes.WatchSettingsCategoryRoute(
                                                section = section.name,
                                                topLevelType = viewModel.selectedTopLevelType.name,
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchSettingsCategoryScreen(
    navBarNav: NavBarNav,
    topBarParams: TopBarParams,
    section: Section,
    topLevelType: TopLevelType,
) {
    val focusManager = LocalFocusManager.current
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val state = rememberSettingsItemsState(navBarNav, topBarParams) ?: return

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .imePadding()
            .clickable(
                interactionSource = dismissInteractionSource,
            indication = null,
            ) { focusManager.clearFocus() },
    ) {
        LaunchedEffect(Unit) {
            topBarParams.searchAvailable(null)
            topBarParams.actions {}
            topBarParams.title(section.title)
        }

        val filteredItems = remember(state.rawSettingsItems, topLevelType, state.debugOptionsEnabled) {
            state.rawSettingsItems.filter {
                it.section == section &&
                        topLevelType.show(it.topLevelType) &&
                        (state.debugOptionsEnabled || !it.isDebugSetting) &&
                        it.show()
            }
        }

        LazyColumn {
            items(
                items = filteredItems,
                key = { it.title },
            ) { item ->
                item.Item()
            }
        }
    }
}

fun basicSettingsActionItem(
    title: String,
    topLevelType: TopLevelType,
    section: Section,
    button: @Composable (() -> Unit)? = null,
    action: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    description: String? = null,
    keywords: String = "",
    show: () -> Boolean = { true },
    badge: String? = null,
    isDebugSetting: Boolean = false,
    onDisplayed: (@Composable () -> Unit)? = null
) = SettingsItem(
    title = title,
    topLevelType = topLevelType,
    section = section,
    keywords = keywords,
    show = show,
    badge = badge,
    isDebugSetting = isDebugSetting,
    item = {
        ListItem(
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (badge != null) {
                        Badge {
                            Text(text = badge)
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    if (button != null) {
                        button()
                    } else {
                        Text(title)
                    }
                }
            },
            supportingContent = {
                if (description != null) {
                    Text(description, fontSize = 12.sp)
                }
            },
            trailingContent = {
                if (action != null) {
                    Icon(
                        imageVector = actionIcon ?: Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            },
            shadowElevation = ELEVATION,
            modifier = Modifier.run() {
                if (action != null) {
                    clickable { action() }
                } else this
            },
        )
    },
    onDisplayed = onDisplayed,
)

fun basicSettingsToggleItem(
    id: String? = null,
    title: String,
    topLevelType: TopLevelType,
    section: Section,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    description: String? = null,
    keywords: String = "",
    show: () -> Boolean = { true },
    isDebugSetting: Boolean = false,
) = SettingsItem(
    id = id,
    title = title,
    topLevelType = topLevelType,
    section = section,
    keywords = keywords,
    show = show,
    isDebugSetting = isDebugSetting,
    item = {
        ListItem(
            headlineContent = {
                Text(title)
            },
            leadingContent = {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckChanged,
                )
            },
            supportingContent = {
                if (description != null) {
                    Text(description, fontSize = 11.sp)
                }
            },
            shadowElevation = ELEVATION,
        )
    },
)

fun basicSettingsNumberItem(
    id: String? = null,
    title: String,
    topLevelType: TopLevelType,
    section: Section,
    value: Long,
    onValueChange: (Long) -> Unit,
    min: Int,
    max: Int,
    unit: String,
    description: String? = null,
    keywords: String = "",
    show: () -> Boolean = { true },
    isDebugSetting: Boolean = false,
    defaultValue: Long? = null,
    valueFormatter: ((Long) -> String)? = null,
    steps: Int? = null,
) = SettingsItem(
    id = id,
    title = title,
    topLevelType = topLevelType,
    section = section,
    keywords = keywords,
    show = show,
    isDebugSetting = isDebugSetting,
    item = {
        ListItem(
            headlineContent = {
                Text(title)
            },
            supportingContent = {
                var sliderPosition by remember(value) { mutableLongStateOf(value) }
                Column {
                    if (description != null) {
                        Text(description, fontSize = 11.sp)
                    }
                    val minF = remember(min) { min.toFloat() }
                    val maxF = remember(max) { max.toFloat() }
                    val resolvedSteps = steps ?: remember(max, min) {
                        val range = max - min
                        if (range in 1..100) range - 1 else 0
                    }
                    Slider(
                        value = sliderPosition.toFloat(),
                        onValueChange = { sliderPosition = it.roundToLong() },
                        valueRange = minF..maxF,
                        steps = resolvedSteps,
                        onValueChangeFinished = {
                            onValueChange(sliderPosition)
                        },
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = valueFormatter?.invoke(sliderPosition) ?: "$sliderPosition $unit",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (defaultValue != null) {
                            TextButton(
                                onClick = {
                                    onValueChange(defaultValue)
                                },
                                enabled = value != defaultValue,
                            ) {
                                Text(
                                    text = localized("Default:", "기본값:") + " ${valueFormatter?.invoke(defaultValue) ?: "$defaultValue $unit"}",
                                    modifier = Modifier.widthIn(max = 150.dp),
                                    maxLines = 1,
                                    lineHeight = 12.sp,
                                )
                            }
                        }
                    }
                }
            },
            shadowElevation = ELEVATION,
        )
    },
)

fun basicSettingsNumberFieldItem(
    id: String? = null,
    title: String,
    topLevelType: TopLevelType,
    section: Section,
    value: Long,
    onValueChange: (Long) -> Unit,
    min: Int,
    max: Int,
    unit: String,
    description: String? = null,
    keywords: String = "",
    show: () -> Boolean = { true },
    isDebugSetting: Boolean = false,
    defaultValue: Long? = null,
    valueFormatter: ((Long) -> String)? = null,
) = SettingsItem(
    id = id,
    title = title,
    topLevelType = topLevelType,
    section = section,
    keywords = keywords,
    show = show,
    isDebugSetting = isDebugSetting,
    item = {
        ListItem(
            headlineContent = {
                Text(title)
            },
            supportingContent = {
                val minL = min.toLong()
                val maxL = max.toLong()
                var textFieldValue by remember(value) {
                    val text = value.toString()
                    mutableStateOf(TextFieldValue(text, selection = TextRange(text.length)))
                }
                var isFocused by remember { mutableStateOf(false) }
                LaunchedEffect(isFocused) {
                    if (isFocused) {
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length),
                        )
                    } else if (textFieldValue.text.toLongOrNull() != value) {
                        val text = value.toString()
                        textFieldValue = TextFieldValue(
                            text = text,
                            selection = TextRange(text.length),
                        )
                    }
                }
                Column {
                    if (description != null) {
                        Text(description, fontSize = 11.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        IconButton(
                            onClick = {
                                val newVal = (value - 1).coerceIn(minL, maxL)
                                if (newVal != value) {
                                    onValueChange(newVal)
                                }
                            },
                            enabled = value > minL,
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = localized("Decrease"))
                        }
                        OutlinedTextField(
                            value = textFieldValue,
                            onValueChange = { input ->
                                val filtered = input.text.filter { it.isDigit() }.take(6)
                                val parsed = filtered.toLongOrNull()
                                val finalText = if (parsed != null && parsed > maxL) {
                                    maxL.toString()
                                } else {
                                    filtered
                                }
                                textFieldValue = input.copy(
                                    text = finalText,
                                    selection = TextRange(finalText.length),
                                )
                                val finalParsed = finalText.toLongOrNull()
                                if (finalParsed != null && finalParsed in minL..maxL && finalParsed != value) {
                                    onValueChange(finalParsed)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            suffix = { Text(unit) },
                            modifier = Modifier
                                .width(110.dp)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                        )
                        IconButton(
                            onClick = {
                                val newVal = (value + 1).coerceIn(minL, maxL)
                                if (newVal != value) {
                                    onValueChange(newVal)
                                }
                            },
                            enabled = value < maxL,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = localized("Increase"))
                        }
                        if (valueFormatter != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = valueFormatter(value),
                                fontSize = 14.sp,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (defaultValue != null) {
                            TextButton(
                                onClick = {
                                    onValueChange(defaultValue)
                                },
                                enabled = value != defaultValue,
                            ) {
                                Text(
                                    text = localized("Default: $defaultValue", "기본값: $defaultValue"),
                                    modifier = Modifier.widthIn(max = 150.dp),
                                    maxLines = 1,
                                    lineHeight = 12.sp,
                                )
                            }
                        }
                    }
                }
            },
            shadowElevation = ELEVATION,
        )
    },
)

fun <T> basicSettingsDropdownItem(
    id: String? = null,
    title: String,
    description: String? = null,
    topLevelType: TopLevelType,
    section: Section,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemText: @Composable (T) -> String,
    keywords: String = "",
    show: () -> Boolean = { true },
    isDebugSetting: Boolean = false,
    extraSupportingContent: (@Composable () -> Unit)? = null,
) = SettingsItem(
    id = id,
    title = title,
    topLevelType = topLevelType,
    section = section,
    keywords = keywords,
    show = show,
    isDebugSetting = isDebugSetting,
    item = {
        ListItem(
            headlineContent = {
                Text(title)
            },
            trailingContent = {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            text = itemText(selectedItem),
                            modifier = Modifier.widthIn(max = 150.dp),
                            maxLines = 1,
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(itemText(option)) },
                                onClick = {
                                    onItemSelected(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            },
            supportingContent = {
                Row {
                    if (description != null) {
                        Text(description, fontSize = 11.sp)
                    }
                    extraSupportingContent?.invoke()
                }
            },
            shadowElevation = ELEVATION,
        )
    }
)

private const val SHOW_DEBUG_OPTIONS_SETTINGS_KEY = "showDebugOptions"

fun Settings.showDebugOptions() = getBoolean(SHOW_DEBUG_OPTIONS_SETTINGS_KEY, false)

@Composable
fun PKJSCopyTokenDialog(onDismissRequest: () -> Unit) {
    val libPebble = rememberLibPebble()
    val lockerEntriesFlow = remember { libPebble.getAllLockerBasicInfo() }
    val lockerEntries by lockerEntriesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(
                    localized("Copy Account Token"),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    items(lockerEntries.size) {
                        val lockerEntry = lockerEntries[it]
                        Column {
                            ListItem(
                                headlineContent = {
                                    Text(lockerEntry.title)
                                },
                                supportingContent = {
                                    Text(lockerEntry.developerName)
                                },
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        val token =
                                            libPebble.getAccountToken(lockerEntry.id)
                                        if (token != null) {
                                            launch {
                                                clipboard.setClipEntry(makeTokenClipEntry(token))
                                            }
                                        } else {
                                            logger.e { "Failed to get account token for ${lockerEntry.id}" }
                                        }
                                        onDismissRequest()
                                    }
                                },
                                shadowElevation = ELEVATION,
                            )
                        }
                    }
                }
            }
        }
    }
}

expect fun getPlatformSTTLanguages(): List<Pair<String, String>>

@Composable
fun STTLanguageDialog(
    onLanguageSelected: (String?) -> Unit,
    onDismissRequest: () -> Unit,
    selectedLanguage: String?,
) {
    var targetLanguage by remember { mutableStateOf(selectedLanguage) }
    M3Dialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.Language, contentDescription = null) },
        title = { Text(localized("Select language")) },
        buttons = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(localized("Cancel"))
            }
            TextButton(
                onClick = {
                    onLanguageSelected(targetLanguage)
                }
            ) {
                Text(localized("OK"))
            }
        }
    ) {
        val supportedLanguages = remember { getPlatformSTTLanguages() }
        LazyColumn {
            items(supportedLanguages.size) {
                val (code, name) = supportedLanguages[it]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            targetLanguage = if (targetLanguage == code) {
                                null
                            } else {
                                code
                            }
                        }
                        .padding(16.dp)
                ) {
                    Checkbox(
                        checked = targetLanguage == code,
                        onCheckedChange = {
                            targetLanguage = if (it) {
                                code
                            } else {
                                null
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(name)
                }
            }
        }
    }
}

@Composable
fun SpokenLanguagePickerDialog(
    selectedCode: String?,
    onLanguageSelected: (String?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val options = remember { SpokenLanguageOptions }
    val filtered = remember(query, options) {
        if (query.isBlank()) {
            options
        } else {
            val q = query.trim().lowercase()
            options.filter { (code, name) ->
                name.lowercase().contains(q) || code.lowercase().contains(q)
            }
        }
    }
    val showAutomatic = query.isBlank() || "automatic".contains(query.trim().lowercase())
    M3Dialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.Language, contentDescription = null) },
        title = { Text(localized("Spoken Language")) },
        buttons = {
            TextButton(onClick = onDismissRequest) { Text(localized("Cancel")) }
        },
    ) {
        Column(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text(localized("Search languages")) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                if (showAutomatic) {
                    item(key = "__automatic__") {
                        SpokenLanguageRow(
                            label = localized("Automatic"),
                            selected = selectedCode == null,
                            onClick = { onLanguageSelected(null) },
                        )
                    }
                }
                items(filtered, key = { it.first }) { (code, name) ->
                    SpokenLanguageRow(
                        label = name,
                        selected = selectedCode == code,
                        onClick = { onLanguageSelected(code) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                localized("Note: Selecting a language may improve accuracy for that language but not all languages on this list are guaranteed to be supported."),
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun SpokenLanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

expect fun makeTokenClipEntry(token: String): ClipEntry

expect fun openGoogleFitApp(uiContext: PlatformUiContext?)

object SettingsKeys {
    const val KEY_ENABLE_MEMFAULT_UPLOADS = "enable_memfault_uploads"
    const val KEY_ENABLE_FIREBASE_UPLOADS = "enable_firebase_uploads"
    const val KEY_ENABLE_MIXPANEL_UPLOADS = "enable_mixpanel_uploads"
}

enum class RegularSyncInterval(
    val period: Duration,
    val displayName: String,
) {
    SixHours(6.hours, localized("6 hours")),
    TwelveHours(12.hours, localized("12 hours")),
    TwentyFourHours(24.hours, localized("24 hours")),
    ;

    companion object {
        fun from(period: Duration): RegularSyncInterval = entries.find { it.period == period } ?: SixHours
    }
}

enum class WeatherSyncInterval(
    val period: Duration,
    val displayName: String,
) {
    FifteenMinutes(15.minutes, localized("15 minutes")),
    ThirtyMinutes(30.minutes, localized("30 minutes")),
    OneHour(1.hours, localized("1 hour")),
    SixHours(6.hours, localized("6 hours")),
    ;

    companion object {
        fun from(period: Duration): WeatherSyncInterval = entries.find { it.period == period } ?: OneHour
    }
}
