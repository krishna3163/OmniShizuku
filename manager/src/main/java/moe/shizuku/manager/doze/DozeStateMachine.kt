package moe.shizuku.manager.doze

/**
 * Domain-specific immutable state model for the Doze & Power Policy Supervisor.
 * Pure Kotlin, zero Android framework dependencies for deterministic JVM testing.
 */

enum class DozeState {
    INACTIVE,
    SCREEN_OFF_PENDING,
    ENTERING_DOZE,
    DEEP_DOZING,
    MAINTENANCE_WINDOW,
    RESTORING,
    FAILED
}

/**
 * Immutable snapshot of device radios and system power settings captured before entering Doze.
 * Tracks exactly which settings were modified so only modified settings are restored,
 * preventing silent clobbering of external user adjustments.
 */
data class DeviceRadioSnapshot(
    val wifiEnabled: Boolean = false,
    val mobileDataEnabled: Boolean = false,
    val airplaneModeEnabled: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val locationModeEnabled: Boolean = false,
    val batterySaverEnabled: Boolean = false,
    val hotspotEnabled: Boolean = false,
    val sensorsRestricted: Boolean = false,
    val biometricsDisabled: Boolean = false,
    val blockedAppPackages: Set<String> = emptySet(),
    val blockedNotificationPackages: Set<String> = emptySet(),
    val modifiedSettings: Set<String> = emptySet(),
    val timestampMonotonicMs: Long = 0L
) {
    companion object {
        const val SETTING_WIFI = "wifi"
        const val SETTING_DATA = "data"
        const val SETTING_AIRPLANE = "airplane"
        const val SETTING_BLUETOOTH = "bluetooth"
        const val SETTING_LOCATION = "location"
        const val SETTING_BATTERY_SAVER = "battery_saver"
        const val SETTING_SENSORS = "sensors"
        const val SETTING_BIOMETRICS = "biometrics"
        const val SETTING_BLOCKED_APPS = "blocked_apps"
        const val SETTING_BLOCKED_NOTIFS = "blocked_notifs"
    }

    /**
     * Determines which settings need to be restored given the current live state of the device.
     * Prevents clobbering external modifications made by the user while the device was in Doze.
     */
    fun computeRequiredRestorations(currentState: DeviceRadioSnapshot): Set<String> {
        val toRestore = mutableSetOf<String>()

        if (SETTING_WIFI in modifiedSettings && wifiEnabled && !currentState.wifiEnabled) {
            toRestore.add(SETTING_WIFI)
        }
        if (SETTING_DATA in modifiedSettings && mobileDataEnabled && !currentState.mobileDataEnabled) {
            toRestore.add(SETTING_DATA)
        }
        if (SETTING_AIRPLANE in modifiedSettings && !airplaneModeEnabled && currentState.airplaneModeEnabled) {
            toRestore.add(SETTING_AIRPLANE)
        }
        if (SETTING_BLUETOOTH in modifiedSettings && bluetoothEnabled && !currentState.bluetoothEnabled) {
            toRestore.add(SETTING_BLUETOOTH)
        }
        if (SETTING_LOCATION in modifiedSettings && locationModeEnabled && !currentState.locationModeEnabled) {
            toRestore.add(SETTING_LOCATION)
        }
        if (SETTING_BATTERY_SAVER in modifiedSettings && !batterySaverEnabled && currentState.batterySaverEnabled) {
            toRestore.add(SETTING_BATTERY_SAVER)
        }
        if (SETTING_SENSORS in modifiedSettings && !sensorsRestricted && currentState.sensorsRestricted) {
            toRestore.add(SETTING_SENSORS)
        }
        if (SETTING_BIOMETRICS in modifiedSettings && !biometricsDisabled && currentState.biometricsDisabled) {
            toRestore.add(SETTING_BIOMETRICS)
        }
        if (SETTING_BLOCKED_APPS in modifiedSettings && blockedAppPackages.isNotEmpty()) {
            toRestore.add(SETTING_BLOCKED_APPS)
        }
        if (SETTING_BLOCKED_NOTIFS in modifiedSettings && blockedNotificationPackages.isNotEmpty()) {
            toRestore.add(SETTING_BLOCKED_NOTIFS)
        }

        return toRestore
    }
}

/**
 * Immutable snapshot of the Doze state machine at any given point in time.
 */
