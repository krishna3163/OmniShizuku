package rikka.shizuku.nightdog

import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.ServiceManager
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object NightDog {

    private const val TAG = "NightDog"

    const val DEFAULT_TIMEOUT_MS = NightDogStateMachine.DEFAULT_TIMEOUT_MS
    const val DEFAULT_POLL_INTERVAL_MS = NightDogStateMachine.DEFAULT_POLL_INTERVAL_MS
    private const val MIN_CHECK_DELAY_MS = 1_000L
    private const val INITIAL_REBIND_DELAY_MS = NightDogStateMachine.INITIAL_REBIND_DELAY_MS
    private const val MAX_REBIND_ATTEMPTS = NightDogStateMachine.MAX_REBIND_ATTEMPTS

    private val CRITICAL_SERVICES = arrayOf("package", "activity", "user", "appops")
    private val OPTIONAL_SERVICES = arrayOf("permissionmgr")

    interface Listener {
        fun onServiceRecovered(name: String) {}
        fun onFatalFault(reason: String) {}
    }

    private val lock = Any()
    private val started = AtomicBoolean(false)
    private var watchdogThread: HandlerThread? = null
    @Volatile
    private var handler: Handler? = null
    private var healthCheckRunnable: Runnable? = null

    private val totalRebinds = AtomicInteger(0)
    private var timeoutMs = DEFAULT_TIMEOUT_MS
    private var pollIntervalMs = DEFAULT_POLL_INTERVAL_MS
    @Volatile
    private var listener: Listener? = null

    val coordinator = NightDogCoordinator()
    val snapshot: NightDogSnapshot get() = coordinator.snapshot

    // Pluggable for testing recovery termination without killing test JVM
    var processTerminator: (reason: String) -> Unit = {
        Process.killProcess(Process.myPid())
    }

    private val bindings = ConcurrentHashMap<String, IBinder>()
    private val deathRecipients = ConcurrentHashMap<String, IBinder.DeathRecipient>()
    private val pendingRebinds = ConcurrentHashMap.newKeySet<String>()

    @JvmStatic
    @JvmOverloads
    fun start(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS,
        listener: Listener? = null
    ) {
        synchronized(lock) {
            if (!started.compareAndSet(false, true)) return

            this.timeoutMs = timeoutMs.coerceAtLeast(5_000L)
            this.pollIntervalMs = pollIntervalMs.coerceAtLeast(5_000L)
            this.listener = listener

            val now = SystemClock.uptimeMillis()
            val gen = coordinator.nextGeneration()

            coordinator.onFatalFaultAction = { reason ->
                terminateForRecovery(reason)
            }
            coordinator.onServiceRecoveredAction = { name ->
                totalRebinds.incrementAndGet()
                try {
                    this.listener?.onServiceRecovered(name)
                } catch (t: Throwable) {
                    Log.w(TAG, "Listener onServiceRecovered($name) threw", t)
                }
            }

            coordinator.transition(
                NightDogEvent.StartRequested(
                    generation = gen,
                    timeoutMs = this.timeoutMs,
                    pollIntervalMs = this.pollIntervalMs,
                    criticalServices = CRITICAL_SERVICES.toList(),
                    optionalServices = OPTIONAL_SERVICES.toList(),
                    currentTimeMs = now
                )
            )

            val thread = HandlerThread("nightdog").apply {
                isDaemon = true
                start()
            }
            val threadHandler = Handler(thread.looper)
            watchdogThread = thread
            handler = threadHandler

            val checkRunnable = Runnable { checkHealth(gen) }
            healthCheckRunnable = checkRunnable

            threadHandler.post {
                if (!started.get() || coordinator.generation != gen) return@post
                coordinator.transition(NightDogEvent.Started(gen))
                linkToServices(gen, critical = true, *CRITICAL_SERVICES)
                linkToServices(gen, critical = false, *OPTIONAL_SERVICES)
                val initialDelay = minOf(this.pollIntervalMs, this.timeoutMs)
                threadHandler.postDelayed(checkRunnable, initialDelay)
            }
        }
    }

    @JvmStatic
    fun stop() {
        synchronized(lock) {
            if (!started.compareAndSet(true, false)) return

            coordinator.stop()

            val currentHandler = handler
            healthCheckRunnable?.let { currentHandler?.removeCallbacks(it) }
            healthCheckRunnable = null
            currentHandler?.removeCallbacksAndMessages(null)

            unlinkAllServices()
            pendingRebinds.clear()
            listener = null

            watchdogThread?.quitSafely()
            watchdogThread = null
            handler = null
        }
    }

    @JvmStatic
    fun beat() {
        if (!started.get()) return
        coordinator.transition(NightDogEvent.HeartbeatReceived(SystemClock.uptimeMillis()))
    }

    @JvmStatic
    fun isStarted(): Boolean = started.get() && coordinator.isRunning

    @JvmStatic
    fun getBoundServiceCount(): Int = bindings.size

    @JvmStatic
    fun getTotalRebinds(): Int = totalRebinds.get()

    private fun unlinkAllServices() {
        for ((name, recipient) in deathRecipients) {
            bindings[name]?.let { binder ->
                try {
                    binder.unlinkToDeath(recipient, 0)
                } catch (_: Exception) {}
            }
        }
        bindings.clear()
        deathRecipients.clear()
    }

    private fun unlinkService(name: String) {
        val binder = bindings.remove(name)
        val recipient = deathRecipients.remove(name)
        if (binder != null && recipient != null) {
            try {
                binder.unlinkToDeath(recipient, 0)
            } catch (_: Exception) {}
        }
    }

    private fun linkToServices(generation: Long, critical: Boolean, vararg names: String) {
        for (name in names) {
            if (!started.get() || coordinator.generation != generation) return
            val binder = ServiceManager.getService(name)
            if (binder == null || !binder.pingBinder() || !linkTo(generation, name, binder)) {
                coordinator.transition(
                    NightDogEvent.ServiceLinkFailed(generation, name, SystemClock.uptimeMillis())
                )
                if (critical) {
                    Log.w(TAG, "Initial bind to '$name' unavailable, scheduling rebind")
                    scheduleRebind(name, INITIAL_REBIND_DELAY_MS, generation)
                }
            } else {
                coordinator.transition(
                    NightDogEvent.ServiceLinked(generation, name, SystemClock.uptimeMillis())
                )
            }
        }
    }

    private fun linkTo(generation: Long, name: String, binder: IBinder): Boolean {
        unlinkService(name)

        val recipient = IBinder.DeathRecipient {
            if (!started.get() || coordinator.generation != generation) return@DeathRecipient
            Log.e(TAG, "System service died: $name")
            handler?.post {
                if (!started.get() || coordinator.generation != generation) return@post
                unlinkService(name)
                coordinator.transition(
                    NightDogEvent.ServiceDeathDetected(generation, name, SystemClock.uptimeMillis())
                )
                scheduleRebind(name, INITIAL_REBIND_DELAY_MS, generation)
            }
        }
        return try {
            binder.linkToDeath(recipient, 0)
            bindings[name] = binder
            deathRecipients[name] = recipient
            true
        } catch (e: Exception) {
            Log.e(TAG, "linkToDeath($name) failed", e)
            false
        }
    }

    private fun scheduleRebind(name: String, delayMs: Long, generation: Long) {
        if (!started.get() || coordinator.generation != generation) return
        if (!pendingRebinds.add(name)) return

        coordinator.transition(
            NightDogEvent.RebindScheduled(generation, name, delayMs, SystemClock.uptimeMillis())
        )

        val posted = handler?.postDelayed({
            pendingRebinds.remove(name)
            if (!started.get() || coordinator.generation != generation) return@postDelayed
            rebind(name, generation)
        }, delayMs) ?: false

        if (!posted) {
            pendingRebinds.remove(name)
        }
    }

    private fun rebind(name: String, generation: Long) {
        if (!started.get() || coordinator.generation != generation) return

        coordinator.transition(
            NightDogEvent.RebindStarted(generation, name, SystemClock.uptimeMillis())
        )

        val binder = ServiceManager.getService(name)
        if (binder != null && binder.pingBinder() && linkTo(generation, name, binder)) {
            Log.i(TAG, "System service recovered: $name")
            coordinator.transition(
                NightDogEvent.ServiceRecovered(generation, name, SystemClock.uptimeMillis())
            )
            return
        }

        val svcSnapshot = coordinator.snapshot.services[name]
        val attempts = svcSnapshot?.rebindAttempts ?: 0

        if (attempts + 1 >= MAX_REBIND_ATTEMPTS) {
            if (svcSnapshot?.isCritical == true) {
                Log.e(
                    TAG,
                    "Critical service '$name' failed to recover after $MAX_REBIND_ATTEMPTS attempts; terminating server for starter recovery"
                )
            } else {
                Log.w(TAG, "Optional service '$name' did not recover after $MAX_REBIND_ATTEMPTS attempts")
            }
            coordinator.transition(
                NightDogEvent.ServiceUnrecoverable(generation, name, SystemClock.uptimeMillis())
            )
            return
        }

        val delay = NightDogStateMachine.calculateBackoffDelay(attempts + 1)
        Log.w(TAG, "Rebind attempt ${attempts + 1}/$MAX_REBIND_ATTEMPTS for '$name' failed; retrying in ${delay}ms")

        coordinator.transition(
            NightDogEvent.RebindFailed(generation, name, delay, SystemClock.uptimeMillis())
        )
        scheduleRebind(name, delay, generation)
    }

    private fun checkHealth(generation: Long) {
        if (!started.get() || coordinator.generation != generation) return

        val now = SystemClock.uptimeMillis()
        val snapshot = coordinator.snapshot
        val beat = snapshot.lastBeatMonotonicMs
        var nextCheckDelay = pollIntervalMs

        if (beat > 0L) {
            val elapsed = now - beat
            if (elapsed >= timeoutMs) {
                Log.e(TAG, "Main thread heartbeat overdue by ${elapsed}ms (timeout=${timeoutMs}ms)")
                dumpMainThreadStackTrace()
                coordinator.transition(
                    NightDogEvent.FatalFaultDetected(generation, "heartbeat_timeout:${elapsed}ms", now)
                )
                return
            }
            val remainingUntilTimeout = (timeoutMs - elapsed).coerceAtLeast(MIN_CHECK_DELAY_MS)
            nextCheckDelay = minOf(pollIntervalMs, remainingUntilTimeout)
        }

        for (name in CRITICAL_SERVICES) {
            if (!started.get() || coordinator.generation != generation) return
            val binder = bindings[name]
            if (binder == null) {
                if (!pendingRebinds.contains(name)) {
                    scheduleRebind(name, INITIAL_REBIND_DELAY_MS, generation)
                }
                continue
            }
            try {
                if (!binder.pingBinder()) {
                    Log.w(TAG, "System service '$name' unresponsive during fallback check")
                    unlinkService(name)
                    coordinator.transition(
                        NightDogEvent.ServiceDeathDetected(generation, name, now)
                    )
                    scheduleRebind(name, INITIAL_REBIND_DELAY_MS, generation)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallback check failed for '$name'", e)
                unlinkService(name)
                coordinator.transition(
                    NightDogEvent.ServiceDeathDetected(generation, name, now)
                )
                scheduleRebind(name, INITIAL_REBIND_DELAY_MS, generation)
            }
        }

        val runnable = healthCheckRunnable ?: return
        if (started.get() && coordinator.generation == generation) {
            handler?.postDelayed(runnable, nextCheckDelay)
        }
    }

    private fun dumpMainThreadStackTrace() {
        try {
            val mainThread = Looper.getMainLooper()?.thread ?: return
            val stack = mainThread.stackTrace
            val sb = StringBuilder(512)
            sb.append("Main thread '").append(mainThread.name)
                .append("' (state=").append(mainThread.state).append(") stack trace:\n")
            for (element in stack) {
                sb.append("    at ").append(element).append('\n')
            }
            Log.e(TAG, sb.toString())
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to capture main thread stack trace", t)
        }
    }

    private fun terminateForRecovery(reason: String) {
        try {
            listener?.onFatalFault(reason)
        } catch (_: Throwable) {}
        stop()
        try {
            processTerminator(reason)
        } catch (_: Throwable) {}
    }
}
