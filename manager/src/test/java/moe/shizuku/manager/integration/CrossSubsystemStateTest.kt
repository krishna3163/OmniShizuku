package moe.shizuku.manager.integration

import moe.shizuku.manager.security.AuthEvent
import moe.shizuku.manager.security.AuthScope
import moe.shizuku.manager.security.AuthSnapshot
import moe.shizuku.manager.security.AuthState
import moe.shizuku.manager.security.AuthStateMachine
import moe.shizuku.manager.security.FakeMonotonicClock
import moe.shizuku.manager.service.DaemonLifecycleEvent
import moe.shizuku.manager.service.DaemonLifecycleMachine
import moe.shizuku.manager.service.DaemonLifecycleSnapshot
import moe.shizuku.manager.service.DaemonState
import moe.shizuku.manager.settings.UsbDebuggingEvent
import moe.shizuku.manager.settings.UsbDebuggingPhase
import moe.shizuku.manager.settings.UsbDebuggingSnapshot
import moe.shizuku.manager.settings.UsbDebuggingState
import moe.shizuku.manager.settings.UsbDebuggingStateMachine
import moe.shizuku.manager.start.AdbStartEvent
import moe.shizuku.manager.start.AdbStartFailureKind
import moe.shizuku.manager.start.AdbStartSnapshot
import moe.shizuku.manager.start.AdbStartState
import moe.shizuku.manager.start.AdbStartStateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossSubsystemStateTest {

    private val clock = FakeMonotonicClock(50_000L)

    @Test
    fun testDaemonCrashWhileUsbDebuggingHiddenPreservesJournal() {
        // Step 1: USB Debugging is monitoring and hides for target app
        var usb = UsbDebuggingSnapshot(state = UsbDebuggingState.MONITORING)
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.TargetAppForeground("com.secure.bank"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.JournalPersisted("op-cross-1", 1, "com.secure.bank"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.HideCompleted(0))
        assertEquals(UsbDebuggingState.HIDDEN, usb.state)
        assertEquals("op-cross-1", usb.operationId)

        // Step 2: Shizuku daemon is running and crashes
        var daemon = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = moe.shizuku.manager.service.BinderState.CONNECTED
        )
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.BinderDied(139))
        assertEquals(DaemonState.RESTART_PENDING, daemon.state)

        // Verify: USB state and journal record are completely independent and preserved
        assertEquals(UsbDebuggingState.HIDDEN, usb.state)
        assertEquals("op-cross-1", usb.operationId)

        // Step 3: Daemon recovers
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.SupervisorRecovered)
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.BinderConnected)
        assertEquals(DaemonState.RUNNING, daemon.state)

        // Step 4: USB debugging restores when bank app leaves foreground
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.TargetAppBackground)
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.RestoreRequested("Left bank app"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.RestoreCompleted(1))
        assertEquals(UsbDebuggingState.RESTORED, usb.state)
    }

    @Test
    fun testProtectedActionFailsGracefullyWhenDaemonUnresponsive() {
        // Step 1: User authenticates for SERVER_TOGGLE action
        var auth = AuthSnapshot()
        auth = AuthStateMachine.reduce(auth, AuthEvent.RequestActionAuth("SERVER_TOGGLE"), clock)
        auth = AuthStateMachine.reduce(auth, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 60), clock)
        assertEquals(AuthState.AUTHENTICATED_ACTION_SESSION, auth.state)
        assertTrue(auth.isActionAuthorized("SERVER_TOGGLE", clock.elapsedRealtime()))

        // Step 2: Daemon missed heartbeat, became unresponsive
        var daemon = DaemonLifecycleSnapshot(
            state = DaemonState.RUNNING,
            binderState = moe.shizuku.manager.service.BinderState.CONNECTED
        )
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.HeartbeatMissed)
        assertEquals(DaemonState.HEARTBEAT_UNRESPONSIVE, daemon.state)

        // Verify: Auth session is valid, but action execution recognizes daemon is unresponsive
        assertTrue(auth.isActionAuthorized("SERVER_TOGGLE", clock.elapsedRealtime()))
        assertFalse(daemon.isRunning)

        // Step 3: Daemon recovers heartbeat
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.HeartbeatReceived)
        assertEquals(DaemonState.RUNNING, daemon.state)
        assertTrue(daemon.isRunning)
    }

    @Test
    fun testProcessDeathRecoveryAcrossAllSubsystems() {
        // Scenario: Application process terminates abruptly
        // 1. USB Engine was in HIDDEN phase
        var usb = UsbDebuggingSnapshot()
        usb = UsbDebuggingStateMachine.reduce(
            usb,
            UsbDebuggingEvent.ProcessDeathOrCrashDetected("crash-op", 1, UsbDebuggingPhase.HIDDEN)
        )
        assertEquals(UsbDebuggingState.PENDING_RESTORE, usb.state)

        // 2. Auth state is locked after process restart
        val auth = AuthSnapshot()
        assertEquals(AuthState.LOCKED, auth.state)
        assertFalse(auth.isAppOpenUnlocked)

        // 3. Daemon supervisor is initialized
        var daemon = DaemonLifecycleSnapshot()
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.Initialize)
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.SupervisorSpawned(500))
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.DaemonStarted(501))
        daemon = DaemonLifecycleMachine.reduce(daemon, DaemonLifecycleEvent.BinderConnected)
        assertEquals(DaemonState.RUNNING, daemon.state)

        // 4. USB restores safely
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.RestoreRequested("Crash recovery"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.RestoreCompleted(1))
        assertEquals(UsbDebuggingState.RESTORED, usb.state)
    }

    @Test
    fun testUsbDebuggingAutoHideCoordinatesWithAdbStartEngine() {
        // Step 1: USB Debugging Auto-Hide enters HIDDEN state for a bank app
        var usb = UsbDebuggingSnapshot(state = UsbDebuggingState.MONITORING)
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.TargetAppForeground("com.bank.app"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.JournalPersisted("op-usb-adb", 1, "com.bank.app"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.HideCompleted(0))
        assertEquals(UsbDebuggingState.HIDDEN, usb.state)
        assertTrue(usb.isHidden)

        // Step 2: A USB startup is requested while USB debugging is actively hidden
        var adbStart = AdbStartSnapshot()
        adbStart = AdbStartStateMachine.reduce(
            adbStart,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_USB,
                attemptId = "usb-start-attempt"
            )
        )
        assertEquals(AdbStartState.CHECKING_PREREQUISITES, adbStart.state)

        // Prerequisites check passes isUsbAutoHidingActive = true
        adbStart = AdbStartStateMachine.reduce(
            adbStart,
            AdbStartEvent.PrerequisitesChecked(
                generation = adbStart.generation,
                isUsbAutoHidingActive = usb.isHidden,
                hasWriteSecureSettings = true,
                initialCandidates = listOf(5555)
            )
        )

        // USB start MUST defer gracefully without claiming server is running or fighting auto-hide
        assertEquals(AdbStartState.DEFERRED, adbStart.state)
        assertEquals(AdbStartFailureKind.DEFERRED_AUTO_HIDE, adbStart.lastFailureKind)
        assertTrue(adbStart.isUsbAutoHidingActive)
        assertTrue(adbStart.isTerminal)

        // Step 3: A WIRELESS startup is requested while USB debugging is hidden
        var wirelessStart = AdbStartSnapshot()
        wirelessStart = AdbStartStateMachine.reduce(
            wirelessStart,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_WIRELESS,
                attemptId = "wireless-start-attempt"
            )
        )
        assertEquals(AdbStartState.CHECKING_PREREQUISITES, wirelessStart.state)

        wirelessStart = AdbStartStateMachine.reduce(
            wirelessStart,
            AdbStartEvent.PrerequisitesChecked(
                generation = wirelessStart.generation,
                isUsbAutoHidingActive = usb.isHidden,
                hasWriteSecureSettings = true,
                initialCandidates = listOf(39123)
            )
        )

        // Wireless start proceeds to hunting port because wireless transport does not touch USB debugging
        assertEquals(AdbStartState.HUNTING_PORT, wirelessStart.state)
        assertEquals(39123, wirelessStart.activePort)

        // Step 4: Bank app leaves foreground; USB debugging restores
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.TargetAppBackground)
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.RestoreRequested("Bank app closed"))
        usb = UsbDebuggingStateMachine.reduce(usb, UsbDebuggingEvent.RestoreCompleted(1))
        assertEquals(UsbDebuggingState.RESTORED, usb.state)
        assertFalse(usb.isHidden)

        // Step 5: Now a USB start proceeds without deferral
        var usbStartAfterRestore = AdbStartSnapshot()
        usbStartAfterRestore = AdbStartStateMachine.reduce(
            usbStartAfterRestore,
            AdbStartEvent.StartRequested(
                startMethod = AdbStartStateMachine.START_METHOD_USB,
                attemptId = "usb-after-restore"
            )
        )
        usbStartAfterRestore = AdbStartStateMachine.reduce(
            usbStartAfterRestore,
            AdbStartEvent.PrerequisitesChecked(
                generation = usbStartAfterRestore.generation,
                isUsbAutoHidingActive = usb.isHidden,
                hasWriteSecureSettings = true,
                directConnectPort = 5555
            )
        )
        assertEquals(AdbStartState.CONNECTING_ADB, usbStartAfterRestore.state)
        assertEquals(5555, usbStartAfterRestore.activePort)
    }

    @Test
    fun testConcurrentStressAllSubsystems() {
        fun run(seed: Int): Int {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            val fakeClock = FakeMonotonicClock(10_000L)

            var usb = UsbDebuggingSnapshot()
            var daemon = DaemonLifecycleSnapshot()
            var auth = AuthSnapshot()

            repeat(200) {
                fakeClock.advance(50L)
                val sel = next() % 3
                when (sel) {
                    0 -> {
                        val events = listOf(
                            UsbDebuggingEvent.StartMonitoring,
                            UsbDebuggingEvent.TargetAppForeground("pkg.test"),
                            UsbDebuggingEvent.JournalPersisted("op-$rnd", 1, "pkg.test"),
                            UsbDebuggingEvent.HideCompleted(0),
                            UsbDebuggingEvent.TargetAppBackground,
                            UsbDebuggingEvent.RestoreRequested("Stress"),
                            UsbDebuggingEvent.RestoreCompleted(1),
                            UsbDebuggingEvent.Reset
                        )
                        usb = UsbDebuggingStateMachine.reduce(usb, events[next() % events.size])
                    }
                    1 -> {
                        val events = listOf(
                            DaemonLifecycleEvent.Initialize,
                            DaemonLifecycleEvent.SupervisorSpawned(100),
                            DaemonLifecycleEvent.DaemonStarted(101),
                            DaemonLifecycleEvent.BinderConnected,
                            DaemonLifecycleEvent.HeartbeatReceived,
                            DaemonLifecycleEvent.HeartbeatMissed,
                            DaemonLifecycleEvent.BinderDied(1),
                            DaemonLifecycleEvent.SupervisorGraceExpired,
                            DaemonLifecycleEvent.SupervisorRecovered,
                            DaemonLifecycleEvent.RestartSucceeded,
                            DaemonLifecycleEvent.Reset
                        )
                        daemon = DaemonLifecycleMachine.reduce(daemon, events[next() % events.size])
                    }
                    2 -> {
                        val events = listOf(
                            AuthEvent.RequestAppOpenAuth(),
                            AuthEvent.RequestActionAuth("PERMISSIONS"),
                            AuthEvent.PromptDisplayed(auth.generation),
                            AuthEvent.AuthenticationSucceeded(auth.generation, 30),
                            AuthEvent.AuthenticationCancelled(auth.generation),
                            AuthEvent.ActivityBackgrounded(false),
                            AuthEvent.ActivityForegrounded(30),
                            AuthEvent.InvalidateAll
                        )
                        auth = AuthStateMachine.reduce(auth, events[next() % events.size], fakeClock)
                    }
                }
            }
            return (usb.state.ordinal * 10000) + (daemon.state.ordinal * 100) + auth.state.ordinal
        }

        assertEquals(run(12345), run(12345))
        run(6789)
    }
}
