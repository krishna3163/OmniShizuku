package moe.shizuku.manager.start

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbStartStateMachineTest {

    @Test
    fun testInitialStateIsIdle() {
        val snapshot = AdbStartSnapshot()
        assertEquals(AdbStartState.IDLE, snapshot.state)
        assertEquals(0L, snapshot.generation)
        assertFalse(snapshot.isInFlight)
        assertFalse(snapshot.isTerminal)
        assertNull(snapshot.attemptId)
    }

    @Test
    fun testStartRequestedTransitionsToCheckingPrerequisites() {
        val initial = AdbStartSnapshot()
        val next = AdbStartStateMachine.reduce(
            initial,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_WIRELESS,
                isLockedStart = false,
                attemptId = "attempt-1"
            )
        )

        assertEquals(AdbStartState.CHECKING_PREREQUISITES, next.state)
        assertEquals(1L, next.generation)
        assertEquals("attempt-1", next.attemptId)
        assertEquals(AdbStartStateMachine.START_METHOD_WIRELESS, next.startMethod)
        assertTrue(next.isInFlight)
        assertFalse(next.isTerminal)
    }

    @Test
    fun testSingleFlightProtectionRejectsDuplicateStartWithoutForce() {
        val running = AdbStartSnapshot(
            state = AdbStartState.CONNECTING_ADB,
            generation = 1L,
            attemptId = "attempt-1",
            activePort = 5555
        )

        val duplicate = AdbStartStateMachine.reduce(
            running,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_USB,
                attemptId = "attempt-2",
                force = false
            )
        )

        // Must reject duplicate attempt, preserving attempt-1
        assertEquals(AdbStartState.CONNECTING_ADB, duplicate.state)
        assertEquals(1L, duplicate.generation)
        assertEquals("attempt-1", duplicate.attemptId)
        assertEquals(5555, duplicate.activePort)
    }

    @Test
    fun testForcedStartCancelsPreviousAttemptAndIncrementsGeneration() {
        val running = AdbStartSnapshot(
            state = AdbStartState.CONNECTING_ADB,
            generation = 1L,
            attemptId = "attempt-1",
            activePort = 5555
        )

        val forced = AdbStartStateMachine.reduce(
            running,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_USB,
                attemptId = "attempt-2",
                force = true
            )
        )

        // Forced restart supersedes previous attempt
        assertEquals(AdbStartState.CHECKING_PREREQUISITES, forced.state)
        assertEquals(2L, forced.generation)
        assertEquals("attempt-2", forced.attemptId)
        assertEquals(AdbStartStateMachine.START_METHOD_USB, forced.startMethod)
        assertNull(forced.activePort)
    }

    @Test
    fun testStaleGenerationCallbacksAreRejected() {
        val current = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 5L,
            attemptId = "attempt-5"
        )

        // Event with older generation 4L
        val stale = AdbStartStateMachine.reduce(
            current,
            AdbStartEvent.FreshPortDiscovered(generation = 4L, port = 37123)
        )

        assertEquals(current, stale)
        assertEquals(5L, stale.generation)
        assertNull(stale.activePort)
    }

    @Test
    fun testUsbDebuggingAutoHideDefersUsbStartGracefully() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.CHECKING_PREREQUISITES,
            generation = 1L,
            attemptId = "attempt-usb",
            startMethod = AdbStartStateMachine.START_METHOD_USB
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PrerequisitesChecked(
                generation = 1L,
                isUsbAutoHidingActive = true,
                hasWriteSecureSettings = true,
                initialCandidates = listOf(5555)
            )
        )

        // Auto-hide must defer USB startup gracefully without claiming server is running
        assertEquals(AdbStartState.DEFERRED, next.state)
        assertTrue(next.isUsbAutoHidingActive)
        assertEquals(AdbStartFailureKind.DEFERRED_AUTO_HIDE, next.lastFailureKind)
        assertTrue(next.isTerminal)
    }

    @Test
    fun testUsbDebuggingAutoHideAllowsWirelessStart() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.CHECKING_PREREQUISITES,
            generation = 1L,
            attemptId = "attempt-wireless",
            startMethod = AdbStartStateMachine.START_METHOD_WIRELESS
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PrerequisitesChecked(
                generation = 1L,
                isUsbAutoHidingActive = true,
                hasWriteSecureSettings = true,
                initialCandidates = listOf(39123)
            )
        )

        // Wireless start does not touch USB debugging toggle, so it proceeds to HUNTING_PORT
        assertEquals(AdbStartState.HUNTING_PORT, next.state)
        assertTrue(next.isUsbAutoHidingActive)
        assertEquals(39123, next.activePort)
    }

    @Test
    fun testDirectConnectPortTransitionsDirectlyToConnecting() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.CHECKING_PREREQUISITES,
            generation = 1L
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PrerequisitesChecked(
                generation = 1L,
                isUsbAutoHidingActive = false,
                hasWriteSecureSettings = true,
                directConnectPort = 5555
            )
        )

        assertEquals(AdbStartState.CONNECTING_ADB, next.state)
        assertEquals(5555, next.activePort)
        assertEquals("DIRECT", next.portSource)
    }

    @Test
    fun testDeadPortRejectionAdvancesToNextCandidate() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L,
            activePort = 5555,
            candidatePorts = listOf(39111, 41222)
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PortCandidateValidated(
                generation = 1L,
                port = 5555,
                isListening = false,
                source = "CLASSIC_PROPERTY"
            )
        )

        assertEquals(AdbStartState.HUNTING_PORT, next.state)
        assertEquals(39111, next.activePort)
        assertEquals(listOf(41222), next.candidatePorts)
        assertTrue(next.failedPorts.contains(5555))
    }

    @Test
    fun testCandidatePortVerifiedListeningTransitionsToConnecting() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L,
            activePort = 39111,
            candidatePorts = listOf(41222)
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PortCandidateValidated(
                generation = 1L,
                port = 39111,
                isListening = true,
                source = "MDNS"
            )
        )

        assertEquals(AdbStartState.CONNECTING_ADB, next.state)
        assertEquals(39111, next.activePort)
        assertEquals("MDNS", next.portSource)
    }

    @Test
    fun testDeadPortExhaustionTriggersFreshMdnsSearch() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L,
            activePort = 5555,
            candidatePorts = emptyList(),
            freshSearchesUsed = 0,
            maxFreshSearches = 3
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PortCandidateValidated(
                generation = 1L,
                port = 5555,
                isListening = false,
                source = "CLASSIC_PROPERTY"
            )
        )

        assertEquals(AdbStartState.HUNTING_PORT, next.state)
        assertNull(next.activePort)
        assertEquals(1, next.freshSearchesUsed)
        assertTrue(next.failedPorts.contains(5555))
    }

    @Test
    fun testFreshPortDiscoveredTransitionsToConnecting() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L,
            activePort = null,
            freshSearchesUsed = 1
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.FreshPortDiscovered(
                generation = 1L,
                port = 42345,
                source = "MDNS"
            )
        )

        assertEquals(AdbStartState.CONNECTING_ADB, next.state)
        assertEquals(42345, next.activePort)
        assertEquals("MDNS", next.portSource)
    }

    @Test
    fun testPortDiscoveryTimeoutWithFallbackPortTransitionsToConnecting() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PortDiscoveryTimedOut(
                generation = 1L,
                fallbackPort = 5555
            )
        )

        assertEquals(AdbStartState.CONNECTING_ADB, next.state)
        assertEquals(5555, next.activePort)
        assertEquals("FALLBACK", next.portSource)
    }

    @Test
    fun testPortDiscoveryTimeoutWithoutFallbackTriggersRetry() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L,
            retryCount = 0,
            maxRetries = 3
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PortDiscoveryTimedOut(
                generation = 1L,
                fallbackPort = null
            )
        )

        assertEquals(AdbStartState.RETRYING, next.state)
        assertEquals(AdbStartFailureKind.PORT_NOT_FOUND, next.lastFailureKind)
    }

    @Test
    fun testPortDiscoveryHotspotRefusedInLockedStartFailsFast() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.HUNTING_PORT,
            generation = 1L,
            isLockedStart = true
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PortDiscoveryFailed(
                generation = 1L,
                reason = "Hotspot refused by keyguard",
                isHotspotRefused = true
            )
        )

        assertEquals(AdbStartState.FAILED, next.state)
        assertEquals(AdbStartFailureKind.HOTSPOT_REFUSED, next.lastFailureKind)
        assertTrue(next.isTerminal)
    }

    @Test
    fun testPairingRejectionTransitionsToTerminalPairingRequired() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.CONNECTING_ADB,
            generation = 1L,
            activePort = 39123
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.PairingRejected(
                generation = 1L,
                message = "TLS handshake rejected: CERTIFICATE_UNKNOWN"
            )
        )

        assertEquals(AdbStartState.PAIRING_REQUIRED, next.state)
        assertEquals(AdbStartFailureKind.PAIRING_REQUIRED, next.lastFailureKind)
        assertTrue(next.isTerminal)
        assertFalse(next.canRetry)
    }

    @Test
    fun testConnectionFailureWithRemainingCandidatesTriesNextCandidate() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.CONNECTING_ADB,
            generation = 1L,
            activePort = 39123,
            candidatePorts = listOf(40123)
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.ConnectionFailed(
                generation = 1L,
                port = 39123,
                failureKind = AdbStartFailureKind.CONNECTION_REFUSED_OR_CLOSED,
                message = "Connection refused"
            )
        )

        assertEquals(AdbStartState.HUNTING_PORT, next.state)
        assertEquals(40123, next.activePort)
        assertTrue(next.candidatePorts.isEmpty())
        assertTrue(next.failedPorts.contains(39123))
    }

    @Test
    fun testConnectionFailureWithRemainingSearchesTriggersFreshSearch() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.CONNECTING_ADB,
            generation = 1L,
            activePort = 39123,
            candidatePorts = emptyList(),
            freshSearchesUsed = 0,
            maxFreshSearches = 3
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.ConnectionFailed(
                generation = 1L,
                port = 39123,
                failureKind = AdbStartFailureKind.CONNECTION_REFUSED_OR_CLOSED,
                message = "Connection reset"
            )
        )

        assertEquals(AdbStartState.HUNTING_PORT, next.state)
        assertNull(next.activePort)
        assertEquals(1, next.freshSearchesUsed)
        assertTrue(next.failedPorts.contains(39123))
    }

    @Test
    fun testHappyPathWirelessStartCompleteLifecycle() {
        var s = AdbStartSnapshot()

        // 1. Request start
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_WIRELESS,
                attemptId = "happy-path"
            )
        )
        assertEquals(AdbStartState.CHECKING_PREREQUISITES, s.state)

        // 2. Prerequisites checked
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.PrerequisitesChecked(
                generation = 1L,
                isUsbAutoHidingActive = false,
                hasWriteSecureSettings = true,
                initialCandidates = emptyList()
            )
        )
        assertEquals(AdbStartState.HUNTING_PORT, s.state)

        // 3. Fresh port found via mDNS
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.FreshPortDiscovered(generation = 1L, port = 41234, source = "MDNS")
        )
        assertEquals(AdbStartState.CONNECTING_ADB, s.state)
        assertEquals(41234, s.activePort)

        // 4. Connecting and authenticated
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.ConnectingAdb(generation = 1L, port = 41234, attemptIndex = 1)
        )
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.Authenticated(generation = 1L, port = 41234)
        )
        assertEquals(AdbStartState.AUTHENTICATING, s.state)

        // 5. Launch daemon
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.LaunchingDaemon(generation = 1L, port = 41234)
        )
        assertEquals(AdbStartState.LAUNCHING_DAEMON, s.state)

        // 6. Command dispatched
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.CommandDispatched(generation = 1L, port = 41234)
        )
        assertEquals(AdbStartState.AWAITING_BINDER, s.state)
        assertTrue(s.commandSent)

        // 7. Binder received!
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.BinderReceived(generation = 1L, pid = 9182)
        )
        assertEquals(AdbStartState.SUCCEEDED, s.state)
        assertEquals(9182, s.daemonPid)
        assertTrue(s.isTerminal)
        assertFalse(s.isInFlight)
    }

    @Test
    fun testHappyPathUsbStartCompleteLifecycle() {
        var s = AdbStartSnapshot()

        // 1. Request USB start
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_USB,
                attemptId = "usb-happy"
            )
        )
        assertEquals(AdbStartState.CHECKING_PREREQUISITES, s.state)

        // 2. Direct connect to classic TCP port 5555
        s = AdbStartStateMachine.reduce(
            s,
            AdbStartEvent.PrerequisitesChecked(
                generation = 1L,
                isUsbAutoHidingActive = false,
                hasWriteSecureSettings = true,
                directConnectPort = 5555
            )
        )
        assertEquals(AdbStartState.CONNECTING_ADB, s.state)
        assertEquals(5555, s.activePort)

        // 3. Authenticate and send command
        s = AdbStartStateMachine.reduce(s, AdbStartEvent.Authenticated(generation = 1L, port = 5555))
        s = AdbStartStateMachine.reduce(s, AdbStartEvent.LaunchingDaemon(generation = 1L, port = 5555))
        s = AdbStartStateMachine.reduce(s, AdbStartEvent.CommandDispatched(generation = 1L, port = 5555))
        assertEquals(AdbStartState.AWAITING_BINDER, s.state)

        // 4. Binder confirmed
        s = AdbStartStateMachine.reduce(s, AdbStartEvent.BinderReceived(generation = 1L, pid = 8821))
        assertEquals(AdbStartState.SUCCEEDED, s.state)
        assertEquals(8821, s.daemonPid)
    }

    @Test
    fun testCommandSentPreventsPortHuntingRetries() {
        // Once command is dispatched, the state is AWAITING_BINDER and commandSent is true.
        // It must never drop back to hunting ports or reconnecting on a fresh port.
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.AWAITING_BINDER,
            generation = 1L,
            activePort = 5555,
            commandSent = true,
            retryCount = 0,
            maxRetries = 3
        )

        // If binder times out, it should enter RETRYING (to retry the wait or entire attempt) rather than port hunting
        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.BinderTimedOut(generation = 1L)
        )

        assertEquals(AdbStartState.RETRYING, next.state)
        assertEquals(AdbStartFailureKind.BINDER_TIMEOUT, next.lastFailureKind)
    }

    @Test
    fun testRetryExhaustionLeadsToFailedState() {
        val snapshot = AdbStartSnapshot(
            state = AdbStartState.RETRYING,
            generation = 1L,
            retryCount = 3,
            maxRetries = 3
        )

        val next = AdbStartStateMachine.reduce(
            snapshot,
            AdbStartEvent.RetryScheduled(generation = 1L, reason = "Attempting restart")
        )

        assertEquals(AdbStartState.FAILED, next.state)
        assertEquals(AdbStartFailureKind.MAX_RETRIES_EXCEEDED, next.lastFailureKind)
        assertTrue(next.isTerminal)
    }

    @Test
    fun testCancellationDuringMajorStages() {
        val stages = listOf(
            AdbStartState.CHECKING_PREREQUISITES,
            AdbStartState.HUNTING_PORT,
            AdbStartState.CONNECTING_ADB,
            AdbStartState.AUTHENTICATING,
            AdbStartState.LAUNCHING_DAEMON,
            AdbStartState.AWAITING_BINDER,
            AdbStartState.RETRYING
        )

        for (stage in stages) {
            val snapshot = AdbStartSnapshot(state = stage, generation = 2L)
            val cancelled = AdbStartStateMachine.reduce(
                snapshot,
                AdbStartEvent.CancelRequested(generation = 2L, reason = "User aborted in $stage")
            )

            assertEquals("Failed to cancel from $stage", AdbStartState.CANCELLED, cancelled.state)
            assertEquals(AdbStartFailureKind.CANCELLED, cancelled.lastFailureKind)
            assertEquals("User aborted in $stage", cancelled.lastErrorMessage)
            assertTrue(cancelled.isTerminal)
            assertFalse(cancelled.isInFlight)
        }
    }

    @Test
    fun testCannotCancelTerminalCompletedState() {
        val succeeded = AdbStartSnapshot(state = AdbStartState.SUCCEEDED, generation = 1L)
        val cancelAttempt = AdbStartStateMachine.reduce(
            succeeded,
            AdbStartEvent.CancelRequested(generation = 1L, reason = "Too late")
        )
        assertEquals(AdbStartState.SUCCEEDED, cancelAttempt.state)

        val failed = AdbStartSnapshot(state = AdbStartState.FAILED, generation = 1L)
        val cancelFailed = AdbStartStateMachine.reduce(
            failed,
            AdbStartEvent.CancelRequested(generation = 1L, reason = "Too late")
        )
        assertEquals(AdbStartState.FAILED, cancelFailed.state)
    }

    @Test
    fun testResetRestoresCleanIdleStateWithIncrementedGeneration() {
        val dirty = AdbStartSnapshot(
            state = AdbStartState.FAILED,
            generation = 7L,
            attemptId = "old",
            failedPorts = setOf(5555, 39123),
            lastFailureKind = AdbStartFailureKind.GENERIC_IO
        )

        val reset = AdbStartStateMachine.reduce(dirty, AdbStartEvent.Reset)
        assertEquals(AdbStartState.IDLE, reset.state)
        assertEquals(8L, reset.generation)
        assertNull(reset.attemptId)
        assertTrue(reset.failedPorts.isEmpty())
        assertEquals(AdbStartFailureKind.NONE, reset.lastFailureKind)
    }

    @Test
    fun testCoordinatorAtomicTransitionsAndListeners() {
        val coordinator = AdbStartCoordinator()
        val observedEvents = mutableListOf<AdbStartEvent>()

        coordinator.addListener { _, _, event ->
            observedEvents.add(event)
        }

        coordinator.transition(
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_WIRELESS,
                attemptId = "coord-test"
            )
        )
        assertEquals(AdbStartState.CHECKING_PREREQUISITES, coordinator.state)

        coordinator.cancel("Testing cancellation")
        assertEquals(AdbStartState.CANCELLED, coordinator.state)

        assertEquals(2, observedEvents.size)
        assertTrue(observedEvents[0] is AdbStartEvent.StartRequested)
        assertTrue(observedEvents[1] is AdbStartEvent.CancelRequested)

        coordinator.reset()
        assertEquals(AdbStartState.IDLE, coordinator.state)
    }
}
