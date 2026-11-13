package moe.shizuku.manager.display

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DisplayRollbackWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val store = DisplayConfigurationStore(applicationContext)
        val pending = store.pending() ?: return Result.success()
        if (pending.state != DisplayRollbackState.AWAITING_CONFIRMATION) {
            return Result.success()
        }
        if (pending.deadlineMillis > System.currentTimeMillis()) {
            return Result.retry()
        }
        val restored = DisplayRollbackManager.restorePending(applicationContext)
        return if (restored) Result.success() else Result.retry()
    }
}
