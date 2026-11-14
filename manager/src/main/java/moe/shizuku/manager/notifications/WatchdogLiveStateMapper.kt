package moe.shizuku.manager.notifications

import moe.shizuku.manager.service.WatchdogRecoveryState

enum class WatchdogLiveState {
    RUNNING,
    MONITORING,
    STOPPED,
    RECOVERY_STARTED,
    RESTARTING,
    WAITING,
    RECONNECTED,
    RECOVERY_FAILED,
    CIRCUIT_BREAKER,
    MANUAL_STOP
}

data class WatchdogLiveDecision(
    val state: WatchdogLiveState,
    val formatArg: Int? = null,
    val showRestartAction: Boolean = false,
    val isRecoverySuccess: Boolean = false
)

/**
 * Pure mapping from authoritative watchdog/server state to live notification content.
 * Rendering must not control recovery logic.
 */
object WatchdogLiveStateMapper {

    fun map(
        serverRunning: Boolean,
        manuallyStopped: Boolean,
        recoveryState: WatchdogRecoveryState,
        attempt: Int,
        recoveryJustSucceeded: Boolean,
        waitingForBinder: Boolean = false
    ): WatchdogLiveDecision {
        return when {
            recoveryJustSucceeded && serverRunning -> WatchdogLiveDecision(
                state = WatchdogLiveState.RECONNECTED,
                isRecoverySuccess = true
            )
            waitingForBinder -> WatchdogLiveDecision(
                state = WatchdogLiveState.WAITING
            )
            manuallyStopped && !serverRunning -> WatchdogLiveDecision(
                state = WatchdogLiveState.MANUAL_STOP
            )
            recoveryState == WatchdogRecoveryState.GAVE_UP -> WatchdogLiveDecision(
                state = WatchdogLiveState.CIRCUIT_BREAKER,
                showRestartAction = true
            )
            recoveryState == WatchdogRecoveryState.RESTARTING -> WatchdogLiveDecision(
                state = WatchdogLiveState.RESTARTING
            )
            recoveryState == WatchdogRecoveryState.PENDING_RESTART || recoveryState == WatchdogRecoveryState.AWAITING_RECOVERY -> WatchdogLiveDecision(
                state = WatchdogLiveState.RECOVERY_STARTED,
                formatArg = attempt.coerceAtLeast(1)
            )
            serverRunning -> WatchdogLiveDecision(
                state = WatchdogLiveState.RUNNING
            )
            else -> WatchdogLiveDecision(
                state = WatchdogLiveState.STOPPED
            )
        }
    }
}
