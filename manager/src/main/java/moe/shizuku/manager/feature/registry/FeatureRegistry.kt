package moe.shizuku.manager.feature.registry

import android.content.Context
import android.os.Build
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ui.Detail
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.ShizukuStateMachine

/**
 * The Labs catalogue's machine-readable source of truth.
 *
 * UI labels remain localized in resources, while this registry owns stable identifiers,
 * capability requirements, search metadata, and safety classification.
 */
object FeatureRegistry {

    val all: List<FeatureDefinition> = listOf(
        definition(Detail.APP_OPS, FeatureCategory.APP_MANAGEMENT, "App Ops", "application operations permissions appops"),
        definition(Detail.FIREWALL, FeatureCategory.APP_MANAGEMENT, "Firewall", "network access block internet"),
        definition(Detail.AUTOSTART, FeatureCategory.APP_MANAGEMENT, "Autostart", "startup background launch"),
        definition(Detail.HIDING, FeatureCategory.APP_MANAGEMENT, "App visibility", "hide packages application visibility"),
        definition(Detail.HIDE_DEVELOPER_OPTIONS, FeatureCategory.APP_MANAGEMENT, "Hide developer options", "hide developer settings"),
        definition(Detail.HIDE_USB_DEBUGGING, FeatureCategory.APP_MANAGEMENT, "Hide USB debugging", "hide adb usb debugging"),
        definition(Detail.HIDE_WIRELESS_DEBUGGING, FeatureCategory.APP_MANAGEMENT, "Hide wireless debugging", "hide adb wifi debugging"),
        definition(Detail.HIDE_ACCESSIBILITY, FeatureCategory.APP_MANAGEMENT, "Hide accessibility", "hide accessibility services"),
        definition(Detail.HIDE_PRIVATE_DNS, FeatureCategory.APP_MANAGEMENT, "Hide private DNS", "hide dns settings"),
        definition(Detail.HIDE_VPN, FeatureCategory.APP_MANAGEMENT, "Hide VPN", "hide vpn settings"),
        definition(Detail.FORCE_DARK, FeatureCategory.APP_MANAGEMENT, "Force dark mode", "dark theme applications"),
        definition(Detail.DEBLOATER, FeatureCategory.APP_MANAGEMENT, "Debloater", "remove uninstall packages"),
        definition(Detail.FREEZER, FeatureCategory.APP_MANAGEMENT, "App freezer", "freeze suspend applications"),
        definition(Detail.OPTIMIZER, FeatureCategory.APP_MANAGEMENT, "Performance profiles", "optimizer gaming dexopt"),
        definition(Detail.POWER, FeatureCategory.SYSTEM_CONTROLS, "Power controls", "reboot recovery bootloader shutdown"),
        definition(Detail.DISPLAY, FeatureCategory.SYSTEM_CONTROLS, "Display manager", "resolution density refresh rate"),
        definition(Detail.WIFI_PASSWORDS, FeatureCategory.SYSTEM_CONTROLS, "Wi-Fi passwords", "wireless networks credentials"),
        definition(Detail.SYSTEMUI_TUNER, FeatureCategory.SYSTEM_CONTROLS, "System UI tuner", "status bar immersive system ui"),
        definition(Detail.ENFORCE_DOZE, FeatureCategory.SYSTEM_CONTROLS, "Doze controller", "idle doze battery"),
        definition(Detail.PRIVATE_DNS, FeatureCategory.SYSTEM_CONTROLS, "Private DNS", "dns hostname resolver"),
        definition(Detail.AUDIO_MASTER, FeatureCategory.SYSTEM_CONTROLS, "Audio stream master", "volume media ring alarm notification"),
        definition(Detail.TETHER, FeatureCategory.SYSTEM_CONTROLS, "Tethering", "hotspot network sharing"),
        definition(Detail.MIXED_AUDIO, FeatureCategory.SYSTEM_CONTROLS, "Mixed audio", "audio focus routing"),
        definition(Detail.SOUND_MASTER, FeatureCategory.SYSTEM_CONTROLS, "Sound master", "per app volume mixer"),
        definition(Detail.UNIVERSAL_PIP, FeatureCategory.SYSTEM_CONTROLS, "Universal picture-in-picture", "pip floating window"),
        definition(Detail.SECURITY, FeatureCategory.SYSTEM_CONTROLS, "Screen security", "secure screenshot capture drm"),
        definition(Detail.AUTODND, FeatureCategory.SYSTEM_CONTROLS, "Automatic Do Not Disturb", "dnd zen schedule notifications"),
        definition(Detail.CAFFEINATE, FeatureCategory.SYSTEM_CONTROLS, "Caffeinate", "keep awake screen timeout"),
        definition(Detail.THEME_PATCHER, FeatureCategory.SYSTEM_CONTROLS, "Theme patcher", "theme customization"),
        definition(Detail.SHIZUWALL, FeatureCategory.SYSTEM_CONTROLS, "ShizuWall", "firewall tracker network"),
        definition(Detail.QUICK_TILES, FeatureCategory.SYSTEM_CONTROLS, "Quick Settings tools", "tiles shortcuts"),
        definition(Detail.EDGE_LIGHTING, FeatureCategory.SYSTEM_CONTROLS, "Edge lighting", "notification edge lighting", experimental = true),
        definition(Detail.MEDIA_KEYS, FeatureCategory.SYSTEM_CONTROLS, "Media keys", "volume key media controls", experimental = true),
        definition(Detail.ESSENTIALS, FeatureCategory.UTILITIES, "Essentials hub", "advanced system shortcuts", experimental = true),
        definition(Detail.ISLAND, FeatureCategory.UTILITIES, "Island providers", "notification island providers", experimental = true),
        definition(Detail.BATTERY_CENTER, FeatureCategory.SYSTEM_CONTROLS, "Battery center", "battery charging power"),
        definition(Detail.SMART_PIXELS, FeatureCategory.SYSTEM_CONTROLS, "Smart pixels", "oled pixel power saving", experimental = true),
        definition(Detail.WATERMARK, FeatureCategory.SYSTEM_CONTROLS, "Watermark", "screen watermark", experimental = true),
        definition(Detail.POCKET_MODE, FeatureCategory.SYSTEM_CONTROLS, "Pocket mode", "proximity accidental touch", experimental = true),
        definition(Detail.SHUT_UP, FeatureCategory.SYSTEM_CONTROLS, "Notification controls", "silence notifications", experimental = true),
        definition(Detail.DEVICE_INFO, FeatureCategory.UTILITIES, "Device information", "hardware battery thermal system"),
        definition(Detail.ACTIVITIES, FeatureCategory.UTILITIES, "Activity launcher", "activities components intents"),
        definition(Detail.LOG, FeatureCategory.UTILITIES, "Log viewer", "logcat diagnostics"),
        definition(Detail.FILES, FeatureCategory.UTILITIES, "Privileged file manager", "files storage permissions"),
        definition(Detail.INSTALL, FeatureCategory.UTILITIES, "Package installer", "apk split package install"),
        definition(Detail.SHELL, FeatureCategory.UTILITIES, "Shell", "terminal command line"),
        definition(Detail.TERMINAL, FeatureCategory.UTILITIES, "Terminal integrations", "terminal command"),
        definition(Detail.LOCAL_SHELL, FeatureCategory.UTILITIES, "Local shell", "adb shell local"),
        definition(Detail.INTENTS, FeatureCategory.UTILITIES, "Intent shell", "intent broadcast activity"),
        definition(Detail.PERMISSIONS, FeatureCategory.UTILITIES, "Permission manager", "runtime permissions access"),
        definition(Detail.STEALTH, FeatureCategory.UTILITIES, "Stealth controls", "visibility detection"),
        definition(Detail.CALL_RECORDING, FeatureCategory.UTILITIES, "Call recording", "phone audio recording"),
        definition(Detail.LOOKBACK, FeatureCategory.UTILITIES, "Screen recording", "screen capture playback"),
        definition(Detail.DFROOT, FeatureCategory.SYSTEM_CONTROLS, "DFRoot", "root kernel exploit", experimental = true),
        definition(Detail.WALLYOU, FeatureCategory.UTILITIES, "WallYou", "wallpaper backgrounds"),
        definition(Detail.WALLYOU_SETTINGS, FeatureCategory.UTILITIES, "WallYou settings", "wallpaper scheduling"),
        definition(Detail.MORE_APPS, FeatureCategory.COMPANION_APPS, "Shizuku Store", "catalog applications")
    )

