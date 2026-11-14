package moe.shizuku.manager.security

import java.util.UUID

/**
 * Domain-specific immutable state model for Biometric and Protected Action Authentication.
 * Pure Kotlin, zero Android framework dependencies for deterministic JVM testing.
 */
enum class AuthState {
    LOCKED,
    AUTHENTICATION_REQUESTED,
    AUTHENTICATING,
    AUTHENTICATED_APP_SESSION,
    AUTHENTICATED_ACTION_SESSION,
    EXPIRED,
    CANCELLED,
    FAILED,
    LOCKED_OUT
}

enum class AuthScope {
    APP_OPEN,
    PROTECTED_ACTION
}

interface MonotonicClock {
    fun elapsedRealtime(): Long
}

object SystemMonotonicClock : MonotonicClock {
    override fun elapsedRealtime(): Long = System.nanoTime() / 1_000_000L
}

data class AuthSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val scope: AuthScope,
    val actionName: String? = null,
    val authenticatedElapsedRealtimeMs: Long,
    val expiresElapsedRealtimeMs: Long = Long.MAX_VALUE,
    val isInvalidated: Boolean = false
) {
    fun isValid(currentElapsedRealtimeMs: Long): Boolean {
        if (isInvalidated) return false
        return currentElapsedRealtimeMs < expiresElapsedRealtimeMs
    }
}

data class AuthSnapshot(
    val state: AuthState = AuthState.LOCKED,
    val generation: Long = 0L,
    val appOpenSession: AuthSession? = null,
    val actionSessions: Map<String, AuthSession> = emptyMap(),
    val pendingAction: String? = null,
    val pendingScope: AuthScope? = null,
    val backgroundTimestampElapsedRealtimeMs: Long = 0L,
    val isChangingConfig: Boolean = false,
    val lastError: String? = null
) {
    val isAppOpenUnlocked: Boolean
        get() = state == AuthState.AUTHENTICATED_APP_SESSION || appOpenSession != null

    fun isActionAuthorized(actionName: String, currentElapsedRealtimeMs: Long): Boolean {
        val session = actionSessions[actionName] ?: return false
        return session.isValid(currentElapsedRealtimeMs)
    }
}

sealed interface AuthEvent {
    data class RequestAppOpenAuth(val generation: Long? = null) : AuthEvent
    data class RequestActionAuth(val actionName: String, val generation: Long? = null) : AuthEvent
    data class PromptDisplayed(val generation: Long) : AuthEvent
    data class AuthenticationSucceeded(
        val generation: Long,
        val timeoutSeconds: Int = 0
    ) : AuthEvent
    data class AuthenticationFailedSoft(val generation: Long, val reason: String) : AuthEvent
    data class AuthenticationCancelled(val generation: Long) : AuthEvent
    data class AuthenticationLockout(val generation: Long, val permanent: Boolean) : AuthEvent
    data class ActivityBackgrounded(val isChangingConfig: Boolean) : AuthEvent
    data class ActivityForegrounded(val timeoutSeconds: Int) : AuthEvent
    data class ConfigurationChangeStarted(val changing: Boolean) : AuthEvent
    data class InvalidateAction(val actionName: String) : AuthEvent
    data object InvalidateAll : AuthEvent
    data object Reset : AuthEvent
}

object AuthStateMachine {

