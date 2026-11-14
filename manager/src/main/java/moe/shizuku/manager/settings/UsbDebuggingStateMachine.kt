package moe.shizuku.manager.settings

/**
 * Domain-specific immutable state model for USB Debugging Auto-Hide engine.
 * Pure Kotlin, zero Android framework dependencies for deterministic testing.
 */
enum class UsbDebuggingState {
    DISABLED,
    MONITORING,
    HIDE_REQUESTED,
    HIDING,
    HIDDEN,
    RESTORE_REQUESTED,
    RESTORING,
    RESTORED,
    PENDING_RESTORE,
    RECOVERY_REQUIRED,
    CONFLICT_DETECTED,
    ERROR
}

enum class UsbDebuggingPhase {
    IDLE,
    HIDING,
    HIDDEN,
    RESTORING,
    RESTORED,
    CONFLICT
}

data class UsbDebuggingSnapshot(
    val state: UsbDebuggingState = UsbDebuggingState.DISABLED,
    val generation: Long = 0L,
    val operationId: String? = null,
    val originalSetting: Int? = null,
    val lastVerifiedSetting: Int? = null,
    val activeTargetPackage: String? = null,
    val lastError: String? = null,
    val conflictDetail: String? = null
) {
    val isHidden: Boolean
        get() = state == UsbDebuggingState.HIDDEN

    val isMonitoring: Boolean
        get() = state == UsbDebuggingState.MONITORING ||
                state == UsbDebuggingState.HIDE_REQUESTED ||
                state == UsbDebuggingState.HIDING ||
                state == UsbDebuggingState.HIDDEN ||
                state == UsbDebuggingState.RESTORE_REQUESTED ||
                state == UsbDebuggingState.RESTORING ||
                state == UsbDebuggingState.RESTORED
}

sealed interface UsbDebuggingEvent {
    data object StartMonitoring : UsbDebuggingEvent
    data object StopMonitoring : UsbDebuggingEvent
    data class TargetAppForeground(val packageName: String) : UsbDebuggingEvent
    data object TargetAppBackground : UsbDebuggingEvent
    data class JournalPersisted(val operationId: String, val originalSetting: Int, val targetPackage: String) : UsbDebuggingEvent
    data class JournalPersistenceFailed(val reason: String) : UsbDebuggingEvent
    data class HideCompleted(val verifiedSetting: Int) : UsbDebuggingEvent
    data class HideFailed(val reason: String) : UsbDebuggingEvent
    data class RestoreRequested(val reason: String) : UsbDebuggingEvent
    data class RestoreCompleted(val verifiedSetting: Int) : UsbDebuggingEvent
    data class RestoreFailed(val reason: String) : UsbDebuggingEvent
    data class ExternalConflictDetected(val currentSetting: Int, val expectedOriginal: Int) : UsbDebuggingEvent
    data class ProcessDeathOrCrashDetected(val operationId: String, val originalSetting: Int, val phase: UsbDebuggingPhase) : UsbDebuggingEvent
    data object ManualRecoveryRequested : UsbDebuggingEvent
    data object Reset : UsbDebuggingEvent
}

object UsbDebuggingStateMachine {

