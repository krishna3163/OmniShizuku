package moe.shizuku.manager.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbDebuggingStateMachineTest {

    // 1. Disabled -> Monitoring
    @Test
    fun testDisabledToMonitoring() {
        val initial = UsbDebuggingSnapshot(state = UsbDebuggingState.DISABLED)
        val next = UsbDebuggingStateMachine.reduce(initial, UsbDebuggingEvent.StartMonitoring)
        assertEquals(UsbDebuggingState.MONITORING, next.state)
        assertEquals(1L, next.generation)
    }

    // 2. Monitoring -> Hiding
    @Test
    fun testMonitoringToHidingViaForegroundAndJournal() {
        val s0 = UsbDebuggingSnapshot(state = UsbDebuggingState.MONITORING)
        val s1 = UsbDebuggingStateMachine.reduce(s0, UsbDebuggingEvent.TargetAppForeground("com.bank.app"))
        assertEquals(UsbDebuggingState.HIDE_REQUESTED, s1.state)
        assertEquals("com.bank.app", s1.activeTargetPackage)

        val s2 = UsbDebuggingStateMachine.reduce(
            s1,
            UsbDebuggingEvent.JournalPersisted("op-1", 1, "com.bank.app")
        )
        assertEquals(UsbDebuggingState.HIDING, s2.state)
        assertEquals("op-1", s2.operationId)
        assertEquals(1, s2.originalSetting)
    }

    // 3. Successful hide
    @Test
    fun testSuccessfulHide() {
        val hiding = UsbDebuggingSnapshot(
            state = UsbDebuggingState.HIDING,
            operationId = "op-1",
            originalSetting = 1,
            activeTargetPackage = "com.bank.app"
        )
        val next = UsbDebuggingStateMachine.reduce(hiding, UsbDebuggingEvent.HideCompleted(verifiedSetting = 0))
        assertEquals(UsbDebuggingState.HIDDEN, next.state)
        assertEquals(0, next.lastVerifiedSetting)
        assertTrue(next.isHidden)
    }

    // 4. Failed hide
    @Test
    fun testFailedHide() {
        val hiding = UsbDebuggingSnapshot(
            state = UsbDebuggingState.HIDING,
            operationId = "op-1",
            originalSetting = 1
        )
        val next = UsbDebuggingStateMachine.reduce(hiding, UsbDebuggingEvent.HideFailed("Provider rejected write"))
        assertEquals(UsbDebuggingState.ERROR, next.state)
        assertTrue(next.lastError!!.contains("Provider rejected write"))
    }

    // 5. Persistence failure before hide
    @Test
    fun testPersistenceFailureBeforeHide() {
        val s = UsbDebuggingSnapshot(state = UsbDebuggingState.HIDE_REQUESTED)
        val next = UsbDebuggingStateMachine.reduce(s, UsbDebuggingEvent.JournalPersistenceFailed("Disk full"))
        assertEquals(UsbDebuggingState.ERROR, next.state)
        assertTrue(next.lastError!!.contains("Disk full"))
    }

    // 6. Process death after journal persistence (HIDING phase)
    @Test
    fun testProcessDeathAfterJournalPersistence() {
        val initial = UsbDebuggingSnapshot(state = UsbDebuggingState.DISABLED)
        val next = UsbDebuggingStateMachine.reduce(
            initial,
            UsbDebuggingEvent.ProcessDeathOrCrashDetected("op-99", 1, UsbDebuggingPhase.HIDING)
        )
        assertEquals(UsbDebuggingState.PENDING_RESTORE, next.state)
        assertEquals("op-99", next.operationId)
    }

    // 7. Process death after setting changes (HIDDEN phase)
    @Test
    fun testProcessDeathAfterSettingChanges() {
        val initial = UsbDebuggingSnapshot(state = UsbDebuggingState.DISABLED)
        val next = UsbDebuggingStateMachine.reduce(
            initial,
            UsbDebuggingEvent.ProcessDeathOrCrashDetected("op-100", 1, UsbDebuggingPhase.HIDDEN)
        )
        assertEquals(UsbDebuggingState.PENDING_RESTORE, next.state)
        assertEquals("op-100", next.operationId)
        assertEquals(1, next.originalSetting)
    }

    // 8. Restore after service recreation
    @Test
    fun testRestoreAfterServiceRecreation() {
        val pending = UsbDebuggingSnapshot(
            state = UsbDebuggingState.PENDING_RESTORE,
            operationId = "op-rec",
            originalSetting = 1
        )
        val restoring = UsbDebuggingStateMachine.reduce(
            pending,
            UsbDebuggingEvent.RestoreRequested("Service recreate recovery")
        )
        assertEquals(UsbDebuggingState.RESTORING, restoring.state)

        val restored = UsbDebuggingStateMachine.reduce(
            restoring,
            UsbDebuggingEvent.RestoreCompleted(verifiedSetting = 1)
        )
        assertEquals(UsbDebuggingState.RESTORED, restored.state)
        assertEquals(1, restored.lastVerifiedSetting)
        assertNull(restored.operationId)
    }

    // 9. Restore after reboot
    @Test
    fun testRestoreAfterReboot() {
        val initial = UsbDebuggingSnapshot(state = UsbDebuggingState.DISABLED)
        val s1 = UsbDebuggingStateMachine.reduce(
            initial,
            UsbDebuggingEvent.ProcessDeathOrCrashDetected("boot-op", 1, UsbDebuggingPhase.HIDDEN)
        )
        assertEquals(UsbDebuggingState.PENDING_RESTORE, s1.state)

        val s2 = UsbDebuggingStateMachine.reduce(s1, UsbDebuggingEvent.RestoreRequested("Boot restore"))
        assertEquals(UsbDebuggingState.RESTORING, s2.state)

        val s3 = UsbDebuggingStateMachine.reduce(s2, UsbDebuggingEvent.RestoreCompleted(verifiedSetting = 1))
        assertEquals(UsbDebuggingState.RESTORED, s3.state)
    }

    // 10. Shizuku binder death / provider failure
    @Test
    fun testProviderFailureTransitionsToError() {
        val s = UsbDebuggingSnapshot(state = UsbDebuggingState.HIDING)
        val next = UsbDebuggingStateMachine.reduce(s, UsbDebuggingEvent.HideFailed("Binder died"))
        assertEquals(UsbDebuggingState.ERROR, next.state)
    }

    // 11. Root provider unavailable
    @Test
    fun testRootUnavailableTransitionsToError() {
        val s = UsbDebuggingSnapshot(state = UsbDebuggingState.HIDING)
        val next = UsbDebuggingStateMachine.reduce(s, UsbDebuggingEvent.HideFailed("su not found"))
        assertEquals(UsbDebuggingState.ERROR, next.state)
        assertTrue(next.lastError!!.contains("su not found"))
    }

    // 12. Permission revoked during operation
    @Test
    fun testPermissionRevokedDuringRestore() {
        val s = UsbDebuggingSnapshot(state = UsbDebuggingState.RESTORING)
        val next = UsbDebuggingStateMachine.reduce(s, UsbDebuggingEvent.RestoreFailed("SecurityException: permission denied"))
        assertEquals(UsbDebuggingState.RECOVERY_REQUIRED, next.state)
        assertTrue(next.lastError!!.contains("SecurityException"))
    }

    // 13. Restore failure
    @Test
    fun testRestoreFailure() {
        val restoring = UsbDebuggingSnapshot(state = UsbDebuggingState.RESTORING)
        val failed = UsbDebuggingStateMachine.reduce(restoring, UsbDebuggingEvent.RestoreFailed("Write failure"))
        assertEquals(UsbDebuggingState.RECOVERY_REQUIRED, failed.state)

        // Manual recovery re-attempts restoring
        val retrying = UsbDebuggingStateMachine.reduce(failed, UsbDebuggingEvent.ManualRecoveryRequested)
        assertEquals(UsbDebuggingState.RESTORING, retrying.state)
    }

    // 14. Repeated restore request
    @Test
    fun testRepeatedRestoreRequestIsIdempotent() {
        val restoring = UsbDebuggingSnapshot(state = UsbDebuggingState.RESTORING)
        val again = UsbDebuggingStateMachine.reduce(restoring, UsbDebuggingEvent.RestoreRequested("Duplicate"))
        assertEquals(UsbDebuggingState.RESTORING, again.state)
    }

    // 15. External user configuration change
    @Test
    fun testExternalUserConfigurationChange() {
        val hidden = UsbDebuggingSnapshot(state = UsbDebuggingState.HIDDEN, originalSetting = 1)
        val conflict = UsbDebuggingStateMachine.reduce(
            hidden,
            UsbDebuggingEvent.ExternalConflictDetected(currentSetting = 1, expectedOriginal = 1)
        )
        assertEquals(UsbDebuggingState.CONFLICT_DETECTED, conflict.state)
        assertTrue(conflict.conflictDetail!!.contains("External modification"))
    }

    // 16. Corrupted journal handling
    @Test
    fun testCorruptedJournalHandling() {
        val record = UsbDebuggingJournalRecord.fromJson("{ invalid json content }")
        assertNull(record)
    }

    // 17. Old journal schema migration
    @Test
    fun testOldJournalSchemaMigration() {
        val migrated = UsbDebuggingJournalRecord.migrateFromLegacy(originalEnabled = 1)
        assertEquals(1, migrated.originalSetting)
        assertEquals(0, migrated.lastAppliedSetting)
        assertEquals(UsbDebuggingPhase.HIDDEN, migrated.phase)
        assertTrue(migrated.recoveryRequired)
        assertTrue(migrated.operationId.startsWith("legacy-"))

        val json = migrated.toJson()
        val parsed = UsbDebuggingJournalRecord.fromJson(json)
        assertNotNull(parsed)
        assertEquals(migrated.operationId, parsed!!.operationId)
        assertEquals(migrated.originalSetting, parsed.originalSetting)
    }

    // 18. Duplicate service start
    @Test
    fun testDuplicateServiceStartIsNoOp() {
        val monitoring = UsbDebuggingSnapshot(state = UsbDebuggingState.MONITORING, generation = 2L)
        val next = UsbDebuggingStateMachine.reduce(monitoring, UsbDebuggingEvent.StartMonitoring)
        assertEquals(UsbDebuggingState.MONITORING, next.state)
        assertEquals(2L, next.generation)
    }

    // 19. Concurrent hide and restore requests
    @Test
    fun testConcurrentHideAndRestoreRequests() {
        val hiding = UsbDebuggingSnapshot(state = UsbDebuggingState.HIDING)
        val restoreReq = UsbDebuggingStateMachine.reduce(hiding, UsbDebuggingEvent.TargetAppBackground)
        assertEquals(UsbDebuggingState.RESTORE_REQUESTED, restoreReq.state)

        val restored = UsbDebuggingStateMachine.reduce(
            UsbDebuggingStateMachine.reduce(restoreReq, UsbDebuggingEvent.RestoreRequested("Background")),
            UsbDebuggingEvent.RestoreCompleted(1)
        )
        assertEquals(UsbDebuggingState.RESTORED, restored.state)
    }

    // 20. Recovery after application update
    @Test
    fun testRecoveryAfterApplicationUpdate() {
        val initial = UsbDebuggingSnapshot(state = UsbDebuggingState.DISABLED)
        val s1 = UsbDebuggingStateMachine.reduce(
            initial,
            UsbDebuggingEvent.ProcessDeathOrCrashDetected("app-update-op", 1, UsbDebuggingPhase.HIDDEN)
        )
        assertEquals(UsbDebuggingState.PENDING_RESTORE, s1.state)
        val s2 = UsbDebuggingStateMachine.reduce(s1, UsbDebuggingEvent.ManualRecoveryRequested)
        assertEquals(UsbDebuggingState.RESTORING, s2.state)
        val s3 = UsbDebuggingStateMachine.reduce(s2, UsbDebuggingEvent.RestoreCompleted(1))
        assertEquals(UsbDebuggingState.RESTORED, s3.state)
    }

    // Deterministic stress test
    @Test
    fun testDeterministicStressSequence() {
        fun run(seed: Int): UsbDebuggingSnapshot {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            val list = listOf(
                UsbDebuggingEvent.StartMonitoring,
                UsbDebuggingEvent.TargetAppForeground("pkg.test"),
                UsbDebuggingEvent.JournalPersisted("op-rnd", 1, "pkg.test"),
                UsbDebuggingEvent.HideCompleted(0),
                UsbDebuggingEvent.TargetAppBackground,
                UsbDebuggingEvent.RestoreRequested("Stress"),
                UsbDebuggingEvent.RestoreCompleted(1),
                UsbDebuggingEvent.HideFailed("Error"),
                UsbDebuggingEvent.RestoreFailed("Error"),
                UsbDebuggingEvent.ExternalConflictDetected(1, 1),
                UsbDebuggingEvent.ManualRecoveryRequested,
                UsbDebuggingEvent.StopMonitoring,
                UsbDebuggingEvent.Reset
            )
            var s = UsbDebuggingSnapshot()
            repeat(300) { s = UsbDebuggingStateMachine.reduce(s, list[next() % list.size]) }
            return s
        }
        assertEquals(run(42), run(42))
        run(10)
    }
}
