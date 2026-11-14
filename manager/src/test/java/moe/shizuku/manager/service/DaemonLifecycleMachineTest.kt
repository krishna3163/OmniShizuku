package moe.shizuku.manager.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DaemonLifecycleMachineTest {

    @Test
    fun testNormalStartupSequence() {
        val s0 = DaemonLifecycleSnapshot()
        val s1 = DaemonLifecycleMachine.reduce(s0, DaemonLifecycleEvent.Initialize)
        assertEquals(DaemonState.CHECKING_EXISTING, s1.state)

        val s2 = DaemonLifecycleMachine.reduce(s1, DaemonLifecycleEvent.SupervisorSpawned(pid = 1234))
        assertEquals(DaemonState.STARTING, s2.state)
        assertEquals(SupervisorState.SUPERVISING, s2.supervisorState)
        assertEquals(1234, s2.supervisorPid)

        val s3 = DaemonLifecycleMachine.reduce(s2, DaemonLifecycleEvent.DaemonStarted(pid = 1235))
        assertEquals(DaemonState.WAITING_FOR_BINDER, s3.state)
        assertEquals(DaemonProcessState.RUNNING, s3.daemonState)
        assertEquals(BinderState.CONNECTING, s3.binderState)

        val s4 = DaemonLifecycleMachine.reduce(s3, DaemonLifecycleEvent.BinderConnected)
        assertEquals(DaemonState.RUNNING, s4.state)
        assertEquals(BinderState.CONNECTED, s4.binderState)
        assertEquals(CoordinatorState.MONITORING, s4.coordinatorState)
        assertTrue(s4.isRunning)
    }

    @Test
    fun testHeartbeatMissedAndRecovered() {
        val running = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = BinderState.CONNECTED,
            coordinatorState = CoordinatorState.MONITORING
        )
        val unresponsive = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.HeartbeatMissed)
        assertEquals(DaemonState.HEARTBEAT_UNRESPONSIVE, unresponsive.state)

        val recovered = DaemonLifecycleMachine.reduce(unresponsive, DaemonLifecycleEvent.HeartbeatReceived)
        assertEquals(DaemonState.RUNNING, recovered.state)
    }

    @Test
    fun testUserInitiatedStopSequence() {
        val running = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = BinderState.CONNECTED
        )
        val s1 = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.UserStopRequested)
        assertEquals(DaemonState.STOP_REQUESTED, s1.state)
        assertTrue(s1.isUserStop)

        val s2 = DaemonLifecycleMachine.reduce(s1, DaemonLifecycleEvent.ConfigFlushed)
        assertEquals(DaemonState.FLUSHING_CONFIGURATION, s2.state)

        val s3 = DaemonLifecycleMachine.reduce(s2, DaemonLifecycleEvent.BinderDied(exitCode = 0))
        assertEquals(DaemonState.STOPPED, s3.state)
        assertEquals(BinderState.DEAD, s3.binderState)
        assertEquals(CoordinatorState.USER_STOPPED, s3.coordinatorState)
        assertFalse(s3.isAwaitingRecovery)
    }

    @Test
    fun testCrashWithSupervisorRecovery() {
        val running = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = BinderState.CONNECTED,
            consecutiveFailures = 0
        )
        val s1 = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.BinderDied(exitCode = 139))
        assertEquals(DaemonState.RESTART_PENDING, s1.state)
        assertEquals(CoordinatorState.SUPERVISOR_GRACE_PERIOD, s1.coordinatorState)
        assertEquals(1, s1.consecutiveFailures)
        assertTrue(s1.isAwaitingRecovery)

        // Native supervisor recovers daemon within grace period
        val s2 = DaemonLifecycleMachine.reduce(s1, DaemonLifecycleEvent.SupervisorRecovered)
        assertEquals(DaemonState.WAITING_FOR_BINDER, s2.state)
        assertEquals(DaemonProcessState.RUNNING, s2.daemonState)

        val s3 = DaemonLifecycleMachine.reduce(s2, DaemonLifecycleEvent.BinderConnected)
        assertEquals(DaemonState.RUNNING, s3.state)
        assertEquals(0, s3.consecutiveFailures)
    }

    @Test
    fun testCrashWithSupervisorGraceExpiredThenRestart() {
        val running = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = BinderState.CONNECTED
        )
        val s1 = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.BinderDied(exitCode = 1))
        assertEquals(DaemonState.RESTART_PENDING, s1.state)

        // Grace period expired: coordinator takes over restart
        val s2 = DaemonLifecycleMachine.reduce(s1, DaemonLifecycleEvent.SupervisorGraceExpired)
        assertEquals(DaemonState.RESTARTING, s2.state)
        assertEquals(CoordinatorState.RESTARTING, s2.coordinatorState)

        val s3 = DaemonLifecycleMachine.reduce(s2, DaemonLifecycleEvent.RestartSucceeded)
        assertEquals(DaemonState.WAITING_FOR_BINDER, s3.state)

        val s4 = DaemonLifecycleMachine.reduce(s3, DaemonLifecycleEvent.BinderConnected)
        assertEquals(DaemonState.RUNNING, s4.state)
    }

    @Test
    fun testCircuitBreakerTripsAfterMaxConsecutiveFailures() {
        var s = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = BinderState.CONNECTED,
            consecutiveFailures = 4
        )
        // 5th failure
        s = DaemonLifecycleMachine.reduce(s, DaemonLifecycleEvent.BinderDied(exitCode = 1))
        assertEquals(DaemonState.RESTART_PENDING, s.state)
        assertEquals(5, s.consecutiveFailures)

        // 6th failure trips circuit breaker
        s = DaemonLifecycleMachine.reduce(s, DaemonLifecycleEvent.RestartFailed("Crash loop"))
        assertEquals(DaemonState.CIRCUIT_BREAKER_OPEN, s.state)
        assertEquals(CoordinatorState.CIRCUIT_BREAKER_OPEN, s.coordinatorState)

        // Further crash events do not trigger auto-recovery
        val sNext = DaemonLifecycleMachine.reduce(s, DaemonLifecycleEvent.BinderDied(exitCode = 1))
        assertEquals(DaemonState.CIRCUIT_BREAKER_OPEN, sNext.state)
    }

    @Test
    fun testCleanExitCodesDoNotTriggerCrashAlarms() {
        val running = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = BinderState.CONNECTED
        )
        // Clean exit 0
        val sExit0 = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.DaemonTerminated(exitCode = 0))
        assertEquals(DaemonState.STOPPED, sExit0.state)
        assertEquals(SupervisorState.EXITED_CLEANLY, sExit0.supervisorState)

        // Manager not found exit 50
        val sExit50 = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.DaemonTerminated(exitCode = 50))
        assertEquals(DaemonState.STOPPED, sExit50.state)
        assertEquals(SupervisorState.EXITED_CLEANLY, sExit50.supervisorState)

        // Crash exit 1
        val sExit1 = DaemonLifecycleMachine.reduce(running, DaemonLifecycleEvent.DaemonTerminated(exitCode = 1))
        assertEquals(DaemonState.FAILED, sExit1.state)
    }

    @Test
    fun testResetClearsFailures() {
        val broken = DaemonLifecycleSnapshot(
            state = DaemonState.CIRCUIT_BREAKER_OPEN,
            consecutiveFailures = 8,
            isUserStop = true
        )
        val reset = DaemonLifecycleMachine.reduce(broken, DaemonLifecycleEvent.Reset)
        assertEquals(DaemonState.UNINITIALIZED, reset.state)
        assertEquals(0, reset.consecutiveFailures)
        assertFalse(reset.isUserStop)
    }

    @Test
    fun testDeterministicStressSequence() {
        fun run(seed: Int): DaemonLifecycleSnapshot {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            val list = listOf(
                DaemonLifecycleEvent.Initialize,
                DaemonLifecycleEvent.SupervisorSpawned(100),
                DaemonLifecycleEvent.DaemonStarted(101),
                DaemonLifecycleEvent.BinderConnected,
                DaemonLifecycleEvent.HeartbeatReceived,
                DaemonLifecycleEvent.HeartbeatMissed,
                DaemonLifecycleEvent.BinderDied(1),
                DaemonLifecycleEvent.SupervisorGraceExpired,
                DaemonLifecycleEvent.SupervisorRecovered,
                DaemonLifecycleEvent.RestartInitiated,
                DaemonLifecycleEvent.RestartSucceeded,
                DaemonLifecycleEvent.RestartFailed("Fail"),
                DaemonLifecycleEvent.UserStopRequested,
                DaemonLifecycleEvent.ConfigFlushed,
                DaemonLifecycleEvent.DaemonTerminated(0),
                DaemonLifecycleEvent.CircuitBreakerTripped,
                DaemonLifecycleEvent.Reset
            )
            var s = DaemonLifecycleSnapshot()
            repeat(300) { s = DaemonLifecycleMachine.reduce(s, list[next() % list.size]) }
            return s
        }
        assertEquals(run(77), run(77))
        run(3)
    }
}
