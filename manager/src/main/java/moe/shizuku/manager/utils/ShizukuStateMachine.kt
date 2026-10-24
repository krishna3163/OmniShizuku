package moe.shizuku.manager.utils

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import moe.shizuku.manager.ShizukuApplication
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.manage.Hiding
import moe.shizuku.manager.utils.Diag
import moe.shizuku.manager.manage.HidingGrants
import moe.shizuku.manager.manage.LabsReapply
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.start.grantLocalHotspotPermissionIfNeeded
import moe.shizuku.manager.start.grantWriteSecureSettingsIfNeeded
import rikka.shizuku.Shizuku

private val appContext = ShizukuApplication.appContext

/**
 * The same one-time handing over, for the two grants the hiding lists need.
 *
 * Only while a list is actually in use, which is where this deliberately differs from the
 * permission granted beside it: that one is needed by the start flow itself, while these two are
 * only ever read by the hiding watch - and usage access is a real special-access grant, not one
 * of the harmless ones. Somebody who has never made a hiding list should not be handing it out.
 *
 * It exists because the permission above it is granted here, on the server being seen running,
 * and hiding's two have no better trigger: this is the moment the shell becomes available, and
 * without a shell there is nothing to ask with.
 */
private fun grantHidingAccessIfListsAreInUse() {
    if (!Hiding.hasAnyApp()) return
    HidingGrants.ensureQuietly()
}

object ShizukuStateMachine {

    enum class State { STARTING, RUNNING, STOPPING, STOPPED, CRASHED }

    /**
     * How long a poll leaves STARTING alone. The same minute `Starter.waitForBinder` waits for the
     * binder, so a start that is still being waited on is not contradicted by the poll that
     * asks whether it has finished.
     */
    const val STARTING_GRACE_MS = 60_000L

    private var state = AtomicReference<State>(State.STOPPED)

    /** When the state last became STARTING, on the elapsed-realtime clock. */
    @Volatile
    private var startingSince = 0L
    private val listeners = CopyOnWriteArrayList<(State) -> Unit>()

    init {
        Shizuku.addBinderReceivedListenerSticky(
            Shizuku.OnBinderReceivedListener { set(State.RUNNING) }
        )
        Shizuku.addBinderDeadListener(
            Shizuku.OnBinderDeadListener { setDead() }
        )
    }

    fun get(): State = state.get()

