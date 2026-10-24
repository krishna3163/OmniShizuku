package moe.shizuku.manager.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ktx.isUserUnlockedCompat
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.receiver.ShizukuReceiverStarter.WorkerState
import moe.shizuku.manager.start.AdbStartRun
import moe.shizuku.manager.utils.Diag

/**
 * Starts the server over wireless debugging before the first unlock.
 *
 * The ordinary start is WorkManager work, and WorkManager's database lives in credential-encrypted
 * storage that cannot be read until the user has unlocked for the first time - which, after a
 * restart, is when they get to type the PIN, half a minute or more after the system is ready for
 * everything this start needs. This service is direct-boot aware and runs the same attempt
 * ([AdbStartRun]) without WorkManager, from LOCKED_BOOT_COMPLETED, so the server can be up by the
 * time the PIN is in.
 *
 * One attempt, no backoff: a failure ends it with the reason in the log, and the start made at the
 * unlock (or here, if the unlock has already come) is the retry. It counts in the same
 * [AdbStartRun.inFlight] as the worker, so every other request - the unlock, BOOT_COMPLETED, a
 * client asking for the binder, the watchdog - joins it instead of beginning another.
 *
 * What the platform allows is the part that cannot be checked here: a foreground service may be
 * started from LOCKED_BOOT_COMPLETED (the exemption from the background-start restrictions
 * names it), and `specialUse` is not among the types Android 15 refuses to a boot receiver
 * (dataSync, camera, mediaPlayback, phoneCall, mediaProjection, microphone). Whether the local-only
 * hotspot, wireless debugging and the TLS handshake with adbd work before the unlock is for a
 * device to say.
 */
class LockedBootStartService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val method = intent?.getIntExtra(EXTRA_START_METHOD, ShizukuSettings.getStartMethod())
            ?: ShizukuSettings.getStartMethod()

        // First, whatever follows: the platform gives a service started with startForegroundService
        // a few seconds to call this, and one that does not is a crash.
        if (!enterForeground(method)) {
            AdbStartRun.releaseReservation()
            stopSelf()
            return START_NOT_STICKY
        }

        synchronized(lock) {
            if (job?.isActive == true) {
                // A second request while one is running. It was counted when it was asked for, and
                // the run that is already going is the one that stands.
                AdbStartRun.releaseReservation()
                Diag.info(AppConstants.TAG, "Start before the unlock: one is already running")
                return START_NOT_STICKY
            }
            // ATOMIC, because the cancel that matters can arrive before this coroutine is
            // dispatched: `cancelRun` cancels a job it can see as active, and a DEFAULT job
            // cancelled in that window never runs its body at all. The reservation that
            // `AdbStartRun.reserveAttempt` took is given back in `execute`'s finally, and
            // `runStart`'s finally is what calls stopSelf, so both would be skipped and the
            // service, its notification and the in-flight count would stay up for good.
            // With ATOMIC the body always starts; the first suspension point throws the
            // cancellation and the existing finally blocks clean up.
            job = scope.launch(start = CoroutineStart.ATOMIC) { runStart(method) }
        }
        return START_NOT_STICKY
    }

    private suspend fun runStart(method: Int) {
        val host = object : AdbStartRun.Host {
            override fun notify(state: WorkerState) =
                ShizukuReceiverStarter.updateNotification(applicationContext, state, method)

            // Already there: this is a foreground service.
            override suspend fun promoteToForeground() = Unit

            override fun promoteToForegroundAsync() = Unit

            override fun stoppedState(): WorkerState = WorkerState.STOPPED
        }

        try {
            val outcome = AdbStartRun(applicationContext, host, lockedStart = true)
                .execute(method, reserved = true)
            if (outcome != AdbStartRun.Outcome.SUCCESS) afterFailure()
        } catch (e: CancellationException) {
            Diag.info(AppConstants.TAG, "Start before the unlock was cancelled")
            throw e
        } catch (e: Throwable) {
            Diag.error(AppConstants.TAG, "Start before the unlock failed unexpectedly", e)
        } finally {
            // After the follow-up, not before: the service is what keeps the process in the
            // foreground until the start that takes over has been handed to WorkManager.
            stopSelf()
        }
    }

    /**
     * The start did not happen. Before the unlock the unlock will ask again, and nothing is in
     * flight by then; after it, nobody is going to, so ask now.
     */
    private fun afterFailure() {
        if (applicationContext.isUserUnlockedCompat()) {
            Diag.info(
                AppConstants.TAG,
                "Start before the unlock failed and the user has unlocked since; starting normally"
            )
            ShizukuReceiverStarter.start(applicationContext, reason = "locked-start-failed")
        } else {
            Diag.info(AppConstants.TAG, "Start before the unlock failed; the unlock will start it")
        }
    }

    /** Puts the service in the foreground with the start's notification; false if it may not. */
    private fun enterForeground(method: Int): Boolean = try {
        val notification = ShizukuReceiverStarter.buildNotification(this, null, method)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                ShizukuReceiverStarter.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(ShizukuReceiverStarter.NOTIFICATION_ID, notification)
        }
        true
    } catch (e: Exception) {
        // ForegroundServiceStartNotAllowedException and the like: an ordinary outcome for a service
        // the platform did not expect to be allowed, and worth knowing about on a device.
        Diag.warn(AppConstants.TAG, "Start before the unlock could not enter the foreground", e)
        false
    }

    override fun onDestroy() {
        scope.cancel()
        synchronized(lock) { job = null }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_START_METHOD = "start_method"

        private val lock = Any()

        @Volatile
        private var job: Job? = null

        /** Whether a start is running in this service at the moment. */
        @JvmStatic
        fun isRunning(): Boolean = job?.isActive == true

        /**
         * Asks for the service, and says whether it was asked for.
         *
         * The attempt is counted as in flight from here, not from when the service gets going, so a
         * request that arrives in between joins it. The count is given back if the service is not
         * going to run.
         */
        @JvmStatic
        fun start(context: Context, method: Int): Boolean {
            AdbStartRun.reserveAttempt()
            val started = try {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, LockedBootStartService::class.java)
                        .putExtra(EXTRA_START_METHOD, method)
                )
            } catch (e: Exception) {
                Diag.warn(AppConstants.TAG, "Start before the unlock could not start its service", e)
                null
            }
            if (started == null) {
                AdbStartRun.releaseReservation()
                return false
            }
            Diag.info(AppConstants.TAG, "Start before the unlock: service requested")
            return true
        }

        /**
         * Cancels the start running in the service and calls [then] once it has ended.
         *
         * Ended, not merely asked to: the blocking calls in the attempt do not stop for a
         * cancellation, and what makes it safe to begin another start is that this one has reached
         * the check in front of the starter and found itself cancelled, or finished. [then] runs on
         * a background thread. With nothing running it is called at once.
         */
        @JvmStatic
        fun cancelRun(reason: String, then: () -> Unit) {
            val running = synchronized(lock) { job?.takeIf { it.isActive } }
            if (running == null) {
                then()
                return
            }
            Diag.info(AppConstants.TAG, "Cancelling the start before the unlock: $reason")
            running.invokeOnCompletion {
                CoroutineScope(Dispatchers.Default).launch { then() }
            }
            running.cancel()
        }
    }
}
