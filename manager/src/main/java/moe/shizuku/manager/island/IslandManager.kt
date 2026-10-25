package moe.shizuku.manager.island

import android.content.Context

enum class CutoutPosition(val id: Int, val label: String) {
    CENTER(0, "Center Punch-Hole"),
    LEFT(1, "Left Cutout"),
    RIGHT(2, "Right Cutout")
}

data class IslandConfig(
    val enabled: Boolean,
    val position: CutoutPosition,
    val pillWidthDp: Int,
    val pillHeightDp: Int,
    val topMarginDp: Int,
    val showMedia: Boolean,
    val showBattery: Boolean,
    val showNotifications: Boolean
)

object IslandManager {

    private const val PREFS_NAME = "omnishizuku_island"
    private const val KEY_ENABLED = "island_enabled"
    private const val KEY_POSITION = "island_position"
    private const val KEY_WIDTH = "island_width"
    private const val KEY_HEIGHT = "island_height"
    private const val KEY_TOP_MARGIN = "island_top_margin"
    private const val KEY_SHOW_MEDIA = "island_show_media"
    private const val KEY_SHOW_BATTERY = "island_show_battery"
    private const val KEY_SHOW_NOTIFICATIONS = "island_show_notifications"

    fun getConfig(context: Context): IslandConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val posId = prefs.getInt(KEY_POSITION, 0)
        val pos = CutoutPosition.entries.firstOrNull { it.id == posId } ?: CutoutPosition.CENTER

        return IslandConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            position = pos,
            pillWidthDp = prefs.getInt(KEY_WIDTH, 140),
            pillHeightDp = prefs.getInt(KEY_HEIGHT, 36),
            topMarginDp = prefs.getInt(KEY_TOP_MARGIN, 12),
            showMedia = prefs.getBoolean(KEY_SHOW_MEDIA, true),
            showBattery = prefs.getBoolean(KEY_SHOW_BATTERY, true),
            showNotifications = prefs.getBoolean(KEY_SHOW_NOTIFICATIONS, true)
        )
    }

    fun setConfig(context: Context, config: IslandConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putInt(KEY_POSITION, config.position.id)
            .putInt(KEY_WIDTH, config.pillWidthDp)
            .putInt(KEY_HEIGHT, config.pillHeightDp)
            .putInt(KEY_TOP_MARGIN, config.topMarginDp)
            .putBoolean(KEY_SHOW_MEDIA, config.showMedia)
            .putBoolean(KEY_SHOW_BATTERY, config.showBattery)
            .putBoolean(KEY_SHOW_NOTIFICATIONS, config.showNotifications)
            .apply()
    }
}
