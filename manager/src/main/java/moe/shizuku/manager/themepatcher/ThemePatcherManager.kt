package moe.shizuku.manager.themepatcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand
import rikka.shizuku.Shizuku

data class SystemThemeOverlay(
    val packageName: String,
    val category: String, // "Colors", "Icon Shapes", "Icon Packs", "Fonts", "Other"
    val displayName: String,
    val isEnabled: Boolean
)

data class OemThemeTrialState(
    val isStoreInstalled: Boolean,
    val storePackage: String?,
    val trialThemeSystem: String?,
    val trialThemeSecure: String?,
    val trialFontSystem: String?,
    val trialFontSecure: String?,
    val oplusThemeUuid: String?,
    val isTrialActive: Boolean
)

object ThemePatcherManager {

    fun isShizukuAvailable(): Boolean = Shizuku.pingBinder()

    suspend fun getSystemOverlays(): List<SystemThemeOverlay> = withContext(Dispatchers.IO) {
        val out = runShellCommand("cmd overlay list") ?: return@withContext emptyList()
        val list = mutableListOf<SystemThemeOverlay>()

        for (line in out.lines()) {
            val trimmed = line.trim()
            if (!trimmed.startsWith("[ ]") && !trimmed.startsWith("[x]")) continue

            val isEnabled = trimmed.startsWith("[x]")
            val pkg = trimmed.substring(3).trim()
            if (pkg.isEmpty()) continue

            // Filter for theme-relevant overlays
            if (!pkg.contains("theme", ignoreCase = true) && !pkg.contains("icon", ignoreCase = true) && !pkg.contains("font", ignoreCase = true)) {
                continue
            }

            val category = when {
                pkg.contains(".color.") -> "Colors"
                pkg.contains(".icon.") -> "Icon Shapes"
                pkg.contains(".icon_pack.") -> "Icon Packs"
                pkg.contains(".font.") -> "Fonts"
                else -> "Other"
            }

            val rawName = pkg.substringAfterLast('.')
            val displayName = formatName(rawName, pkg)

            list.add(SystemThemeOverlay(pkg, category, displayName, isEnabled))
        }

        list.sortedWith(compareBy({ it.category }, { it.displayName }))
    }

    private fun formatName(raw: String, pkg: String): String {
        return when (raw.lowercase()) {
            "pebble" -> "Pebble"
            "vessel" -> "Vessel"
            "taperedrect" -> "Tapered Rectangle"
            "teardrop" -> "Teardrop"
            "squircle" -> "Squircle"
            "roundedrect" -> "Rounded Rectangle"
            "cinnamon" -> "Cinnamon"
            "black" -> "Pitch Black"
            "green" -> "Forest Green"
            "ocean" -> "Ocean Blue"
            "space" -> "Deep Space"
            "orchid" -> "Orchid Violet"
            "purple" -> "Royal Purple"
            "notoserifsource" -> "Noto Serif"
            else -> {
                if (pkg.contains("icon_pack")) {
                    val style = when {
                        pkg.contains("rounded") -> "Rounded"
                        pkg.contains("filled") -> "Filled"
                        pkg.contains("circular") -> "Circular"
                        else -> "Custom"
                    }
                    val target = when {
                        pkg.contains("systemui") -> "System UI"
                        pkg.contains("settings") -> "Settings"
                        pkg.contains("launcher") -> "Launcher"
                        pkg.contains("android") -> "Framework"
                        else -> raw.replaceFirstChar { it.uppercase() }
                    }
                    "$style ($target)"
                } else {
                    raw.replaceFirstChar { it.uppercase() }
                }
            }
        }
    }