    private fun transition(transform: (State) -> State) {
        val oldState = state.getAndUpdate(transform)
        val newState = transform(oldState)
        if(oldState != newState) {
            // Every transition, into the log the phone can read: "why did Shizuku stop" is
            // usually answered by the sequence of these rather than by any one line.
            val stamp = if (newState == State.STARTING || newState == State.RUNNING) BootTimeline.suffix() else ""
            Diag.info("ShizukuStateMachine", "${oldState.name} -> ${newState.name}$stamp")
            // After the line, so the RUNNING that ends the first start is itself stamped.
            if (newState == State.RUNNING) BootTimeline.markRunning()
            if (newState == State.STARTING) startingSince = SystemClock.elapsedRealtime()
            // A confirmed running server lifts manual-stop suppression, so a
            // later crash is auto-restarted by the watchdog.
            if (newState == State.RUNNING) {
                ShizukuSettings.setManuallyStopped(false)
                // A server that is up is one the watchdog may keep up: the boot gate is over
                // until the next reboot.
                ShizukuSettings.setServerRanSinceBoot(true)
                // The server is up, so it can hand us the ADB-only permission the wireless
                // flow needs the user shouldn't have to reach for a computer for it.
                grantWriteSecureSettingsIfNeeded()
                // And the one the no-network start's hotspot needs, which Android 17 leaves
                // ungranted on a fresh install because its own ask is for a different
                // permission there - see [localHotspotPermission].
                grantLocalHotspotPermissionIfNeeded()
                grantHidingAccessIfListsAreInUse()
                // And whatever the platform has lost: a block is its state and goes with the
                // framework - a full reboot or a soft one - while the list of what was blocked is
                // this app's. This is the first moment there is a server to run the commands
                // with, and the platform is asked what is still in place rather than a reboot
                // being guessed at.
                LabsReapply.putBack()
                // Remember how the server was launched so later background starts
                // know whether to use root or wireless debugging (previously done by
                // the removed HomeViewModel).
                runCatching {
                    ShizukuSettings.setLastLaunchMode(
                        if (Shizuku.getUid() == 0) ShizukuSettings.LaunchMethod.ROOT
                        else ShizukuSettings.LaunchMethod.ADB
                    )
                }
                // A start this app queued and is still waiting to make is answered by the server
                // being up, whoever started it: the queued work goes, and with it the "Waiting for
                // Wi-Fi" notification it left behind over a service that is already running. Left
                // there, that work would have come due when a network appeared and started a
                // second server on top of the one that is here now.
                runCatching { ShizukuReceiverStarter.onServerRunning(appContext) }
                    .onFailure { Diag.warn("ShizukuStateMachine", "Could not drop the queued start", it) }
            }
            // A deliberate stop (STOPPING -> STOPPED) is where the debugging toggles get
            // switched off, if the settings ask for it.
            if (oldState == State.STOPPING && newState == State.STOPPED) {
                disableDebuggingTogglesIfAsked()
            }

            // Deliberately NOT clearing the recorded transport when the server stops.
            // It describes how the server was launched, so it stays true after the
            // launch ends and clearing it here also wiped it on every transient
            // STOPPED while a start was still coming up (the binder is not up yet),
            // which left a running server being reported as "Unknown".
            // The broadcast goes out first, and the listeners are each guarded.
            //
            // This is the one piece of news that leaves the app, and it used to sit after the
            // listener loop, so a listener that threw took it with it. Measured on the device
            // while answering issue #80: the transitions were logged from this very block and no
            // broadcast reached the system for any of them, while the same action sent from a
            // shell was recorded. A status broadcast is not something a listener gets to cancel,
            // and neither is a listener's own failure the state machine's problem.
            when (newState) {
                State.RUNNING, State.STOPPED, State.CRASHED -> sendShizukuChangedBroadcast(newState)
                else -> Unit
            }
            listeners.forEach { listener ->
                runCatching { listener(newState) }
                    .onFailure { Diag.warn("ShizukuStateMachine", "A state listener failed", it) }
            }
            Log.d("ShizukuStateMachine", newState.toString())
        }
    }

    // Broadcast so automation apps (e.g. MacroDroid/Tasker) can react to
    // Shizuku starting or stopping.
    private fun sendShizukuChangedBroadcast(newState: State) {
        val status = if (newState == State.RUNNING) 1 else 0
        val intent = Intent("${appContext.packageName}.SHIZUKU_CHANGED").apply {
            putExtra("status", status)
            addFlags(
                Intent.FLAG_INCLUDE_STOPPED_PACKAGES or
                    // Sent urgently rather than as bookkeeping to catch up on: this is a status
                    // change somebody's automation is waiting on.
                    Intent.FLAG_RECEIVER_FOREGROUND
            )
        }
        // Written down either way round: whether it went, and what it said. A send that fails is
        // the one thing an automation author needs to know about, and without this line it looks
        // exactly like nobody having registered for it.
        runCatching { appContext.sendBroadcast(intent) }
            .onSuccess { Diag.info("ShizukuStateMachine", "status broadcast sent: $status") }
            .onFailure { Diag.warn("ShizukuStateMachine", "Could not send the status broadcast", it) }
    }

    fun set(newState: State) = transition { newState }

    fun setDead() = transition {
        when (it) {
            State.RUNNING -> State.CRASHED
            State.STOPPING -> State.STOPPED
            else -> it
        }
    }