    fun reduce(snapshot: UsbDebuggingSnapshot, event: UsbDebuggingEvent): UsbDebuggingSnapshot {
        return when (event) {
            UsbDebuggingEvent.StartMonitoring -> when (snapshot.state) {
                UsbDebuggingState.DISABLED,
                UsbDebuggingState.RESTORED,
                UsbDebuggingState.ERROR -> snapshot.copy(
                    state = UsbDebuggingState.MONITORING,
                    generation = snapshot.generation + 1,
                    lastError = null,
                    conflictDetail = null
                )
                else -> snapshot // ignore if already monitoring, hiding, etc.
            }

            UsbDebuggingEvent.StopMonitoring -> when (snapshot.state) {
                UsbDebuggingState.HIDDEN,
                UsbDebuggingState.HIDING,
                UsbDebuggingState.HIDE_REQUESTED -> snapshot.copy(
                    state = UsbDebuggingState.RESTORE_REQUESTED,
                    lastError = null
                )
                UsbDebuggingState.PENDING_RESTORE,
                UsbDebuggingState.RECOVERY_REQUIRED -> snapshot // must resolve recovery first
                else -> snapshot.copy(
                    state = UsbDebuggingState.DISABLED,
                    activeTargetPackage = null,
                    lastError = null
                )
            }

            is UsbDebuggingEvent.TargetAppForeground -> when (snapshot.state) {
                UsbDebuggingState.MONITORING,
                UsbDebuggingState.RESTORED -> snapshot.copy(
                    state = UsbDebuggingState.HIDE_REQUESTED,
                    activeTargetPackage = event.packageName,
                    lastError = null
                )
                UsbDebuggingState.HIDDEN -> {
                    // Update active target if changing between target apps
                    snapshot.copy(activeTargetPackage = event.packageName)
                }
                else -> snapshot
            }

            UsbDebuggingEvent.TargetAppBackground -> when (snapshot.state) {
                UsbDebuggingState.HIDDEN,
                UsbDebuggingState.HIDING,
                UsbDebuggingState.HIDE_REQUESTED -> snapshot.copy(
                    state = UsbDebuggingState.RESTORE_REQUESTED,
                    activeTargetPackage = null
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.JournalPersisted -> when (snapshot.state) {
                UsbDebuggingState.HIDE_REQUESTED,
                UsbDebuggingState.MONITORING -> snapshot.copy(
                    state = UsbDebuggingState.HIDING,
                    operationId = event.operationId,
                    originalSetting = event.originalSetting,
                    activeTargetPackage = event.targetPackage,
                    lastError = null
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.JournalPersistenceFailed -> snapshot.copy(
                state = UsbDebuggingState.ERROR,
                lastError = "Persistence failed: ${event.reason}"
            )

            is UsbDebuggingEvent.HideCompleted -> when (snapshot.state) {
                UsbDebuggingState.HIDING -> snapshot.copy(
                    state = UsbDebuggingState.HIDDEN,
                    lastVerifiedSetting = event.verifiedSetting,
                    lastError = null
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.HideFailed -> when (snapshot.state) {
                UsbDebuggingState.HIDING,
                UsbDebuggingState.HIDE_REQUESTED -> snapshot.copy(
                    state = UsbDebuggingState.ERROR,
                    lastError = "Hide failed: ${event.reason}"
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.RestoreRequested -> when (snapshot.state) {
                UsbDebuggingState.HIDDEN,
                UsbDebuggingState.RESTORE_REQUESTED,
                UsbDebuggingState.PENDING_RESTORE,
                UsbDebuggingState.RECOVERY_REQUIRED -> snapshot.copy(
                    state = UsbDebuggingState.RESTORING,
                    lastError = null
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.RestoreCompleted -> when (snapshot.state) {
                UsbDebuggingState.RESTORING -> snapshot.copy(
                    state = UsbDebuggingState.RESTORED,
                    lastVerifiedSetting = event.verifiedSetting,
                    operationId = null,
                    originalSetting = null,
                    activeTargetPackage = null,
                    lastError = null,
                    conflictDetail = null
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.RestoreFailed -> when (snapshot.state) {
                UsbDebuggingState.RESTORING -> snapshot.copy(
                    state = UsbDebuggingState.RECOVERY_REQUIRED,
                    lastError = "Restore failed: ${event.reason}"
                )
                else -> snapshot
            }

            is UsbDebuggingEvent.ExternalConflictDetected -> snapshot.copy(
                state = UsbDebuggingState.CONFLICT_DETECTED,
                conflictDetail = "External modification detected: current=${event.currentSetting}, expected=${event.expectedOriginal}"
            )

            is UsbDebuggingEvent.ProcessDeathOrCrashDetected -> {
                when (event.phase) {
                    UsbDebuggingPhase.HIDDEN,
                    UsbDebuggingPhase.HIDING -> snapshot.copy(
                        state = UsbDebuggingState.PENDING_RESTORE,
                        operationId = event.operationId,
                        originalSetting = event.originalSetting,
                        lastError = "Recovered after crash with active journal"
                    )
                    UsbDebuggingPhase.RESTORING -> snapshot.copy(
                        state = UsbDebuggingState.RECOVERY_REQUIRED,
                        operationId = event.operationId,
                        originalSetting = event.originalSetting,
                        lastError = "Crash occurred while restoring"
                    )
                    else -> snapshot
                }
            }

            UsbDebuggingEvent.ManualRecoveryRequested -> when (snapshot.state) {
                UsbDebuggingState.RECOVERY_REQUIRED,
                UsbDebuggingState.PENDING_RESTORE,
                UsbDebuggingState.CONFLICT_DETECTED,
                UsbDebuggingState.ERROR -> snapshot.copy(
                    state = UsbDebuggingState.RESTORING,
                    lastError = null
                )
                else -> snapshot
            }

            UsbDebuggingEvent.Reset -> UsbDebuggingSnapshot(
                state = UsbDebuggingState.DISABLED,
                generation = snapshot.generation + 1
            )
        }
    }
}
