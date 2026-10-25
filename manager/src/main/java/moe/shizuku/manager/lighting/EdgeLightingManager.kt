package moe.shizuku.manager.lighting

import android.content.Context
import androidx.compose.ui.graphics.Color

data class EdgeLightingConfig(
    val enabled: Boolean,
    val colorHex: Long,
    val durationSeconds: Int,
    val wakeScreen: Boolean
)

object EdgeLightingManager {

    private const val PREFS_NAME = "omnishizuku_edge_lighting"
    private const val KEY_ENABLED = "lighting_enabled"
    private const val KEY_COLOR = "lighting_color"
    private const val KEY_DURATION = "lighting_duration"
    private const val KEY_WAKE = "lighting_wake_screen"

    val PRESET_COLORS = listOf(
        0xFF00E5FF to "Cyber Cyan",
        0xFFD500F9 to "Neon Violet",
        0xFFFFD600 to "Amber Gold",
        0xFF00E676 to "Emerald Green",
        0xFFFF1744 to "Electric Crimson"
    )

    fun getConfig(context: Context): EdgeLightingConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return EdgeLightingConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            colorHex = prefs.getLong(KEY_COLOR, 0xFF00E5FF),
            durationSeconds = prefs.getInt(KEY_DURATION, 4),
            wakeScreen = prefs.getBoolean(KEY_WAKE, true)
        )
    }

    fun setConfig(context: Context, config: EdgeLightingConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putLong(KEY_COLOR, config.colorHex)
            .putInt(KEY_DURATION, config.durationSeconds)
            .putBoolean(KEY_WAKE, config.wakeScreen)
            .apply()
    }
}
