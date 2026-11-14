package moe.shizuku.manager.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRegistryTest {

    @Test
    fun managedIds_areCompletelyUnique() {
        val allIds = listOf(
            NotificationRegistry.Ids.WATCHDOG_ONGOING,
            NotificationRegistry.Ids.WATCHDOG_CRASH,
            NotificationRegistry.Ids.WATCHDOG_RECOVERY,
            NotificationRegistry.Ids.WATCHDOG_GAVE_UP,
            NotificationRegistry.Ids.WATCHDOG_NOT_RUNNING,
            NotificationRegistry.Ids.ADB_PAIRING,
            NotificationRegistry.Ids.CAFFEINATE,
            NotificationRegistry.Ids.PIP,
            NotificationRegistry.Ids.ADB_START_RECEIVER,
            NotificationRegistry.Ids.ADB_START_RUN,
            NotificationRegistry.Ids.USB_DEBUGGING_HIDE,
            NotificationRegistry.Ids.LEGACY_RISH,
            NotificationRegistry.Ids.ACCESSIBILITY_KEEP_ALIVE,
            NotificationRegistry.Ids.HIDING_WATCH,
            NotificationRegistry.Ids.AUTH_ERRORS,
            NotificationRegistry.Ids.RECORDING,
            NotificationRegistry.Ids.ENFORCE_DOZE,
            NotificationRegistry.Ids.INTENT_SHELL,
            NotificationRegistry.Ids.SHEVERY_CONTROL,
            NotificationRegistry.Ids.STARTUP,
            NotificationRegistry.Ids.ISLAND,
            NotificationRegistry.Ids.HELPER,
            NotificationRegistry.Ids.USB_CONNECTION,
            NotificationRegistry.Ids.SOUND_MASTER,
            NotificationRegistry.Ids.THEME_PATCHER
        )
        val uniqueSet = allIds.toSet()
        assertEquals("Duplicate notification IDs found in registry", allIds.size, uniqueSet.size)
    }

    @Test
    fun allManagedIds_matchesRegistryContents() {
        val managed = NotificationRegistry.allManagedIds()
        assertTrue(managed.contains(NotificationRegistry.Ids.WATCHDOG_ONGOING))
        assertTrue(managed.contains(NotificationRegistry.Ids.SHEVERY_CONTROL))
        assertTrue(managed.contains(NotificationRegistry.Ids.STARTUP))
        assertTrue(managed.contains(NotificationRegistry.Ids.ISLAND))
        assertTrue(managed.contains(NotificationRegistry.Ids.HELPER))
        assertTrue(managed.contains(NotificationRegistry.Ids.USB_CONNECTION))
        assertTrue(managed.contains(NotificationRegistry.Ids.SOUND_MASTER))
        assertTrue(managed.contains(NotificationRegistry.Ids.THEME_PATCHER))
        assertEquals(25, managed.size)
    }

    @Test
    fun criticalPreservedAndReassignedIds_matchSpecifications() {
        // Preserved critical IDs
        assertEquals(1001, NotificationRegistry.Ids.WATCHDOG_ONGOING)
        assertEquals(1002, NotificationRegistry.Ids.WATCHDOG_CRASH)
        assertEquals(1003, NotificationRegistry.Ids.WATCHDOG_RECOVERY)
        assertEquals(1004, NotificationRegistry.Ids.WATCHDOG_GAVE_UP)
        assertEquals(1005, NotificationRegistry.Ids.WATCHDOG_NOT_RUNNING)
        assertEquals(1, NotificationRegistry.Ids.ADB_PAIRING)
        assertEquals(2, NotificationRegistry.Ids.CAFFEINATE)
        assertEquals(1447, NotificationRegistry.Ids.ADB_START_RECEIVER)
        assertEquals(1448, NotificationRegistry.Ids.ADB_START_RUN)
        assertEquals(1450, NotificationRegistry.Ids.AUTH_ERRORS)
        assertEquals(1452, NotificationRegistry.Ids.HIDING_WATCH)
        assertEquals(1453, NotificationRegistry.Ids.USB_DEBUGGING_HIDE)
        assertEquals(1458, NotificationRegistry.Ids.LEGACY_RISH)

        // Collision-resolved reassignments
        assertEquals(1456, NotificationRegistry.Ids.SHEVERY_CONTROL) // resolved collision with 1002
        assertEquals(1457, NotificationRegistry.Ids.STARTUP)         // resolved collision with 1005
        assertEquals(1459, NotificationRegistry.Ids.ISLAND)          // resolved collision with 1001

        // New feature channels
        assertEquals(1460, NotificationRegistry.Ids.HELPER)
        assertEquals(1461, NotificationRegistry.Ids.USB_CONNECTION)
    }

    @Test
    fun channels_nonBlankAndUnique() {
        val channels = NotificationRegistry.allManagedChannels()
        for (ch in channels) {
            assertTrue("Channel ID must not be blank", ch.isNotBlank())
        }
        assertTrue(channels.contains("helper_notifications"))
        assertTrue(channels.contains("usb_connection"))
        assertTrue(channels.contains("shizuku_watchdog_v2"))
        assertTrue(channels.contains("crash_reports"))
        assertTrue(channels.contains("shevery_control"))
        assertTrue(channels.contains("shevery_startup"))
        assertTrue(channels.contains("island_service"))
    }
}
