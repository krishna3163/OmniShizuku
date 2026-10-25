package moe.shizuku.manager.display.smartpixels

import android.content.Context

enum class SmartPixelsPattern(val percent: Int, val label: String, val powerSaving: String) {
    DISABLED(0, "Off", "0%"),
    LIGHT(25, "Light (25%)", "~15% OLED power saved"),
    MEDIUM(50, "Balanced (50%)", "~28% OLED power saved"),
    AGGRESSIVE(75, "Extreme (75%)", "~44% OLED power saved")
}

data class SmartPixelsConfig(
    val enabled: Boolean,
    val pattern: SmartPixelsPattern,
    val burnInShiftMinutes: Int,
    val autoOnLowBattery: Boolean
)

object SmartPixelsManager {

    private const val PREFS_NAME = "omnishizuku_smart_pixels"
    private const val KEY_ENABLED = "smart_pixels_enabled"
    private const val KEY_PATTERN = "smart_pixels_pattern"
    private const val KEY_SHIFT_MINUTES = "smart_pixels_shift"
    private const val KEY_AUTO_LOW_BATTERY = "smart_pixels_auto_low"

    fun getConfig(context: Context): SmartPixelsConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val patternName = prefs.getString(KEY_PATTERN, SmartPixelsPattern.MEDIUM.name)
        val pattern = try {
            SmartPixelsPattern.valueOf(patternName ?: SmartPixelsPattern.MEDIUM.name)
        } catch (_: Exception) {
            SmartPixelsPattern.MEDIUM
        }

        return SmartPixelsConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            pattern = pattern,
            burnInShiftMinutes = prefs.getInt(KEY_SHIFT_MINUTES, 30),
            autoOnLowBattery = prefs.getBoolean(KEY_AUTO_LOW_BATTERY, false)
        )
    }

    fun saveConfig(context: Context, config: SmartPixelsConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putString(KEY_PATTERN, config.pattern.name)
            .putInt(KEY_SHIFT_MINUTES, config.burnInShiftMinutes)
            .putBoolean(KEY_AUTO_LOW_BATTERY, config.autoOnLowBattery)
            .apply()
    }
}
