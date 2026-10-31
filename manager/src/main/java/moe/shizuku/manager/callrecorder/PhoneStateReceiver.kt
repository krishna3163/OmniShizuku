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

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        CallDetectionOrchestrator.getInstance(context.applicationContext).handlePhoneState(state, number)
    }
}

/**
 * Orchestrates call detection and automatic recording decisions.
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

    private var currentCallData: CallData? = null

    fun handlePhoneState(state: String, number: String?) {
        val autoRecord = RecordingStateHolder.autoRecordEnabled.value
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                currentCallData = CallData(
                    number = number,
                    direction = if (number != null) CallDirection.INCOMING else CallDirection.UNKNOWN,
                    startTimeMs = System.currentTimeMillis()
                )
                if (autoRecord && RecordingStateHolder.recordIncoming.value && shouldRecord(number)) {
                    startRecording()
                }
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (currentCallData == null) {
                    currentCallData = CallData(
                        number = number,
                        direction = CallDirection.OUTGOING,
                        startTimeMs = System.currentTimeMillis()
                    )
                }
                if (autoRecord && RecordingStateHolder.recordOutgoing.value && shouldRecord(number)) {
                    startRecording()
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (RecordingStateHolder.isRecording.value) {
                    stopRecording()
                }
                currentCallData = null
            }
        }
    }

    private fun shouldRecord(number: String?): Boolean {
        if (number.isNullOrBlank() && RecordingStateHolder.ignoreAnonymous.value) {
            return false
        }
        return true
    }

    private fun startRecording() {
        val intent = Intent(context, RecordingForegroundService::class.java).apply {
            action = RecordingForegroundService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopRecording() {
        val intent = Intent(context, RecordingForegroundService::class.java).apply {
            action = RecordingForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }
}