    private val byDetail = all.associateBy { it.detail }

    fun get(detail: Detail): FeatureDefinition? = byDetail[detail]

    fun require(detail: Detail): FeatureDefinition =
        requireNotNull(get(detail)) { "No feature definition registered for ${detail.name}" }

    suspend fun capability(context: Context, detail: Detail): FeatureCapability =
        withContext(Dispatchers.IO) {
            val definition = require(detail)
            if (Build.VERSION.SDK_INT < definition.minApi) {
                return@withContext FeatureCapability(
                    definition,
                    FeatureAvailability.UNSUPPORTED_ANDROID_VERSION,
                    emptySet()
                )
            }

            val available = buildSet {
                add(FeatureProvider.ANDROID_API)
                if (ShizukuStateMachine.isRunning()) add(FeatureProvider.SHIZUKU)
                if (EnvironmentUtils.getActiveAdbPort() > 0) add(FeatureProvider.ADB)
                if (runCatching { Shell.getShell().isRoot }.getOrDefault(false)) {
                    add(FeatureProvider.ROOT)
                }
            }
            val providerAvailable = definition.requiredProviders.isEmpty() ||
                definition.requiredProviders.any { it in available }
            val missing = if (providerAvailable) null else definition.requiredProviders.firstOrNull()
            val availability = when (missing) {
                FeatureProvider.SHIZUKU -> FeatureAvailability.REQUIRES_SHIZUKU
                FeatureProvider.ROOT -> FeatureAvailability.REQUIRES_ROOT
                FeatureProvider.ADB -> FeatureAvailability.REQUIRES_ADB
                null -> if (definition.experimental) FeatureAvailability.EXPERIMENTAL
                else FeatureAvailability.AVAILABLE
                else -> FeatureAvailability.UNSUPPORTED_DEVICE
            }
            FeatureCapability(definition, availability, available)
        }