data class DozeSnapshot(
    val state: DozeState = DozeState.INACTIVE,
    val generation: Long = 0L,
    val screenOffDeadlineMs: Long = 0L,
    val isScreenOn: Boolean = true,
    val isCharging: Boolean = false,
    val disableWhenCharging: Boolean = true,
    val isCallActive: Boolean = false,
    val initialSnapshot: DeviceRadioSnapshot? = null,
    val currentIdleMode: String = "ACTIVE",
    val wakeLockHeld: Boolean = false,
    val pendingDelayMs: Long = 0L,
    val lastExitReason: String? = null,
    val lastError: String? = null,
    val dozeEnterCount: Int = 0
) {
    val isDozing: Boolean
        get() = state == DozeState.DEEP_DOZING || state == DozeState.MAINTENANCE_WINDOW

    val isActiveOrPending: Boolean
        get() = state != DozeState.INACTIVE && state != DozeState.FAILED
}

/**
 * Typed events driving transitions within the DozeStateMachine.
 */
sealed interface DozeEvent {
    data class ServiceStarted(val isScreenOn: Boolean, val isCharging: Boolean) : DozeEvent
    data object ServiceStopped : DozeEvent

    data class ScreenTurnedOff(
        val isCharging: Boolean,
        val isCallActive: Boolean,
        val delayMs: Long,
        val currentTimeMs: Long
    ) : DozeEvent

    data class ScreenTurnedOn(
        val currentTimeMs: Long
    ) : DozeEvent

    data class PowerConnected(val currentTimeMs: Long) : DozeEvent
    data class PowerDisconnected(val currentTimeMs: Long) : DozeEvent

    data class PendingDelayElapsed(val generation: Long) : DozeEvent

    data class RadioStateCaptured(
        val generation: Long,
        val snapshot: DeviceRadioSnapshot
    ) : DozeEvent

    data class DozeApplied(
        val generation: Long,
        val idleMode: String,
        val wakeLockAcquired: Boolean = false
    ) : DozeEvent

    data class IdleModeChanged(
        val newIdleMode: String
    ) : DozeEvent

    data class MaintenanceWindowEntered(val generation: Long) : DozeEvent
    data class MaintenanceWindowExited(val generation: Long) : DozeEvent

    data class WakeLockReleased(val generation: Long) : DozeEvent

    data class RestoringCompleted(
        val generation: Long,
        val restoredSettings: Set<String>
    ) : DozeEvent

    data class OperationFailed(
        val generation: Long? = null,
        val reason: String
    ) : DozeEvent

    data object Reset : DozeEvent
}

/**
 * Pure, deterministic state machine reducer for the Doze & Power Policy engine.
 */
object DozeStateMachine {

