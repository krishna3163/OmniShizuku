package moe.shizuku.manager.callrecorder

import android.telecom.Call
import android.telecom.InCallService
import android.telephony.TelephonyManager

class CallRecorderInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallDetectionOrchestrator.getInstance(this)
            .handlePhoneState(TelephonyManager.EXTRA_STATE_RINGING, null)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallDetectionOrchestrator.getInstance(this)
            .handlePhoneState(TelephonyManager.EXTRA_STATE_IDLE, null)
    }
}
