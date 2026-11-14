package moe.shizuku.manager.notifications

import android.content.Context
import moe.shizuku.manager.service.WatchdogRecoveryState
import moe.shizuku.manager.service.WatchdogService
import moe.shizuku.manager.start.StartStatus
import moe.shizuku.manager.utils.ShizukuStateMachine

/** Source-of-truth snapshot for Home/diagnostics; independent of helper notification preference. */
data class ServerHealthSnapshot(
    val serverState: ShizukuStateMachine.State,
    val binderAvailable: Boolean,
    val running: Boolean,
    val recoveryInProgress: Boolean,
    val recoveryAttempt: Int,
    val recoveryState: WatchdogRecoveryState?,
    val startStatus: StartStatus,
    val lastFailure: String?,
    val currentError: String?,
    val actionRequired: String?
)

object ServerHealth {
    fun snapshot(context: Context, startStatus: StartStatus): ServerHealthSnapshot {
        val state = ShizukuStateMachine.update()
        val binder = try { rikka.shizuku.Shizuku.pingBinder() } catch (_: Throwable) { false }
        val recState = WatchdogService.currentRecoveryState()
        val recAttempt = WatchdogService.currentRecoveryAttemptCount()
        val recoveryInProgress = recState == WatchdogRecoveryState.AWAITING_RECOVERY ||
            recState == WatchdogRecoveryState.PENDING_RESTART ||
            recState == WatchdogRecoveryState.RESTARTING
        val failed = startStatus as? StartStatus.Failed
        val gaveUp = recState == WatchdogRecoveryState.GAVE_UP
        val currentError = when {
            failed != null -> failed.message
            gaveUp -> "Automatic recovery temporarily paused"
            state == ShizukuStateMachine.State.CRASHED -> "Shizuku server stopped unexpectedly"
            else -> null
        }
        val actionRequired = when {
            failed != null -> when (failed.kind) {
                moe.shizuku.manager.start.StartFailureKind.WIFI -> "Connect to Wi-Fi"
                moe.shizuku.manager.start.StartFailureKind.PAIRING -> "Pair ADB"
                moe.shizuku.manager.start.StartFailureKind.PORT -> "Open ADB port"
                moe.shizuku.manager.start.StartFailureKind.SETTINGS -> "Enable wireless debugging / permission"
                else -> "Open OmniShizuku to investigate"
            }
            gaveUp -> "Restart or review recovery status"
            state == ShizukuStateMachine.State.CRASHED -> "Investigate or restart server"
            else -> null
        }
        val lastFailure = failed?.message ?: if (state == ShizukuStateMachine.State.CRASHED) "Server crashed/stopped unexpectedly" else null
        return ServerHealthSnapshot(
            serverState = state,
            binderAvailable = binder,
            running = state == ShizukuStateMachine.State.RUNNING && binder,
            recoveryInProgress = recoveryInProgress,
            recoveryAttempt = recAttempt,
            recoveryState = recState,
            startStatus = startStatus,
            lastFailure = lastFailure,
            currentError = currentError,
            actionRequired = actionRequired
        )
    }
}
