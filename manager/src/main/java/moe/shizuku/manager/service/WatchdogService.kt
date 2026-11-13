package moe.shizuku.manager.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import moe.shizuku.manager.R
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbStarter
import moe.shizuku.manager.ktx.isUserUnlockedCompat
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.receiver.StartNowDecision
import moe.shizuku.manager.receiver.decideStartNow
import moe.shizuku.manager.receiver.WatchdogAlarmReceiver
import moe.shizuku.manager.start.runningMethodLabel
import moe.shizuku.manager.start.runningMethodSuffix
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.start.AdbStartRun
import moe.shizuku.manager.worker.AdbStartWorker
import moe.shizuku.manager.utils.BootTimeline
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.SettingsPage
import moe.shizuku.manager.utils.ShizukuStateMachine
import moe.shizuku.manager.utils.createChannelCompat
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import moe.shizuku.manager.service.HidingWatchService
import moe.shizuku.manager.utils.Diag

class WatchdogService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val recoveryState = AtomicReference(WatchdogRecoveryState.IDLE)

    /** Only one restart at a time, and not one every few seconds for the same outage. */
    private val restartInFlight = AtomicBoolean(false)

    @Volatile
    private var lastRestartAt = 0L

    /**
     * Counts failed starts, and stops the retrying once a run of them means the start is the
     * thing that is broken. See [StartCircuitBreaker] for the failure it answers.
     */
    private val breaker = StartCircuitBreaker(BREAKER_MAX_ATTEMPTS, BREAKER_WINDOW_MS)

    private fun transition(event: WatchdogRecoveryEvent): WatchdogRecoveryState =
        recoveryState.updateAndGet { WatchdogRecoveryMachine.reduce(it, event) }

    private fun isPendingOrRestarting(): Boolean = when (recoveryState.get()) {
        WatchdogRecoveryState.PENDING_RESTART, WatchdogRecoveryState.RESTARTING -> true
        else -> false
    }

    private fun markAwaitingRecovery() {
        if (recoveryState.get() == WatchdogRecoveryState.GAVE_UP) return
        transition(WatchdogRecoveryEvent.CrashDetected)
    }

    private val stateListener: (ShizukuStateMachine.State) -> Unit = { state ->
        when (state) {
            ShizukuStateMachine.State.CRASHED ->
                when {
                    WatchdogGuard.consumeExpectedDeath() ->
                        Diag.debug(TAG, "Server went down as expected, nothing to report")

                    ShizukuSettings.getManuallyStopped() -> {
                        Diag.debug(TAG, "Server was stopped on purpose, nothing to report")
                        transition(WatchdogRecoveryEvent.ManualStop)
                    }

                    else -> {
                        Diag.warn(TAG, "Server died: reporting it and starting it again")
                        showCrashNotification()
                        markAwaitingRecovery()
                        attemptRestart()
                    }
                }

            ShizukuStateMachine.State.RUNNING -> {
                val prev = recoveryState.getAndSet(WatchdogRecoveryState.IDLE)
                resetBreaker()
                if (prev == WatchdogRecoveryState.AWAITING_RECOVERY || prev == WatchdogRecoveryState.GAVE_UP) {
                    showRecoveryNotification()
                }
            }

            else -> Unit
        }
    }

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_USER_PRESENT) return
            if (isPendingOrRestarting()) {
                Diag.debug(TAG, "Screen unlocked with pending restart retrying now")
                attemptRestart()
            } else {
                checkServerAndRestartIfDead()
            }
        }
    }

    private fun checkServerAndRestartIfDead() {
        serviceScope.launch {
            delay(BINDER_GRACE_MS)
            if (ShizukuSettings.getManuallyStopped()) return@launch
            if (!ShizukuSettings.isWatchdogStartAllowed()) {
                Diag.debug(TAG, "Server not started since boot and start on boot is off; not starting it")
                return@launch
            }
            if (WatchdogGuard.isExpectingDeath()) return@launch
            when (ShizukuStateMachine.get()) {
                ShizukuStateMachine.State.STARTING,
                ShizukuStateMachine.State.STOPPING -> {
                    delay(IN_FLIGHT_GRACE_MS)
                    if (ShizukuSettings.getManuallyStopped()) return@launch
                }
                else -> Unit
            }
            if (ShizukuStateMachine.update() != ShizukuStateMachine.State.RUNNING) {
                Diag.debug(TAG, "Server not running while watchdog active attempting restart")
                attemptRestart()
            }
        }
    }

    private fun startRecoveryLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(RECOVERY_POLL_MS)
                if (ShizukuSettings.getManuallyStopped()) continue
                if (!ShizukuSettings.isWatchdogStartAllowed()) continue
                if (ShizukuStateMachine.isRunning()) continue
                checkServerAndRestartIfDead()
            }
        }
    }

    private fun attemptRestart() {
        if (WatchdogGuard.isExpectingDeath() || ShizukuSettings.getManuallyStopped()) {
            Diag.debug(TAG, "Restart attempt skipped: one is expected, or the stop was deliberate")
            return
        }

        when (decideStartNow(
            applicationContext.isUserUnlockedCompat(),
            ShizukuSettings.isWatchdogStartAllowed(),
            ShizukuSettings.getManuallyStopped()
        )) {
            StartNowDecision.START -> Unit
            StartNowDecision.SKIP_STOPPED -> {
                Diag.debug(TAG, "Restart attempt skipped: one is expected, or the stop was deliberate")
                return
            }
            StartNowDecision.SKIP_NOT_WANTED -> {
                Diag.debug(TAG, "Restart attempt skipped: the server has not run since boot")
                return
            }
            StartNowDecision.DEFER_UNTIL_UNLOCK, StartNowDecision.START_LOCKED -> {
                Diag.debug(TAG, "Restart deferred until the user is unlocked${BootTimeline.suffix()}")
                if (recoveryState.get() != WatchdogRecoveryState.GAVE_UP) transition(WatchdogRecoveryEvent.RestartDeferred)
                return
            }
        }

        if (breaker.open) {
            Diag.debug(TAG, "Restart attempt skipped: the breaker is open")
            transition(WatchdogRecoveryEvent.BreakerOpened)
            return
        }

        if (HidingWatchService.holding()) {
            Diag.debug(TAG, "Restart attempt skipped: hiding is holding the debugging toggle off")
            if (recoveryState.get() != WatchdogRecoveryState.GAVE_UP) transition(WatchdogRecoveryEvent.RestartDeferred)
            return
        }

        if (AdbStartRun.startInProgress()) {
            Diag.debug(TAG, "Restart attempt skipped: a start is already in flight")
            if (recoveryState.get() != WatchdogRecoveryState.GAVE_UP) transition(WatchdogRecoveryEvent.RestartDeferred)
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastRestartAt < RESTART_COOLDOWN_MS) {
            Diag.debug(TAG, "Restart attempt skipped: one was made moments ago")
            if (recoveryState.get() != WatchdogRecoveryState.GAVE_UP) transition(WatchdogRecoveryEvent.RestartDeferred)
            return
        }
        if (!restartInFlight.compareAndSet(false, true)) {
            Diag.debug(TAG, "Restart attempt skipped: one is already in flight")
            return
        }
        lastRestartAt = now
        recordAttempt(now)
        if (recoveryState.get() == WatchdogRecoveryState.GAVE_UP) {
            restartInFlight.set(false)
            return
        }
        recoveryState.set(WatchdogRecoveryState.RESTARTING)

        WorkManager.getInstance(applicationContext).cancelUniqueWork(AdbStartWorker.UNIQUE_WORK_NAME)

        serviceScope.launch {
            try {
                val tcpPort = EnvironmentUtils.getAdbTcpPort()
                val usbMethod =
                    ShizukuSettings.getStartMethod() == ShizukuSettings.StartMethod.USB
                if (usbMethod && tcpPort > 0 && EnvironmentUtils.isUsbDebuggingEnabled()) {
                    recoveryState.compareAndSet(WatchdogRecoveryState.RESTARTING, WatchdogRecoveryState.AWAITING_RECOVERY)
                    AdbStarter.startAdb(applicationContext, tcpPort)
                    Starter.waitForBinder()
                    AdbStarter.logStartedPids(tcpPort)
                } else {
                    if (recoveryState.get() != WatchdogRecoveryState.GAVE_UP) transition(WatchdogRecoveryEvent.RestartDeferred)
                    ShizukuReceiverStarter.start(
                        applicationContext,
                        forceStart = true,
                        reason = "watchdog-restart"
                    )
                }
            } catch (e: Exception) {
                Diag.warn(TAG, "Direct restart failed, falling back", e)
                if (recoveryState.get() != WatchdogRecoveryState.GAVE_UP) transition(WatchdogRecoveryEvent.RestartDeferred)
                if (ShizukuStateMachine.markStartFailed() != ShizukuStateMachine.State.RUNNING) {
                    ShizukuReceiverStarter.start(
                        applicationContext,
                        forceStart = true,
                        reason = "watchdog-fallback"
                    )
                }
            } finally {
                restartInFlight.set(false)
            }

            delay(RECOVERY_CHECK_MS)
            awaitStartEnd()
            withTimeoutOrNull(SERVER_BOOT_GRACE_MS) {
                ShizukuStateMachine.asFlow().first { it == ShizukuStateMachine.State.RUNNING }
            }
            ShizukuStateMachine.update()
            if (ShizukuStateMachine.isRunning()) {
                transition(WatchdogRecoveryEvent.Reset)
            } else if (!ShizukuSettings.getManuallyStopped() && recoveryState.get() != WatchdogRecoveryState.GAVE_UP) {
                Diag.warn(TAG, "Restart did not bring the server back, leaving the retry armed")
                transition(WatchdogRecoveryEvent.RestartDeferred)
                markAwaitingRecovery()
            }
        }
    }

    private suspend fun awaitStartEnd() {
        val limit =
            if (ShizukuSettings.getForceWirelessDebugging()) FORCED_IN_FLIGHT_GRACE_MS
            else IN_FLIGHT_GRACE_MS
        var waited = 0L
        while (waited < limit && AdbStartRun.startInProgress()) {
            delay(START_END_POLL_MS)
            waited += START_END_POLL_MS
        }
    }

    private fun recordAttempt(now: Long) {
        if (!breaker.record(now)) return
        transition(WatchdogRecoveryEvent.BreakerOpened)
        Diag.warn(
            TAG,
            "Giving up: $BREAKER_MAX_ATTEMPTS starts in under " +
                "${BREAKER_WINDOW_MS / 1000}s and the server is still down"
        )
        showGaveUpNotification()
    }

    private fun resetBreaker() {
        breaker.reset()
    }


    override fun onCreate() {
        super.onCreate()
        isRunning.set(true)
        sendWatchdogChangedBroadcast(applicationContext, true)
        // The state it starts in is worth a line: a watchdog that came up after the server
        // was already gone is the case it cannot see a transition for, and that is what the
        // unlock probe is for.
        Diag.info(TAG, "Watchdog started, server is ${ShizukuStateMachine.get()}")
        // A notice that the watchdog was not running is answered by the watchdog running: this is
        // every path back in, so clearing it here covers the user's tap, a boot and a start asked
        // for by anything else, without each of them having to remember to.
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(NOTIFICATION_ID_NOT_RUNNING)
        ShizukuStateMachine.addListener(stateListener)
        registerReceiver(screenOnReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
        startRecoveryLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_NOW) {
            // The breaker exists to stop the app retrying on its own, not to refuse the user.
            // A start from here goes through the same path as the home screen's button, so the
            // server is asked for afresh and the count starts over - and if that start works,
            // the RUNNING transition clears the breaker and posts nothing further.
            Diag.info(TAG, "Start asked for from the notification, resetting the breaker")
            resetBreaker()
            ShizukuReceiverStarter.start(
                applicationContext,
                forceStart = true,
                userInitiated = true,
                reason = "watchdog-notification"
            )
            return START_STICKY
        }
        if (intent?.action == "ACTION_STOP_SERVICE") {
            // User explicitly turned the watchdog off via the notification persist
            // the setting directly instead of calling setWatchdog() (which would
            // redundantly call stop() while we're already stopping via stopSelf).
            ShizukuSettings.getPreferences().edit()
                .putBoolean(ShizukuSettings.Keys.KEY_WATCHDOG, false).apply()
            // This path writes the preference directly rather than going through
            // ShizukuSettings.setWatchdog, so it has to cancel the backstop itself: an alarm left
            // armed would find the watchdog gone and start it again, switching back on the thing
            // the user just switched off.
            WatchdogAlarmReceiver.cancel(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID_WATCHDOG,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_ID_WATCHDOG,
                buildNotification()
            )
        }
        checkServerAndRestartIfDead()
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        transition(WatchdogRecoveryEvent.Reset)
        ShizukuStateMachine.removeListener(stateListener)
        runCatching { unregisterReceiver(screenOnReceiver) }
        isRunning.set(false)
        sendWatchdogChangedBroadcast(applicationContext, false)
        // Do NOT persist watchdog=false here: onDestroy runs both when the user
        // manually stops Shizuku (temporary) and when the notification stop button
        // is used (permanent). Only the notification stop button and the settings
        // toggle should persist the preference.
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        // The v2 suffix is the point, not an oversight: a channel's settings are frozen
        // once it exists, so turning the badge off on the old id would do nothing for
        // anyone who already has it. The service is always running, so a dot over it says
        // nothing except that something is permanently there.
        val channelId = "shizuku_watchdog_v2"
        val channelName = "Watchdog"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createChannelCompat(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW,
            showBadge = false
        )

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or 
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        val launchPendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WatchdogService::class.java).apply {
            action = "ACTION_STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Say how Shizuku is running, like every other status notification does.
        val runningMethod = runningMethodLabel()

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.watchdog_running))
            .apply { if (runningMethod != null) setContentText(runningMethod) }
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentIntent(launchPendingIntent)
            .addAction(
                R.drawable.ic_close_24,
                getString(R.string.watchdog_turn_off),
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun showCrashNotification() {
        val channelId = CRASH_CHANNEL_ID
        val channelName = "Crash Reports"

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createChannelCompat(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        val learnMoreIntent = Intent(Intent.ACTION_VIEW).apply {
            setData(Uri.parse("https://github.com/thedjchi/Shizuku/wiki#shizuku-keeps-stopping-randomly"))
        }
        val learnMorePendingIntent = PendingIntent.getActivity(this, 0, learnMoreIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val disableIntent = SettingsPage.Notifications.NotificationChannel.buildIntent(applicationContext)
        val disablePendingIntent = PendingIntent.getActivity(this, 0, disableIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.watchdog_shizuku_crashed_title))
            .setContentText(
                getString(R.string.watchdog_shizuku_crashed_text) + runningMethodSuffix()
            )
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentIntent(learnMorePendingIntent)
            .setAutoCancel(true)
            .addAction(0, getString(R.string.watchdog_shizuku_crashed_action_turn_off_alerts), disablePendingIntent)
            .build()

        nm.notify(NOTIFICATION_ID_CRASH, notification)
    }

    /**
     * The other half of the crash notification, and only ever the other half: it is posted
     * when a restart answered an outage the user was already told about, so nothing about a
     * service that came and went on its own reaches anyone.
     */
    private fun showRecoveryNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createChannelCompat(CRASH_CHANNEL_ID, "Crash Reports", NotificationManager.IMPORTANCE_DEFAULT)

        val notification = NotificationCompat.Builder(this, CRASH_CHANNEL_ID)
            .setContentTitle(getString(R.string.watchdog_shizuku_recovered_title))
            .setContentText(
                getString(R.string.watchdog_shizuku_recovered_text) + runningMethodSuffix()
            )
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID_RECOVERY, notification)
        // The alarm it answers is spent either way.
        nm.cancel(NOTIFICATION_ID_CRASH)
    }

    /**
     * The other outcome of an outage, and the one that used to be silent.
     *
     * Says how many starts were tried rather than only that it stopped trying, because the
     * count is the evidence: one failed start is a phone having a bad moment, five in twenty
     * seconds is a reason that will not fix itself. The action is a plain Start, which goes
     * through the same path as the home screen's button and so clears [gaveUp] with it - the
     * breaker is meant to stop the app retrying on its own, never to take the decision away.
     */
    private fun showGaveUpNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createChannelCompat(CRASH_CHANNEL_ID, "Crash Reports", NotificationManager.IMPORTANCE_DEFAULT)

        val notification = NotificationCompat.Builder(this, CRASH_CHANNEL_ID)
            .setContentTitle(getString(R.string.watchdog_given_up_title))
            .setContentText(
                getString(R.string.watchdog_given_up_text, BREAKER_MAX_ATTEMPTS) +
                    runningMethodSuffix()
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    getString(R.string.watchdog_given_up_text, BREAKER_MAX_ATTEMPTS) +
                        runningMethodSuffix()
                )
            )
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)
            .addAction(
                0,
                getString(R.string.watchdog_given_up_action_start),
                PendingIntent.getService(
                    this,
                    2,
                    Intent(this, WatchdogService::class.java).setAction(ACTION_START_NOW),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

        nm.notify(NOTIFICATION_ID_GAVE_UP, notification)
        // It replaces the crash notice rather than stacking under it: both describe the same
        // outage, and this is the later and more useful word on it.
        nm.cancel(NOTIFICATION_ID_CRASH)
    }

    companion object {
        private const val TAG = "ShizukuWatchdog"
        private const val BINDER_GRACE_MS = 3000L
        private const val IN_FLIGHT_GRACE_MS = 90_000L

        /** Longer for the no-network start, which searches for its port for up to 130 seconds. */
        private const val FORCED_IN_FLIGHT_GRACE_MS = 140_000L

        /** How often a restart looks again at whether the start it made has ended. */
        private const val START_END_POLL_MS = 1_000L

        /** How long the server gets to answer after its start reports done. */
        private const val SERVER_BOOT_GRACE_MS = 20_000L

        /** Long enough for a wireless start to have got somewhere, short enough to be news. */
        private const val RECOVERY_CHECK_MS = 15_000L

        /** Two restarts within this are the same outage, not two. */
        private const val RESTART_COOLDOWN_MS = 15_000L

        /**
         * How often a server that is down and not coming back is tried again.
         *
         * Half a minute: long enough that a start which needs something the device does not
         * have yet is not asked for constantly, short enough that it lands while the user is
         * still the one who noticed.
         */
        private const val RECOVERY_POLL_MS = 30_000L

        /**
         * How many failed starts inside [BREAKER_WINDOW_MS] mean the start itself is broken.
         *
         * Five, matching the same idea upstream in ReShizukuX: enough that a phone which
         * merely has not got its Wi-Fi back yet is not cut off, few enough that the loop is
         * stopped in the first couple of minutes rather than after a night of it.
         */
        private const val BREAKER_MAX_ATTEMPTS = 5

        /**
         * The span those attempts have to fall inside, and it has to be wide enough for the
         * retries themselves: the poll is 30s and the cooldown 15s, so a genuine outage being
         * retried lands roughly one attempt per poll - a little under three attempts a minute,
         * which is about a minute and a half to open the breaker. Narrower than this and the
         * poll's own spacing would never fill it.
         */
        private const val BREAKER_WINDOW_MS = 120_000L

        private const val NOTIFICATION_ID_WATCHDOG = 1001
        private const val NOTIFICATION_ID_CRASH = 1002
        private const val NOTIFICATION_ID_RECOVERY = 1003
        private const val NOTIFICATION_ID_GAVE_UP = 1004
        /** Posted by the alarm backstop when a revive was refused; cleared by [onCreate]. */
        const val NOTIFICATION_ID_NOT_RUNNING = 1005
        const val CRASH_CHANNEL_ID = "crash_reports"
        const val ACTION_WATCHDOG_CHANGED = "WATCHDOG_CHANGED"

        /** A start the user asked for from the notification, which the breaker must not block. */
        const val ACTION_START_NOW = "ACTION_START_NOW"
        const val EXTRA_WATCHDOG_STATUS = "status"

        private val isRunning = AtomicBoolean(false)

        // Broadcast so automation apps (e.g. MacroDroid/Tasker) can react to
        // the watchdog being enabled or disabled.
        @JvmStatic
        fun sendWatchdogChangedBroadcast(context: Context, enabled: Boolean) {
            val intent = Intent("${context.packageName}.$ACTION_WATCHDOG_CHANGED").apply {
                putExtra(EXTRA_WATCHDOG_STATUS, if (enabled) 1 else 0)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(intent)
        }

        /**
         * Asks the platform to start the watchdog, and says whether it agreed.
         *
         * The answer matters to exactly one caller. Everywhere else the request comes from
         * something in front of the user - the settings switch, the app starting, a reboot - and
         * the platform allows those starts, so the failure is worth a log line and nothing more.
         * The alarm backstop is the exception: it runs in the background, where an app targeting
         * Android 12 or later may not start a foreground service at all, and it carries the one
         * duty that has no alternative - bringing the watchdog back. A silent refusal there would
         * look exactly like the outage it exists to end, so it is told, and it can tell the user.
         *
         * @return false when the start was refused rather than made.
         */
        @JvmStatic
        fun start(context: Context): Boolean {
            return try {
                // ContextCompat, not the plain call: the two-argument startForegroundService
                // only exists from Android 8 (API 26), and a NoSuchMethodError is an Error the
                // catch below cannot see. The compat helper falls back to startService on the
                // platforms that predate it.
                val started = ContextCompat.startForegroundService(
                    context,
                    Intent(context, WatchdogService::class.java)
                )
                // Null is the platform saying there is no such service to start: this one is not
                // direct-boot aware, so before the first unlock the process cannot see it ("Unable to
                // start service ... U=0: not found"), and the call returns instead of throwing. Taking
                // that for a start is how the alarm backstop and the log reported a watchdog that was
                // never there.
                if (started == null) {
                    Diag.warn(
                        "ShizukuApplication",
                        "WatchdogService was not started: the platform did not find it" +
                            if (context.isUserUnlockedCompat()) "" else " (the user has not unlocked yet)"
                    )
                    false
                } else {
                    true
                }
            } catch (e: Exception) {
                // ForegroundServiceStartNotAllowedException on Android 12+, which is an ordinary
                // outcome for a background caller rather than an error in this app.
                Diag.error("ShizukuApplication", "Failed to start WatchdogService: ${e.message}")
                false
            }
        }

        @JvmStatic
        fun stop(context: Context) {
            context.stopService(Intent(context, WatchdogService::class.java))
        }

        @JvmStatic
        fun isRunning(): Boolean = isRunning.get()
    }
}





