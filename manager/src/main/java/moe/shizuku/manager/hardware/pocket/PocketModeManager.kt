package moe.shizuku.manager.hardware.pocket

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

data class PocketModeConfig(
    val enabled: Boolean,
    val lockScreenOnDetection: Boolean,
    val vibrateOnTrigger: Boolean,
    val useAmbientLightSensor: Boolean,
    val triggerDelaySeconds: Int
)

object PocketModeManager {

    private const val PREFS_NAME = "omnishizuku_pocket_mode"
    private const val KEY_ENABLED = "pocket_mode_enabled"
    private const val KEY_LOCK_SCREEN = "pocket_mode_lock_screen"
    private const val KEY_VIBRATE = "pocket_mode_vibrate"
    private const val KEY_LIGHT_SENSOR = "pocket_mode_light_sensor"
    private const val KEY_DELAY = "pocket_mode_delay"

    fun getConfig(context: Context): PocketModeConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PocketModeConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            lockScreenOnDetection = prefs.getBoolean(KEY_LOCK_SCREEN, true),
            vibrateOnTrigger = prefs.getBoolean(KEY_VIBRATE, true),
            useAmbientLightSensor = prefs.getBoolean(KEY_LIGHT_SENSOR, true),
            triggerDelaySeconds = prefs.getInt(KEY_DELAY, 3)
        )
    }

    fun saveConfig(context: Context, config: PocketModeConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putBoolean(KEY_LOCK_SCREEN, config.lockScreenOnDetection)
            .putBoolean(KEY_VIBRATE, config.vibrateOnTrigger)
            .putBoolean(KEY_LIGHT_SENSOR, config.useAmbientLightSensor)
            .putInt(KEY_DELAY, config.triggerDelaySeconds)
            .apply()
    }

    suspend fun lockScreenPrivileged(): Boolean = withContext(Dispatchers.IO) {
        // Shizuku power sleep keyevent
        val out = runShellCommand("input keyevent 26")
        out != null
    }
}
