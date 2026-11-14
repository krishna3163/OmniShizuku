package moe.shizuku.manager.notifications

import moe.shizuku.manager.service.WatchdogRecoveryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchdogLiveStateMapperTest {

    @Test
    fun recoveredRunning_mapsToReconnected() {
        val d = WatchdogLiveStateMapper.map(true, false, WatchdogRecoveryState.IDLE, 0, true)
        assertEquals(WatchdogLiveState.RECONNECTED, d.state)
        assertTrue(d.isRecoverySuccess)
        assertFalse(d.showRestartAction)
    }

    @Test
    fun waitingForBinder_mapsToWaiting() {
        val d = WatchdogLiveStateMapper.map(
            serverRunning = false,
            manuallyStopped = false,
            recoveryState = WatchdogRecoveryState.IDLE,
            attempt = 0,
            recoveryJustSucceeded = false,
            waitingForBinder = true
        )
        assertEquals(WatchdogLiveState.WAITING, d.state)
        assertFalse(d.isRecoverySuccess)
    }

    @Test
    fun gaveUp_mapsToCircuitBreakerWithRestart() {
        val d = WatchdogLiveStateMapper.map(false, false, WatchdogRecoveryState.GAVE_UP, 5, false)
        assertEquals(WatchdogLiveState.CIRCUIT_BREAKER, d.state)
        assertTrue(d.showRestartAction)
        assertFalse(d.isRecoverySuccess)
    }

    @Test
    fun restarting_mapsToRestarting() {
        val d = WatchdogLiveStateMapper.map(false, false, WatchdogRecoveryState.RESTARTING, 2, false)
        assertEquals(WatchdogLiveState.RESTARTING, d.state)
        assertFalse(d.showRestartAction)
    }

    @Test
    fun awaitingRecovery_mapsToRecoveryAttempt() {
        val d = WatchdogLiveStateMapper.map(false, false, WatchdogRecoveryState.AWAITING_RECOVERY, 3, false)
        assertEquals(WatchdogLiveState.RECOVERY_STARTED, d.state)
        assertEquals(3, d.formatArg)
        assertFalse(d.showRestartAction)
    }

    @Test
    fun manualStop_mapsToManualStop() {
        val d = WatchdogLiveStateMapper.map(false, true, WatchdogRecoveryState.IDLE, 0, false)
        assertEquals(WatchdogLiveState.MANUAL_STOP, d.state)
        assertFalse(d.showRestartAction)
    }

    @Test
    fun runningIdle_mapsToRunning() {
        val d = WatchdogLiveStateMapper.map(true, false, WatchdogRecoveryState.IDLE, 0, false)
        assertEquals(WatchdogLiveState.RUNNING, d.state)
        assertFalse(d.showRestartAction)
    }

    @Test
    fun stopped_mapsToStopped() {
        val d = WatchdogLiveStateMapper.map(false, false, WatchdogRecoveryState.IDLE, 0, false)
        assertEquals(WatchdogLiveState.STOPPED, d.state)
        assertFalse(d.showRestartAction)
    }
}
