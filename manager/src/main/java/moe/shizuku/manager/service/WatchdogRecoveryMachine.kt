package moe.shizuku.manager.service

/**
 * Pure watchdog recovery state machine for deterministic testing.
 */
enum class WatchdogRecoveryState { IDLE, PENDING_RESTART, RESTARTING, AWAITING_RECOVERY, GAVE_UP }

sealed interface WatchdogRecoveryEvent {
    data object CrashDetected : WatchdogRecoveryEvent
    data object ServerRunning : WatchdogRecoveryEvent
    data object ManualStop : WatchdogRecoveryEvent
    data object RestartDeferred : WatchdogRecoveryEvent
    data object RestartStarted : WatchdogRecoveryEvent
    data object RestartAttemptFailed : WatchdogRecoveryEvent
    data object BreakerOpened : WatchdogRecoveryEvent
    data object Reset : WatchdogRecoveryEvent
}

object WatchdogRecoveryMachine {
    fun reduce(state: WatchdogRecoveryState, event: WatchdogRecoveryEvent): WatchdogRecoveryState = when (event) {
        WatchdogRecoveryEvent.CrashDetected -> when (state) {
            WatchdogRecoveryState.GAVE_UP -> WatchdogRecoveryState.GAVE_UP
            else -> WatchdogRecoveryState.AWAITING_RECOVERY
        }
        WatchdogRecoveryEvent.ServerRunning -> WatchdogRecoveryState.IDLE
        WatchdogRecoveryEvent.ManualStop,
        WatchdogRecoveryEvent.Reset -> WatchdogRecoveryState.IDLE
        WatchdogRecoveryEvent.RestartDeferred -> when (state) {
            WatchdogRecoveryState.GAVE_UP -> WatchdogRecoveryState.GAVE_UP
            else -> WatchdogRecoveryState.PENDING_RESTART
        }
        WatchdogRecoveryEvent.RestartStarted -> when (state) {
            WatchdogRecoveryState.GAVE_UP -> WatchdogRecoveryState.GAVE_UP
            else -> WatchdogRecoveryState.RESTARTING
        }
        WatchdogRecoveryEvent.RestartAttemptFailed -> when (state) {
            WatchdogRecoveryState.GAVE_UP -> WatchdogRecoveryState.GAVE_UP
            else -> WatchdogRecoveryState.PENDING_RESTART
        }
        WatchdogRecoveryEvent.BreakerOpened -> WatchdogRecoveryState.GAVE_UP
    }
}
