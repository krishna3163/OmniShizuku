package moe.shizuku.manager.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HelperNotificationPolicyTest {

    private lateinit var policy: HelperNotificationPolicy

    @Before
    fun setUp() {
        policy = HelperNotificationPolicy(debounceMs = 5000L)
    }

    @Test
    fun shouldPost_allowsNewEventWhenEnabledAndAllowed() {
        val event = HelperEvent(
            type = HelperEventType.SERVER_STOPPED,
            serverGeneration = 1L,
            errorIdentity = "test_error"
        )
        val should = policy.shouldPost(event, nowElapsed = 1000L, helperEnabled = true, notificationsAllowed = true)
        assertTrue(should)
    }

    @Test
    fun shouldPost_rejectsWhenPreferenceDisabled() {
        val event = HelperEvent(type = HelperEventType.SERVER_STOPPED)
        val should = policy.shouldPost(event, nowElapsed = 1000L, helperEnabled = false, notificationsAllowed = true)
        assertFalse("Must not post when helper notifications preference is false", should)
    }

    @Test
    fun shouldPost_rejectsWhenNotificationsNotAllowed() {
        val event = HelperEvent(type = HelperEventType.SERVER_STOPPED)
        val should = policy.shouldPost(event, nowElapsed = 1000L, helperEnabled = true, notificationsAllowed = false)
        assertFalse("Must not post when system notifications permission is denied", should)
    }

    @Test
    fun shouldPost_rejectsIdenticalDuplicateEvent() {
        val event = HelperEvent(
            type = HelperEventType.SERVER_STOPPED,
            serverGeneration = 1L,
            errorIdentity = "crash",
            recoveryAttemptId = 1L
        )
        assertTrue(policy.shouldPost(event, nowElapsed = 1000L, helperEnabled = true, notificationsAllowed = true))
        policy.recordPosted(event, nowElapsed = 1000L)

        // Exact same event should be rejected as duplicate
        assertFalse(policy.shouldPost(event, nowElapsed = 10000L, helperEnabled = true, notificationsAllowed = true))
    }

    @Test
    fun shouldPost_enforcesDebounceWindowForSameType() {
        val event1 = HelperEvent(
            type = HelperEventType.NEEDS_ATTENTION,
            serverGeneration = 1L,
            errorIdentity = "err_1"
        )
        val event2 = HelperEvent(
            type = HelperEventType.NEEDS_ATTENTION,
            serverGeneration = 1L,
            errorIdentity = "err_2"
        )

        assertTrue(policy.shouldPost(event1, nowElapsed = 1000L, helperEnabled = true, notificationsAllowed = true))
        policy.recordPosted(event1, nowElapsed = 1000L)

        // 2 seconds later (within 5s debounce window)
        assertFalse(policy.shouldPost(event2, nowElapsed = 3000L, helperEnabled = true, notificationsAllowed = true))

        // 6 seconds later (outside debounce window)
        assertTrue(policy.shouldPost(event2, nowElapsed = 7001L, helperEnabled = true, notificationsAllowed = true))
    }

    @Test
    fun onDismissed_suppressesExactEventUntilStateChange() {
        val event = HelperEvent(
            type = HelperEventType.NEEDS_ATTENTION,
            serverGeneration = 1L,
            errorIdentity = "circuit_breaker"
        )
        policy.recordPosted(event, nowElapsed = 1000L)
        policy.onDismissed(helperDedupeKey(event))

        // Even after debounce expires, user-dismissed notification must not re-post
        assertFalse(policy.shouldPost(event, nowElapsed = 20000L, helperEnabled = true, notificationsAllowed = true))

        // Different error or new attempt allows posting
        val newEvent = event.copy(recoveryAttemptId = 2L)
        assertTrue(policy.shouldPost(newEvent, nowElapsed = 21000L, helperEnabled = true, notificationsAllowed = true))
    }

    @Test
    fun onHealthy_signalsOutdatedCancellationAndResetsDismissed() {
        val failureEvent = HelperEvent(
            type = HelperEventType.SERVER_STOPPED,
            errorIdentity = "crashed"
        )
        policy.recordPosted(failureEvent, nowElapsed = 1000L)
        policy.onDismissed(helperDedupeKey(failureEvent))

        // When server becomes healthy:
        val shouldCancelOutdated = policy.onHealthy()
        assertTrue("Previous failure notification should be cancelled on healthy", shouldCancelOutdated)

        // Subsequent failure should now be allowed (dismissed was cleared)
        val recoveredEvent = HelperEvent(type = HelperEventType.RECOVERED)
        policy.recordPosted(recoveredEvent, nowElapsed = 5000L)
        assertEquals(helperDedupeKey(recoveredEvent), policy.lastKey)
    }
}
