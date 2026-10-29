package moe.shizuku.manager.utils

import android.os.IBinder
import rikka.shizuku.Shizuku

/**
 * Defensive wrapper around Shizuku binder calls.
 *
 * On Realme / ColorOS and other heavily customized ROMs, the Shizuku service process can be
 * killed by aggressive background restrictions. When that happens [Shizuku.getBinder()] returns
 * null and [Shizuku.pingBinder()] may throw. Every public method here guards against null
 * binders and swallows unexpected throwables so the app never crashes from a missing binder.
 */
object ShizukuGuard {

    /** True only when the Shizuku binder is reachable right now. */
    fun isAvailable(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    /** The current binder, or null if Shizuku is not running. */
    fun binder(): IBinder? = runCatching { Shizuku.getBinder() }.getOrNull()

    /**
     * Runs [action] only when the binder is available. Returns false (and does nothing) when
     * the binder is null or dead, instead of throwing.
     */
    fun runIfAvailable(action: (IBinder) -> Unit): Boolean {
        val binder = binder() ?: return false
        return runCatching {
            action(binder)
            true
        }.getOrDefault(false)
    }

    /**
     * Runs [action] only when the binder is available. Returns the result, or [defaultValue]
     * when the binder is null or dead, instead of throwing.
     */
    fun <T> runIfAvailableOrDefault(defaultValue: T, action: (IBinder) -> T): T {
        val binder = binder() ?: return defaultValue
        return runCatching { action(binder) }.getOrDefault(defaultValue)
    }
}
