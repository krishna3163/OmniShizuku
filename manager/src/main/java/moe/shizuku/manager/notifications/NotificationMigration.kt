package moe.shizuku.manager.notifications

/**
 * Safe, idempotent notification upgrade migration.
 *
 * Cancels only exact obsolete notification IDs owned by OmniShizuku, preventing
 * orphaned notifications in the notification drawer across app updates.
 */
object NotificationMigration {

    const val PREF_KEY_MIGRATION_VERSION = "notification_migration_version"
    const val CURRENT_MIGRATION_VERSION = 1

    interface NotificationCanceller {
        fun cancel(id: Int)
    }

    interface MigrationStorage {
        fun getVersion(): Int
        fun setVersion(version: Int)
    }

    /**
     * Exact obsolete notification IDs that may have been posted by prior releases
     * and should be cleaned up upon upgrading to the unified NotificationRegistry.
     *
     * Note: IDs currently in active use (e.g. WATCHDOG_ONGOING = 1001) are NOT
     * in this list, to prevent killing active foreground services.
     */
    val OBSOLETE_NOTIFICATION_IDS: Set<Int> = setOf(
        9999,
        9998
    )

    fun migrate(canceller: NotificationCanceller, storage: MigrationStorage): Boolean {
        val current = storage.getVersion()
        if (current >= CURRENT_MIGRATION_VERSION) {
            return false
        }
        for (id in OBSOLETE_NOTIFICATION_IDS) {
            canceller.cancel(id)
        }
        storage.setVersion(CURRENT_MIGRATION_VERSION)
        return true
    }
}
