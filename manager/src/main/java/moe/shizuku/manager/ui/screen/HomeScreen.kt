@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package moe.shizuku.manager.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import java.io.File
import moe.shizuku.manager.ui.component.PillButton
import moe.shizuku.manager.ui.component.ScrollToTopButton
import moe.shizuku.manager.ui.component.PillButtonQuiet
import moe.shizuku.manager.ui.component.ExpressiveCard
import moe.shizuku.manager.ui.component.StatusIndicator
import moe.shizuku.manager.ui.component.SectionHeading
import moe.shizuku.manager.ui.component.EmptyState
import moe.shizuku.manager.ui.component.ErrorState
import moe.shizuku.manager.ui.component.LoadingState
import moe.shizuku.manager.BuildConfig
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.LocalContentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.Helps
import moe.shizuku.manager.Manifest
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.home.showAccessibilityDialog
import moe.shizuku.manager.manage.Hiding
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.start.StartFailureKind
import moe.shizuku.manager.start.StartStatus
import moe.shizuku.manager.start.StartStatusReporter
import moe.shizuku.manager.start.stillInTheWay
import moe.shizuku.manager.utils.Diag
import moe.shizuku.manager.service.HidingWatchService
import moe.shizuku.manager.start.isDeveloperOptionsEnabled
import moe.shizuku.manager.start.isPermissionPermanentlyDenied
import moe.shizuku.manager.start.restoreDeveloperOptions
import moe.shizuku.manager.start.localNetworkPermission
import moe.shizuku.manager.start.needsLocalNetworkPermissionFor
import moe.shizuku.manager.start.openAppSettings
import moe.shizuku.manager.start.openAdbPortAndStart
import moe.shizuku.manager.start.StartMethodGuard
import moe.shizuku.manager.start.startMethodLabelRes
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.starter.StarterActivity
import moe.shizuku.manager.ui.component.SegmentedColumn
import moe.shizuku.manager.ui.theme.LocalAmoledTheme
import moe.shizuku.manager.ui.component.SegmentedListItem
import moe.shizuku.manager.ui.component.stripHtmlTags
import moe.shizuku.manager.ui.component.LiveDeviceInfoSection
import moe.shizuku.manager.utils.CustomTabsHelper
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.PermissionOwnership
import moe.shizuku.manager.utils.SettingsHelper
import moe.shizuku.manager.utils.SettingsPage
import moe.shizuku.manager.utils.ShizukuStateMachine
import moe.shizuku.manager.utils.permissionOwnership
import moe.shizuku.manager.utils.runShellCommand
import moe.shizuku.manager.utils.UpdateHelper
import rikka.core.util.ClipboardUtils
import rikka.shizuku.Shizuku

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(bottomPadding: Dp) {
    val context = LocalContext.current

    var running by remember { mutableStateOf(ShizukuStateMachine.isRunning()) }
    var uid by remember { mutableStateOf(if (running) runCatching { Shizuku.getUid() }.getOrDefault(-1) else -1) }
    var batteryIgnored by remember {
        mutableStateOf(SettingsHelper.isIgnoringBatteryOptimizations(context))
    }
    var showAdbCommand by remember { mutableStateOf(false) }
    var permissionOwner by remember { mutableStateOf<PermissionOwnership>(PermissionOwnership.Ours) }
    var permissionWarningDismissed by remember { mutableStateOf<PermissionOwnership?>(null) }
    var updateAvailable by remember { mutableStateOf(false) }
    var rooted by remember { mutableStateOf(false) }
    var startMethod by remember { mutableStateOf(ShizukuSettings.getStartMethod()) }
    var developerOptionsOn by remember { mutableStateOf(context.isDeveloperOptionsEnabled()) }
    var hidingActive by remember { mutableStateOf(false) }
    var otherMethodsOpen by rememberSaveable { mutableStateOf(false) }
    var selinuxRes by remember { mutableStateOf<Int?>(null) }
    var seccompRes by remember { mutableStateOf<Int?>(null) }
    var diagnostics by remember { mutableStateOf("") }
    val startStatus by StartStatusReporter.status.collectAsState()
    val scope = rememberCoroutineScope()

    var pendingLocalNetworkAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val localNetworkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingLocalNetworkAction
        pendingLocalNetworkAction = null
        val permission = localNetworkPermission()
        if (!granted && permission != null && context.isPermissionPermanentlyDenied(permission)) {
            Toast.makeText(
                context,
                context.getString(R.string.permissions_open_settings_hint),
                Toast.LENGTH_LONG
            ).show()
            context.openAppSettings()
            return@rememberLauncherForActivityResult
        }
        action?.invoke()
    }

    fun startWithLocalNetworkPermission(
        @ShizukuSettings.StartMethod method: Int,
        action: () -> Unit
    ) {
        if (context.needsLocalNetworkPermissionFor(method)) {
            pendingLocalNetworkAction = action
            localNetworkPermission()?.let { localNetworkLauncher.launch(it) }
        } else {
            action()
        }
    }

    suspend fun refresh() {
        ShizukuStateMachine.update()
        running = ShizukuStateMachine.isRunning()
        batteryIgnored = SettingsHelper.isIgnoringBatteryOptimizations(context)
        developerOptionsOn = context.isDeveloperOptionsEnabled()
        hidingActive = HidingWatchService.isRunning() && Hiding.isActive()
        startMethod = StartMethodGuard.resolve()
        if (running) {
            StartStatusReporter.clear()
        } else {
            val status = StartStatusReporter.status.value
            if (status is StartStatus.Failed &&
                !status.kind.stillInTheWay(
                    wifiConnected = EnvironmentUtils.isWifiConnected(),
                    wirelessDebuggingOn = EnvironmentUtils.isWirelessDebuggingEnabled(),
                    adbPort = EnvironmentUtils.getAdbTcpPort()
                )
            ) {
                Diag.info(TAG, "dropping the start failure whose cause is gone: ${status.kind}")
                StartStatusReporter.clear()
            }
        }

        withContext(Dispatchers.IO) {
            uid = if (running) runCatching { Shizuku.getUid() }.getOrDefault(-1) else -1
            rooted = runCatching { EnvironmentUtils.isRooted() }.getOrDefault(false)
            val (selinux, seccomp) = readDeviceStatus()
            selinuxRes = selinux
            seccompRes = seccomp
            diagnostics = homeBuildDiagnostics(
                context = context,
                running = running,
                uid = uid,
                method = startMethod,
                hidingActive = hidingActive,
                batteryIgnored = batteryIgnored,
                rooted = rooted
            )
        }
    }

    fun turnDeveloperOptionsBackOn() {
        ShizukuSettings.setAdbWithoutDeveloperOptions(context, false)
        restoreDeveloperOptions(context)
        scope.launch { refresh() }
    }

    DisposableEffect(Unit) {
        val listener: (ShizukuStateMachine.State) -> Unit = {
            scope.launch { refresh() }
        }
        ShizukuStateMachine.addListener(listener)
        onDispose { ShizukuStateMachine.removeListener(listener) }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { refresh() }
        permissionOwner = readPermissionOwnership(context)
    }

    LaunchedEffect(Unit) {
        refresh()
        permissionOwner = readPermissionOwnership(context)
        updateAvailable = runCatching {
            UpdateHelper.isCheckForUpdatesEnabled() && UpdateHelper.isNewUpdateAvailable()
        }.getOrDefault(false)
        if (updateAvailable) {
            runCatching { UpdateHelper.updateLastPromptedVersion() }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Permission ownership warning
                val ownership = permissionOwner
                if (ownership !is PermissionOwnership.Ours && ownership != permissionWarningDismissed) {
                    item(key = "permission_ownership", contentType = "warning") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = moe.shizuku.manager.ui.theme.OmniTokens.shapeLarge
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        if (ownership is PermissionOwnership.Other)
                                            R.string.home_dialog_duplicate_app_detected_title
                                        else R.string.home_dialog_reboot_required_title
                                    ),
                                    style = moe.shizuku.manager.ui.theme.OmniTokens.displayMedium(),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = if (ownership is PermissionOwnership.Other) {
                                        stringResource(
                                            R.string.home_permission_owned_by,
                                            ownership.packageName
                                        )
                                    } else {
                                        stringResource(R.string.home_permission_not_registered)
                                    },
                                    style = moe.shizuku.manager.ui.theme.OmniTokens.bodyMedium(),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                PillButtonQuiet(onClick = { permissionWarningDismissed = ownership }) {
                                    Text(stringResource(R.string.action_dismiss))
                                }
                            }
                        }
                    }
                }

                // Update available banner
                if (updateAvailable) {
                    item(key = "update_banner", contentType = "banner") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            shape = moe.shizuku.manager.ui.theme.OmniTokens.shapeLarge
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.snackbar_update_available),
                                    modifier = Modifier.weight(1f),
                                    style = moe.shizuku.manager.ui.theme.OmniTokens.bodyMedium(),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                PillButton(onClick = {
                                    scope.launch {
                                        runCatching { UpdateHelper.update() }
                                        updateAvailable = false
                                    }
                                }) { Text(stringResource(R.string.snackbar_action_update)) }
                            }
                        }
                    }
                }

                // Battery warning
                if (!batteryIgnored) {
                    item(key = "battery_warning", contentType = "warning") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = moe.shizuku.manager.ui.theme.OmniTokens.shapeLarge
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.home_battery_warning),
                                    style = moe.shizuku.manager.ui.theme.OmniTokens.bodyMedium(),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                ) {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            SettingsHelper.requestIgnoreBatteryOptimizationsPrivileged(context) {
                                                batteryIgnored = SettingsHelper.isIgnoringBatteryOptimizations(context)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    ) { Text(stringResource(R.string.snackbar_action_fix)) }
                                }
                            }
                        }
                    }
                }

                // Hiding active info
                if (hidingActive) {
                    item(key = "hiding_active", contentType = "info") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            shape = moe.shizuku.manager.ui.theme.OmniTokens.shapeLarge
                        ) {
                            Text(
                                text = stringResource(R.string.home_hiding_active_note),
                                modifier = Modifier.padding(16.dp),
                                style = moe.shizuku.manager.ui.theme.OmniTokens.bodyMedium(),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Status section heading
                item(key = "status_heading", contentType = "heading") {
                    SectionHeading(text = stringResource(R.string.home_section_status))
                }

                // Status card
                item(key = "status_card", contentType = "status") {
                    StatusCard(
                        running = running,
                        uid = uid,
                        startMethodLabelRes = startMethodLabelRes(startMethod)
                    )
                }

                // Server actions section heading
                item(key = "actions_heading", contentType = "heading") {
                    SectionHeading(text = stringResource(R.string.home_section_actions))
                }

                // Server action buttons
                item(key = "server_actions", contentType = "actions") {
                    ServerActionButtons(
                        running = running,
                        starting = startStatus is StartStatus.Starting,
                        onStart = {
                            startWithLocalNetworkPermission(ShizukuSettings.getStartMethod()) {
                                ShizukuReceiverStarter.start(context, userInitiated = true, reason = "home-start")
                            }
                        },
                        onStop = {
                            ShizukuSettings.setManuallyStopped(true)
                            ShizukuStateMachine.set(ShizukuStateMachine.State.STOPPING)
                            runCatching { Shizuku.exit() }
                        },
                        onRestart = {
                            startWithLocalNetworkPermission(ShizukuSettings.getStartMethod()) {
                                ShizukuReceiverStarter.start(
                                    context,
                                    forceStart = true,
                                    userInitiated = true,
                                    reason = "home-restart"
                                )
                            }
                        }
                    )
                }

                // Start methods section heading
                item(key = "methods_heading", contentType = "heading") {
                    SectionHeading(text = stringResource(R.string.home_section_methods))
                }

                // Start methods card
                item(key = "start_methods", contentType = "methods") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExpressiveCard(
                            icon = Icons.Rounded.MoreHoriz,
                            title = stringResource(R.string.home_other_methods),
                            onClick = { otherMethodsOpen = !otherMethodsOpen },
                            trailing = {
                                val turn by animateFloatAsState(
                                    targetValue = if (otherMethodsOpen) 180f else 0f,
                                    label = "otherMethodsChevron"
                                )
                                Icon(
                                    Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.rotate(turn)
                                )
                            }
                        )

                        if (otherMethodsOpen) {
                            ExpressiveCard(
                                icon = Icons.Rounded.Wifi,
                                title = stringResource(R.string.home_wireless_adb_title),
                                body = if (startStatus is StartStatus.Starting &&
                                    ShizukuSettings.getForceWirelessDebugging()
                                ) {
                                    stringResource(R.string.home_wireless_adb_starting_without_wifi)
                                } else {
                                    stringResource(R.string.home_wireless_adb_summary)
                                },
                                enabled = !running,
                                onClick = {
                                    startWithLocalNetworkPermission(ShizukuSettings.StartMethod.WIRELESS) {
                                        ShizukuReceiverStarter.start(
                                            context,
                                            userInitiated = true,
                                            startMethod = ShizukuSettings.StartMethod.WIRELESS,
                                            reason = "home-wireless"
                                        )
                                    }
                                }
                            )
                            ExpressiveCard(
                                icon = Icons.Rounded.Usb,
                                title = stringResource(R.string.home_usb_adb_title),
                                body = if (!EnvironmentUtils.isWifiConnected() &&
                                    EnvironmentUtils.getAdbTcpPort() <= 0
                                ) {
                                    stringResource(R.string.home_usb_adb_needs_network).stripHtmlTags()
                                } else {
                                    stringResource(R.string.home_usb_adb_summary).stripHtmlTags()
                                },
                                enabled = !running,
                                onClick = {
                                    ShizukuReceiverStarter.start(
                                        context,
                                        userInitiated = true,
                                        startMethod = ShizukuSettings.StartMethod.USB,
                                        reason = "home-usb"
                                    )
                                }
                            )
                            if (rooted) {
                                val rootDescription = stringResource(
                                    R.string.home_root_description,
                                    "<b><a href=\"${Helps.SUI.get()}\">Sui</a></b>",
                                    "Sui"
                                ).stripHtmlTags()

                                ExpressiveCard(
                                    icon = Icons.Rounded.Numbers,
                                    title = stringResource(R.string.home_root_title),
                                    body = rootDescription,
                                    enabled = !running,
                                    onClick = {
                                        context.startActivity(
                                            Intent(context, StarterActivity::class.java)
                                                .putExtra(StarterActivity.EXTRA_IS_ROOT, true)
                                        )
                                    }
                                )
                            }
                            ExpressiveCard(
                                icon = Icons.Rounded.AdminPanelSettings,
                                title = stringResource(R.string.home_system_title),
                                body = stringResource(R.string.home_system_summary),
                                enabled = !running,
                                onClick = {
                                    ShizukuReceiverStarter.start(
                                        context,
                                        userInitiated = true,
                                        startMethod = ShizukuSettings.StartMethod.SYSTEM,
                                        reason = "home-system"
                                    )
                                }
                            )
                            ExpressiveCard(
                                icon = Icons.Rounded.Computer,
                                title = stringResource(R.string.intents_adb_command),
                                body = stringResource(R.string.home_adb_command_summary),
                                enabled = !running,
                                onClick = { showAdbCommand = true }
                            )
                        }
                    }
                }

                // Device details section heading
                item(key = "device_heading", contentType = "heading") {
                    SectionHeading(text = stringResource(R.string.home_section_device))
                }

                // Device details
                item(key = "device_details", contentType = "details") {
                    SegmentedColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_manager_version)) },
                                supportingContent = { Text(BuildConfig.VERSION_NAME) }
                            )
                        }
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_android_version)) },
                                supportingContent = { Text(androidVersion()) }
                            )
                        }
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_kernel_version)) },
                                supportingContent = { Text(kernelVersion()) }
                            )
                        }
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_model)) },
                                supportingContent = { Text(deviceModel()) }
                            )
                        }
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_fingerprint)) },
                                supportingContent = { Text(Build.FINGERPRINT ?: "-") }
                            )
                        }
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_selinux)) },
                                supportingContent = { Text(selinuxRes?.let { stringResource(it) } ?: "-") }
                            )
                        }
                        item {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.home_device_seccomp)) },
                                supportingContent = { Text(seccompRes?.let { stringResource(it) } ?: "-") }
                            )
                        }
                    }
                }

                // Diagnostics card
                if (diagnostics.isNotEmpty()) {
                    item(key = "diagnostics", contentType = "diagnostics") {
                        DiagnosticsCard(
                            text = diagnostics,
                            onCopy = {
                                val clipboard = context.getSystemService(ClipboardManager::class.java)
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText(
                                            context.getString(R.string.diagnostics_title),
                                            diagnostics
                                        )
                                    )
                                    Toast.makeText(
                                        context,
                                        R.string.diagnostics_copied,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }

                // Live metrics section heading
                item(key = "metrics_heading", contentType = "heading") {
                    SectionHeading(text = stringResource(R.string.home_section_metrics))
                }

                // Live metrics
                item(key = "live_metrics", contentType = "metrics") {
                    LiveDeviceInfoSection()
                }

                // Support coffee section heading
                item(key = "support_heading", contentType = "heading") {
                    SectionHeading(text = stringResource(R.string.home_section_support))
                }

                // Support coffee
                item(key = "support_coffee", contentType = "coffee") {
                    ExpressiveCard(
                        icon = Icons.Rounded.Coffee,
                        title = stringResource(R.string.settings_support_coffee),
                        body = stringResource(R.string.home_support_body),
                        onClick = {
                            CustomTabsHelper.launchUrlOrCopy(
                                context,
                                context.getString(R.string.buymeacoffee_url)
                            )
                        }
                    )
                }
            }

            ScrollToTopButton(
                listState = listState,
                bottomPadding = bottomPadding,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    if (showAdbCommand) {
        AlertDialog(
            onDismissRequest = { showAdbCommand = false },
            title = { Text(stringResource(R.string.intents_adb_command)) },
            text = { Text(Starter.adbCommand, fontFamily = FontFamily.Monospace) },
            confirmButton = {
                PillButton(onClick = {
                    if (ClipboardUtils.put(context, Starter.adbCommand)) {
                        Toast.makeText(context, context.getString(R.string.toast_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    }
                    showAdbCommand = false
                }) { Text(stringResource(R.string.intents_copy)) }
            },
            dismissButton = {
                PillButtonQuiet(onClick = { showAdbCommand = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun StatusCard(
    running: Boolean,
    uid: Int,
    @StringRes startMethodLabelRes: Int
) {
    val context = LocalContext.current
    val containerColor = if (running) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (running) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val outlined = LocalAmoledTheme.current && !running

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (outlined) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = moe.shizuku.manager.ui.theme.OmniTokens.shapeLarge
                    )
                } else {
                    Modifier
                }
            ),
        color = containerColor,
        contentColor = contentColor,
        shape = moe.shizuku.manager.ui.theme.OmniTokens.shapeLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                if (running) Icons.Rounded.CheckCircle else Icons.Rounded.StopCircle,
                contentDescription = null,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        if (running) R.string.status_running_short else R.string.status_stopped_short
                    ),
                    style = moe.shizuku.manager.ui.theme.OmniTokens.displayMedium()
                )
                StatusFacts(
                    StatusFactEntry(
                        R.string.home_status_started_with,
                        if (running) uid.toString() else stringResource(R.string.status_value_none)
                    ),
                    StatusFactEntry(
                        R.string.home_status_started_default,
                        stringResource(startMethodLabelRes)
                    ),
                    StatusFactEntry(
                        R.string.home_status_transport_label,
                        if (running) "Wireless" else stringResource(R.string.status_value_none)
                    ),
                    StatusFactEntry(
                        R.string.uid_label,
                        if (uid < 0) stringResource(R.string.status_value_none) else uid.toString(),
                        when {
                            uid == 1000 -> "system"
                            uid == 0 -> "root"
                            else -> uid.toString()
                        }
                    )
                )
            }
        }
    }
}

private const val TAG = "StartStatus"

private fun deviceModel(): String = buildString {
    append(Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
    if (!Build.BRAND.equals(Build.MANUFACTURER, ignoreCase = true)) {
        append(' ').append(Build.BRAND)
    }
    append(' ').append(Build.MODEL)
}

private fun kernelVersion(): String = System.getProperty("os.version").orEmpty().ifEmpty { "-" }

private fun androidVersion(): String = "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

@StringRes
private fun readSelinuxStatus(): Int? {
    val raw = runShellCommand("cat /sys/fs/selinux/enforce")
        ?: runCatching { File("/sys/fs/selinux/enforce").readText().trim() }.getOrNull()

    when (raw) {
        "1" -> return R.string.selinux_enforcing
        "0" -> return R.string.selinux_permissive
    }

    val (stdout, stderr) = runCatching {
        val process = ProcessBuilder("/system/bin/sh", "-c", "getenforce").start()
        val out = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val err = process.errorStream.bufferedReader().use { it.readText() }.trim()
        process.waitFor()
        out to err
    }.getOrNull() ?: return null

    return when {
        stdout.equals("Enforcing", true) -> R.string.selinux_enforcing
        stdout.equals("Permissive", true) -> R.string.selinux_permissive
        stdout.equals("Disabled", true) -> R.string.selinux_disabled
        stderr.contains("Permission denied") -> R.string.selinux_enforcing
        else -> null
    }
}

@StringRes
private fun readSeccompStatus(): Int? {
    val mode = runCatching {
        File("/proc/self/status").readLines()
            .firstOrNull { it.startsWith("Seccomp:") }
            ?.substringAfter(':')
            ?.trim()
    }.getOrNull()

    return when (mode) {
        "0" -> R.string.seccomp_disabled
        "1" -> R.string.seccomp_strict
        "2" -> R.string.seccomp_filter
        else -> null
    }
}

private fun readDeviceStatus(): Pair<Int?, Int?> = readSelinuxStatus() to readSeccompStatus()

private data class StatusFactEntry(
    @StringRes val label: Int,
    val value: String,
    val detail: String? = null
)

@Composable
private fun StatusFacts(vararg facts: StatusFactEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(IntrinsicSize.Min)
    ) {
        facts.forEachIndexed { index, fact ->
            if (index > 0) {
                VerticalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    thickness = 1.dp,
                    color = LocalContentColor.current.copy(alpha = 0.25f)
                )
            }
            StatusFact(fact, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun StatusFact(fact: StatusFactEntry, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            stringResource(fact.label),
            style = moe.shizuku.manager.ui.theme.OmniTokens.captionStyle(),
            color = LocalContentColor.current.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            fact.value,
            style = moe.shizuku.manager.ui.theme.OmniTokens.bodyMedium(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        fact.detail?.let {
            Text(
                it,
                style = moe.shizuku.manager.ui.theme.OmniTokens.captionStyle(),
                color = LocalContentColor.current.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val RestartBlue = Color(0xFF2563EB)

@Composable
private fun ServerActionButtons(
    running: Boolean,
    starting: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f),
            enabled = !running && !starting,
            onClick = onStart
        ) {
            if (starting) {
                LoadingIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(stringResource(R.string.action_start))
            }
        }

        Button(
            modifier = Modifier.weight(1f),
            enabled = running,
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text(stringResource(R.string.action_stop))
        }

        Button(
            modifier = Modifier.weight(1f),
            enabled = running,
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(
                containerColor = RestartBlue,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.action_restart))
        }
    }
}

private fun readPermissionOwnership(context: Context): PermissionOwnership {
    val manager = context.packageManager
    val owner = runCatching {
        manager.getPermissionInfo(Manifest.permission.API_V23, 0).packageName
    }.getOrNull()
    val groupResolved = runCatching {
        manager.getPermissionGroupInfo(Manifest.permission_group.API, 0)
    }.isSuccess

    return permissionOwnership(context.packageName, owner, groupResolved)
}

private fun homeBuildDiagnostics(
    context: Context,
    running: Boolean,
    uid: Int,
    method: Int,
    hidingActive: Boolean,
    batteryIgnored: Boolean,
    rooted: Boolean
): String {
    val sb = StringBuilder()
    sb.append("Shizuku status: ${if (running) "running" else "stopped"}\n")
    sb.append("Start method: $method\n")
    if (uid >= 0) sb.append("UID: $uid\n")
    sb.append("Rooted: $rooted\n")
    sb.append("Battery optimization ignored: $batteryIgnored\n")
    sb.append("Hiding active: $hidingActive\n")
    return sb.toString()
}
