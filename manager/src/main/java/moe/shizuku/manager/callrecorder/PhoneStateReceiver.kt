package moe.shizuku.manager.callrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/**
 * PhoneStateReceiver listens for phone call state changes and forwards them
 * to the call recording decision engine.
 */
class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        CallDetectionOrchestrator.getInstance(context.applicationContext).handlePhoneState(state, number)
    }
}

/**
 * Orchestrates call detection and recording decisions.
 */
class CallDetectionOrchestrator private constructor(private val context: Context) {
    companion object {
        @Volatile private var instance: CallDetectionOrchestrator? = null
        fun getInstance(context: Context): CallDetectionOrchestrator {
            return instance ?: synchronized(this) {
                instance ?: CallDetectionOrchestrator(context.applicationContext).also { instance = it }
            }
        }
    }

    private var isAutoRecordEnabled = false
    private var currentCallData: CallData? = null

    fun handlePhoneState(state: String, number: String?) {
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                currentCallData = CallData(number = number,
                    direction = if (number != null) CallDirection.INCOMING else CallDirection.UNKNOWN,
                    startTimeMs = System.currentTimeMillis())
                if (isAutoRecordEnabled && shouldRecord(number)) {
                    startRecording()
                }
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (currentCallData == null) {
                    currentCallData = CallData(number = number,
                        direction = CallDirection.OUTGOING,
                        startTimeMs = System.currentTimeMillis())
                }
                if (isAutoRecordEnabled && shouldRecord(number)) {
                    startRecording()
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                stopRecording()
                currentCallData = null
            }
        }
    }

    private fun shouldRecord(number: String?): Boolean {
        return true
    }

    private fun startRecording() {
        val intent = android.content.Intent(context, RecordingForegroundService::class.java)
        intent.action = "com.krishna.omnishizuku.action.START_RECORDING"
        context.startForegroundService(intent)
    }

    private fun stopRecording() {
        val intent = android.content.Intent(context, RecordingForegroundService::class.java)
        intent.action = "com.krishna.omnishizuku.action.STOP_RECORDING"
        context.startService(intent)
    }

    fun setAutoRecord(enabled: Boolean) {
        isAutoRecordEnabled = enabled
    }
}
