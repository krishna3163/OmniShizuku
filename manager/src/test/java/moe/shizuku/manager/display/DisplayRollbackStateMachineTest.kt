package moe.shizuku.manager.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class DisplayRollbackStateMachineTest {

    private val sampleBefore = DisplaySnapshot(
        size = "1080x2400",
        density = 420,
        minRefreshRate = "60",
        peakRefreshRate = "120"
    )

    private val sampleTarget = DisplayChange(
        size = "1440x3200",
        density = 560
    )

    @Test
    fun initialStateIsIdle() {
        val initial = DisplayRollbackSnapshot()
        assertEquals(DisplayRollbackState.IDLE, initial.state)
        assertEquals(0L, initial.generation)
        assertNull(initial.targetChange)
        assertNull(initial.beforeSnapshot)
        assertFalse(initial.isAwaitingConfirmation)
        assertTrue(initial.isTerminalOrIdle)
    }

    @Test
    fun startChangeTransitionsToApplying() {
        val initial = DisplayRollbackSnapshot()
        val event = DisplayRollbackEvent.StartChange(
            generation = 1L,
            target = sampleTarget,
            before = sampleBefore,
            timeoutMillis = 15_000L,
            currentTimeMs = 1000L
        )

        val next = DisplayRollbackStateMachine.reduce(initial, event)
        assertEquals(DisplayRollbackState.APPLYING, next.state)
        assertEquals(1L, next.generation)
        assertEquals(sampleTarget, next.targetChange)
        assertEquals(sampleBefore, next.beforeSnapshot)
        assertEquals(16_000L, next.deadlineMonotonicMs)
        assertEquals(15_000L, next.remainingMillis)
        assertFalse(next.isTerminalOrIdle)
    }

    @Test
    fun startChangeRejectedIfAlreadyInFlight() {
        val inFlight = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 1L
        )
        val event = DisplayRollbackEvent.StartChange(
            generation = 2L,
            target = sampleTarget,
            before = sampleBefore,
            timeoutMillis = 10_000L,
            currentTimeMs = 2000L
        )

        val result = DisplayRollbackStateMachine.reduce(inFlight, event)
        assertEquals(inFlight, result)
    }

    @Test
    fun applySucceededTransitionsToAwaitingConfirmation() {
        val applying = DisplayRollbackSnapshot(
            state = DisplayRollbackState.APPLYING,
            generation = 1L
        )
        val next = DisplayRollbackStateMachine.reduce(applying, DisplayRollbackEvent.ApplySucceeded(1L))
        assertEquals(DisplayRollbackState.AWAITING_CONFIRMATION, next.state)
        assertTrue(next.isAwaitingConfirmation)
    }

    @Test
    fun applyFailedTransitionsToFailed() {
        val applying = DisplayRollbackSnapshot(
            state = DisplayRollbackState.APPLYING,
            generation = 1L
        )
        val next = DisplayRollbackStateMachine.reduce(
            applying,
            DisplayRollbackEvent.ApplyFailed(1L, "Shell failure")
        )
        assertEquals(DisplayRollbackState.FAILED, next.state)
        assertEquals("Shell failure", next.lastError)
        assertTrue(next.isTerminalOrIdle)
    }

    @Test
    fun tickUpdatesRemainingMillis() {
        val awaiting = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 1L,
            deadlineMonotonicMs = 20_000L,
            remainingMillis = 10_000L
        )
        val next = DisplayRollbackStateMachine.reduce(awaiting, DisplayRollbackEvent.Tick(15_000L))
        assertEquals(5000L, next.remainingMillis)
        assertEquals(DisplayRollbackState.AWAITING_CONFIRMATION, next.state)
    }

    @Test
    fun tickAtOrPastDeadlineTransitionsToRollingBack() {
        val awaiting = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 1L,
            deadlineMonotonicMs = 20_000L
        )
        val next = DisplayRollbackStateMachine.reduce(awaiting, DisplayRollbackEvent.Tick(20_001L))
        assertEquals(DisplayRollbackState.ROLLING_BACK, next.state)
        assertEquals(0L, next.remainingMillis)
    }

    @Test
    fun confirmRequestedWithinDeadlineTransitionsToConfirmed() {
        val awaiting = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 1L,
            deadlineMonotonicMs = 30_000L
        )
        val event = DisplayRollbackEvent.ConfirmRequested(generation = 1L, currentTimeMs = 25_000L)
        val next = DisplayRollbackStateMachine.reduce(awaiting, event)
        assertEquals(DisplayRollbackState.CONFIRMED, next.state)
        assertTrue(next.isTerminalOrIdle)
    }

    @Test
    fun confirmRequestedPastDeadlineTransitionsToRollingBack() {
        val awaiting = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 1L,
            deadlineMonotonicMs = 30_000L
        )
        val event = DisplayRollbackEvent.ConfirmRequested(generation = 1L, currentTimeMs = 30_001L)
        val next = DisplayRollbackStateMachine.reduce(awaiting, event)
        assertEquals(DisplayRollbackState.ROLLING_BACK, next.state)
    }

    @Test
    fun rejectRequestedTransitionsToRollingBack() {
        val awaiting = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 1L
        )
        val next = DisplayRollbackStateMachine.reduce(awaiting, DisplayRollbackEvent.RejectRequested(1L))
        assertEquals(DisplayRollbackState.ROLLING_BACK, next.state)
    }

    @Test
    fun rollbackSucceededTransitionsToRestored() {
        val rollingBack = DisplayRollbackSnapshot(
            state = DisplayRollbackState.ROLLING_BACK,
            generation = 1L
        )
        val next = DisplayRollbackStateMachine.reduce(rollingBack, DisplayRollbackEvent.RollbackSucceeded(1L))
        assertEquals(DisplayRollbackState.RESTORED, next.state)
        assertTrue(next.isTerminalOrIdle)
    }

    @Test
    fun rollbackFailedTransitionsToFailed() {
        val rollingBack = DisplayRollbackSnapshot(
            state = DisplayRollbackState.ROLLING_BACK,
            generation = 1L
        )
        val next = DisplayRollbackStateMachine.reduce(
            rollingBack,
            DisplayRollbackEvent.RollbackFailed(1L, "Restore command failed")
        )
        assertEquals(DisplayRollbackState.FAILED, next.state)
        assertEquals("Restore command failed", next.lastError)
        assertTrue(next.isTerminalOrIdle)
    }

    @Test
    fun staleGenerationEventsIgnored() {
        val awaiting = DisplayRollbackSnapshot(
            state = DisplayRollbackState.AWAITING_CONFIRMATION,
            generation = 2L
        )
        val staleConfirm = DisplayRollbackEvent.ConfirmRequested(generation = 1L, currentTimeMs = 1000L)
        val result = DisplayRollbackStateMachine.reduce(awaiting, staleConfirm)
        assertEquals(awaiting, result)
    }

    @Test
    fun resetCleansesToIdle() {
        val confirmed = DisplayRollbackSnapshot(
            state = DisplayRollbackState.CONFIRMED,
            generation = 5L,
            targetChange = sampleTarget
        )
        val next = DisplayRollbackStateMachine.reduce(confirmed, DisplayRollbackEvent.Reset)
        assertEquals(DisplayRollbackSnapshot(), next)
    }

    @Test
    fun coordinatorAtomicTransitionsAndListeners() {
        val coordinator = DisplayRollbackCoordinator()
        val transitionCount = AtomicInteger(0)

        coordinator.addListener { old, new, event ->
            transitionCount.incrementAndGet()
        }

        val gen = coordinator.nextGeneration()
        assertEquals(1L, gen)

        coordinator.transition(
            DisplayRollbackEvent.StartChange(
                generation = gen,
                target = sampleTarget,
                before = sampleBefore,
                timeoutMillis = 10_000L,
                currentTimeMs = 100L
            )
        )
        assertEquals(DisplayRollbackState.APPLYING, coordinator.state)

        coordinator.transition(DisplayRollbackEvent.ApplySucceeded(gen))
        assertEquals(DisplayRollbackState.AWAITING_CONFIRMATION, coordinator.state)

        coordinator.transition(DisplayRollbackEvent.ConfirmRequested(gen, currentTimeMs = 500L))
        assertEquals(DisplayRollbackState.CONFIRMED, coordinator.state)
        assertEquals(3, transitionCount.get())
    }
}
