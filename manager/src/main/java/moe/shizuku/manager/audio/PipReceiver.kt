package moe.shizuku.manager.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.shizuku.manager.audio.PipStarterActivity.Companion.handlePip

class PipReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.handlePip()
    }
}