    fun reduce(snapshot: DozeSnapshot, event: DozeEvent): DozeSnapshot {
        // 1. Reset event always returns INACTIVE with incremented generation
        if (event is DozeEvent.Reset) {
            return DozeSnapshot(generation = snapshot.generation + 1)
        }

        // 2. Service stopped transitions to RESTORING (if initialSnapshot was captured) or INACTIVE
        if (event is DozeEvent.ServiceStopped) {
            return if (snapshot.initialSnapshot != null && snapshot.initialSnapshot.modifiedSettings.isNotEmpty()) {
                snapshot.copy(
                    state = DozeState.RESTORING,
                    generation = snapshot.generation + 1,
                    wakeLockHeld = false,
                    lastExitReason = "ServiceStopped"
                )
            } else {
                DozeSnapshot(
                    state = DozeState.INACTIVE,
                    generation = snapshot.generation + 1,
                    wakeLockHeld = false
                )
            }
        }

        // 3. Generation check for asynchronous callbacks
        val eventGen = getEventGeneration(event)
        if (eventGen != null && eventGen != snapshot.generation) {
            return snapshot // Stale callback dropped
        }

        // 4. State transitions
        return when (snapshot.state) {
            DozeState.INACTIVE -> when (event) {
                is DozeEvent.ServiceStarted -> {
                    snapshot.copy(
                        isScreenOn = event.isScreenOn,
                        isCharging = event.isCharging
                    )
                }

                is DozeEvent.ScreenTurnedOff -> {
                    // Preconditions: charging when disableWhenCharging is set, or active telephony call
                    if (event.isCharging && snapshot.disableWhenCharging) {
                        return snapshot.copy(isScreenOn = false, isCharging = true)
                    }
                    if (event.isCallActive) {
                        return snapshot.copy(isScreenOn = false, isCallActive = true)
                    }

                    val nextGen = snapshot.generation + 1
                    if (event.delayMs <= 0L) {
                        // Enter doze immediately
                        snapshot.copy(
                            state = DozeState.ENTERING_DOZE,
                            generation = nextGen,
                            isScreenOn = false,
                            isCharging = event.isCharging,
                            isCallActive = false,
                            pendingDelayMs = 0L,
                            screenOffDeadlineMs = event.currentTimeMs
                        )
                    } else {
                        // Pending debounce countdown
                        snapshot.copy(
                            state = DozeState.SCREEN_OFF_PENDING,
                            generation = nextGen,
                            isScreenOn = false,
                            isCharging = event.isCharging,
                            isCallActive = false,
                            pendingDelayMs = event.delayMs,
                            screenOffDeadlineMs = event.currentTimeMs + event.delayMs
                        )
                    }
                }

                is DozeEvent.PowerConnected -> snapshot.copy(isCharging = true)
                is DozeEvent.PowerDisconnected -> snapshot.copy(isCharging = false)
                else -> snapshot
            }

            DozeState.SCREEN_OFF_PENDING -> when (event) {
                is DozeEvent.ScreenTurnedOn -> {
                    // Rapid screen on: cancel pending doze timer immediately
                    snapshot.copy(
                        state = DozeState.INACTIVE,
                        isScreenOn = true,
                        generation = snapshot.generation + 1,
                        pendingDelayMs = 0L,
                        screenOffDeadlineMs = 0L,
                        lastExitReason = "ScreenOnDuringPending"
                    )
                }

                is DozeEvent.PowerConnected -> {
                    if (snapshot.disableWhenCharging) {
                        // Charger connected while pending: abort pending doze
                        snapshot.copy(
                            state = DozeState.INACTIVE,
                            isCharging = true,
                            generation = snapshot.generation + 1,
                            pendingDelayMs = 0L,
                            screenOffDeadlineMs = 0L,
                            lastExitReason = "ChargingDuringPending"
                        )
                    } else {
                        snapshot.copy(isCharging = true)
                    }
                }

                is DozeEvent.PowerDisconnected -> snapshot.copy(isCharging = false)

                is DozeEvent.PendingDelayElapsed -> {
                    // Debounce delay elapsed: transition to ENTERING_DOZE
                    snapshot.copy(
                        state = DozeState.ENTERING_DOZE,
                        pendingDelayMs = 0L
                    )
                }

                else -> snapshot
            }

            DozeState.ENTERING_DOZE -> when (event) {
                is DozeEvent.ScreenTurnedOn -> {
                    // Screen turned back on while entering: abort and restore if needed
                    val needsRestore = snapshot.initialSnapshot != null && snapshot.initialSnapshot.modifiedSettings.isNotEmpty()
                    snapshot.copy(
                        state = if (needsRestore) DozeState.RESTORING else DozeState.INACTIVE,
                        isScreenOn = true,
                        wakeLockHeld = false,
                        lastExitReason = "ScreenOnDuringEnter"
                    )
                }

                is DozeEvent.PowerConnected -> {
                    val isCharging = true
                    if (snapshot.disableWhenCharging) {
                        val needsRestore = snapshot.initialSnapshot != null && snapshot.initialSnapshot.modifiedSettings.isNotEmpty()
                        snapshot.copy(
                            state = if (needsRestore) DozeState.RESTORING else DozeState.INACTIVE,
                            isCharging = true,
                            wakeLockHeld = false,
                            lastExitReason = "ChargingDuringEnter"
                        )
                    } else {
                        snapshot.copy(isCharging = true)
                    }
                }

                is DozeEvent.RadioStateCaptured -> {
                    snapshot.copy(
                        initialSnapshot = event.snapshot
                    )
                }

                is DozeEvent.DozeApplied -> {
                    snapshot.copy(
                        state = DozeState.DEEP_DOZING,
                        currentIdleMode = event.idleMode,
                        wakeLockHeld = event.wakeLockAcquired,
                        dozeEnterCount = snapshot.dozeEnterCount + 1
                    )
                }

                is DozeEvent.WakeLockReleased -> {
                    snapshot.copy(wakeLockHeld = false)
                }

                is DozeEvent.OperationFailed -> {
                    val needsRestore = snapshot.initialSnapshot != null && snapshot.initialSnapshot.modifiedSettings.isNotEmpty()
                    snapshot.copy(
                        state = if (needsRestore) DozeState.RESTORING else DozeState.FAILED,
                        lastError = event.reason,
                        wakeLockHeld = false
                    )
                }

                else -> snapshot
            }

            DozeState.DEEP_DOZING -> when (event) {
                is DozeEvent.ScreenTurnedOn -> {
                    // Screen turned on: exit deep doze, transition to RESTORING
                    snapshot.copy(
                        state = DozeState.RESTORING,
                        isScreenOn = true,
                        wakeLockHeld = false,
                        lastExitReason = "ScreenOn"
                    )
                }

                is DozeEvent.PowerConnected -> {
                    if (snapshot.disableWhenCharging) {
                        // Charger connected during deep doze: exit and restore
                        snapshot.copy(
                            state = DozeState.RESTORING,
                            isCharging = true,
                            wakeLockHeld = false,
                            lastExitReason = "Charging"
                        )
                    } else {
                        snapshot.copy(isCharging = true)
                    }
                }

                is DozeEvent.PowerDisconnected -> snapshot.copy(isCharging = false)

                is DozeEvent.MaintenanceWindowEntered -> {
                    snapshot.copy(
                        state = DozeState.MAINTENANCE_WINDOW,
                        currentIdleMode = "IDLE_MAINTENANCE"
                    )
                }

                is DozeEvent.IdleModeChanged -> {
                    if (event.newIdleMode.contains("MAINTENANCE", ignoreCase = true)) {
                        snapshot.copy(
                            state = DozeState.MAINTENANCE_WINDOW,
                            currentIdleMode = event.newIdleMode
                        )
                    } else {
                        snapshot.copy(currentIdleMode = event.newIdleMode)
                    }
                }

                is DozeEvent.WakeLockReleased -> snapshot.copy(wakeLockHeld = false)

                else -> snapshot
            }

            DozeState.MAINTENANCE_WINDOW -> when (event) {
                is DozeEvent.ScreenTurnedOn -> {
                    snapshot.copy(
                        state = DozeState.RESTORING,
                        isScreenOn = true,
                        wakeLockHeld = false,
                        lastExitReason = "ScreenOnDuringMaintenance"
                    )
                }

                is DozeEvent.PowerConnected -> {
                    if (snapshot.disableWhenCharging) {
                        snapshot.copy(
                            state = DozeState.RESTORING,
                            isCharging = true,
                            wakeLockHeld = false,
                            lastExitReason = "ChargingDuringMaintenance"
                        )
                    } else {
                        snapshot.copy(isCharging = true)
                    }
                }

                is DozeEvent.PowerDisconnected -> snapshot.copy(isCharging = false)

                is DozeEvent.MaintenanceWindowExited -> {
                    snapshot.copy(
                        state = DozeState.DEEP_DOZING,
                        currentIdleMode = "IDLE"
                    )
                }

                is DozeEvent.IdleModeChanged -> {
                    if (event.newIdleMode.equals("IDLE", ignoreCase = true)) {
                        snapshot.copy(
                            state = DozeState.DEEP_DOZING,
                            currentIdleMode = event.newIdleMode
                        )
                    } else {
                        snapshot.copy(currentIdleMode = event.newIdleMode)
                    }
                }

                else -> snapshot
            }

            DozeState.RESTORING -> when (event) {
                is DozeEvent.WakeLockReleased -> snapshot.copy(wakeLockHeld = false)

                is DozeEvent.RestoringCompleted -> {
                    // Restoration completed: transition to INACTIVE, clear initial snapshot
                    snapshot.copy(
                        state = DozeState.INACTIVE,
                        initialSnapshot = null,
                        wakeLockHeld = false
                    )
                }

                is DozeEvent.OperationFailed -> {
                    snapshot.copy(
                        state = DozeState.FAILED,
                        lastError = event.reason,
                        wakeLockHeld = false
                    )
                }

                // If screen turns off again while restoring is completing, stay in RESTORING until completed
                is DozeEvent.ScreenTurnedOff -> snapshot.copy(isScreenOn = false)
                is DozeEvent.PowerConnected -> snapshot.copy(isCharging = true)
                is DozeEvent.PowerDisconnected -> snapshot.copy(isCharging = false)

                else -> snapshot
            }

            DozeState.FAILED -> when (event) {
                is DozeEvent.ScreenTurnedOn -> {
                    // Screen turned on from failed state: attempt restore if snapshot exists
                    if (snapshot.initialSnapshot != null && snapshot.initialSnapshot.modifiedSettings.isNotEmpty()) {
                        snapshot.copy(
                            state = DozeState.RESTORING,
                            isScreenOn = true,
                            generation = snapshot.generation + 1
                        )
                    } else {
                        snapshot.copy(
                            state = DozeState.INACTIVE,
                            isScreenOn = true,
                            generation = snapshot.generation + 1
                        )
                    }
                }

                else -> snapshot
            }
        }
    }

    private fun getEventGeneration(event: DozeEvent): Long? = when (event) {
        is DozeEvent.ServiceStarted -> null
        is DozeEvent.ServiceStopped -> null
        is DozeEvent.ScreenTurnedOff -> null
        is DozeEvent.ScreenTurnedOn -> null
        is DozeEvent.PowerConnected -> null
        is DozeEvent.PowerDisconnected -> null
        is DozeEvent.PendingDelayElapsed -> event.generation
        is DozeEvent.RadioStateCaptured -> event.generation
        is DozeEvent.DozeApplied -> event.generation
        is DozeEvent.IdleModeChanged -> null
        is DozeEvent.MaintenanceWindowEntered -> event.generation
        is DozeEvent.MaintenanceWindowExited -> event.generation
        is DozeEvent.WakeLockReleased -> event.generation
        is DozeEvent.RestoringCompleted -> event.generation
        is DozeEvent.OperationFailed -> event.generation
        is DozeEvent.Reset -> null
    }
}
