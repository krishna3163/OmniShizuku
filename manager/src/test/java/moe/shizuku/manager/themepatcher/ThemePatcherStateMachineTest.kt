package moe.shizuku.manager.themepatcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ThemePatcherStateMachineTest {

    @Test
    fun initialStateIsIdle() {
        val initial = ThemePatcherSnapshot()
        assertEquals(ThemePatcherState.IDLE, initial.state)
        assertEquals(0L, initial.generation)
        assertTrue(initial.storePackages.isEmpty())
        assertTrue(initial.detectedItems.isEmpty())
        assertFalse(initial.isRunning)
        assertFalse(initial.isTerminal)
        assertNull(initial.lastError)
    }

    @Test
    fun startRequestedTransitionsToWatchingForTrial() {
        val initial = ThemePatcherSnapshot()
        val event = ThemePatcherEvent.StartRequested(
            generation = 1L,
            storePackages = listOf("com.heytap.themestore"),
            timeoutDeadlineMs = 300_000L,
            currentTimeMs = 1000L
        )

        val next = ThemePatcherStateMachine.reduce(initial, event)
        assertEquals(ThemePatcherState.WATCHING_FOR_TRIAL, next.state)
        assertEquals(1L, next.generation)
        assertEquals(listOf("com.heytap.themestore"), next.storePackages)
        assertEquals(300_000L, next.timeoutDeadlineMs)
        assertEquals(0, next.pollCount)
        assertTrue(next.isRunning)
        assertFalse(next.isTerminal)
    }

    @Test
    fun staleGenerationStartRequestedRejected() {
        val running = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 2L
        )
        val staleEvent = ThemePatcherEvent.StartRequested(
            generation = 1L,
            storePackages = listOf("com.stale"),
            timeoutDeadlineMs = 500L,
            currentTimeMs = 100L
        )

        val result = ThemePatcherStateMachine.reduce(running, staleEvent)
        assertEquals(running, result)
    }

    @Test
    fun trialPolledWithoutTrialIncrementsPollCount() {
        val watching = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 1L,
            timeoutDeadlineMs = 100_000L,
            pollCount = 2
        )
        val event = ThemePatcherEvent.TrialPolled(
            generation = 1L,
            hasTrial = false,
            items = emptyList(),
            currentTimeMs = 5000L
        )

        val next = ThemePatcherStateMachine.reduce(watching, event)
        assertEquals(ThemePatcherState.WATCHING_FOR_TRIAL, next.state)
        assertEquals(3, next.pollCount)
    }

    @Test
    fun trialPolledWithTrialTransitionsToTrialDetected() {
        val watching = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 1L,
            timeoutDeadlineMs = 100_000L,
            pollCount = 5
        )
        val items = listOf("Theme : Cyberpunk", "Font : Roboto")
        val event = ThemePatcherEvent.TrialPolled(
            generation = 1L,
            hasTrial = true,
            items = items,
            currentTimeMs = 10_000L
        )

        val next = ThemePatcherStateMachine.reduce(watching, event)
        assertEquals(ThemePatcherState.TRIAL_DETECTED, next.state)
        assertEquals(items, next.detectedItems)
        assertEquals(6, next.pollCount)
    }

    @Test
    fun trialPolledTimeoutByDeadline() {
        val watching = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 1L,
            timeoutDeadlineMs = 50_000L,
            pollCount = 10
        )
        val event = ThemePatcherEvent.TrialPolled(
            generation = 1L,
            hasTrial = false,
            items = emptyList(),
            currentTimeMs = 50_001L // Expired
        )

        val next = ThemePatcherStateMachine.reduce(watching, event)
        assertEquals(ThemePatcherState.TIMED_OUT, next.state)
        assertTrue(next.isTerminal)
        assertFalse(next.isRunning)
    }

    @Test
    fun trialPolledTimeoutByMaxPollCount() {
        val watching = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 1L,
            timeoutDeadlineMs = 1_000_000L,
            pollCount = 84,
            maxPollCount = 85
        )
        val event = ThemePatcherEvent.TrialPolled(
            generation = 1L,
            hasTrial = false,
            items = emptyList(),
            currentTimeMs = 10_000L
        )

        val next = ThemePatcherStateMachine.reduce(watching, event)
        assertEquals(ThemePatcherState.TIMED_OUT, next.state)
        assertEquals(85, next.pollCount)
        assertTrue(next.isTerminal)
    }

    @Test
    fun staleGenerationTrialPolledIgnored() {
        val watching = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 3L,
            pollCount = 2
        )
        val staleEvent = ThemePatcherEvent.TrialPolled(
            generation = 2L,
            hasTrial = true,
            items = listOf("Theme : Stale"),
            currentTimeMs = 5000L
        )

        val next = ThemePatcherStateMachine.reduce(watching, staleEvent)
        assertEquals(watching, next)
    }

    @Test
    fun storesTerminatedTransitionsToApplyingPatch() {
        val detected = ThemePatcherSnapshot(
            state = ThemePatcherState.TRIAL_DETECTED,
            generation = 1L,
            detectedItems = listOf("Theme : Neon")
        )
        val event = ThemePatcherEvent.StoresTerminated(generation = 1L)

        val next = ThemePatcherStateMachine.reduce(detected, event)
        assertEquals(ThemePatcherState.APPLYING_PATCH, next.state)
    }

    @Test
    fun patchAppliedSuccessTransitionsToVerifying() {
        val applying = ThemePatcherSnapshot(
            state = ThemePatcherState.APPLYING_PATCH,
            generation = 1L
        )
        val event = ThemePatcherEvent.PatchApplied(
            generation = 1L,
            success = true
        )

        val next = ThemePatcherStateMachine.reduce(applying, event)
        assertEquals(ThemePatcherState.VERIFYING, next.state)
    }

    @Test
    fun patchAppliedFailureTransitionsToFailed() {
        val applying = ThemePatcherSnapshot(
            state = ThemePatcherState.APPLYING_PATCH,
            generation = 1L
        )
        val event = ThemePatcherEvent.PatchApplied(
            generation = 1L,
            success = false,
            error = "Shell command error"
        )

        val next = ThemePatcherStateMachine.reduce(applying, event)
        assertEquals(ThemePatcherState.FAILED, next.state)
        assertEquals("Shell command error", next.lastError)
        assertTrue(next.isTerminal)
    }

    @Test
    fun verificationCompletedSuccessTransitionsToCompleted() {
        val verifying = ThemePatcherSnapshot(
            state = ThemePatcherState.VERIFYING,
            generation = 1L
        )
        val event = ThemePatcherEvent.VerificationCompleted(
            generation = 1L,
            verified = true
        )

        val next = ThemePatcherStateMachine.reduce(verifying, event)
        assertEquals(ThemePatcherState.COMPLETED, next.state)
        assertTrue(next.isTerminal)
        assertFalse(next.isRunning)
    }

    @Test
    fun verificationCompletedFailureTransitionsToFailed() {
        val verifying = ThemePatcherSnapshot(
            state = ThemePatcherState.VERIFYING,
            generation = 1L
        )
        val event = ThemePatcherEvent.VerificationCompleted(
            generation = 1L,
            verified = false
        )

        val next = ThemePatcherStateMachine.reduce(verifying, event)
        assertEquals(ThemePatcherState.FAILED, next.state)
        assertTrue(next.lastError!!.contains("Verification failed"))
        assertTrue(next.isTerminal)
    }

    @Test
    fun cancelRequestedFromActiveStateTransitionsToCancelled() {
        val active = ThemePatcherSnapshot(
            state = ThemePatcherState.WATCHING_FOR_TRIAL,
            generation = 1L
        )
        val next = ThemePatcherStateMachine.reduce(active, ThemePatcherEvent.CancelRequested)
        assertEquals(ThemePatcherState.CANCELLED, next.state)
        assertTrue(next.isTerminal)
    }

    @Test
    fun cancelRequestedFromTerminalStateIsIgnored() {
        val completed = ThemePatcherSnapshot(
            state = ThemePatcherState.COMPLETED,
            generation = 1L
        )
        val next = ThemePatcherStateMachine.reduce(completed, ThemePatcherEvent.CancelRequested)
        assertEquals(completed, next)
    }

    @Test
    fun resetReturnsToFreshIdle() {
        val terminal = ThemePatcherSnapshot(
            state = ThemePatcherState.COMPLETED,
            generation = 5L,
            pollCount = 20,
            detectedItems = listOf("Theme : A")
        )
        val next = ThemePatcherStateMachine.reduce(terminal, ThemePatcherEvent.Reset)
        assertEquals(ThemePatcherState.IDLE, next.state)
        assertEquals(0L, next.generation)
        assertEquals(0, next.pollCount)
    }

    @Test
    fun coordinatorAtomicTransitionsAndListeners() {
        val coordinator = ThemePatcherCoordinator()
        val transitionCount = AtomicInteger(0)

        coordinator.addListener { old, new, event ->
            transitionCount.incrementAndGet()
        }

        val gen = coordinator.nextGeneration()
        assertEquals(1L, gen)

        coordinator.transition(
            ThemePatcherEvent.StartRequested(
                generation = gen,
                storePackages = listOf("com.oppo.themestore"),
                timeoutDeadlineMs = 5000L,
                currentTimeMs = 100L
            )
        )
        assertEquals(ThemePatcherState.WATCHING_FOR_TRIAL, coordinator.state)

        coordinator.transition(
            ThemePatcherEvent.TrialPolled(
                generation = gen,
                hasTrial = true,
                items = listOf("Theme : Custom"),
                currentTimeMs = 200L
            )
        )
        assertEquals(ThemePatcherState.TRIAL_DETECTED, coordinator.state)

        coordinator.cancel()
        assertEquals(ThemePatcherState.CANCELLED, coordinator.state)
        assertEquals(3, transitionCount.get())
    }
}
