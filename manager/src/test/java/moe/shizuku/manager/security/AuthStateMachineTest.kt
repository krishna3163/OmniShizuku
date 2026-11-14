package moe.shizuku.manager.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeMonotonicClock(var currentTimeMs: Long = 1000L) : MonotonicClock {
    override fun elapsedRealtime(): Long = currentTimeMs
    fun advance(ms: Long) { currentTimeMs += ms }
}

class AuthStateMachineTest {

    private val clock = FakeMonotonicClock(100_000L)

    // 1. Initial state is LOCKED
    @Test
    fun testInitialStateIsLocked() {
        val initial = AuthSnapshot()
        assertEquals(AuthState.LOCKED, initial.state)
        assertFalse(initial.isAppOpenUnlocked)
        assertNull(initial.appOpenSession)
    }

    // 2. App open auth request -> AUTHENTICATION_REQUESTED
    @Test
    fun testAppOpenAuthRequest() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        assertEquals(AuthState.AUTHENTICATION_REQUESTED, s1.state)
        assertEquals(AuthScope.APP_OPEN, s1.pendingScope)
        assertEquals(1L, s1.generation)
    }

    // 3. App open auth success -> AUTHENTICATED_APP_SESSION
    @Test
    fun testAppOpenAuthSuccess() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.PromptDisplayed(generation = 1L), clock)
        assertEquals(AuthState.AUTHENTICATING, s2.state)

        val s3 = AuthStateMachine.reduce(s2, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 60), clock)
        assertEquals(AuthState.AUTHENTICATED_APP_SESSION, s3.state)
        assertTrue(s3.isAppOpenUnlocked)
        assertNotNull(s3.appOpenSession)
    }

    // 4. App open session validity check within timeout
    @Test
    fun testAppOpenSessionValidityWithinTimeout() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 30), clock)

        assertTrue(s2.appOpenSession?.isValid(clock.elapsedRealtime()) == true)
        clock.advance(15_000L)
        assertTrue(s2.appOpenSession?.isValid(clock.elapsedRealtime()) == true)
    }

    // 5. App open session expiration when backgrounded beyond timeout
    @Test
    fun testAppOpenSessionExpirationWhenBackgroundedBeyondTimeout() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 30), clock)

        val sBg = AuthStateMachine.reduce(s2, AuthEvent.ActivityBackgrounded(isChangingConfig = false), clock)
        clock.advance(31_000L) // Advance beyond 30s timeout

        val sFg = AuthStateMachine.reduce(sBg, AuthEvent.ActivityForegrounded(timeoutSeconds = 30), clock)
        assertEquals(AuthState.LOCKED, sFg.state)
        assertFalse(sFg.isAppOpenUnlocked)
        assertNull(sFg.appOpenSession)
    }

    // 6. App open session instant expiration when timeout is 0
    @Test
    fun testAppOpenSessionInstantExpirationWhenTimeoutZero() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 0), clock)

        val sBg = AuthStateMachine.reduce(s2, AuthEvent.ActivityBackgrounded(isChangingConfig = false), clock)
        clock.advance(100L)

        val sFg = AuthStateMachine.reduce(sBg, AuthEvent.ActivityForegrounded(timeoutSeconds = 0), clock)
        assertEquals(AuthState.LOCKED, sFg.state)
        assertFalse(sFg.isAppOpenUnlocked)
    }

    // 7. Scope isolation: App open does NOT authorize PERMISSIONS
    @Test
    fun testScopeIsolationAppOpenDoesNotAuthorizeAction() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 60), clock)

        assertTrue(s2.isAppOpenUnlocked)
        // Individual protected actions must NOT be automatically authorized!
        assertFalse(s2.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
        assertFalse(s2.isActionAuthorized("SERVER_TOGGLE", clock.elapsedRealtime()))
    }

    // 8. Action auth request -> AUTHENTICATION_REQUESTED for specific action
    @Test
    fun testActionAuthRequest() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestActionAuth("PERMISSIONS"), clock)
        assertEquals(AuthState.AUTHENTICATION_REQUESTED, s1.state)
        assertEquals(AuthScope.PROTECTED_ACTION, s1.pendingScope)
        assertEquals("PERMISSIONS", s1.pendingAction)
    }

    // 9. Action auth success -> AUTHENTICATED_ACTION_SESSION for that action only
    @Test
    fun testActionAuthSuccess() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestActionAuth("PERMISSIONS"), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 60), clock)

        assertEquals(AuthState.AUTHENTICATED_ACTION_SESSION, s2.state)
        assertTrue(s2.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
    }

    // 10. Action session does NOT authorize a different protected action
    @Test
    fun testActionSessionDoesNotAuthorizeOtherActions() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestActionAuth("PERMISSIONS"), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 60), clock)

        assertTrue(s2.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
        assertFalse(s2.isActionAuthorized("DEVICE_OWNER", clock.elapsedRealtime()))
        assertFalse(s2.isActionAuthorized("STUB_MANAGEMENT", clock.elapsedRealtime()))
    }

    // 11. User cancels biometric prompt -> CANCELLED / returns to LOCKED
    @Test
    fun testUserCancelsBiometricPromptFromLocked() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationCancelled(generation = 1L), clock)
        assertEquals(AuthState.LOCKED, s2.state)
        assertFalse(s2.isAppOpenUnlocked)
    }

    // 12. User cancels action prompt when app-open was already authenticated returns to AUTHENTICATED_APP_SESSION
    @Test
    fun testUserCancelsActionPromptWhenAppOpenUnlocked() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 300), clock)

        // Request action
        val s3 = AuthStateMachine.reduce(s2, AuthEvent.RequestActionAuth("PERMISSIONS"), clock)
        assertEquals(2L, s3.generation)

        // Cancel action prompt
        val s4 = AuthStateMachine.reduce(s3, AuthEvent.AuthenticationCancelled(generation = 2L), clock)
        assertEquals(AuthState.AUTHENTICATED_APP_SESSION, s4.state)
        assertTrue(s4.isAppOpenUnlocked)
        assertFalse(s4.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
    }

    // 13. Biometric lockout -> LOCKED_OUT and invalidates sessions
    @Test
    fun testBiometricLockout() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationLockout(generation = 1L, permanent = true), clock)
        assertEquals(AuthState.LOCKED_OUT, s2.state)
        assertNull(s2.appOpenSession)
        assertTrue(s2.actionSessions.isEmpty())
    }

    // 14. InvalidateAll clears both app-open and action-scoped sessions
    @Test
    fun testInvalidateAllClearsSessions() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 300), clock)
        val s3 = AuthStateMachine.reduce(s2, AuthEvent.RequestActionAuth("PERMISSIONS"), clock)
        val s4 = AuthStateMachine.reduce(s3, AuthEvent.AuthenticationSucceeded(generation = 2L, timeoutSeconds = 300), clock)

        val invalidated = AuthStateMachine.reduce(s4, AuthEvent.InvalidateAll, clock)
        assertEquals(AuthState.LOCKED, invalidated.state)
        assertNull(invalidated.appOpenSession)
        assertTrue(invalidated.actionSessions.isEmpty())
    }

    // 15. InvalidateAction removes only the specified action session
    @Test
    fun testInvalidateActionRemovesOnlySpecifiedAction() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestActionAuth("PERMISSIONS"), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 300), clock)
        val s3 = AuthStateMachine.reduce(s2, AuthEvent.RequestActionAuth("SERVER_TOGGLE"), clock)
        val s4 = AuthStateMachine.reduce(s3, AuthEvent.AuthenticationSucceeded(generation = 2L, timeoutSeconds = 300), clock)

        assertTrue(s4.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
        assertTrue(s4.isActionAuthorized("SERVER_TOGGLE", clock.elapsedRealtime()))

        val s5 = AuthStateMachine.reduce(s4, AuthEvent.InvalidateAction("PERMISSIONS"), clock)
        assertFalse(s5.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
        assertTrue(s5.isActionAuthorized("SERVER_TOGGLE", clock.elapsedRealtime()))
    }

    // 16. Activity configuration change (rotation) does NOT trigger background timeout
    @Test
    fun testConfigurationChangeDoesNotTriggerBackgroundTimeout() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 10), clock)

        // Rotation: stopped with isChangingConfig = true
        val sRot = AuthStateMachine.reduce(s2, AuthEvent.ActivityBackgrounded(isChangingConfig = true), clock)
        clock.advance(15_000L) // More than 10s

        val sRestored = AuthStateMachine.reduce(sRot, AuthEvent.ActivityForegrounded(timeoutSeconds = 10), clock)
        assertEquals(AuthState.AUTHENTICATED_APP_SESSION, sRestored.state)
        assertTrue(sRestored.isAppOpenUnlocked)
    }

    // 17. Activity backgrounding starts monotonic background timer
    @Test
    fun testActivityBackgroundingMonotonicTimer() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 30), clock)

        val sBg = AuthStateMachine.reduce(s2, AuthEvent.ActivityBackgrounded(isChangingConfig = false), clock)
        assertEquals(100_000L, sBg.backgroundTimestampElapsedRealtimeMs)
    }

    // 18. Activity foregrounding within timeout preserves session
    @Test
    fun testForegroundingWithinTimeoutPreservesSession() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock)
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.AuthenticationSucceeded(generation = 1L, timeoutSeconds = 30), clock)

        val sBg = AuthStateMachine.reduce(s2, AuthEvent.ActivityBackgrounded(isChangingConfig = false), clock)
        clock.advance(10_000L) // 10s < 30s timeout

        val sFg = AuthStateMachine.reduce(sBg, AuthEvent.ActivityForegrounded(timeoutSeconds = 30), clock)
        assertEquals(AuthState.AUTHENTICATED_APP_SESSION, sFg.state)
        assertEquals(0L, sFg.backgroundTimestampElapsedRealtimeMs)
    }

    // 19. Stale prompt callback rejected via generation token
    @Test
    fun testStalePromptCallbackRejected() {
        val s0 = AuthSnapshot()
        val s1 = AuthStateMachine.reduce(s0, AuthEvent.RequestAppOpenAuth(), clock) // gen 1
        val s2 = AuthStateMachine.reduce(s1, AuthEvent.RequestActionAuth("PERMISSIONS"), clock) // gen 2

        // Stale success callback with gen 1 should be ignored
        val sIgnored = AuthStateMachine.reduce(s2, AuthEvent.AuthenticationSucceeded(generation = 1L), clock)
        assertEquals(s2, sIgnored)
        assertFalse(sIgnored.isActionAuthorized("PERMISSIONS", clock.elapsedRealtime()))
    }

    // 20. Deterministic stress sequence
    @Test
    fun testDeterministicStressSequence() {
        fun run(seed: Int): AuthSnapshot {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            val fakeClock = FakeMonotonicClock(10_000L)
            var s = AuthSnapshot()
            repeat(300) {
                val actionIdx = next() % 10
                fakeClock.advance(100L)
                val event = when (actionIdx) {
                    0 -> AuthEvent.RequestAppOpenAuth()
                    1 -> AuthEvent.RequestActionAuth("ACT_${next() % 3}")
                    2 -> AuthEvent.PromptDisplayed(s.generation)
                    3 -> AuthEvent.AuthenticationSucceeded(s.generation, timeoutSeconds = 30)
                    4 -> AuthEvent.AuthenticationFailedSoft(s.generation, "Soft")
                    5 -> AuthEvent.AuthenticationCancelled(s.generation)
                    6 -> AuthEvent.ActivityBackgrounded(isChangingConfig = next() % 2 == 0)
                    7 -> AuthEvent.ActivityForegrounded(timeoutSeconds = 30)
                    8 -> AuthEvent.InvalidateAction("ACT_0")
                    9 -> AuthEvent.InvalidateAll
                    else -> AuthEvent.Reset
                }
                s = AuthStateMachine.reduce(s, event, fakeClock)
            }
            return s
        }
        assertEquals(run(55), run(55))
        run(9)
    }
}
