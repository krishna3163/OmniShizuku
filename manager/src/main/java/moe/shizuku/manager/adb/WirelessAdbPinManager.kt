package moe.shizuku.manager.adb

import android.content.Context
import android.content.SharedPreferences
import moe.shizuku.manager.ShizukuSettings

/**
 * Whether the manager may step in and enter the wireless ADB pairing PIN for the user through
 * an accessibility service, instead of leaving them to type it by hand.
 *
 * Off   - the pairing dialog is shown as normal, the user types the PIN themselves.
 * Manual - the manager tells the user the pairing dialog is up and waits for them to enter it.
 * Automatic - an accessibility service enters the PIN into the pairing dialog when it appears.
 *
 * The user must opt in: nothing is wired up until they switch it on in settings, and they can
 * switch it off again at any time.
 */
enum class WirelessAdbPinMode {
    Off, Manual, Automatic
}

object WirelessAdbPinManager {

    private const val PREFS_KEY_MODE = "wireless_adb_auto_pin_mode"
    private const val PREFS_KEY_PIN = "wireless_adb_auto_pin"

    fun mode(context: Context): WirelessAdbPinMode {
        return ShizukuSettings.getPreferences()
            .getString(PREFS_KEY_MODE, WirelessAdbPinMode.Off.name)
            ?.let { WirelessAdbPinMode.valueOf(it) }
            ?: WirelessAdbPinMode.Off
    }

    fun setMode(context: Context, mode: WirelessAdbPinMode) {
        ShizukuSettings.getPreferences().edit()
            .putString(PREFS_KEY_MODE, mode.name)
            .apply()
    }

    fun pin(context: Context): String? = ShizukuSettings.getPreferences()
        .getString(PREFS_KEY_PIN, null)

    fun setPin(context: Context, pin: String?) {
        ShizukuSettings.getPreferences().edit()
            .putString(PREFS_KEY_PIN, pin)
            .apply()
    }

    /** Whether an accessibility service should be listening for the pairing dialog. */
    fun isAutomatic(context: Context): Boolean = mode(context) == WirelessAdbPinMode.Automatic
}
