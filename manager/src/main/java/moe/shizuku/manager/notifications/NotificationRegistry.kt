package moe.shizuku.manager.notifications

/**
 * Authoritative notification/channel registry derived from actual repository implementation.
 *
 * Existing foreground-service/ongoing IDs and user-facing actions are preserved unless a
 * collision required reassignment; reassigned IDs are documented here.
 */
object NotificationRegistry {

    object Channels {
        const val WATCHDOG_ONGOING = "shizuku_watchdog_v2"
        const val WATCHDOG_REPORTS = "crash_reports"
        const val WATCHDOG_NOT_RUNNING = "watchdog_not_running"
        const val SHEVERY_CONTROL = "shevery_control"
        const val STARTUP = "shevery_startup"
        const val ADB_PAIRING = "adb_pairing"
        const val ADB_START = "AdbStartWorker"
        const val USB_HIDE = "usb_debugging_hide"
        const val LEGACY_RISH = "legacy_rish_compatibility"
        const val HELPER = "helper_notifications"
        const val USB_CONNECTION = "usb_connection"
        const val ACCESSIBILITY_KEEP_ALIVE = "accessibility_keep_alive"
        const val HIDING_WATCH = "hiding"
        const val AUTH_ERRORS = "auth_errors"
        const val RECORDING = "call_recording_channel"
        const val ISLAND = "island_service"
        const val ENFORCE_DOZE_STATS = "enforcedoze_stats"
        const val ENFORCE_DOZE_SILENT = "enforcedoze_silent"
        const val ENFORCE_DOZE_DISABLED = "enforcedoze_disable"
        const val CAFFEINATE_LIVE = "caffeinate_live"
        const val SOUND_MASTER = "notifications"
        const val THEME_PATCHER = "notifications"
        const val INTENT_SHELL = "intent_shell"
    }

    object Ids {
        // Preserved
        const val WATCHDOG_ONGOING = 1001
        const val WATCHDOG_CRASH = 1002
        const val WATCHDOG_RECOVERY = 1003
        const val WATCHDOG_GAVE_UP = 1004
        const val WATCHDOG_NOT_RUNNING = 1005
        const val ADB_PAIRING = 1
        const val CAFFEINATE = 2
        const val PIP = 4
        const val ADB_START_RECEIVER = 1447
        const val ADB_START_RUN = 1448
        const val USB_DEBUGGING_HIDE = 1453
        const val LEGACY_RISH = 1458
        const val ACCESSIBILITY_KEEP_ALIVE = 1006
        const val HIDING_WATCH = 1452
        const val AUTH_ERRORS = 1450
        const val RECORDING = 2002
        const val ENFORCE_DOZE = 1234
        const val INTENT_SHELL = 0x7F040010

        // Reassigned to resolve pre-existing collisions (behavior/actions preserved)
        const val SHEVERY_CONTROL = 1456 // was 1002, collided with WATCHDOG_CRASH
        const val STARTUP = 1457         // was 1005, collided with WATCHDOG_NOT_RUNNING
        const val ISLAND = 1459          // was 1001, collided with WATCHDOG_ONGOING

        // New
        const val HELPER = 1460
        const val USB_CONNECTION = 1461
        const val SOUND_MASTER = 1462
        const val THEME_PATCHER = 1463
    }

    fun allManagedIds(): Set<Int> = setOf(
        Ids.WATCHDOG_ONGOING, Ids.WATCHDOG_CRASH, Ids.WATCHDOG_RECOVERY, Ids.WATCHDOG_GAVE_UP,
        Ids.WATCHDOG_NOT_RUNNING, Ids.ADB_PAIRING, Ids.CAFFEINATE, Ids.PIP, Ids.ADB_START_RECEIVER,
        Ids.ADB_START_RUN, Ids.USB_DEBUGGING_HIDE, Ids.LEGACY_RISH, Ids.ACCESSIBILITY_KEEP_ALIVE,
        Ids.HIDING_WATCH, Ids.AUTH_ERRORS, Ids.RECORDING, Ids.ENFORCE_DOZE, Ids.INTENT_SHELL,
        Ids.SHEVERY_CONTROL, Ids.STARTUP, Ids.ISLAND, Ids.HELPER, Ids.USB_CONNECTION,
        Ids.SOUND_MASTER, Ids.THEME_PATCHER
    )

    fun allManagedChannels(): Set<String> = setOf(
        Channels.WATCHDOG_ONGOING, Channels.WATCHDOG_REPORTS, Channels.WATCHDOG_NOT_RUNNING,
        Channels.SHEVERY_CONTROL, Channels.STARTUP, Channels.ADB_PAIRING, Channels.ADB_START,
        Channels.USB_HIDE, Channels.LEGACY_RISH, Channels.HELPER, Channels.USB_CONNECTION,
        Channels.ACCESSIBILITY_KEEP_ALIVE, Channels.HIDING_WATCH, Channels.AUTH_ERRORS,
        Channels.RECORDING, Channels.ISLAND, Channels.ENFORCE_DOZE_STATS, Channels.ENFORCE_DOZE_SILENT,
        Channels.ENFORCE_DOZE_DISABLED, Channels.CAFFEINATE_LIVE, Channels.SOUND_MASTER,
        Channels.THEME_PATCHER, Channels.INTENT_SHELL
    )
}
