package moe.shizuku.manager.service

import org.junit.Assert.assertEquals
import org.junit.Test

class WatchdogRecoveryMachineTest {

    @Test
    fun crashAndRecoveryFlow() {
        var s = WatchdogRecoveryState.IDLE
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.CrashDetected)
        assertEquals(WatchdogRecoveryState.AWAITING_RECOVERY, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartStarted)
        assertEquals(WatchdogRecoveryState.RESTARTING, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartAttemptFailed)
        assertEquals(WatchdogRecoveryState.PENDING_RESTART, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartStarted)
        assertEquals(WatchdogRecoveryState.RESTARTING, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.ServerRunning)
        assertEquals(WatchdogRecoveryState.IDLE, s)
    }

    @Test
    fun breakerOpensAndBlocksFurtherTransitionsExceptRecoveryReset() {
        var s = WatchdogRecoveryState.AWAITING_RECOVERY
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.BreakerOpened)
        assertEquals(WatchdogRecoveryState.GAVE_UP, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.CrashDetected)
        assertEquals(WatchdogRecoveryState.GAVE_UP, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartStarted)
        assertEquals(WatchdogRecoveryState.GAVE_UP, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartDeferred)
        assertEquals(WatchdogRecoveryState.GAVE_UP, s)
        // genuine recovery or manual reset returns to idle
        assertEquals(WatchdogRecoveryState.IDLE, WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.ServerRunning))
        assertEquals(WatchdogRecoveryState.IDLE, WatchdogRecoveryMachine.reduce(WatchdogRecoveryState.GAVE_UP, WatchdogRecoveryEvent.Reset))
    }

    @Test
    fun manualStopResetsToIdle() {
        assertEquals(WatchdogRecoveryState.IDLE, WatchdogRecoveryMachine.reduce(WatchdogRecoveryState.PENDING_RESTART, WatchdogRecoveryEvent.ManualStop))
        assertEquals(WatchdogRecoveryState.IDLE, WatchdogRecoveryMachine.reduce(WatchdogRecoveryState.AWAITING_RECOVERY, WatchdogRecoveryEvent.ManualStop))
    }

    @Test
    fun deterministicStressSequence() {
        fun run(seed: Int): WatchdogRecoveryState {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            val list = listOf(
                WatchdogRecoveryEvent.CrashDetected,
                WatchdogRecoveryEvent.ServerRunning,
                WatchdogRecoveryEvent.ManualStop,
                WatchdogRecoveryEvent.RestartDeferred,
                WatchdogRecoveryEvent.RestartStarted,
                WatchdogRecoveryEvent.RestartAttemptFailed,
                WatchdogRecoveryEvent.BreakerOpened,
                WatchdogRecoveryEvent.Reset
            )
            var s = WatchdogRecoveryState.IDLE
            repeat(300) { s = WatchdogRecoveryMachine.reduce(s, list[next() % list.size]) }
            return s
        }
        assertEquals(run(99), run(99))
        run(1)
    }
}

    @Test
    fun duplicateCrashAndPendingRestartScenarios() {
        // duplicate crash notifications while awaiting/restarting keep recovery intent (not gave up)
        var s = WatchdogRecoveryState.IDLE
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.CrashDetected)
        assertEquals(WatchdogRecoveryState.AWAITING_RECOVERY, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.CrashDetected)
        assertEquals(WatchdogRecoveryState.AWAITING_RECOVERY, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartStarted)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.CrashDetected)
        assertEquals(WatchdogRecoveryState.AWAITING_RECOVERY, s)
        // deferred/pending then restart attempt failure keeps pending
        s = WatchdogRecoveryMachine.reduce(WatchdogRecoveryState.PENDING_RESTART, WatchdogRecoveryEvent.RestartAttemptFailed)
        assertEquals(WatchdogRecoveryState.PENDING_RESTART, s)
    }

    @Test
    fun timeoutThenNewAttemptAndGenuineRecoveryBehavior() {
        // modeled as failed attempt -> pending -> new start -> running resets
        var s = WatchdogRecoveryState.RESTARTING
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartAttemptFailed)
        assertEquals(WatchdogRecoveryState.PENDING_RESTART, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.RestartStarted)
        assertEquals(WatchdogRecoveryState.RESTARTING, s)
        s = WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.ServerRunning)
        assertEquals(WatchdogRecoveryState.IDLE, s)
        // breaker then genuine recovery clears gave-up at reducer level
        s = WatchdogRecoveryMachine.reduce(WatchdogRecoveryState.RESTARTING, WatchdogRecoveryEvent.BreakerOpened)
        assertEquals(WatchdogRecoveryState.GAVE_UP, WatchdogRecoveryMachine.reduce(s, WatchdogRecoveryEvent.ServerRunning))
    }
