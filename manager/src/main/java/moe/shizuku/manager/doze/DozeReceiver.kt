package moe.shizuku.manager.doze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DozeReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "DozeReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received action: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        if (DozeManager.isAggressiveDozeEnabled(context)) {
                            Log.i(TAG, "Screen off: Triggering instant Deep Doze")
                            DozeManager.forceDeepIdle()
                            if (DozeManager.isMotionKillEnabled(context)) {
                                DozeManager.disableMotion()
                            }
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        if (DozeManager.isAggressiveDozeEnabled(context)) {
                            Log.i(TAG, "Screen on: Unforcing Doze")
                            DozeManager.unforceIdle()
                            if (DozeManager.isMotionKillEnabled(context)) {
                                DozeManager.enableMotion()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling Doze broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
