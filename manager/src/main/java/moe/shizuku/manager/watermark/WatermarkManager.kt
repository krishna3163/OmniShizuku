package moe.shizuku.manager.watermark

import android.content.Context
import android.os.Build

enum class WatermarkStyle(val label: String, val brand: String) {
    LEICA_FRAME("Leica White Frame", "LEICA"),
    HASSELBLAD_MINIMAL("Hasselblad Minimal", "HASSELBLAD"),
    CORNER_BADGE("Corner Badge", "OMNI")
}

enum class FrameTheme(val label: String) {
    LIGHT("Classic White"),
    DARK("Obsidian Black")
}

data class WatermarkConfig(
    val enabled: Boolean = true,
    val style: WatermarkStyle = WatermarkStyle.LEICA_FRAME,
    val theme: FrameTheme = FrameTheme.LIGHT,
    val deviceName: String = Build.MODEL,
    val customArtistText: String = "Shot on OmniShizuku",
    val showExif: Boolean = true,
    val showFocalLength: Boolean = true,
    val showAperture: Boolean = true,
    val showIso: Boolean = true,
    val showShutterSpeed: Boolean = true,
    val showTimestamp: Boolean = true
)

object WatermarkManager {

    private const val PREFS_NAME = "omnishizuku_watermark"
    private const val KEY_ENABLED = "watermark_enabled"
    private const val KEY_STYLE = "watermark_style"
    private const val KEY_THEME = "watermark_theme"
    private const val KEY_DEVICE = "watermark_device"
    private const val KEY_ARTIST = "watermark_artist"
    private const val KEY_SHOW_EXIF = "watermark_show_exif"
    private const val KEY_SHOW_FOCAL = "watermark_show_focal"
    private const val KEY_SHOW_APERTURE = "watermark_show_aperture"
    private const val KEY_SHOW_ISO = "watermark_show_iso"
    private const val KEY_SHOW_SHUTTER = "watermark_show_shutter"
    private const val KEY_SHOW_TIMESTAMP = "watermark_show_timestamp"

    fun getConfig(context: Context): WatermarkConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val styleName = prefs.getString(KEY_STYLE, WatermarkStyle.LEICA_FRAME.name)
        val themeName = prefs.getString(KEY_THEME, FrameTheme.LIGHT.name)

        val style = try {
            WatermarkStyle.valueOf(styleName ?: WatermarkStyle.LEICA_FRAME.name)
        } catch (_: Exception) {
            WatermarkStyle.LEICA_FRAME
        }

        val theme = try {
            FrameTheme.valueOf(themeName ?: FrameTheme.LIGHT.name)
        } catch (_: Exception) {
            FrameTheme.LIGHT
        }

        return WatermarkConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            style = style,
            theme = theme,
            deviceName = prefs.getString(KEY_DEVICE, Build.MODEL) ?: Build.MODEL,
            customArtistText = prefs.getString(KEY_ARTIST, "Shot on OmniShizuku") ?: "Shot on OmniShizuku",
            showExif = prefs.getBoolean(KEY_SHOW_EXIF, true),
            showFocalLength = prefs.getBoolean(KEY_SHOW_FOCAL, true),
            showAperture = prefs.getBoolean(KEY_SHOW_APERTURE, true),
            showIso = prefs.getBoolean(KEY_SHOW_ISO, true),
            showShutterSpeed = prefs.getBoolean(KEY_SHOW_SHUTTER, true),
            showTimestamp = prefs.getBoolean(KEY_SHOW_TIMESTAMP, true)
        )
    }

    fun saveConfig(context: Context, config: WatermarkConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putString(KEY_STYLE, config.style.name)
            .putString(KEY_THEME, config.theme.name)
            .putString(KEY_DEVICE, config.deviceName)
            .putString(KEY_ARTIST, config.customArtistText)
            .putBoolean(KEY_SHOW_EXIF, config.showExif)
            .putBoolean(KEY_SHOW_FOCAL, config.showFocalLength)
            .putBoolean(KEY_SHOW_APERTURE, config.showAperture)
            .putBoolean(KEY_SHOW_ISO, config.showIso)
            .putBoolean(KEY_SHOW_SHUTTER, config.showShutterSpeed)
            .putBoolean(KEY_SHOW_TIMESTAMP, config.showTimestamp)
            .apply()
    }
}
