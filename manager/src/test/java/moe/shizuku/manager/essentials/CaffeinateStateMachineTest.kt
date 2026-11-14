package moe.shizuku.manager.essentials

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class CaffeinateStateMachineTest {

    @Test
    fun initialStateIsInactive() {
        val initial = CaffeinateSnapshot()
        assertEquals(CaffeinateState.INACTIVE, initial.state)
        assertEquals(0L, initial.generation)
        assertEquals(5, initial.selectedTimeoutMinutes)
        assertEquals(0, initial.selectionSecondsRemaining)
        assertFalse(initial.isActive)
        assertFalse(initial.isStarting)
        assertFalse(initial.isRunning)
        assertFalse(initial.wakeLockHeld)
        assertTrue(initial.isTerminalOrIdle)
    }

    @Test
    fun startSelectionTransitionsToCountdownSelection() {
        val initial = CaffeinateSnapshot()
        val event = CaffeinateEvent.StartSelection(
            generation = 1L,
            timeoutMinutes = 10,
            countdownSeconds = 5,
            skipCountdown = false,
            abortOnScreenOff = true,
            currentTimeMs = 1000L
        )

        val next = CaffeinateStateMachine.reduce(initial, event)
        assertEquals(CaffeinateState.COUNTDOWN_SELECTION, next.state)
        assertEquals(1L, next.generation)
        assertEquals(10, next.selectedTimeoutMinutes)
        assertEquals(5, next.selectionSecondsRemaining)
        assertTrue(next.isStarting)
        assertFalse(next.isActive)
        assertTrue(next.isRunning)
        assertFalse(next.wakeLockHeld)
    }

    @Test
    fun startSelectionWithSkipCountdownTransitionsDirectlyToActive() {
        val initial = CaffeinateSnapshot()
        val event = CaffeinateEvent.StartSelection(
            generation = 1L,
            timeoutMinutes = 30,
            countdownSeconds = 5,
            skipCountdown = true,
            abortOnScreenOff = true,
            currentTimeMs = 50_000L
        )

        val next = CaffeinateStateMachine.reduce(initial, event)
        assertEquals(CaffeinateState.ACTIVE, next.state)
        assertEquals(1L, next.generation)
        assertEquals(30, next.selectedTimeoutMinutes)
        assertEquals(0, next.selectionSecondsRemaining)
        assertTrue(next.isActive)
        assertFalse(next.isStarting)
        assertTrue(next.wakeLockHeld)
        assertEquals(50_000L, next.startedAtMonotonicMs)
        assertEquals(50_000L + (30 * 60 * 1000L), next.deadlineMonotonicMs)
        assertEquals(30 * 60 * 1000L, next.remainingActiveMillis)
    }

    @Test
    fun cycleTimeoutCyclesThroughPresets() {
        val presets = listOf(5, 10, 30, -1)
        val selection = CaffeinateSnapshot(
            state = CaffeinateState.COUNTDOWN_SELECTION,
            selectedTimeoutMinutes = 5,
            selectionSecondsRemaining = 2
        )

        val cycle1 = CaffeinateStateMachine.reduce(
            selection,
            CaffeinateEvent.CycleTimeout(presets, resetCountdownSeconds = 5)
        )
        assertEquals(10, cycle1.selectedTimeoutMinutes)
        assertEquals(5, cycle1.selectionSecondsRemaining)

        val cycle2 = CaffeinateStateMachine.reduce(
            cycle1,
            CaffeinateEvent.CycleTimeout(presets, resetCountdownSeconds = 5)
        )
        assertEquals(30, cycle2.selectedTimeoutMinutes)

        val cycle3 = CaffeinateStateMachine.reduce(
            cycle2,
            CaffeinateEvent.CycleTimeout(presets, resetCountdownSeconds = 5)
        )
        assertEquals(-1, cycle3.selectedTimeoutMinutes)

        val cycle4 = CaffeinateStateMachine.reduce(
            cycle3,
            CaffeinateEvent.CycleTimeout(presets, resetCountdownSeconds = 5)
        )
        assertEquals(5, cycle4.selectedTimeoutMinutes)
    }

    @Test
    fun selectionTickDecrementsRemainingSeconds() {
        val selection = CaffeinateSnapshot(
            state = CaffeinateState.COUNTDOWN_SELECTION,
            selectionSecondsRemaining = 5
        )
        val next = CaffeinateStateMachine.reduce(selection, CaffeinateEvent.SelectionTick(4))
        assertEquals(4, next.selectionSecondsRemaining)
    }

    @Test
    fun selectionConfirmedTransitionsToActive() {
        val selection = CaffeinateSnapshot(
            state = CaffeinateState.COUNTDOWN_SELECTION,
            generation = 2L,
            selectedTimeoutMinutes = 10,
            selectionSecondsRemaining = 0
        )
        val event = CaffeinateEvent.SelectionConfirmed(currentTimeMs = 10_000L)

        val next = CaffeinateStateMachine.reduce(selection, event)
        assertEquals(CaffeinateState.ACTIVE, next.state)
        assertTrue(next.isActive)
        assertTrue(next.wakeLockHeld)
        assertEquals(10_000L, next.startedAtMonotonicMs)
        assertEquals(10_000L + (10 * 60 * 1000L), next.deadlineMonotonicMs)
        assertEquals(10 * 60 * 1000L, next.remainingActiveMillis)
    }

    @Test
    fun wakeLockStateChangedUpdatesSnapshot() {
        val active = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 5L,
            wakeLockHeld = false
        )
        val event = CaffeinateEvent.WakeLockStateChanged(generation = 5L, held = true)
        val next = CaffeinateStateMachine.reduce(active, event)
        assertTrue(next.wakeLockHeld)

        val staleEvent = CaffeinateEvent.WakeLockStateChanged(generation = 4L, held = false)
        val staleResult = CaffeinateStateMachine.reduce(next, staleEvent)
        assertEquals(next, staleResult)
    }

    @Test
    fun activeTickUpdatesRemainingMillis() {
        val active = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 1L,
            remainingActiveMillis = 300_000L
        )
        val next = CaffeinateStateMachine.reduce(active, CaffeinateEvent.ActiveTick(250_000L))
        assertEquals(250_000L, next.remainingActiveMillis)
    }

    @Test
    fun activeExpiredTransitionsToExpired() {
        val active = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 1L,
            wakeLockHeld = true,
            remainingActiveMillis = 1000L
        )
        val next = CaffeinateStateMachine.reduce(active, CaffeinateEvent.ActiveExpired(generation = 1L))
        assertEquals(CaffeinateState.EXPIRED, next.state)
        assertFalse(next.isActive)
        assertFalse(next.wakeLockHeld)
        assertEquals(0L, next.remainingActiveMillis)
        assertTrue(next.isTerminalOrIdle)
    }

    @Test
    fun screenTurnedOffWhenAbortEnabledTransitionsToAborted() {
        val active = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 1L,
            wakeLockHeld = true,
            abortOnScreenOff = true
        )
        val next = CaffeinateStateMachine.reduce(active, CaffeinateEvent.ScreenTurnedOff(currentTimeMs = 2000L))
        assertEquals(CaffeinateState.ABORTED, next.state)
        assertFalse(next.wakeLockHeld)
        assertFalse(next.isActive)
        assertTrue(next.isTerminalOrIdle)
    }

    @Test
    fun screenTurnedOffWhenAbortDisabledIsIgnored() {
        val active = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 1L,
            wakeLockHeld = true,
            abortOnScreenOff = false
        )
        val next = CaffeinateStateMachine.reduce(active, CaffeinateEvent.ScreenTurnedOff(currentTimeMs = 2000L))
        assertEquals(active, next)
        assertTrue(next.wakeLockHeld)
    }

    @Test
    fun stopRequestedFromAnyStateTransitionsToInactive() {
        val active = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 1L,
            wakeLockHeld = true,
            remainingActiveMillis = 50_000L
        )
        val next = CaffeinateStateMachine.reduce(active, CaffeinateEvent.StopRequested("User clicked stop"))
        assertEquals(CaffeinateState.INACTIVE, next.state)
        assertFalse(next.wakeLockHeld)
        assertEquals(0L, next.remainingActiveMillis)
        assertFalse(next.isRunning)
    }

    @Test
    fun resetReturnsToCleanDefault() {
        val modified = CaffeinateSnapshot(
            state = CaffeinateState.ACTIVE,
            generation = 10L,
            wakeLockHeld = true,
            selectedTimeoutMinutes = 60
        )
        val next = CaffeinateStateMachine.reduce(modified, CaffeinateEvent.Reset)
        assertEquals(CaffeinateSnapshot(), next)
    }

    @Test
    fun coordinatorAtomicTransitionsAndListenerNotifications() {
        val coordinator = CaffeinateCoordinator()
        val transitionCount = AtomicInteger(0)

        coordinator.addListener { old, new, event ->
            transitionCount.incrementAndGet()
        }

        val gen = coordinator.nextGeneration()
        assertEquals(1L, gen)

        coordinator.transition(
            CaffeinateEvent.StartSelection(
                generation = gen,
                timeoutMinutes = 10,
                countdownSeconds = 5,
                skipCountdown = false,
                currentTimeMs = 100L
            )
        )
        assertEquals(CaffeinateState.COUNTDOWN_SELECTION, coordinator.state)

        coordinator.transition(CaffeinateEvent.SelectionConfirmed(currentTimeMs = 200L))
        assertEquals(CaffeinateState.ACTIVE, coordinator.state)
        assertTrue(coordinator.isActive)

        coordinator.stop()
        assertEquals(CaffeinateState.INACTIVE, coordinator.state)
        assertFalse(coordinator.isActive)
        assertEquals(3, transitionCount.get())
    }
}
