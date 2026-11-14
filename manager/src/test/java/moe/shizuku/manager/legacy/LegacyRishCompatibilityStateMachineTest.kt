package moe.shizuku.manager.legacy

import org.junit.Assert.*
import org.junit.Test

class LegacyRishCompatibilityStateMachineTest {

    @Test
    fun initial_disabledByDefault() {
        val s = LegacyRishCompatibilityStateMachine.initial(false)
        assertEquals(LegacyRishCompatibilityState.DISABLED, s.state)
        assertFalse(s.enabled)
        assertEquals(0L, s.generation)
    }

    @Test
    fun initial_enabledWaitsForServer() {
        val s = LegacyRishCompatibilityStateMachine.initial(true)
        assertEquals(LegacyRishCompatibilityState.WAITING_FOR_SERVER, s.state)
        assertTrue(s.enabled)
    }

    @Test
    fun enable_whileServerStopped_waits() {
        var s = LegacyRishCompatibilityStateMachine.initial(false)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(true))
        assertTrue(s.enabled)
        assertEquals(1L, s.generation)
        assertEquals(LegacyRishCompatibilityState.WAITING_FOR_SERVER, s.state)
    }

    @Test
    fun enable_thenServerStarts_transitionsToStarting() {
        var s = LegacyRishCompatibilityStateMachine.initial(false)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(true))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(true, true))
        assertEquals(LegacyRishCompatibilityState.STARTING, s.state)
        assertTrue(s.serverRunning)
        assertTrue(s.binderAvailable)
    }

    @Test
    fun serviceStarted_withServerReady_becomesActive() {
        var s = LegacyRishCompatibilityStateMachine.initial(false)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(true))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(true, true))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServiceStarted(s.generation))
        assertEquals(LegacyRishCompatibilityState.ACTIVE, s.state)
        assertTrue(s.serviceRunning)
        assertTrue(s.notificationActive)
    }

    @Test
    fun serverShutdown_fromActive_waitsForServer() {
        var s = active()
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(false, false))
        assertEquals(LegacyRishCompatibilityState.WAITING_FOR_SERVER, s.state)
        assertFalse(s.serverRunning)
    }

    @Test
    fun binderDeath_fromActive_reconnects_thenBinderReturns() {
        var s = active()
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.BinderDied(s.generation))
        assertEquals(LegacyRishCompatibilityState.RECONNECTING, s.state)
        assertFalse(s.binderAvailable)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(true, true))
        assertEquals(LegacyRishCompatibilityState.ACTIVE, s.state)
    }

    @Test
    fun binderDeath_thenServerLost_waits() {
        var s = active()
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.BinderDied(s.generation))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(false, false))
        assertEquals(LegacyRishCompatibilityState.WAITING_FOR_SERVER, s.state)
    }

    @Test
    fun disable_whileActive_stops() {
        var s = active()
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(false))
        assertEquals(LegacyRishCompatibilityState.STOPPING, s.state)
        assertFalse(s.enabled)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServiceStopped(s.generation))
        assertEquals(LegacyRishCompatibilityState.DISABLED, s.state)
        assertFalse(s.serviceRunning)
    }

    @Test
    fun duplicateStartStop_areIdempotent() {
        var s = LegacyRishCompatibilityStateMachine.initial(false)
        val a = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(false))
        assertEquals(s.copy(lastEvent = s.lastEvent), LegacyRishCompatibilityStateMachine.reduce(a, LegacyRishEvent.Refresh).copy(lastEvent = a.lastEvent))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(true))
        val g = s.generation
        val b = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(true))
        assertEquals(g, b.generation)
        val c = LegacyRishCompatibilityStateMachine.reduce(b, LegacyRishEvent.PreferenceChanged(true))
        assertEquals(g, c.generation)
    }

    @Test
    fun staleCallbacks_areRejected() {
        var s = active()
        val stale = s.generation - 1
        val before = s
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServiceStopped(stale))
        assertSameState(before, s)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.Error("boom", stale))
        assertSameState(before, s)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.BinderDied(stale))
        assertSameState(before, s)
    }

    @Test
    fun error_thenServerReady_retries() {
        var s = active()
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.Error("x", s.generation))
        assertEquals(LegacyRishCompatibilityState.ERROR, s.state)
        assertEquals("x", s.lastError)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(true, true))
        assertEquals(LegacyRishCompatibilityState.STARTING, s.state)
        assertNull(s.lastError)
    }

    @Test
    fun rapidStartStop_noInvalidState() {
        var s = LegacyRishCompatibilityStateMachine.initial(false)
        val seq = listOf(
            LegacyRishEvent.PreferenceChanged(true),
            LegacyRishEvent.ServerStateChanged(true, true),
            LegacyRishEvent.PreferenceChanged(false),
            LegacyRishEvent.PreferenceChanged(true),
            LegacyRishEvent.ServerStateChanged(false, false),
            LegacyRishEvent.ServerStateChanged(true, true),
        )
        for (e in seq) {
            s = LegacyRishCompatibilityStateMachine.reduce(s, e)
            assertNotNull(s.state)
            assertTrue(s.generation >= 0)
        }
        assertTrue(s.enabled)
        assertTrue(s.state == LegacyRishCompatibilityState.STARTING || s.state == LegacyRishCompatibilityState.WAITING_FOR_SERVER || s.state == LegacyRishCompatibilityState.ACTIVE)
    }

    @Test
    fun serviceStoppedUnexpectedly_restartsWhenReady() {
        var s = active()
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServiceStopped(s.generation))
        assertEquals(LegacyRishCompatibilityState.STARTING, s.state)
        assertFalse(s.serviceRunning)
    }

    @Test
    fun processRecreation_enabledButServerUnknown_waits() {
        val s = LegacyRishCompatibilityStateMachine.initial(true)
        assertEquals(LegacyRishCompatibilityState.WAITING_FOR_SERVER, s.state)
        assertTrue(s.enabled)
    }

    private fun active(): LegacyRishSnapshot {
        var s = LegacyRishCompatibilityStateMachine.initial(false)
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.PreferenceChanged(true))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServerStateChanged(true, true))
        s = LegacyRishCompatibilityStateMachine.reduce(s, LegacyRishEvent.ServiceStarted(s.generation))
        return s
    }

    private fun assertSameState(a: LegacyRishSnapshot, b: LegacyRishSnapshot) {
        assertEquals(a.state, b.state)
        assertEquals(a.enabled, b.enabled)
        assertEquals(a.serverRunning, b.serverRunning)
        assertEquals(a.binderAvailable, b.binderAvailable)
        assertEquals(a.serviceRunning, b.serviceRunning)
        assertEquals(a.notificationActive, b.notificationActive)
        assertEquals(a.generation, b.generation)
        assertEquals(a.lastEvent, b.lastEvent)
        assertEquals(a.lastError, b.lastError)
    }
}
