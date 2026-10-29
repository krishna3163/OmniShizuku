package moe.shizuku.manager.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ui.Detail
import moe.shizuku.manager.ui.screen.ActivitiesScreen
import moe.shizuku.manager.ui.screen.CatalogKind
import moe.shizuku.manager.ui.screen.CatalogScreen
import moe.shizuku.manager.ui.screen.DeviceInfoScreen
import moe.shizuku.manager.ui.screen.DisplayScreen
import moe.shizuku.manager.ui.screen.FilesScreen
import moe.shizuku.manager.ui.screen.ForceDarkScreen
import moe.shizuku.manager.ui.screen.HidingScreen
import moe.shizuku.manager.ui.screen.InstallerScreen
import moe.shizuku.manager.ui.screen.IntentsScreen
import moe.shizuku.manager.ui.screen.LabsToggleScreen
import moe.shizuku.manager.ui.screen.LogScreen
import moe.shizuku.manager.ui.screen.ManageScreen
import moe.shizuku.manager.ui.screen.PermissionsScreen
import moe.shizuku.manager.ui.screen.PowerScreen
import moe.shizuku.manager.ui.screen.ShellScreen
import moe.shizuku.manager.ui.screen.StealthScreen
import moe.shizuku.manager.ui.screen.TerminalScreen
import moe.shizuku.manager.ui.screen.WifiPasswordsScreen
import moe.shizuku.manager.debloater.DebloaterScreen
import moe.shizuku.manager.tuner.SystemUiTunerScreen
import moe.shizuku.manager.doze.DozeScreen
import moe.shizuku.manager.dns.PrivateDnsScreen

@Composable
fun DetailHost(
    detail: Detail,
    onBack: () -> Unit,
    onOpenDetail: (Detail) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                when (detail) {
                    Detail.APP_OPS -> ManageScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.SHELL -> ShellScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.HIDING -> HidingScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack,
                        onOpenDetail = onOpenDetail
                    )
                    Detail.FIREWALL,
                    Detail.AUTOSTART,
                    Detail.HIDE_DEVELOPER_OPTIONS,
                    Detail.HIDE_USB_DEBUGGING,
                    Detail.HIDE_WIRELESS_DEBUGGING,
                    Detail.HIDE_ACCESSIBILITY,
                    Detail.HIDE_PRIVATE_DNS,
                    Detail.HIDE_VPN -> LabsToggleScreen(
                        feature = requireNotNull(detail.feature),
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.DEVICE_INFO -> DeviceInfoScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.FORCE_DARK -> ForceDarkScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.ACTIVITIES -> ActivitiesScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.WIFI_PASSWORDS -> WifiPasswordsScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.FILES -> FilesScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.INSTALL -> InstallerScreen(
                        path = null,
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.DISPLAY -> DisplayScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.POWER -> PowerScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.LOG -> LogScreen(
                        bottomPadding = 0.dp,
                        onBack = onBack
                    )
                    Detail.MORE_APPS -> CatalogScreen(
                        kind = CatalogKind.APPS,
                        installedKeys = emptySet(),
                        onBack = onBack,
                        onInstalled = {}
                    )
                    Detail.STEALTH -> StealthScreen(onBack = onBack)
                    Detail.DEBLOATER -> DebloaterScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.SYSTEMUI_TUNER -> SystemUiTunerScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.ENFORCE_DOZE -> DozeScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.PRIVATE_DNS -> PrivateDnsScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.FREEZER -> moe.shizuku.manager.freeze.FreezerScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.AUDIO_MASTER -> moe.shizuku.manager.audio.AudioMasterScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.TETHER -> moe.shizuku.manager.tether.TetherScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.SECURITY -> moe.shizuku.manager.security.ScreenSecurityScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.AUTODND -> moe.shizuku.manager.dnd.AutoDndScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.OPTIMIZER -> moe.shizuku.manager.optimizer.OptimizerScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.EDGE_LIGHTING -> moe.shizuku.manager.essentials.EdgeLightingScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.CAFFEINATE -> moe.shizuku.manager.essentials.CaffeinateScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.MEDIA_KEYS -> moe.shizuku.manager.essentials.MediaKeyScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.ESSENTIALS -> moe.shizuku.manager.essentials.EssentialsScreen(bottomPadding = 0.dp, onBack = onBack, onOpenDetail = onOpenDetail)
                    Detail.ISLAND -> moe.shizuku.manager.essentials.IslandScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.BATTERY_CENTER -> moe.shizuku.manager.essentials.BatteryCenterScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.SMART_PIXELS -> moe.shizuku.manager.essentials.SmartPixelsScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.WATERMARK -> moe.shizuku.manager.essentials.WatermarkScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.POCKET_MODE -> moe.shizuku.manager.essentials.PocketModeScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.SHUT_UP -> moe.shizuku.manager.essentials.ShutUpScreen(bottomPadding = 0.dp, onBack = onBack)
                    Detail.TERMINAL -> TerminalScreen(onBack = onBack)
                    Detail.INTENTS -> IntentsScreen(onBack = onBack)
                    Detail.PERMISSIONS -> PermissionsScreen(onBack = onBack)
                    Detail.THEME_PATCHER -> moe.shizuku.manager.themepatcher.ThemePatcherScreen(onBack = onBack)
                    Detail.MIXED_AUDIO -> moe.shizuku.manager.audio.MixedAudioScreen(onBack = onBack)
                    Detail.SOUND_MASTER -> moe.shizuku.manager.audio.SoundMasterScreen(onBack = onBack)
                    Detail.LOOKBACK -> moe.shizuku.manager.lookback.LookBackScreen(onBack = onBack)
                    Detail.UNIVERSAL_PIP -> moe.shizuku.manager.pip.UniversalPiPScreen(onBack = onBack)
                    Detail.LOCAL_SHELL -> moe.shizuku.manager.shell.LocalShellScreen(onBack = onBack)
                    Detail.CALL_RECORDING -> moe.shizuku.manager.callrecorder.CallRecorderScreen(onBack = onBack)
                }
            }
        }
    }
}