    fun reduce(
        snapshot: AuthSnapshot,
        event: AuthEvent,
        clock: MonotonicClock = SystemMonotonicClock
    ): AuthSnapshot {
        val now = clock.elapsedRealtime()

        return when (event) {
            is AuthEvent.RequestAppOpenAuth -> {
                snapshot.copy(
                    state = AuthState.AUTHENTICATION_REQUESTED,
                    pendingScope = AuthScope.APP_OPEN,
                    pendingAction = null,
                    generation = snapshot.generation + 1,
                    lastError = null
                )
            }

            is AuthEvent.RequestActionAuth -> {
                snapshot.copy(
                    state = AuthState.AUTHENTICATION_REQUESTED,
                    pendingScope = AuthScope.PROTECTED_ACTION,
                    pendingAction = event.actionName,
                    generation = snapshot.generation + 1,
                    lastError = null
                )
            }

            is AuthEvent.PromptDisplayed -> {
                if (event.generation != snapshot.generation) return snapshot // reject stale prompt
                snapshot.copy(state = AuthState.AUTHENTICATING)
            }

            is AuthEvent.AuthenticationSucceeded -> {
                if (event.generation != snapshot.generation) return snapshot // reject stale callback

                val expiresAt = if (event.timeoutSeconds > 0) {
                    now + (event.timeoutSeconds * 1000L)
                } else {
                    Long.MAX_VALUE
                }

                when (snapshot.pendingScope) {
                    AuthScope.PROTECTED_ACTION -> {
                        val act = snapshot.pendingAction ?: "unknown"
                        val session = AuthSession(
                            scope = AuthScope.PROTECTED_ACTION,
                            actionName = act,
                            authenticatedElapsedRealtimeMs = now,
                            expiresElapsedRealtimeMs = expiresAt
                        )
                        val updatedActions = snapshot.actionSessions.toMutableMap()
                        updatedActions[act] = session
                        snapshot.copy(
                            state = AuthState.AUTHENTICATED_ACTION_SESSION,
                            actionSessions = updatedActions,
                            pendingAction = null,
                            pendingScope = null,
                            lastError = null
                        )
                    }
                    AuthScope.APP_OPEN, null -> {
                        val session = AuthSession(
                            scope = AuthScope.APP_OPEN,
                            authenticatedElapsedRealtimeMs = now,
                            expiresElapsedRealtimeMs = expiresAt
                        )
                        snapshot.copy(
                            state = AuthState.AUTHENTICATED_APP_SESSION,
                            appOpenSession = session,
                            pendingAction = null,
                            pendingScope = null,
                            backgroundTimestampElapsedRealtimeMs = 0L,
                            lastError = null
                        )
                    }
                }
            }

            is AuthEvent.AuthenticationFailedSoft -> {
                if (event.generation != snapshot.generation) return snapshot
                snapshot.copy(
                    state = AuthState.FAILED,
                    lastError = event.reason
                )
            }

            is AuthEvent.AuthenticationCancelled -> {
                if (event.generation != snapshot.generation) return snapshot
                val fallbackState = if (snapshot.appOpenSession?.isValid(now) == true) {
                    AuthState.AUTHENTICATED_APP_SESSION
                } else {
                    AuthState.LOCKED
                }
                snapshot.copy(
                    state = fallbackState,
                    pendingAction = null,
                    pendingScope = null,
                    lastError = "Authentication cancelled by user"
                )
            }

            is AuthEvent.AuthenticationLockout -> {
                if (event.generation != snapshot.generation) return snapshot
                snapshot.copy(
                    state = AuthState.LOCKED_OUT,
                    appOpenSession = null,
                    actionSessions = emptyMap(),
                    pendingAction = null,
                    pendingScope = null,
                    lastError = if (event.permanent) "Biometric permanently locked" else "Biometric temporarily locked"
                )
            }

            is AuthEvent.ActivityBackgrounded -> {
                if (event.isChangingConfig) {
                    snapshot.copy(isChangingConfig = true)
                } else {
                    snapshot.copy(
                        backgroundTimestampElapsedRealtimeMs = now,
                        isChangingConfig = false
                    )
                }
            }

            is AuthEvent.ActivityForegrounded -> {
                if (snapshot.isChangingConfig) {
                    snapshot.copy(isChangingConfig = false)
                } else {
                    val bgTime = snapshot.backgroundTimestampElapsedRealtimeMs
                    if (bgTime > 0L) {
                        val elapsedMs = now - bgTime
                        val timeoutMs = event.timeoutSeconds * 1000L
                        if (event.timeoutSeconds <= 0 || elapsedMs >= timeoutMs) {
                            // Expired while in background
                            snapshot.copy(
                                state = AuthState.LOCKED,
                                appOpenSession = null,
                                actionSessions = emptyMap(),
                                backgroundTimestampElapsedRealtimeMs = 0L,
                                lastError = "Session expired in background"
                            )
                        } else {
                            snapshot.copy(backgroundTimestampElapsedRealtimeMs = 0L)
                        }
                    } else {
                        snapshot
                    }
                }
            }

            is AuthEvent.ConfigurationChangeStarted -> {
                snapshot.copy(isChangingConfig = event.changing)
            }

            is AuthEvent.InvalidateAction -> {
                val updated = snapshot.actionSessions.toMutableMap()
                updated.remove(event.actionName)
                snapshot.copy(actionSessions = updated)
            }

            AuthEvent.InvalidateAll -> {
                snapshot.copy(
                    state = AuthState.LOCKED,
                    appOpenSession = null,
                    actionSessions = emptyMap(),
                    pendingAction = null,
                    pendingScope = null,
                    backgroundTimestampElapsedRealtimeMs = 0L
                )
            }

            AuthEvent.Reset -> {
                AuthSnapshot(generation = snapshot.generation + 1)
            }
        }
    }
}
