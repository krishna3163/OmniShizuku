package rikka.shizuku.nightdog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class NightDogStateMachineTest {

    private val criticalServices = listOf("package", "activity", "user", "appops")
    private val optionalServices = listOf("permissionmgr")

    @Test
    fun initialStateIsStopped() {
        val initial = NightDogSnapshot()
        assertEquals(NightDogSupervisorState.STOPPED, initial.supervisorState)
        assertEquals(0L, initial.generation)
        assertEquals(0, initial.boundCount)
        assertFalse(initial.isSupervising)
        assertFalse(initial.isRunning)
        assertNull(initial.fatalReason)
    }

    @Test
    fun startRequestedInitializesServicesInUnboundState() {
        val initial = NightDogSnapshot()
        val event = NightDogEvent.StartRequested(
            generation = 1L,
            timeoutMs = 60_000L,
            pollIntervalMs = 60_000L,
            criticalServices = criticalServices,
            optionalServices = optionalServices,
            currentTimeMs = 1000L
        )

        val next = NightDogStateMachine.reduce(initial, event)
        assertEquals(NightDogSupervisorState.STARTING, next.supervisorState)
        assertEquals(1L, next.generation)
        assertEquals(5, next.services.size)
        assertEquals(1000L, next.lastBeatMonotonicMs)

        for (name in criticalServices) {
            val svc = next.services[name]!!
            assertTrue(svc.isCritical)
            assertEquals(NightDogServiceState.UNBOUND, svc.state)
            assertEquals(0, svc.rebindAttempts)
        }
        val opt = next.services["permissionmgr"]!!
        assertFalse(opt.isCritical)
        assertEquals(NightDogServiceState.UNBOUND, opt.state)
    }

    @Test
    fun startedTransitionsToSupervisingWhenAllBound() {
        val starting = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.STARTING,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", state = NightDogServiceState.LINKED),
                "activity" to MonitoredServiceSnapshot("activity", state = NightDogServiceState.LINKED)
            )
        )

        val next = NightDogStateMachine.reduce(starting, NightDogEvent.Started(1L))
        assertEquals(NightDogSupervisorState.SUPERVISING, next.supervisorState)
        assertTrue(next.isSupervising)
        assertFalse(next.isDegraded)
    }

    @Test
    fun heartbeatReceivedUpdatesLastBeatAndCount() {
        val supervising = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.SUPERVISING,
            generation = 1L,
            lastBeatMonotonicMs = 1000L,
            beatCount = 5L
        )

        val next = NightDogStateMachine.reduce(supervising, NightDogEvent.HeartbeatReceived(2000L))
        assertEquals(2000L, next.lastBeatMonotonicMs)
        assertEquals(6L, next.beatCount)

        // Stale heartbeat when stopped is ignored
        val stopped = NightDogSnapshot(supervisorState = NightDogSupervisorState.STOPPED)
        val ignored = NightDogStateMachine.reduce(stopped, NightDogEvent.HeartbeatReceived(3000L))
        assertEquals(0L, ignored.lastBeatMonotonicMs)
    }

    @Test
    fun healthCheckOverdueHeartbeatTransitionsToRestartRequested() {
        val supervising = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.SUPERVISING,
            generation = 1L,
            timeoutMs = 60_000L,
            lastBeatMonotonicMs = 10_000L
        )

        // Elapsed = 70_001 - 10_000 = 60_001ms >= 60_000ms timeout
        val next = NightDogStateMachine.reduce(supervising, NightDogEvent.HealthCheckTick(70_001L))
        assertEquals(NightDogSupervisorState.RESTART_REQUESTED, next.supervisorState)
        assertTrue(next.fatalReason!!.contains("heartbeat_timeout"))
    }

    @Test
    fun serviceLinkedUpdatesServiceAndRecoversSupervisingState() {
        val degraded = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.DEATH_DETECTED),
                "activity" to MonitoredServiceSnapshot("activity", isCritical = true, state = NightDogServiceState.LINKED)
            )
        )

        val event = NightDogEvent.ServiceLinked(generation = 1L, name = "package", currentTimeMs = 5000L)
        val next = NightDogStateMachine.reduce(degraded, event)

        assertEquals(NightDogServiceState.LINKED, next.services["package"]!!.state)
        assertEquals(NightDogSupervisorState.SUPERVISING, next.supervisorState)
        assertEquals(2, next.boundCount)
    }

    @Test
    fun serviceDeathDetectedTransitionsToDegraded() {
        val supervising = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.SUPERVISING,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.LINKED),
                "activity" to MonitoredServiceSnapshot("activity", isCritical = true, state = NightDogServiceState.LINKED)
            )
        )

        val next = NightDogStateMachine.reduce(
            supervising,
            NightDogEvent.ServiceDeathDetected(generation = 1L, name = "package", currentTimeMs = 2000L)
        )

        assertEquals(NightDogServiceState.DEATH_DETECTED, next.services["package"]!!.state)
        assertEquals(NightDogSupervisorState.DEGRADED, next.supervisorState)
        assertTrue(next.isDegraded)
    }

    @Test
    fun rebindScheduledAndStartedProgressState() {
        val degraded = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.DEATH_DETECTED)
            )
        )

        val scheduled = NightDogStateMachine.reduce(
            degraded,
            NightDogEvent.RebindScheduled(generation = 1L, name = "package", delayMs = 2000L, currentTimeMs = 3000L)
        )
        assertEquals(NightDogServiceState.REBIND_PENDING, scheduled.services["package"]!!.state)
        assertEquals(2000L, scheduled.services["package"]!!.nextRetryDelayMs)

        val started = NightDogStateMachine.reduce(
            scheduled,
            NightDogEvent.RebindStarted(generation = 1L, name = "package", currentTimeMs = 5000L)
        )
        assertEquals(NightDogServiceState.REBINDING, started.services["package"]!!.state)
    }

    @Test
    fun serviceRecoveredResetsAttemptsAndIncrementsTotalRecoveries() {
        val rebinding = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            totalRecoveries = 2,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.REBINDING, rebindAttempts = 3)
            )
        )

        val next = NightDogStateMachine.reduce(
            rebinding,
            NightDogEvent.ServiceRecovered(generation = 1L, name = "package", currentTimeMs = 8000L)
        )

        assertEquals(NightDogServiceState.RECOVERED, next.services["package"]!!.state)
        assertEquals(0, next.services["package"]!!.rebindAttempts)
        assertEquals(3, next.totalRecoveries)
        assertEquals(NightDogSupervisorState.SUPERVISING, next.supervisorState)
    }

    @Test
    fun rebindFailedIncrementsAttemptsAndAppliesBackoff() {
        val rebinding = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.REBINDING, rebindAttempts = 0)
            )
        )

        val nextDelay = NightDogStateMachine.calculateBackoffDelay(1)
        assertEquals(5_000L, nextDelay)

        val next = NightDogStateMachine.reduce(
            rebinding,
            NightDogEvent.RebindFailed(generation = 1L, name = "package", nextDelayMs = nextDelay, currentTimeMs = 4000L)
        )

        assertEquals(NightDogServiceState.REBIND_PENDING, next.services["package"]!!.state)
        assertEquals(1, next.services["package"]!!.rebindAttempts)
        assertEquals(5000L, next.services["package"]!!.nextRetryDelayMs)

        // Verify exponential backoff curve
        assertEquals(5_000L, NightDogStateMachine.calculateBackoffDelay(1))
        assertEquals(10_000L, NightDogStateMachine.calculateBackoffDelay(2))
        assertEquals(20_000L, NightDogStateMachine.calculateBackoffDelay(3))
        assertEquals(40_000L, NightDogStateMachine.calculateBackoffDelay(4))
        assertEquals(60_000L, NightDogStateMachine.calculateBackoffDelay(5))
        assertEquals(60_000L, NightDogStateMachine.calculateBackoffDelay(10)) // Capped at MAX_BACKOFF_MS
    }

    @Test
    fun criticalServiceRetryExhaustionTransitionsToRestartRequested() {
        val exhausted = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.REBINDING, rebindAttempts = 9)
            )
        )

        val next = NightDogStateMachine.reduce(
            exhausted,
            NightDogEvent.ServiceUnrecoverable(generation = 1L, name = "package", currentTimeMs = 50_000L)
        )

        assertEquals(NightDogServiceState.UNRECOVERABLE, next.services["package"]!!.state)
        assertEquals(NightDogSupervisorState.RESTART_REQUESTED, next.supervisorState)
        assertTrue(next.fatalReason!!.contains("critical_service_unrecoverable:package"))
    }

    @Test
    fun optionalServiceRetryExhaustionRemainsDegradedWithoutRestart() {
        val degraded = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.LINKED),
                "permissionmgr" to MonitoredServiceSnapshot("permissionmgr", isCritical = false, state = NightDogServiceState.REBINDING, rebindAttempts = 9)
            )
        )

        val next = NightDogStateMachine.reduce(
            degraded,
            NightDogEvent.ServiceUnrecoverable(generation = 1L, name = "permissionmgr", currentTimeMs = 50_000L)
        )

        assertEquals(NightDogServiceState.UNRECOVERABLE, next.services["permissionmgr"]!!.state)
        assertEquals(NightDogSupervisorState.DEGRADED, next.supervisorState)
        assertNull(next.fatalReason)
    }

    @Test
    fun multipleServicesFailingSimultaneouslyHandledIndependently() {
        val initial = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.SUPERVISING,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.LINKED),
                "activity" to MonitoredServiceSnapshot("activity", isCritical = true, state = NightDogServiceState.LINKED)
            )
        )

        val death1 = NightDogStateMachine.reduce(
            initial,
            NightDogEvent.ServiceDeathDetected(generation = 1L, name = "package", currentTimeMs = 1000L)
        )
        val death2 = NightDogStateMachine.reduce(
            death1,
            NightDogEvent.ServiceDeathDetected(generation = 1L, name = "activity", currentTimeMs = 1100L)
        )

        assertEquals(NightDogServiceState.DEATH_DETECTED, death2.services["package"]!!.state)
        assertEquals(NightDogServiceState.DEATH_DETECTED, death2.services["activity"]!!.state)
        assertEquals(NightDogSupervisorState.DEGRADED, death2.supervisorState)

        val recovered1 = NightDogStateMachine.reduce(
            death2,
            NightDogEvent.ServiceRecovered(generation = 1L, name = "package", currentTimeMs = 3000L)
        )
        // One recovered, one still dead -> still DEGRADED
        assertEquals(NightDogSupervisorState.DEGRADED, recovered1.supervisorState)

        val recovered2 = NightDogStateMachine.reduce(
            recovered1,
            NightDogEvent.ServiceRecovered(generation = 1L, name = "activity", currentTimeMs = 4000L)
        )
        // Both recovered -> SUPERVISING
        assertEquals(NightDogSupervisorState.SUPERVISING, recovered2.supervisorState)
    }

    @Test
    fun staleGenerationEventsAreSafelyRejected() {
        val supervising = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.SUPERVISING,
            generation = 3L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.LINKED)
            )
        )

        val staleDeath = NightDogEvent.ServiceDeathDetected(generation = 2L, name = "package", currentTimeMs = 1000L)
        val result = NightDogStateMachine.reduce(supervising, staleDeath)
        assertEquals(supervising, result)

        val staleRecovered = NightDogEvent.ServiceRecovered(generation = 1L, name = "package", currentTimeMs = 2000L)
        val result2 = NightDogStateMachine.reduce(supervising, staleRecovered)
        assertEquals(supervising, result2)
    }

    @Test
    fun stopRequestedAndStoppedClearsAllState() {
        val supervising = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.SUPERVISING,
            generation = 2L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", state = NightDogServiceState.LINKED)
            )
        )

        val stopping = NightDogStateMachine.reduce(supervising, NightDogEvent.StopRequested(2L))
        assertEquals(NightDogSupervisorState.STOPPING, stopping.supervisorState)

        val stopped = NightDogStateMachine.reduce(stopping, NightDogEvent.Stopped)
        assertEquals(NightDogSupervisorState.STOPPED, stopped.supervisorState)
        assertTrue(stopped.services.isEmpty())
        assertEquals(0L, stopped.lastBeatMonotonicMs)
    }

    @Test
    fun cleanupIdempotency() {
        val stopped = NightDogSnapshot(supervisorState = NightDogSupervisorState.STOPPED)
        val next1 = NightDogStateMachine.reduce(stopped, NightDogEvent.Stopped)
        val next2 = NightDogStateMachine.reduce(next1, NightDogEvent.Reset)
        assertEquals(NightDogSupervisorState.STOPPED, next2.supervisorState)
    }

    @Test
    fun binderDeathDuringActiveRebindHandledDeterministically() {
        val rebinding = NightDogSnapshot(
            supervisorState = NightDogSupervisorState.DEGRADED,
            generation = 1L,
            services = mapOf(
                "package" to MonitoredServiceSnapshot("package", isCritical = true, state = NightDogServiceState.REBINDING)
            )
        )

        val death = NightDogEvent.ServiceDeathDetected(generation = 1L, name = "package", currentTimeMs = 5000L)
        val next = NightDogStateMachine.reduce(rebinding, death)

        assertEquals(NightDogServiceState.DEATH_DETECTED, next.services["package"]!!.state)
        assertEquals(NightDogSupervisorState.DEGRADED, next.supervisorState)
    }

    @Test
    fun coordinatorAtomicTransitionsAndCallbacks() {
        var fatalReasonReported: String? = null
        var recoveredServiceReported: String? = null
        val transitionCount = AtomicInteger(0)

        val coordinator = NightDogCoordinator(
            onFatalFaultAction = { reason -> fatalReasonReported = reason },
            onServiceRecoveredAction = { name -> recoveredServiceReported = name }
        )

        coordinator.addListener { old, new, event ->
            transitionCount.incrementAndGet()
        }

        val gen = coordinator.nextGeneration()
        assertEquals(1L, gen)

        coordinator.transition(
            NightDogEvent.StartRequested(
                generation = gen,
                criticalServices = listOf("package"),
                optionalServices = emptyList(),
                currentTimeMs = 100L
            )
        )
        assertEquals(NightDogSupervisorState.STARTING, coordinator.supervisorState)

        coordinator.transition(NightDogEvent.Started(gen))
        coordinator.transition(NightDogEvent.ServiceLinked(gen, "package", 200L))
        assertEquals(NightDogSupervisorState.SUPERVISING, coordinator.supervisorState)

        coordinator.transition(NightDogEvent.ServiceDeathDetected(gen, "package", 300L))
        assertEquals(NightDogSupervisorState.DEGRADED, coordinator.supervisorState)

        coordinator.transition(NightDogEvent.ServiceRecovered(gen, "package", 400L))
        assertEquals(NightDogSupervisorState.SUPERVISING, coordinator.supervisorState)
        assertEquals("package", recoveredServiceReported)

        // Trigger fatal fault
        coordinator.transition(NightDogEvent.FatalFaultDetected(gen, "test_fatal", 500L))
        assertEquals(NightDogSupervisorState.RESTART_REQUESTED, coordinator.supervisorState)
        assertEquals("test_fatal", fatalReasonReported)

        assertTrue(transitionCount.get() >= 6)
    }
}
