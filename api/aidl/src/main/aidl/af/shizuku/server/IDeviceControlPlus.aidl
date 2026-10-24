package af.shizuku.server;

/**
 * Device-level control operations accessible from uid 2000 (ADB/shell).
 *
 * Covers everything that `adb shell svc / settings / media` commands expose but that
 * previously had no clean IPC interface in ShizukuPlus:
 *
 *   - Connectivity toggles  (airplane, WiFi, BT, mobile data, NFC)
 *   - USB function switching (MTP, ADB, charging, RNDIS)
 *   - Power lifecycle       (reboot with optional reason, shutdown)
 *   - Display comfort       (brightness level, auto-brightness, screen timeout, auto-rotate)
 *   - Audio                 (per-stream volume get/set)
 *   - System appearance     (font scale, animation scales)
 *   - Settings convenience  (put/get any system|secure|global key)
 *
 * Shell (uid 2000) can perform all of these without root because:
 *   - WRITE_SETTINGS / WRITE_SECURE_SETTINGS are shell install-time grants
 *   - `svc` commands delegate to system services the shell is already authorised to call
 *   - `reboot` and `svc power shutdown` require no special permission for uid 2000
 */
interface IDeviceControlPlus {

    // ── Connectivity ──────────────────────────────────────────────────────────

    /**
     * Enable or disable airplane mode.
     * Uses 'settings put global airplane_mode_on' + broadcasts AIRPLANE_MODE intent.
     */
    boolean setAirplaneModeEnabled(boolean enabled);

    /**
     * Enable or disable WiFi radio.
     * Uses 'svc wifi enable|disable'.
     */
    boolean setWifiEnabled(boolean enabled);

    /**
     * Enable or disable Bluetooth adapter.
     * Uses 'svc bluetooth enable|disable'.
     */
    boolean setBluetoothEnabled(boolean enabled);

    /**
     * Enable or disable mobile data.
     * Uses 'svc data enable|disable'.
     */
    boolean setMobileDataEnabled(boolean enabled);

    /**
     * Enable or disable NFC adapter.
     * Uses 'svc nfc enable|disable'.
     */
    boolean setNfcEnabled(boolean enabled);

    // ── USB ───────────────────────────────────────────────────────────────────

    /**
     * Switch the USB function.
     * Valid values: "mtp", "adb", "charging", "none", "rndis", "midi".
     * Uses 'svc usb setFunctions <function>'.
     */
    boolean setUsbFunction(String function);

    // ── Power ─────────────────────────────────────────────────────────────────

    /**
     * Reboot the device.
     * reason: null = normal reboot; "recovery", "bootloader", "fastboot", "edl" for modes.
     * Uses the 'reboot' binary or PowerManager binder.
     */
    boolean reboot(String reason);

    /**
     * Shut down the device.
     * Uses 'svc power shutdown'.
     */
    boolean shutdown();

    // ── Display ───────────────────────────────────────────────────────────────

    /**
     * Set screen brightness level (0-255).
     * Also disables auto-brightness to make the setting take immediate effect.
     * Uses 'settings put system screen_brightness'.
     */
    boolean setScreenBrightness(int level);

    /**
     * Enable or disable adaptive (auto) brightness.
     * Uses 'settings put system screen_brightness_mode 1|0'.
     */
    boolean setAutoBrightnessEnabled(boolean enabled);

    /**
     * Set screen-off timeout in milliseconds (e.g. 30000 = 30 s, -1 = never).
     * Uses 'settings put system screen_off_timeout'.
     */
    boolean setScreenTimeout(int ms);

    /**
     * Enable or disable auto-rotate (accelerometer-based rotation).
     * Uses 'settings put system accelerometer_rotation 1|0'.
     */
    boolean setAutoRotateEnabled(boolean enabled);

    // ── Audio ─────────────────────────────────────────────────────────────────

    /**
     * Set the volume level for an audio stream.
     * stream: AudioManager.STREAM_* (0=VOICE_CALL, 1=SYSTEM, 2=RING, 3=MUSIC,
     *         4=ALARM, 5=NOTIFICATION).
     * Uses 'media volume --stream <s> --set <level>'.
     */
    boolean setStreamVolume(int stream, int level);

    /**
     * Get the current volume level for an audio stream.
     * Returns -1 on failure.
     * Uses 'media volume --stream <s> --get'.
     */
    int getStreamVolume(int stream);

    // ── System Appearance ─────────────────────────────────────────────────────

    /**
     * Override the system font scale (e.g. 0.85, 1.0, 1.15, 1.3).
     * Uses 'settings put system font_scale'.
     */
    boolean setFontScale(float scale);

    /**
     * Enable or disable all animation scales (window, transition, animator).
     * Disabling (false) sets all three global scales to 0 — matches what Developer
     * Options "Disable animations" does. Enabling (true) restores them to 1.0.
     * Uses 'settings put global window_animation_scale / transition_animation_scale /
     * animator_duration_scale'.
     */
    boolean setAnimationsEnabled(boolean enabled);

    // ── Settings convenience ──────────────────────────────────────────────────

    /**
     * Write a single settings key.
     * namespace: "system", "secure", or "global".
     * Uses 'settings put <namespace> <key> <value>'.
     * Shell has WRITE_SETTINGS + WRITE_SECURE_SETTINGS.
     */
    boolean putSetting(String namespace, String key, String value);

    /**
     * Read a single settings key.
     * namespace: "system", "secure", or "global".
     * Uses 'settings get <namespace> <key>'.
     * Returns null if the key does not exist or the call fails.
     */
    String getSetting(String namespace, String key);
}