    private fun definition(
        detail: Detail,
        category: FeatureCategory,
        title: String,
        keywords: String,
        minApi: Int = 26,
        experimental: Boolean = false
    ): FeatureDefinition {
        val requiresShell = detail in setOf(
            Detail.APP_OPS, Detail.FIREWALL, Detail.AUTOSTART, Detail.HIDING,
            Detail.FORCE_DARK, Detail.DEBLOATER, Detail.FREEZER, Detail.OPTIMIZER,
            Detail.POWER, Detail.DISPLAY, Detail.WIFI_PASSWORDS, Detail.SYSTEMUI_TUNER,
            Detail.ENFORCE_DOZE, Detail.PRIVATE_DNS, Detail.TETHER, Detail.MIXED_AUDIO,
            Detail.SOUND_MASTER, Detail.UNIVERSAL_PIP, Detail.SECURITY, Detail.THEME_PATCHER,
            Detail.SHIZUWALL, Detail.CALL_RECORDING, Detail.DFROOT, Detail.SHELL,
            Detail.LOCAL_SHELL, Detail.FILES, Detail.INSTALL
        )
        return FeatureDefinition(
            id = detail.name.lowercase(),
            title = title,
            detail = detail,
            category = category,
            description = keywords,
            searchKeywords = keywords.split(' ').filter(String::isNotBlank).toSet(),
            minApi = minApi,
            requiredProviders = if (requiresShell) {
                setOf(FeatureProvider.SHIZUKU, FeatureProvider.ROOT)
            } else {
                setOf(FeatureProvider.ANDROID_API)
            },
            experimental = experimental
        )
    }
}