    /**
     * Turns the debugging toggles off after a deliberate stop, when the user asked for
     * that in settings.
     *
     * Called from the transition rather than from [setDead] because the binder dying can
     * be noticed by [update] first which reaches STOPPED from STOPPING just the same,
     * and used to skip this entirely.
     */
    private fun disableDebuggingTogglesIfAsked() {
        try {
            val granted = appContext.checkSelfPermission(WRITE_SECURE_SETTINGS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return

            if (ShizukuSettings.getAutoDisableUsbDebugging()) {
                Settings.Global.putInt(appContext.contentResolver, Settings.Global.ADB_ENABLED, 0)
            }
            // Wireless debugging is kept on by default that is what lets Shizuku restart
            // with no Wi-Fi so turning it off with Shizuku is opt-in. The opt-in is
            // ignored while the experiment that keeps it on without a network is in use:
            // stopping Shizuku would otherwise undo the state the whole trick exists to
            // hold, and the next start would have to win it all over again.
            if (ShizukuSettings.getAutoDisableWirelessDebugging() &&
                !ShizukuSettings.getForceWirelessDebugging()
            ) {
                Settings.Global.putInt(appContext.contentResolver, "adb_wifi_enabled", 0)
            }
        } catch (e: Exception) {
            Log.w("ShizukuStateMachine", "Failed to disable the debugging toggles", e)
        }
    }

    /**
     * How long the state has been STARTING, or null when it is something else.
     *
     * What lets a caller tell a start that is under way from one that was abandoned, which the
     * state alone cannot: see [STARTING_GRACE_MS].
     */
    fun startingAgeMs(): Long? =
        if (state.get() == State.STARTING) SystemClock.elapsedRealtime() - startingSince else null

    /**
     * Brings the state in line with the binder.
     *
     * A failed ping does not end a start that is still inside [STARTING_GRACE_MS]: the server is
     * expected to be unreachable then, and calling it STOPPED announced a stop that had not
     * happened. A start that really failed says so itself, through [markStartFailed].
     */
    fun update(): State {
        val alive = Shizuku.pingBinder()
        val state = resolvePolledState(get(), alive, startingAgeMs(), STARTING_GRACE_MS)
        if (state != get()) set(state)
        // Also covers a server that was already running when this process started, or a
        // permission that was revoked behind our back: there is no transition to hook then.
        if (state == State.RUNNING) {
            grantWriteSecureSettingsIfNeeded()
            grantLocalHotspotPermissionIfNeeded()
            grantHidingAccessIfListsAreInUse()
        }
        return state
    }

    /**
     * A start that failed for real: STOPPED, unless the server turns out to be up after all.
     *
     * [update] no longer turns a failed ping during STARTING into STOPPED, so the places that know
     * a start is over say so here instead. Not for a cancelled start, which usually means another
     * start took over the work.
     */
    fun markStartFailed(): State {
        if (Shizuku.pingBinder()) {
            set(State.RUNNING)
            return State.RUNNING
        }
        set(State.STOPPED)
        return State.STOPPED
    }

    fun isRunning(): Boolean {
        return get() == State.RUNNING
    }

    fun isDead(): Boolean {
        return (get() == State.STOPPED || get() == State.CRASHED) 
    }

    fun addListener(listener: (State) -> Unit) {
        listeners.add(listener)
        listener(state.get())
    }

    fun removeListener(listener: (State) -> Unit) {
        listeners.remove(listener)
    }

    fun asFlow(): Flow<State> = callbackFlow {
        val listener: (State) -> Unit = { trySend(it).isSuccess }
        addListener(listener)
        awaitClose { removeListener(listener) }
    }

    fun onStartRequested() {
        try {
            if (isRunning() || Shizuku.pingBinder()) {
                moe.shizuku.manager.service.WatchdogManager.expectingDeath = true
            }
        } catch (_: Throwable) {}
        set(State.STARTING)
    }

    fun recordStarting() = onStartRequested()

    fun onStartFailed() {
        markStartFailed()
    }

    fun onStopRequested() {
        set(State.STOPPING)
    }

    suspend fun awaitRunning(timeoutMs: Long = 10_000L): Boolean {
        if (isRunning() || Shizuku.pingBinder()) {
            if (!isRunning()) set(State.RUNNING)
            return true
        }
        return withTimeoutOrNull(timeoutMs) {
            asFlow().first { it == State.RUNNING }
            true
        } ?: (isRunning() || Shizuku.pingBinder())
    }

    suspend fun awaitStopped(timeoutMs: Long = 5_000L): Boolean {
        if (isDead() && !Shizuku.pingBinder()) {
            return true
        }
        return withTimeoutOrNull(timeoutMs) {
            asFlow().first { it == State.STOPPED || it == State.CRASHED }
            true
        } ?: (isDead() || !Shizuku.pingBinder())
    }
}