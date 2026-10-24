package moe.shizuku.manager.accessibility

import moe.shizuku.manager.ShizukuSettings

/**
 * Persistence for the accessibility keep-alive (pinned) service list.
 *
 * Stores colon-separated accessibility service IDs (e.g. "pkg/cls:pkg2/cls2:")
 * in the shared preferences, mirroring the format Android uses for
 * [android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES].
 */
object AccessibilityKeepAliveStore {

    private const val KEY_KEEP_ALIVE_LIST = "accessibility_keep_alive_list"
    private const val KEY_KEEP_ALIVE_ENABLED = "accessibility_keep_alive_enabled"
    private const val KEY_AUTO_BOOT = "accessibility_keep_alive_auto_boot"

    /** Master switch: whether the keep-alive daemon should run at all. */
    fun isKeepAliveEnabled(): Boolean {
        return ShizukuSettings.getPreferences().getBoolean(KEY_KEEP_ALIVE_ENABLED, false)
    }

    fun setKeepAliveEnabled(enabled: Boolean) {
        ShizukuSettings.getPreferences().edit().putBoolean(KEY_KEEP_ALIVE_ENABLED, enabled).apply()
    }

    /** Whether to auto-start the daemon on device boot. */
    fun isAutoBootEnabled(): Boolean {
        return ShizukuSettings.getPreferences().getBoolean(KEY_AUTO_BOOT, true)
    }

    fun setAutoBootEnabled(enabled: Boolean) {
        ShizukuSettings.getPreferences().edit().putBoolean(KEY_AUTO_BOOT, enabled).apply()
    }

    /** Set of currently pinned (keep-alive) service IDs. */
    fun getKeepAliveIds(): Set<String> {
        val raw = ShizukuSettings.getPreferences().getString(KEY_KEEP_ALIVE_LIST, "") ?: ""
        return parseIds(raw)
    }

    fun isPinned(serviceId: String): Boolean = getKeepAliveIds().contains(serviceId)

    fun addPinned(serviceId: String): Set<String> {
        val ids = getKeepAliveIds().toMutableSet()
        ids.add(serviceId)
        writeIds(ids)
        return ids
    }

    fun removePinned(serviceId: String): Set<String> {
        val ids = getKeepAliveIds().toMutableSet()
        ids.remove(serviceId)
        writeIds(ids)
        return ids
    }

    private fun writeIds(ids: Set<String>) {
        val raw = ids.joinToString(":") { it } + if (ids.isNotEmpty()) ":" else ""
        ShizukuSettings.getPreferences().edit().putString(KEY_KEEP_ALIVE_LIST, raw).apply()
    }

    private fun parseIds(raw: String): Set<String> {
        if (raw.isBlank()) return emptySet()
        return raw.split(":")
            .filter { it.isNotBlank() }
            .toSet()
    }
}
