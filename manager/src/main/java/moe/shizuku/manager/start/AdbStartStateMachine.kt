package moe.shizuku.manager.start

/**
 * Domain-specific immutable state model for the ADB Start & Port Discovery Engine.
 * Pure Kotlin, zero Android framework dependencies for deterministic testing and formal verification.
 */

enum class AdbStartState {
    IDLE,
    CHECKING_PREREQUISITES,
    HUNTING_PORT,
    PAIRING_REQUIRED,
    CONNECTING_ADB,
    AUTHENTICATING,
    LAUNCHING_DAEMON,
    AWAITING_BINDER,
    RETRYING,
    SUCCEEDED,
    DEFERRED,
    FAILED,
    CANCELLED
}

enum class AdbStartFailureKind {
    NONE,
    PAIRING_REQUIRED,
    CONNECTION_REFUSED_OR_CLOSED,
    TLS_HANDSHAKE_FAILED,
    PORT_NOT_FOUND,
    PERMISSION_DENIED,
    BINDER_TIMEOUT,
    HOTSPOT_REFUSED,
    DEFERRED_AUTO_HIDE,
    GENERIC_IO,
    MAX_RETRIES_EXCEEDED,
    CANCELLED
}

/**
 * Immutable snapshot of the ADB Start engine state at any given point in time.
 */
data class AdbStartSnapshot(
    val state: AdbStartState = AdbStartState.IDLE,
    val generation: Long = 0L,
    val attemptId: String? = null,
    val startMethod: Int = 0, // 0 = WIRELESS, 1 = USB, 2 = WIRELESS_NO_NETWORK
    val isLockedStart: Boolean = false,
    val activePort: Int? = null,
    val portSource: String? = null,
    val candidatePorts: List<Int> = emptyList(),
    val failedPorts: Set<Int> = emptySet(),
    val freshSearchesUsed: Int = 0,
    val maxFreshSearches: Int = 3,
    val connectAttemptsUsed: Int = 0,
    val maxConnectAttempts: Int = 5,
    val commandSent: Boolean = false,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val lastFailureKind: AdbStartFailureKind = AdbStartFailureKind.NONE,
    val lastErrorMessage: String? = null,
    val daemonPid: Int? = null,
    val isUsbAutoHidingActive: Boolean = false
) {
    val isInFlight: Boolean
        get() = state == AdbStartState.CHECKING_PREREQUISITES ||
                state == AdbStartState.HUNTING_PORT ||
                state == AdbStartState.CONNECTING_ADB ||
                state == AdbStartState.AUTHENTICATING ||
                state == AdbStartState.LAUNCHING_DAEMON ||
                state == AdbStartState.AWAITING_BINDER ||
                state == AdbStartState.RETRYING

    val isTerminal: Boolean
        get() = state == AdbStartState.SUCCEEDED ||
                state == AdbStartState.DEFERRED ||
                state == AdbStartState.FAILED ||
                state == AdbStartState.CANCELLED ||
                state == AdbStartState.PAIRING_REQUIRED

    val canRetry: Boolean
        get() = !isTerminal && retryCount < maxRetries && !commandSent &&
                lastFailureKind != AdbStartFailureKind.PAIRING_REQUIRED
}

/**
 * Typed events driving transitions within the AdbStartStateMachine.
 */
sealed interface AdbStartEvent {
    /**
     * Request to begin an ADB startup sequence.
     * If an existing run is in-flight and [force] is false, single-flight protection rejects the new start.
     */
    data class StartRequested(
        val startMethod: Int,
        val isLockedStart: Boolean = false,
        val attemptId: String,
        val force: Boolean = false
    ) : AdbStartEvent

    /**
     * Result of prerequisites check (permissions, USB auto-hide state, initial candidate ports).
     */
    data class PrerequisitesChecked(
        val generation: Long,
        val isUsbAutoHidingActive: Boolean,
        val hasWriteSecureSettings: Boolean,
        val initialCandidates: List<Int> = emptyList(),
        val directConnectPort: Int? = null
    ) : AdbStartEvent

    /**
     * A candidate port has been evaluated for listening status.
     */
    data class PortCandidateValidated(
        val generation: Long,
        val port: Int,
        val isListening: Boolean,
        val source: String
    ) : AdbStartEvent

    /**
     * A fresh port was discovered via mDNS during active hunting.
     */
    data class FreshPortDiscovered(
        val generation: Long,
        val port: Int,
        val source: String = "MDNS"
    ) : AdbStartEvent

    /**
     * Discovery timed out waiting for a fresh port announcement.
     */
    data class PortDiscoveryTimedOut(
        val generation: Long,
        val fallbackPort: Int? = null
    ) : AdbStartEvent

