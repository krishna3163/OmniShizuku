package moe.shizuku.manager.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationMigrationTest {

    private class FakeNotificationCanceller : NotificationMigration.NotificationCanceller {
        val cancelledIds = mutableListOf<Int>()
        override fun cancel(id: Int) {
            cancelledIds.add(id)
        }
    }

    private class FakeMigrationStorage(private var version: Int = 0) : NotificationMigration.MigrationStorage {
        override fun getVersion(): Int = version
        override fun setVersion(version: Int) {
            this.version = version
        }
    }

    @Test
    fun migration_initialRun_cancelsObsoleteIdsAndSetsVersion() {
        val canceller = FakeNotificationCanceller()
        val storage = FakeMigrationStorage(version = 0)

        val migrated = NotificationMigration.migrate(canceller, storage)

        assertTrue("First migration run should report success", migrated)
        assertEquals(NotificationMigration.CURRENT_MIGRATION_VERSION, storage.getVersion())
        assertEquals(NotificationMigration.OBSOLETE_NOTIFICATION_IDS.size, canceller.cancelledIds.size)
        assertTrue(canceller.cancelledIds.containsAll(NotificationMigration.OBSOLETE_NOTIFICATION_IDS))
    }

    @Test
    fun migration_idempotent_subsequentRunsDoNothing() {
        val canceller = FakeNotificationCanceller()
        val storage = FakeMigrationStorage(version = NotificationMigration.CURRENT_MIGRATION_VERSION)

        val migrated = NotificationMigration.migrate(canceller, storage)

        assertFalse("Subsequent migration run should be a no-op", migrated)
        assertEquals(0, canceller.cancelledIds.size)
        assertEquals(NotificationMigration.CURRENT_MIGRATION_VERSION, storage.getVersion())
    }

    @Test
    fun migration_doesNotCancelActiveSubsystemIds() {
        val activeIds = NotificationRegistry.allManagedIds()
        for (obsolete in NotificationMigration.OBSOLETE_NOTIFICATION_IDS) {
            assertFalse(
                "Obsolete cleanup ID $obsolete must not collide with active registry IDs",
                activeIds.contains(obsolete)
            )
        }
    }
}
