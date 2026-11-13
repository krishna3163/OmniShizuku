package moe.shizuku.manager.display

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DisplayRollbackManager {

    private const val WORK_NAME = "display-configuration-rollback"

    suspend fun beginTemporary(
        context: Context,
        change: DisplayChange,
        modes: List<DisplayModeInfo>,
        timeoutMillis: Long = 30_000L
    ): Result = withContext(Dispatchers.IO) {
        DisplayConfigurationValidator.validate(change, modes)?.let {
            return@withContext Result(false, it, null)
        }
        val store = DisplayConfigurationStore(context)
        if (store.pending() != null) {
            return@withContext Result(false, "A display change is already awaiting confirmation.", null)
        }
        val before = DisplayRepository.readShellSnapshot()
        store.savePending(
            PendingDisplayChange(
                before = before,
                deadlineMillis = System.currentTimeMillis() + timeoutMillis,
                state = DisplayRollbackState.APPLYING
            )
        )
        val applied = DisplayRepository.apply(context, change)
        if (!applied) {
            store.clearPending()
            return@withContext Result(false, "The display rejected this configuration.", null)
        }
        val pending = store.pending() ?: return@withContext Result(false, "Recovery state was lost.", null)
        store.savePending(pending.copy(state = DisplayRollbackState.AWAITING_CONFIRMATION))
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DisplayRollbackWorker>()
                .setInitialDelay(timeoutMillis, TimeUnit.MILLISECONDS)
                .build()
        )
        Result(true, null, pending.copy(state = DisplayRollbackState.AWAITING_CONFIRMATION))
    }

    suspend fun confirm(context: Context): Boolean = withContext(Dispatchers.IO) {
        val store = DisplayConfigurationStore(context)
        val pending = store.pending() ?: return@withContext false
        if (pending.deadlineMillis <= System.currentTimeMillis()) {
            return@withContext restorePending(context)
        }
        store.appendHistory(pending.before)
        store.clearPending()
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        true
    }

    suspend fun reject(context: Context): Boolean = restorePending(context)

    suspend fun restoreLast(context: Context): Boolean = withContext(Dispatchers.IO) {
        val store = DisplayConfigurationStore(context)
        val snapshot = store.history().lastOrNull() ?: return@withContext false
        DisplayRepository.restore(context, snapshot)
    }

    suspend fun restorePending(context: Context): Boolean = withContext(Dispatchers.IO) {
        val store = DisplayConfigurationStore(context)
        val pending = store.pending() ?: return@withContext false
        store.savePending(pending.copy(state = DisplayRollbackState.ROLLING_BACK))
        val restored = DisplayRepository.restore(context, pending.before)
        if (restored) {
            store.appendHistory(pending.before)
            store.clearPending()
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        } else {
            store.savePending(pending.copy(state = DisplayRollbackState.FAILED))
        }
        restored
    }

    data class Result(
        val applied: Boolean,
        val error: String?,
        val pending: PendingDisplayChange?
    )
}
