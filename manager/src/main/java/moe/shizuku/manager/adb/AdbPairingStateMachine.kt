package moe.shizuku.manager.adb

/** Pure ADB pairing protocol state for deterministic validation. */
enum class AdbPairingState { READY, EXCHANGING_MSGS, EXCHANGING_PEER_INFO, STOPPED }

object AdbPairingStateMachine {
    fun canStart(state: AdbPairingState): Boolean = state == AdbPairingState.READY

    fun onStart(state: AdbPairingState): AdbPairingState =
        if (state == AdbPairingState.READY) AdbPairingState.EXCHANGING_MSGS else state

    fun onMsgsExchanged(state: AdbPairingState, ok: Boolean): AdbPairingState = when {
        state != AdbPairingState.EXCHANGING_MSGS -> state
        ok -> AdbPairingState.EXCHANGING_PEER_INFO
        else -> AdbPairingState.STOPPED
    }

    fun onPeerInfoExchanged(state: AdbPairingState, ok: Boolean): AdbPairingState = when {
        state != AdbPairingState.EXCHANGING_PEER_INFO -> state
        ok -> AdbPairingState.STOPPED
        else -> AdbPairingState.STOPPED
    }

    fun onClose(state: AdbPairingState): AdbPairingState = AdbPairingState.STOPPED
}
