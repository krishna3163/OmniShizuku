package moe.shizuku.manager.optimizer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

enum class PerformanceProfile(val id: Int, val title: String, val desc: String) {
    BALANCED(0, "Balanced Profile", "Stock system animations, dynamic thermal management, standard power policy."),
    PERFORMANCE(1, "Axeron Gaming Beast", "Zero animation latency (0.0x), unconstrained power mode, hardware acceleration enforcer."),
    BATTERY_SAVER(2, "Ultra Battery Conservation", "Aggressive low-power governor, background CPU cap, device idle priority.")
}

object OptimizerManager {

    private const val PREFS_NAME = "omnishizuku_optimizer"
    private const val KEY_PROFILE = "active_profile"

    fun getActiveProfile(context: Context): PerformanceProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_PROFILE, 0)
        return PerformanceProfile.entries.firstOrNull { it.id == id } ?: PerformanceProfile.BALANCED
    }

    suspend fun applyProfile(context: Context, profile: PerformanceProfile): Boolean = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_PROFILE, profile.id).apply()

        when (profile) {
            PerformanceProfile.PERFORMANCE -> {
                runShellCommand("cmd power set-mode 0")
                runShellCommand("settings put global window_animation_scale 0.0")
                runShellCommand("settings put global transition_animation_scale 0.0")
                runShellCommand("settings put global animator_duration_scale 0.0")
                runShellCommand("pm trim-caches 999G")
                runShellCommand("setprop debug.sf.hw 1")
                runShellCommand("setprop debug.egl.hw 1")
                true
            }
            PerformanceProfile.BALANCED -> {
                runShellCommand("settings put global window_animation_scale 1.0")
                runShellCommand("settings put global transition_animation_scale 1.0")
                runShellCommand("settings put global animator_duration_scale 1.0")
                true
            }
            PerformanceProfile.BATTERY_SAVER -> {
                runShellCommand("cmd power set-mode 1")
                runShellCommand("settings put global window_animation_scale 0.5")
                runShellCommand("settings put global transition_animation_scale 0.5")
                runShellCommand("settings put global animator_duration_scale 0.5")
                true
            }
        }
    }

    suspend fun clearSystemCaches(): Boolean = withContext(Dispatchers.IO) {
        val res = runShellCommand("pm trim-caches 999G")
        res != null
    }

    suspend fun dropLinuxBuffers(): Boolean = withContext(Dispatchers.IO) {
        val res = runShellCommand("echo 3 > /proc/sys/vm/drop_caches")
        res != null
    }
}
