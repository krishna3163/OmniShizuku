package moe.shizuku.manager.accessibility

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * Core controller for accessibility service management.
 *
 * Reads and writes [Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES] directly
 * via the ContentResolver. Shevery holds WRITE_SECURE_SETTINGS (granted via
 * Shizuku/ADB), so no shell command is needed — this is synchronous, atomic,
 * and matches exactly how the reference app works.
 *
 * The enabled list is a colon-separated string of flattened ComponentNames:
 *   "com.example/com.example.MyService:com.other/com.other.Svc"
 *
 * ComponentName.flattenToString() produces "pkg/pkg.ServiceName" — the exact
 * format the system stores. ComponentName.unflattenFromString() reverses it.
 */
object AccessibilityManager {

    private const val TAG = "AccessibilityManager"
    private const val KEY = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES

    /**
     * Set just before a write and cleared by the daemon's ContentObserver
     * when it fires for that write, so the daemon never re-restores the
     * services it just wrote. Mirrors the reference app's isSelfModification.
     */
    @Volatile
    private var selfWritePending = false

    fun isSelfWrite(): Boolean = selfWritePending

    fun clearSelfWrite() {
        selfWritePending = false
    }

    // -----------------------------------------------------------------
    // Reads
    // -----------------------------------------------------------------

    /**
     * Returns the set of enabled service ComponentNames, parsed from
     * [Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES]. Uses the app's
     * own ContentResolver — NOT filtered by package visibility, because
     * we're reading a settings string, not querying PackageManager.
     */
    fun getEnabledServices(context: Context): Set<ComponentName> {
        val raw = Settings.Secure.getString(context.contentResolver, KEY) ?: return emptySet()
        if (raw.isBlank()) return emptySet()
        return raw.split(':')
            .filter { it.isNotBlank() }
            .mapNotNull { ComponentName.unflattenFromString(it) }
            .toSet()
    }

    /** Returns true if [serviceId] (flattenToString format) is currently enabled. */
    fun isServiceEnabled(context: Context, serviceId: String): Boolean {
        val target = ComponentName.unflattenFromString(serviceId) ?: return false
        return getEnabledServices(context).contains(target)
    }

    // -----------------------------------------------------------------
    // Writes (synchronous, atomic)
    // -----------------------------------------------------------------

    /**
     * Enable [serviceId] (flattenToString format). Atomically reads the
     * current list, adds the service, and writes back.
     */
    fun enableService(context: Context, serviceId: String): Boolean {
        val cn = ComponentName.unflattenFromString(serviceId) ?: run {
            Log.w(TAG, "Cannot parse service id: $serviceId")
            return false
        }
        val enabled = getEnabledServices(context).toMutableSet()
        if (!enabled.add(cn)) return true // already enabled
        return writeServices(context, enabled)
    }

    /**
     * Disable [serviceId] (flattenToString format). Atomically reads the
     * current list, removes the service, and writes back.
     */
    fun disableService(context: Context, serviceId: String): Boolean {
        val cn = ComponentName.unflattenFromString(serviceId) ?: run {
            Log.w(TAG, "Cannot parse service id: $serviceId")
            return false
        }
        val enabled = getEnabledServices(context).toMutableSet()
        if (!enabled.remove(cn)) return true // already disabled
        return writeServices(context, enabled)
    }

    /**
     * Re-enable every pinned-but-missing service. Returns the list that was
     * restored (empty if nothing needed restoring or the write failed).
     */
    fun restorePinnedServices(context: Context): List<String> {
        val pinnedRaw = AccessibilityKeepAliveStore.getKeepAliveIds()
        if (pinnedRaw.isEmpty()) return emptyList()

        val pinned = pinnedRaw.mapNotNull { ComponentName.unflattenFromString(it) }
        if (pinned.isEmpty()) return emptyList()

        val enabled = getEnabledServices(context).toMutableSet()
        val missing = pinned.filter { it !in enabled }
        if (missing.isEmpty()) return emptyList()

        enabled.addAll(missing)
        if (writeServices(context, enabled)) {
            Log.d(TAG, "Restored pinned accessibility services: $missing")
            return missing.map { it.flattenToString() }
        }
        Log.w(TAG, "Failed to restore pinned accessibility services: $missing")
        return emptyList()
    }

    // -----------------------------------------------------------------
    // Low-level write
    // -----------------------------------------------------------------

    /**
     * Writes the full set of enabled services to Settings.Secure.
     * Sets [selfWritePending] so the daemon's ContentObserver knows to
     * ignore the resulting change notification.
     */
    private fun writeServices(context: Context, services: Set<ComponentName>): Boolean {
        val value = services.joinToString(":") { it.flattenToString() }
        selfWritePending = true
        val ok = try {
            Settings.Secure.putString(context.contentResolver, KEY, value)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write enabled accessibility services: ${e.message}")
            selfWritePending = false
            false
        }
        if (ok) {
            Log.d(TAG, "Wrote enabled accessibility services (${services.size})")
        } else {
            selfWritePending = false
        }
        return ok
    }
}