    /**
     * Port discovery failed outright (e.g. hotspot error or mDNS failure).
     */
    data class PortDiscoveryFailed(
        val generation: Long,
        val reason: String,
        val isHotspotRefused: Boolean = false
    ) : AdbStartEvent

    /**
     * Beginning a socket TCP connection attempt on the specified port.
     */
    data class ConnectingAdb(
        val generation: Long,
        val port: Int,
        val attemptIndex: Int = 1
    ) : AdbStartEvent

    /**
     * The connection attempt failed (refused, closed, or generic I/O).
     */
    data class ConnectionFailed(
        val generation: Long,
        val port: Int,
        val failureKind: AdbStartFailureKind,
        val message: String
    ) : AdbStartEvent

    /**
     * The device rejected the TLS key or pairing certificate.
     */
    data class PairingRejected(
        val generation: Long,
        val message: String
    ) : AdbStartEvent

    /**
     * TLS authentication succeeded.
     */
    data class Authenticated(
        val generation: Long,
        val port: Int
    ) : AdbStartEvent

    /**
     * The starter shell command is being dispatched over ADB shell.
     */
    data class LaunchingDaemon(
        val generation: Long,
        val port: Int
    ) : AdbStartEvent

    /**
     * The starter shell command was successfully sent.
     */
    data class CommandDispatched(
        val generation: Long,
        val port: Int
    ) : AdbStartEvent

    /**
     * The starter shell command failed before dispatch.
     */
    data class CommandFailed(
        val generation: Long,
        val reason: String
    ) : AdbStartEvent

    /**
     * Shizuku Binder IPC was detected and verified alive.
     */
    data class BinderReceived(
        val generation: Long,
        val pid: Int? = null
    ) : AdbStartEvent

    /**
     * Waiting for Shizuku Binder timed out.
     */
    data class BinderTimedOut(
        val generation: Long
    ) : AdbStartEvent

    /**
     * A transient failure occurred; retry is scheduled.
     */
    data class RetryScheduled(
        val generation: Long,
        val reason: String
    ) : AdbStartEvent

    /**
     * Explicit cancellation requested by caller, host, or user.
     */
    data class CancelRequested(
        val generation: Long? = null,
        val reason: String
    ) : AdbStartEvent

    /**
     * Reset the state machine back to IDLE.
     */
    data object Reset : AdbStartEvent
}

/**
 * Pure state machine reducer for the ADB Start & Port Discovery Engine.
 */
object AdbStartStateMachine {

    const val START_METHOD_WIRELESS = 0
    const val START_METHOD_USB = 1
    const val START_METHOD_WIRELESS_NO_NETWORK = 2

