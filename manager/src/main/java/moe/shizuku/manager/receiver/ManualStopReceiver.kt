package moe.shizuku.manager.receiver

import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.service.LockedBootStartService
import moe.shizuku.manager.utils.ShizukuStateMachine
import moe.shizuku.manager.worker.AdbStartWorker
import rikka.shizuku.Shizuku

class ManualStopReceiver : AuthenticatedReceiver() {
    override fun onAuthenticated(context: Context, intent: Intent) {
        // The runtime package: see [ManualStartReceiver] for why the build-time one is wrong here.
        val applicationId = context.packageName
        if (intent.action != "${applicationId}.STOP") return

        // Mark the stop as intentional so the watchdog's dead-check doesn't undo
        // it, and drop any queued background start.
        ShizukuSettings.setManuallyStopped(true)
        WorkManager.getInstance(context).cancelUniqueWork(AdbStartWorker.UNIQUE_WORK_NAME)
        // And the start the foreground service began before the unlock, if it is still going.
        LockedBootStartService.cancelRun("stopped by the user") {}

        if (!ShizukuStateMachine.isRunning()) return

        ShizukuStateMachine.set(ShizukuStateMachine.State.STOPPING)
        runCatching { Shizuku.exit() }
    }
}