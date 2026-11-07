package moe.shizuku.manager.manage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.dnd.AutoDndManager
import moe.shizuku.manager.dns.PrivateDnsManager
import moe.shizuku.manager.doze.DozeManager
import moe.shizuku.manager.doze.EnforceDozeManager
import moe.shizuku.manager.essentials.CaffeinateController
import moe.shizuku.manager.freeze.FreezerManager
import moe.shizuku.manager.optimizer.OptimizerManager
import moe.shizuku.manager.optimizer.PerformanceProfile
import moe.shizuku.manager.quicktiles.QuickTileSettingsManager
import moe.shizuku.manager.security.ScreenSecurityManager
import moe.shizuku.manager.shizuwall.ShizuWallManager
import moe.shizuku.manager.ui.Detail
import moe.shizuku.manager.ui.screen.AppToggleFeature

/**
 * Unified lightweight active-state and status summary registry for Labs features.
 * All IO checks run on Dispatchers.IO with defensive fallbacks.
 */
object ActiveFeaturesRegistry {

    suspend fun getActiveDetails(context: Context): Set<Detail> = withContext(Dispatchers.IO) {
        val activeSet = mutableSetOf<Detail>()

        // 1. AppToggleFeatures: Firewall, Autostart, Hiding
        AppToggleFeature.entries.forEach { feature ->
            val detail = if (feature.signal != null) Detail.HIDING else Detail.of(feature) ?: return@forEach
            val count = when {
                feature.signal != null ->
                    if (Hiding.isSignalEnabled(feature.signal)) Hiding.appsFor(feature.signal).size else 0
                feature == AppToggleFeature.AUTOSTART ->
                    PackageTools.readOpDeniedInstalled("RUN_IN_BACKGROUND").size
                else ->
                    PackageTools.readFirewallBlocked(context).size
            }
            if (count > 0) activeSet.add(detail)
        }

        // 2. Force Dark
        if (ForceDark.hasWorkToDo()) {
            activeSet.add(Detail.FORCE_DARK)
        }

        // 3. Enforce Doze & Deep Sleep
        if (EnforceDozeManager.isServiceEnabled(context) || DozeManager.isAggressiveDozeEnabled(context)) {
            activeSet.add(Detail.ENFORCE_DOZE)
        }

        // 4. Private DNS (Custom hostname or automatic active)
        runCatching {
            val mode = PrivateDnsManager.getCurrentMode()
            if (mode == PrivateDnsManager.DnsMode.CUSTOM || mode == PrivateDnsManager.DnsMode.AUTOMATIC) {
                activeSet.add(Detail.PRIVATE_DNS)
            }
        }

        // 5. Caffeinate
        CaffeinateController.check(context)
        if (CaffeinateController.isActive.value) {
            activeSet.add(Detail.CAFFEINATE)
        }

        // 6. Auto DND (Zen Mode)
        if (AutoDndManager.isEnabled(context)) {
            activeSet.add(Detail.AUTODND)
        }

        // 7. Axeron Gaming Optimizer
        if (OptimizerManager.getActiveProfile(context) != PerformanceProfile.BALANCED) {
            activeSet.add(Detail.OPTIMIZER)
        }

        // 8. Screen Security & DRM
        runCatching {
            if (ScreenSecurityManager.isSecureWindowsDisabled()) {
                activeSet.add(Detail.SECURITY)
            }
        }

        // 9. App Freezer
        if (FreezerManager.isAutoFreezeEnabled(context)) {
            activeSet.add(Detail.FREEZER)
        }

        // 10. ShizuWall Firewall
        if (ShizuWallManager.isMasterEnabled(context)) {
            activeSet.add(Detail.SHIZUWALL)
        }

        // 11. Quick Settings Tiles (Auto revert active or debugging enabled)
        if (QuickTileSettingsManager.isUsbAutoRevert(context) || QuickTileSettingsManager.isDnsAutoRevert(context)) {
            activeSet.add(Detail.QUICK_TILES)
        }

        // 12. Universal PiP
        val pipPrefs = context.getSharedPreferences("universal_pip", Context.MODE_PRIVATE)
        if (pipPrefs.getBoolean("persistent_noti", false)) {
            activeSet.add(Detail.UNIVERSAL_PIP)
        }

        activeSet
    }

    suspend fun getFeatureSummary(context: Context, detail: Detail): String? = withContext(Dispatchers.IO) {
        when (detail) {
            Detail.FIREWALL -> {
                val blocked = PackageTools.readFirewallBlocked(context).size
                if (blocked > 0) "$blocked apps blocked" else null
            }
            Detail.SHIZUWALL -> {
                if (ShizuWallManager.isMasterEnabled(context)) {
                    val count = PackageTools.readFirewallBlocked(context).size
                    "$count blocked • Master active"
                } else null
            }
            Detail.AUTOSTART -> {
                val count = PackageTools.readOpDeniedInstalled("RUN_IN_BACKGROUND").size
                if (count > 0) "$count restricted" else null
            }
            Detail.HIDING -> {
                val anyApps = Hiding.hasAnyApp()
                if (anyApps) "Hiding enabled" else null
            }
            Detail.FORCE_DARK -> {
                val count = ForceDark.apps().size
                if (count > 0) "$count apps" else null
            }
            Detail.ENFORCE_DOZE -> {
                if (EnforceDozeManager.isServiceEnabled(context)) "Enforced active"
                else if (DozeManager.isAggressiveDozeEnabled(context)) "Aggressive active"
                else null
            }
            Detail.PRIVATE_DNS -> {
                val mode = PrivateDnsManager.getCurrentMode()
                val host = PrivateDnsManager.getCurrentHostname()
                if (mode == PrivateDnsManager.DnsMode.CUSTOM && !host.isNullOrBlank()) host
                else if (mode == PrivateDnsManager.DnsMode.AUTOMATIC) "Automatic"
                else null
            }
            Detail.CAFFEINATE -> {
                if (CaffeinateController.isActive.value) "Screen awake active" else null
            }
            Detail.AUTODND -> {
                if (AutoDndManager.isEnabled(context)) "Auto DND active" else null
            }
            Detail.OPTIMIZER -> {
                val profile = OptimizerManager.getActiveProfile(context)
                if (profile != PerformanceProfile.BALANCED) profile.title else null
            }
            Detail.SECURITY -> {
                if (ScreenSecurityManager.isSecureWindowsDisabled()) "Capture allowed" else null
            }
            Detail.QUICK_TILES -> {
                val usb = QuickTileSettingsManager.isUsbDebuggingEnabled(context)
                if (usb) "USB Debugging On" else null
            }
            else -> null
        }
    }
}