    fun reduce(snapshot: AdbStartSnapshot, event: AdbStartEvent): AdbStartSnapshot {
        // 1. Reset event always returns IDLE with incremented generation
        if (event is AdbStartEvent.Reset) {
            return AdbStartSnapshot(generation = snapshot.generation + 1)
        }

        // 2. Cancellation handling
        if (event is AdbStartEvent.CancelRequested) {
            if (event.generation != null && event.generation != snapshot.generation) {
                return snapshot // Stale cancellation rejection
            }
            if (snapshot.state == AdbStartState.SUCCEEDED || snapshot.state == AdbStartState.FAILED) {
                return snapshot // Cannot cancel terminal completed state
            }
            return snapshot.copy(
                state = AdbStartState.CANCELLED,
                lastFailureKind = AdbStartFailureKind.CANCELLED,
                lastErrorMessage = event.reason
            )
        }

        // 3. StartRequested handling with single-flight protection
        if (event is AdbStartEvent.StartRequested) {
            if (snapshot.isInFlight && !event.force) {
                // Single-flight protection: reject duplicate concurrent start attempt
                return snapshot
            }
            return AdbStartSnapshot(
                state = AdbStartState.CHECKING_PREREQUISITES,
                generation = snapshot.generation + 1,
                attemptId = event.attemptId,
                startMethod = event.startMethod,
                isLockedStart = event.isLockedStart,
                retryCount = 0,
                freshSearchesUsed = 0,
                failedPorts = emptySet(),
                candidatePorts = emptyList(),
                activePort = null,
                commandSent = false,
                lastFailureKind = AdbStartFailureKind.NONE,
                lastErrorMessage = null
            )
        }

        // 4. Stale generation rejection: drop events from superseded attempts
        val eventGen = getEventGeneration(event)
        if (eventGen != null && eventGen != snapshot.generation) {
            return snapshot // Stale callback dropped
        }

        // 5. Terminal states drop all other events
        if (snapshot.isTerminal) {
            return snapshot
        }

        // 6. State-specific transitions
        return when (snapshot.state) {
            AdbStartState.IDLE -> snapshot

            AdbStartState.CHECKING_PREREQUISITES -> when (event) {
                is AdbStartEvent.PrerequisitesChecked -> {
                    // USB Debugging Auto-Hide Coordination:
                    // If USB auto-hide is actively hiding USB debugging, a USB start cannot proceed
                    // without conflicting with the auto-hide engine.
                    if (event.isUsbAutoHidingActive) {
                        if (snapshot.startMethod == START_METHOD_USB) {
                            // Defer the start gracefully without claiming the server is running
                            return snapshot.copy(
                                state = AdbStartState.DEFERRED,
                                isUsbAutoHidingActive = true,
                                lastFailureKind = AdbStartFailureKind.DEFERRED_AUTO_HIDE,
                                lastErrorMessage = "Start deferred: USB debugging auto-hide active"
                            )
                        }
                    }

                    // Direct connection port provided (e.g. valid TCP port or explicit candidate)
                    if (event.directConnectPort != null && event.directConnectPort > 0) {
                        snapshot.copy(
                            state = AdbStartState.CONNECTING_ADB,
                            activePort = event.directConnectPort,
                            portSource = "DIRECT",
                            candidatePorts = event.initialCandidates,
                            isUsbAutoHidingActive = event.isUsbAutoHidingActive
                        )
                    } else if (event.initialCandidates.isNotEmpty()) {
                        // Start hunting / probing candidate ports
                        val firstCandidate = event.initialCandidates.first()
                        val remaining = event.initialCandidates.drop(1)
                        snapshot.copy(
                            state = AdbStartState.HUNTING_PORT,
                            activePort = firstCandidate,
                            candidatePorts = remaining,
                            isUsbAutoHidingActive = event.isUsbAutoHidingActive
                        )
                    } else {
                        // Need mDNS discovery
                        snapshot.copy(
                            state = AdbStartState.HUNTING_PORT,
                            candidatePorts = emptyList(),
                            activePort = null,
                            isUsbAutoHidingActive = event.isUsbAutoHidingActive
                        )
                    }
                }
                else -> snapshot
            }

            AdbStartState.HUNTING_PORT -> when (event) {
                is AdbStartEvent.PortCandidateValidated -> {
                    if (event.isListening) {
                        // Port verified listening! Transition to CONNECTING_ADB
                        snapshot.copy(
                            state = AdbStartState.CONNECTING_ADB,
                            activePort = event.port,
                            portSource = event.source
                        )
                    } else {
                        // Port dead / refused probe: mark failed, advance to next candidate or discovery
                        val updatedFailed = snapshot.failedPorts + event.port
                        if (snapshot.candidatePorts.isNotEmpty()) {
                            val next = snapshot.candidatePorts.first()
                            val rem = snapshot.candidatePorts.drop(1)
                            snapshot.copy(
                                activePort = next,
                                candidatePorts = rem,
                                failedPorts = updatedFailed
                            )
                        } else if (snapshot.freshSearchesUsed < snapshot.maxFreshSearches) {
                            // Trigger fresh mDNS search
                            snapshot.copy(
                                activePort = null,
                                failedPorts = updatedFailed,
                                freshSearchesUsed = snapshot.freshSearchesUsed + 1
                            )
                        } else {
                            // No candidates and searches exhausted
                            if (snapshot.retryCount < snapshot.maxRetries) {
                                snapshot.copy(
                                    state = AdbStartState.RETRYING,
                                    failedPorts = updatedFailed,
                                    lastFailureKind = AdbStartFailureKind.PORT_NOT_FOUND,
                                    lastErrorMessage = "No live candidate port listening"
                                )
                            } else {
                                snapshot.copy(
                                    state = AdbStartState.FAILED,
                                    failedPorts = updatedFailed,
                                    lastFailureKind = AdbStartFailureKind.PORT_NOT_FOUND,
                                    lastErrorMessage = "No live candidate port listening"
                                )
                            }
                        }
                    }
                }

                is AdbStartEvent.FreshPortDiscovered -> {
                    snapshot.copy(
                        state = AdbStartState.CONNECTING_ADB,
                        activePort = event.port,
                        portSource = event.source
                    )
                }

                is AdbStartEvent.PortDiscoveryTimedOut -> {
                    if (event.fallbackPort != null && event.fallbackPort > 0) {
                        snapshot.copy(
                            state = AdbStartState.CONNECTING_ADB,
                            activePort = event.fallbackPort,
                            portSource = "FALLBACK"
                        )
                    } else if (snapshot.retryCount < snapshot.maxRetries) {
                        snapshot.copy(
                            state = AdbStartState.RETRYING,
                            lastFailureKind = AdbStartFailureKind.PORT_NOT_FOUND,
                            lastErrorMessage = "mDNS port discovery timed out"
                        )
                    } else {
                        snapshot.copy(
                            state = AdbStartState.FAILED,
                            lastFailureKind = AdbStartFailureKind.PORT_NOT_FOUND,
                            lastErrorMessage = "mDNS port discovery timed out"
                        )
                    }
                }

                is AdbStartEvent.PortDiscoveryFailed -> {
                    val kind = if (event.isHotspotRefused) {
                        AdbStartFailureKind.HOTSPOT_REFUSED
                    } else {
                        AdbStartFailureKind.PORT_NOT_FOUND
                    }
                    if (snapshot.isLockedStart || snapshot.retryCount >= snapshot.maxRetries) {
                        snapshot.copy(
                            state = AdbStartState.FAILED,
                            lastFailureKind = kind,
                            lastErrorMessage = event.reason
                        )
                    } else {
                        snapshot.copy(
                            state = AdbStartState.RETRYING,
                            lastFailureKind = kind,
                            lastErrorMessage = event.reason
                        )
                    }
                }

                else -> snapshot
            }

            AdbStartState.CONNECTING_ADB -> when (event) {
                is AdbStartEvent.ConnectingAdb -> {
                    snapshot.copy(
                        connectAttemptsUsed = event.attemptIndex
                    )
                }

                is AdbStartEvent.Authenticated -> {
                    snapshot.copy(
                        state = AdbStartState.AUTHENTICATING,
                        activePort = event.port,
                        connectAttemptsUsed = 0
                    )
                }

                is AdbStartEvent.PairingRejected -> {
                    // Terminal: device rejected pairing certificate
                    snapshot.copy(
                        state = AdbStartState.PAIRING_REQUIRED,
                        lastFailureKind = AdbStartFailureKind.PAIRING_REQUIRED,
                        lastErrorMessage = event.message
                    )
                }

                is AdbStartEvent.ConnectionFailed -> {
                    val updatedFailed = snapshot.failedPorts + event.port
                    // If candidates remain, try next candidate
                    if (snapshot.candidatePorts.isNotEmpty()) {
                        val next = snapshot.candidatePorts.first()
                        val rem = snapshot.candidatePorts.drop(1)
                        snapshot.copy(
                            state = AdbStartState.HUNTING_PORT,
                            activePort = next,
                            candidatePorts = rem,
                            failedPorts = updatedFailed,
                            lastFailureKind = event.failureKind,
                            lastErrorMessage = event.message
                        )
                    } else if (snapshot.freshSearchesUsed < snapshot.maxFreshSearches && !snapshot.commandSent) {
                        // Search for a fresh wireless port
                        snapshot.copy(
                            state = AdbStartState.HUNTING_PORT,
                            activePort = null,
                            failedPorts = updatedFailed,
                            freshSearchesUsed = snapshot.freshSearchesUsed + 1,
                            lastFailureKind = event.failureKind,
                            lastErrorMessage = event.message
                        )
                    } else if (snapshot.retryCount < snapshot.maxRetries) {
                        snapshot.copy(
                            state = AdbStartState.RETRYING,
                            failedPorts = updatedFailed,
                            lastFailureKind = event.failureKind,
                            lastErrorMessage = event.message
                        )
                    } else {
                        snapshot.copy(
                            state = AdbStartState.FAILED,
                            failedPorts = updatedFailed,
                            lastFailureKind = event.failureKind,
                            lastErrorMessage = event.message
                        )
                    }
                }

                else -> snapshot
            }

            AdbStartState.AUTHENTICATING -> when (event) {
                is AdbStartEvent.LaunchingDaemon -> {
                    snapshot.copy(
                        state = AdbStartState.LAUNCHING_DAEMON,
                        activePort = event.port
                    )
                }

                is AdbStartEvent.PairingRejected -> {
                    snapshot.copy(
                        state = AdbStartState.PAIRING_REQUIRED,
                        lastFailureKind = AdbStartFailureKind.PAIRING_REQUIRED,
                        lastErrorMessage = event.message
                    )
                }

                is AdbStartEvent.ConnectionFailed -> {
                    snapshot.copy(
                        state = AdbStartState.RETRYING,
                        failedPorts = snapshot.failedPorts + (snapshot.activePort ?: 0),
                        lastFailureKind = event.failureKind,
                        lastErrorMessage = event.message
                    )
                }

                else -> snapshot
            }

            AdbStartState.LAUNCHING_DAEMON -> when (event) {
                is AdbStartEvent.CommandDispatched -> {
                    // Command sent! Once commandSent is true, retrying over another port is forbidden
                    // because the server may already be coming up.
                    snapshot.copy(
                        state = AdbStartState.AWAITING_BINDER,
                        commandSent = true,
                        activePort = event.port
                    )
                }

                is AdbStartEvent.CommandFailed -> {
                    if (snapshot.retryCount < snapshot.maxRetries) {
                        snapshot.copy(
                            state = AdbStartState.RETRYING,
                            lastFailureKind = AdbStartFailureKind.GENERIC_IO,
                            lastErrorMessage = event.reason
                        )
                    } else {
                        snapshot.copy(
                            state = AdbStartState.FAILED,
                            lastFailureKind = AdbStartFailureKind.GENERIC_IO,
                            lastErrorMessage = event.reason
                        )
                    }
                }

                else -> snapshot
            }

            AdbStartState.AWAITING_BINDER -> when (event) {
                is AdbStartEvent.BinderReceived -> {
                    snapshot.copy(
                        state = AdbStartState.SUCCEEDED,
                        daemonPid = event.pid,
                        lastFailureKind = AdbStartFailureKind.NONE,
                        lastErrorMessage = null
                    )
                }

                is AdbStartEvent.BinderTimedOut -> {
                    // Starter command was already sent.
                    if (snapshot.isLockedStart) {
                        // Pre-unlock starts do not loop; let unlock retry
                        snapshot.copy(
                            state = AdbStartState.RETRYING,
                            lastFailureKind = AdbStartFailureKind.BINDER_TIMEOUT,
                            lastErrorMessage = "Awaiting binder timed out in locked start"
                        )
                    } else if (snapshot.retryCount < snapshot.maxRetries) {
                        snapshot.copy(
                            state = AdbStartState.RETRYING,
                            lastFailureKind = AdbStartFailureKind.BINDER_TIMEOUT,
                            lastErrorMessage = "Awaiting binder timed out"
                        )
                    } else {
                        snapshot.copy(
                            state = AdbStartState.FAILED,
                            lastFailureKind = AdbStartFailureKind.BINDER_TIMEOUT,
                            lastErrorMessage = "Awaiting binder timed out after max retries"
                        )
                    }
                }

                else -> snapshot
            }

            AdbStartState.RETRYING -> when (event) {
                is AdbStartEvent.RetryScheduled -> {
                    if (snapshot.retryCount >= snapshot.maxRetries) {
                        snapshot.copy(
                            state = AdbStartState.FAILED,
                            lastFailureKind = AdbStartFailureKind.MAX_RETRIES_EXCEEDED,
                            lastErrorMessage = "Maximum retry attempts (${snapshot.maxRetries}) exceeded"
                        )
                    } else {
                        snapshot.copy(
                            state = AdbStartState.CHECKING_PREREQUISITES,
                            retryCount = snapshot.retryCount + 1,
                            commandSent = false,
                            activePort = null,
                            connectAttemptsUsed = 0
                        )
                    }
                }

                else -> snapshot
            }

            AdbStartState.SUCCEEDED,
            AdbStartState.DEFERRED,
            AdbStartState.FAILED,
            AdbStartState.CANCELLED,
            AdbStartState.PAIRING_REQUIRED -> snapshot
        }
    }

    private fun getEventGeneration(event: AdbStartEvent): Long? = when (event) {
        is AdbStartEvent.StartRequested -> null
        is AdbStartEvent.PrerequisitesChecked -> event.generation
        is AdbStartEvent.PortCandidateValidated -> event.generation
        is AdbStartEvent.FreshPortDiscovered -> event.generation
        is AdbStartEvent.PortDiscoveryTimedOut -> event.generation
        is AdbStartEvent.PortDiscoveryFailed -> event.generation
        is AdbStartEvent.ConnectingAdb -> event.generation
        is AdbStartEvent.ConnectionFailed -> event.generation
        is AdbStartEvent.PairingRejected -> event.generation
        is AdbStartEvent.Authenticated -> event.generation
        is AdbStartEvent.LaunchingDaemon -> event.generation
        is AdbStartEvent.CommandDispatched -> event.generation
        is AdbStartEvent.CommandFailed -> event.generation
        is AdbStartEvent.BinderReceived -> event.generation
        is AdbStartEvent.BinderTimedOut -> event.generation
        is AdbStartEvent.RetryScheduled -> event.generation
        is AdbStartEvent.CancelRequested -> event.generation
        is AdbStartEvent.Reset -> null
    }
}
