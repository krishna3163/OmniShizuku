package moe.shizuku.manager.doze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DozeStateMachineTest {

    @Test
    fun testInitialStateIsInactive() {
        val snapshot = DozeSnapshot()
        assertEquals(DozeState.INACTIVE, snapshot.state)
        assertEquals(0L, snapshot.generation)
        assertFalse(snapshot.isDozing)
        assertFalse(snapshot.wakeLockHeld)
        assertNull(snapshot.initialSnapshot)
    }

    @Test
    fun testScreenTurnedOffImmediateDoze() {
        val initial = DozeSnapshot()
        val next = DozeStateMachine.reduce(
            initial,
            DozeEvent.ScreenTurnedOff(
                isCharging = false,
                isCallActive = false,
                delayMs = 0L,
                currentTimeMs = 1000L
            )
        )

        assertEquals(DozeState.ENTERING_DOZE, next.state)
        assertEquals(1L, next.generation)
        assertFalse(next.isScreenOn)
        assertEquals(0L, next.pendingDelayMs)
        assertEquals(1000L, next.screenOffDeadlineMs)
    }

    @Test
    fun testScreenTurnedOffDebouncePending() {
        val initial = DozeSnapshot()
        val next = DozeStateMachine.reduce(
            initial,
            DozeEvent.ScreenTurnedOff(
                isCharging = false,
                isCallActive = false,
                delayMs = 3000L,
                currentTimeMs = 1000L
            )
        )

        assertEquals(DozeState.SCREEN_OFF_PENDING, next.state)
        assertEquals(1L, next.generation)
        assertFalse(next.isScreenOn)
        assertEquals(3000L, next.pendingDelayMs)
        assertEquals(4000L, next.screenOffDeadlineMs)
    }

    @Test
    fun testPendingDelayElapsedTransitionsToEnteringDoze() {
        val pending = DozeSnapshot(
            state = DozeState.SCREEN_OFF_PENDING,
            generation = 1L,
            pendingDelayMs = 3000L
        )

        val entering = DozeStateMachine.reduce(
            pending,
            DozeEvent.PendingDelayElapsed(generation = 1L)
        )

        assertEquals(DozeState.ENTERING_DOZE, entering.state)
        assertEquals(0L, entering.pendingDelayMs)
    }

    @Test
    fun testRapidScreenOnCancelsPendingDoze() {
        val pending = DozeSnapshot(
            state = DozeState.SCREEN_OFF_PENDING,
            generation = 1L,
            pendingDelayMs = 5000L,
            screenOffDeadlineMs = 6000L
        )

        val cancelled = DozeStateMachine.reduce(
            pending,
            DozeEvent.ScreenTurnedOn(currentTimeMs = 1500L)
        )

        assertEquals(DozeState.INACTIVE, cancelled.state)
        assertTrue(cancelled.isScreenOn)
        assertEquals(2L, cancelled.generation)
        assertEquals(0L, cancelled.pendingDelayMs)
        assertEquals("ScreenOnDuringPending", cancelled.lastExitReason)
    }

    @Test
    fun testRapidScreenOnOffCyclesAdvanceGeneration() {
        var s = DozeSnapshot()
        s = DozeStateMachine.reduce(s, DozeEvent.ScreenTurnedOff(false, false, 2000L, 1000L))
        assertEquals(DozeState.SCREEN_OFF_PENDING, s.state)
        assertEquals(1L, s.generation)

        s = DozeStateMachine.reduce(s, DozeEvent.ScreenTurnedOn(1200L))
        assertEquals(DozeState.INACTIVE, s.state)
        assertEquals(2L, s.generation)

        s = DozeStateMachine.reduce(s, DozeEvent.ScreenTurnedOff(false, false, 2000L, 1300L))
        assertEquals(DozeState.SCREEN_OFF_PENDING, s.state)
        assertEquals(3L, s.generation)
    }

    @Test
    fun testChargerConnectedCancelsPendingDozeWhenConfigured() {
        val pending = DozeSnapshot(
            state = DozeState.SCREEN_OFF_PENDING,
            generation = 1L,
            disableWhenCharging = true,
            isCharging = false
        )

        val cancelled = DozeStateMachine.reduce(
            pending,
            DozeEvent.PowerConnected(currentTimeMs = 2000L)
        )

        assertEquals(DozeState.INACTIVE, cancelled.state)
        assertTrue(cancelled.isCharging)
        assertEquals(2L, cancelled.generation)
        assertEquals("ChargingDuringPending", cancelled.lastExitReason)
    }

    @Test
    fun testChargerConnectedIgnoredWhenDisableWhenChargingIsFalse() {
        val pending = DozeSnapshot(
            state = DozeState.SCREEN_OFF_PENDING,
            generation = 1L,
            disableWhenCharging = false,
            isCharging = false
        )

        val continuing = DozeStateMachine.reduce(
            pending,
            DozeEvent.PowerConnected(currentTimeMs = 2000L)
        )

        assertEquals(DozeState.SCREEN_OFF_PENDING, continuing.state)
        assertTrue(continuing.isCharging)
    }

    @Test
    fun testCallActivePreventsEnteringDoze() {
        val initial = DozeSnapshot()
        val s = DozeStateMachine.reduce(
            initial,
            DozeEvent.ScreenTurnedOff(
                isCharging = false,
                isCallActive = true,
                delayMs = 0L,
                currentTimeMs = 1000L
            )
        )

        assertEquals(DozeState.INACTIVE, s.state)
        assertTrue(s.isCallActive)
    }

    @Test
    fun testRadioStateCapturedAndDozeApplied() {
        val entering = DozeSnapshot(
            state = DozeState.ENTERING_DOZE,
            generation = 1L
        )

        val radioSnapshot = DeviceRadioSnapshot(
            wifiEnabled = true,
            bluetoothEnabled = true,
            modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI, DeviceRadioSnapshot.SETTING_BLUETOOTH)
        )

        var s = DozeStateMachine.reduce(
            entering,
            DozeEvent.RadioStateCaptured(generation = 1L, snapshot = radioSnapshot)
        )
        assertEquals(radioSnapshot, s.initialSnapshot)

        s = DozeStateMachine.reduce(
            s,
            DozeEvent.DozeApplied(generation = 1L, idleMode = "IDLE", wakeLockAcquired = true)
        )
        assertEquals(DozeState.DEEP_DOZING, s.state)
        assertTrue(s.isDozing)
        assertTrue(s.wakeLockHeld)
        assertEquals(1, s.dozeEnterCount)
    }

    @Test
    fun testScreenTurnedOnDuringEnteringDozeTriggersRestoreIfModified() {
        val entering = DozeSnapshot(
            state = DozeState.ENTERING_DOZE,
            generation = 1L,
            initialSnapshot = DeviceRadioSnapshot(
                wifiEnabled = true,
                modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI)
            )
        )

        val s = DozeStateMachine.reduce(entering, DozeEvent.ScreenTurnedOn(currentTimeMs = 2000L))
        assertEquals(DozeState.RESTORING, s.state)
        assertEquals("ScreenOnDuringEnter", s.lastExitReason)
    }

    @Test
    fun testScreenTurnedOnDuringEnteringDozeReturnsInactiveIfNoModifications() {
        val entering = DozeSnapshot(
            state = DozeState.ENTERING_DOZE,
            generation = 1L,
            initialSnapshot = DeviceRadioSnapshot(modifiedSettings = emptySet())
        )

        val s = DozeStateMachine.reduce(entering, DozeEvent.ScreenTurnedOn(currentTimeMs = 2000L))
        assertEquals(DozeState.INACTIVE, s.state)
    }

    @Test
    fun testDeepDozingExitsToRestoringOnScreenOn() {
        val dozing = DozeSnapshot(
            state = DozeState.DEEP_DOZING,
            generation = 1L,
            wakeLockHeld = true,
            initialSnapshot = DeviceRadioSnapshot(wifiEnabled = true, modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI))
        )

        val s = DozeStateMachine.reduce(dozing, DozeEvent.ScreenTurnedOn(currentTimeMs = 5000L))
        assertEquals(DozeState.RESTORING, s.state)
        assertFalse(s.wakeLockHeld)
        assertTrue(s.isScreenOn)
        assertEquals("ScreenOn", s.lastExitReason)
    }

    @Test
    fun testDeepDozingExitsToRestoringOnPowerConnected() {
        val dozing = DozeSnapshot(
            state = DozeState.DEEP_DOZING,
            generation = 1L,
            disableWhenCharging = true,
            wakeLockHeld = true
        )

        val s = DozeStateMachine.reduce(dozing, DozeEvent.PowerConnected(currentTimeMs = 5000L))
        assertEquals(DozeState.RESTORING, s.state)
        assertFalse(s.wakeLockHeld)
        assertTrue(s.isCharging)
        assertEquals("Charging", s.lastExitReason)
    }

    @Test
    fun testMaintenanceWindowCycle() {
        val dozing = DozeSnapshot(
            state = DozeState.DEEP_DOZING,
            generation = 1L,
            currentIdleMode = "IDLE"
        )

        var s = DozeStateMachine.reduce(dozing, DozeEvent.MaintenanceWindowEntered(generation = 1L))
        assertEquals(DozeState.MAINTENANCE_WINDOW, s.state)
        assertEquals("IDLE_MAINTENANCE", s.currentIdleMode)

        s = DozeStateMachine.reduce(s, DozeEvent.MaintenanceWindowExited(generation = 1L))
        assertEquals(DozeState.DEEP_DOZING, s.state)
        assertEquals("IDLE", s.currentIdleMode)
    }

    @Test
    fun testMaintenanceWindowExitsToRestoringOnScreenOn() {
        val mw = DozeSnapshot(
            state = DozeState.MAINTENANCE_WINDOW,
            generation = 1L,
            initialSnapshot = DeviceRadioSnapshot(modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI))
        )

        val s = DozeStateMachine.reduce(mw, DozeEvent.ScreenTurnedOn(currentTimeMs = 10000L))
        assertEquals(DozeState.RESTORING, s.state)
        assertEquals("ScreenOnDuringMaintenance", s.lastExitReason)
    }

    @Test
    fun testRestoringCompletedTransitionsToInactiveAndClearsSnapshot() {
        val restoring = DozeSnapshot(
            state = DozeState.RESTORING,
            generation = 1L,
            initialSnapshot = DeviceRadioSnapshot(wifiEnabled = true, modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI)),
            wakeLockHeld = true
        )

        val s = DozeStateMachine.reduce(
            restoring,
            DozeEvent.RestoringCompleted(generation = 1L, restoredSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI))
        )

        assertEquals(DozeState.INACTIVE, s.state)
        assertNull(s.initialSnapshot)
        assertFalse(s.wakeLockHeld)
    }

    @Test
    fun testSelectiveRestorationOnlyRestoresModifiedSettings() {
        // Feature only turned OFF WiFi. Mobile Data and Bluetooth were untouched.
        val captured = DeviceRadioSnapshot(
            wifiEnabled = true,
            mobileDataEnabled = true,
            bluetoothEnabled = true,
            modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI)
        )

        // Live state: WiFi is off, Data is off, Bluetooth is off
        val liveState = DeviceRadioSnapshot(
            wifiEnabled = false,
            mobileDataEnabled = false,
            bluetoothEnabled = false
        )

        val needed = captured.computeRequiredRestorations(liveState)

        // ONLY WiFi must be restored. Untouched radios must NOT be toggled!
        assertEquals(setOf(DeviceRadioSnapshot.SETTING_WIFI), needed)
    }

    @Test
    fun testExternalUserChangesAreNotClobberedDuringRestoration() {
        // Feature turned OFF WiFi and turned OFF Bluetooth
        val captured = DeviceRadioSnapshot(
            wifiEnabled = true,
            bluetoothEnabled = true,
            modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI, DeviceRadioSnapshot.SETTING_BLUETOOTH)
        )

        // While in doze, the user manually turned WiFi back ON, but Bluetooth remained OFF
        val liveState = DeviceRadioSnapshot(
            wifiEnabled = true, // User already turned it on!
            bluetoothEnabled = false
        )

        val needed = captured.computeRequiredRestorations(liveState)

        // WiFi must NOT be touched; Bluetooth must be restored
        assertEquals(setOf(DeviceRadioSnapshot.SETTING_BLUETOOTH), needed)
        assertFalse(needed.contains(DeviceRadioSnapshot.SETTING_WIFI))
    }

    @Test
    fun testWakeLockReleasedTracking() {
        val s = DozeSnapshot(
            state = DozeState.ENTERING_DOZE,
            generation = 1L,
            wakeLockHeld = true
        )

        val released = DozeStateMachine.reduce(s, DozeEvent.WakeLockReleased(generation = 1L))
        assertFalse(released.wakeLockHeld)
    }

    @Test
    fun testStaleGenerationCallbacksAreRejected() {
        val s = DozeSnapshot(
            state = DozeState.ENTERING_DOZE,
            generation = 5L
        )

        // Callback from old generation 4L
        val stale = DozeStateMachine.reduce(
            s,
            DozeEvent.DozeApplied(generation = 4L, idleMode = "IDLE")
        )

        assertEquals(s, stale)
        assertEquals(5L, stale.generation)
        assertEquals(DozeState.ENTERING_DOZE, stale.state)
    }

    @Test
    fun testOperationFailedDuringEnteringDozeTriggersRestoreIfModified() {
        val entering = DozeSnapshot(
            state = DozeState.ENTERING_DOZE,
            generation = 1L,
            initialSnapshot = DeviceRadioSnapshot(modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI)),
            wakeLockHeld = true
        )

        val failed = DozeStateMachine.reduce(
            entering,
            DozeEvent.OperationFailed(generation = 1L, reason = "Shell dumpsys failure")
        )

        assertEquals(DozeState.RESTORING, failed.state)
        assertFalse(failed.wakeLockHeld)
    }

    @Test
    fun testServiceStoppedDuringDozeTriggersRestoring() {
        val dozing = DozeSnapshot(
            state = DozeState.DEEP_DOZING,
            generation = 1L,
            initialSnapshot = DeviceRadioSnapshot(modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI))
        )

        val s = DozeStateMachine.reduce(dozing, DozeEvent.ServiceStopped)
        assertEquals(DozeState.RESTORING, s.state)
        assertEquals("ServiceStopped", s.lastExitReason)
    }

    @Test
    fun testResetRestoresCleanInactiveState() {
        val dirty = DozeSnapshot(
            state = DozeState.DEEP_DOZING,
            generation = 4L,
            wakeLockHeld = true,
            initialSnapshot = DeviceRadioSnapshot(modifiedSettings = setOf(DeviceRadioSnapshot.SETTING_WIFI))
        )

        val reset = DozeStateMachine.reduce(dirty, DozeEvent.Reset)
        assertEquals(DozeState.INACTIVE, reset.state)
        assertEquals(5L, reset.generation)
        assertFalse(reset.wakeLockHeld)
        assertNull(reset.initialSnapshot)
    }

    @Test
    fun testCoordinatorAtomicTransitionsAndListeners() {
        val coordinator = DozeCoordinator()
        val eventsObserved = mutableListOf<DozeEvent>()

        coordinator.addListener { _, _, event ->
            eventsObserved.add(event)
        }

        coordinator.transition(
            DozeEvent.ScreenTurnedOff(
                isCharging = false,
                isCallActive = false,
                delayMs = 0L,
                currentTimeMs = 1000L
            )
        )
        assertEquals(DozeState.ENTERING_DOZE, coordinator.state)

        coordinator.reset()
        assertEquals(DozeState.INACTIVE, coordinator.state)

        assertEquals(2, eventsObserved.size)
        assertTrue(eventsObserved[0] is DozeEvent.ScreenTurnedOff)
        assertTrue(eventsObserved[1] is DozeEvent.Reset)
    }
}