    suspend fun enableOverlay(overlay: SystemThemeOverlay, allOverlays: List<SystemThemeOverlay>): Boolean = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) return@withContext false

        // For exclusive categories like Colors, Icon Shapes, Fonts, disable other active ones in same category first
        if (overlay.category in listOf("Colors", "Icon Shapes", "Fonts")) {
            val siblings = allOverlays.filter { it.category == overlay.category && it.packageName != overlay.packageName && it.isEnabled }
            for (sib in siblings) {
                runShellCommand("cmd overlay disable ${sib.packageName}")
            }
        }

        val res = runShellCommand("cmd overlay enable ${overlay.packageName}")
        res != null
    }

    suspend fun disableOverlay(overlay: SystemThemeOverlay): Boolean = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) return@withContext false
        val res = runShellCommand("cmd overlay disable ${overlay.packageName}")
        res != null
    }

    suspend fun disableAllCategory(category: String, allOverlays: List<SystemThemeOverlay>): Boolean = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) return@withContext false
        val targets = allOverlays.filter { it.category == category && it.isEnabled }
        for (t in targets) {
            runShellCommand("cmd overlay disable ${t.packageName}")
        }
        true
    }

    // OEM Theme Store Patching (Oppo, OnePlus, Realme)
    private val zeroKeys = listOf(
        "persist.sys.trial.theme",
        "persist.sys.trial_theme",
        "persist.sys.trial.font",
        "persist.sys.trial.live_wp"
    )
    private val uuidKeys = listOf(
        "persist.sys.oplus.theme_uuid",
        "persist.sys.oppo.theme.uuid",
        "persist.sys.oppo.theme_uuid"
    )

    suspend fun getOemTrialState(context: Context): OemThemeTrialState = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val storeApp = installed.firstOrNull { app ->
            val pkg = app.packageName.lowercase()
            pkg.contains("themestore") || pkg.contains("heytap.themestore") || pkg.contains("nearme.themestore") || (pkg.contains("theme") && pkg.contains("store"))
        }

        val trialThemeSys = runShellCommand("settings get system persist.sys.trial.theme")?.trim()
        val trialThemeSec = runShellCommand("settings get secure persist.sys.trial.theme")?.trim()
        val trialFontSys = runShellCommand("settings get system persist.sys.trial.font")?.trim()
        val trialFontSec = runShellCommand("settings get secure persist.sys.trial.font")?.trim()
        val oplusUuid = runShellCommand("settings get system persist.sys.oplus.theme_uuid")?.trim()

        val isTrial = (trialThemeSys != null && trialThemeSys != "0" && trialThemeSys != "null") ||
                      (trialFontSys != null && trialFontSys != "0" && trialFontSys != "null") ||
                      (trialThemeSec != null && trialThemeSec != "0" && trialThemeSec != "null")

        OemThemeTrialState(
            isStoreInstalled = storeApp != null,
            storePackage = storeApp?.packageName,
            trialThemeSystem = if (trialThemeSys == "null" || trialThemeSys.isNullOrBlank()) "0 (Normal)" else trialThemeSys,
            trialThemeSecure = if (trialThemeSec == "null" || trialThemeSec.isNullOrBlank()) "0 (Normal)" else trialThemeSec,
            trialFontSystem = if (trialFontSys == "null" || trialFontSys.isNullOrBlank()) "0 (Normal)" else trialFontSys,
            trialFontSecure = if (trialFontSec == "null" || trialFontSec.isNullOrBlank()) "0 (Normal)" else trialFontSec,
            oplusThemeUuid = if (oplusUuid == "null" || oplusUuid.isNullOrBlank()) "None" else oplusUuid,
            isTrialActive = isTrial
        )
    }

    suspend fun applyOemTrialFreeze(context: Context): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) return@withContext Pair(false, "Shizuku is not running")

        val tables = listOf("system", "secure")
        for (table in tables) {
            for (key in zeroKeys) {
                runShellCommand("settings put $table $key 0")
            }
            for (key in uuidKeys) {
                runShellCommand("settings put $table $key -1")
            }
        }

        // Force stop theme stores to lock in changes
        val state = getOemTrialState(context)
        state.storePackage?.let {
            runShellCommand("am force-stop $it")
        }
        runShellCommand("am force-stop com.heytap.themestore")
        runShellCommand("am force-stop com.nearme.themestore")

        Pair(true, "Theme Store trial timer frozen successfully. Themes/fonts set to permanent.")
    }

    suspend fun resetOemTrialDefaults(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) return@withContext Pair(false, "Shizuku is not running")

        val tables = listOf("system", "secure")
        for (table in tables) {
            for (key in zeroKeys) {
                runShellCommand("settings delete $table $key")
            }
            for (key in uuidKeys) {
                runShellCommand("settings delete $table $key")
            }
        }

        Pair(true, "Trial settings reset to default values.")
    }

    fun launchThemeStore(context: Context): Boolean {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val storeApp = installed.firstOrNull { app ->
            val pkg = app.packageName.lowercase()
            pkg.contains("themestore") || pkg.contains("heytap.themestore") || pkg.contains("nearme.themestore") || (pkg.contains("theme") && pkg.contains("store"))
        }

        val targetPkg = storeApp?.packageName ?: return false
        val intent = pm.getLaunchIntentForPackage(targetPkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        // Start background watcher service
        try {
            val serviceIntent = Intent(context, ThemePatcherService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Exception) {}

        return true
    }
}

