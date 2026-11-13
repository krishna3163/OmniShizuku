package moe.shizuku.manager.adb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbPairingStateMachineTest {

    @Test
    fun validProtocolSequence() {
        var s = AdbPairingState.READY
        assertTrue(AdbPairingStateMachine.canStart(s))
        s = AdbPairingStateMachine.onStart(s)
        assertEquals(AdbPairingState.EXCHANGING_MSGS, s)
        s = AdbPairingStateMachine.onMsgsExchanged(s, true)
        assertEquals(AdbPairingState.EXCHANGING_PEER_INFO, s)
        s = AdbPairingStateMachine.onPeerInfoExchanged(s, true)
        assertEquals(AdbPairingState.STOPPED, s)
    }

    @Test
    fun invalidTransitionsAndFailures() {
        assertFalse(AdbPairingStateMachine.canStart(AdbPairingState.EXCHANGING_MSGS))
        assertFalse(AdbPairingStateMachine.canStart(AdbPairingState.EXCHANGING_PEER_INFO))
        assertFalse(AdbPairingStateMachine.canStart(AdbPairingState.STOPPED))
        assertEquals(AdbPairingState.STOPPED, AdbPairingStateMachine.onMsgsExchanged(AdbPairingState.EXCHANGING_MSGS, false))
        assertEquals(AdbPairingState.STOPPED, AdbPairingStateMachine.onPeerInfoExchanged(AdbPairingState.EXCHANGING_PEER_INFO, false))
        // wrong-state events are ignored/no-op to current
        assertEquals(AdbPairingState.READY, AdbPairingStateMachine.onMsgsExchanged(AdbPairingState.READY, true))
        assertEquals(AdbPairingState.STOPPED, AdbPairingStateMachine.onPeerInfoExchanged(AdbPairingState.STOPPED, true))
    }

    @Test
    fun closeAlwaysStopsAndRepeatedCloseSafe() {
        AdbPairingState.values().forEach { st ->
            val closed = AdbPairingStateMachine.onClose(st)
            assertEquals(AdbPairingState.STOPPED, closed)
            assertEquals(AdbPairingState.STOPPED, AdbPairingStateMachine.onClose(closed))
        }
    }

    @Test
    fun deterministicStressSequence() {
        fun run(seed: Int): AdbPairingState {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            var s = AdbPairingState.READY
            repeat(200) {
                s = when (next() % 5) {
                    0 -> AdbPairingStateMachine.onStart(s)
                    1 -> AdbPairingStateMachine.onMsgsExchanged(s, next() % 2 == 0)
                    2 -> AdbPairingStateMachine.onPeerInfoExchanged(s, next() % 2 == 0)
                    3 -> AdbPairingStateMachine.onClose(s)
                    else -> if (AdbPairingStateMachine.canStart(s)) AdbPairingStateMachine.onStart(s) else s
                }
                // invariant: stopped cannot continue exchanging
                if (s == AdbPairingState.STOPPED) {
                    assertFalse(AdbPairingStateMachine.canStart(s))
                }
            }
            s
        }
        assertEquals(run(123), run(123))
        run(5)
    }
}
