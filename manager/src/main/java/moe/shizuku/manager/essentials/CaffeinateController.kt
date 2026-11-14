/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Domain Layer Models & Registries
 * File: CaffeinateController.kt
 * Description: Domain model and business logic entry for CaffeinateController.kt.
 */

package moe.shizuku.manager.essentials

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.service.quicksettings.TileService
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.tile.CaffeinateTileService

object CaffeinateController {

    val coordinator = CaffeinateCoordinator()
    val snapshot: CaffeinateSnapshot get() = coordinator.snapshot

    // Compose state properties preserved for UI/Tile observation
    val isActive = mutableStateOf(false)
    val isStarting = mutableStateOf(false)
    val startingTimeLeft = mutableStateOf(0)
    val selectedTimeout = mutableStateOf(5)
    val endTime = mutableStateOf(0L)
    val durationMillis = mutableStateOf(0L)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startingJob: Job? = null
    private var screenOffReceiver: BroadcastReceiver? = null

    val timeoutPresets = listOf(5, 10, 30, 60, -1)

    init {
        coordinator.addListener { _, new, _ ->
            isActive.value = new.isActive
            isStarting.value = new.isStarting
            startingTimeLeft.value = new.selectionSecondsRemaining
            selectedTimeout.value = new.selectedTimeoutMinutes
            durationMillis.value = new.totalActiveDurationMillis
            endTime.value = new.deadlineMonotonicMs
        }
    }

    fun check(context: Context) {
        isActive.value = coordinator.isActive || isWakeLockServiceRunning(context)
    }

    fun toggle(context: Context) {
        if (coordinator.isRunning || isActive.value || isStarting.value) {
            cancelAll(context)
        } else {
            startSelection(context)
        }
        refreshTile(context)
    }

    fun start(context: Context) {
        startingJob?.cancel()
        val gen = coordinator.nextGeneration()
        coordinator.transition(
            CaffeinateEvent.StartSelection(
                generation = gen,
                timeoutMinutes = -1,
                skipCountdown = true,
                currentTimeMs = SystemClock.elapsedRealtime()
            )
        )
        startService(context)
    }

    fun startFor(context: Context, timeoutMinutes: Int) {
        require(timeoutMinutes == -1 || timeoutMinutes > 0)
        startingJob?.cancel()

        val skip = isSkipCountdownEnabled(context)
        val gen = coordinator.nextGeneration()
        val abortScreenOff = getAbortOnScreenOff(context)

        coordinator.transition(
            CaffeinateEvent.StartSelection(
                generation = gen,
                timeoutMinutes = timeoutMinutes,
                countdownSeconds = 5,
                skipCountdown = skip,
                abortOnScreenOff = abortScreenOff,
                currentTimeMs = SystemClock.elapsedRealtime()
            )
        )

        registerScreenOffReceiver(context)

        if (skip) {
            startService(context)
            return
        }
        resetSelectionTimer(context, gen)
    }

    private fun refreshTile(context: Context) {
        TileService.requestListeningState(
            context,
            ComponentName(context, CaffeinateTileService::class.java),
        )
    }

    private fun startSelection(context: Context) {
        val enabledPresets = getEnabledPresets(context)
        if (enabledPresets.isEmpty()) return

        val sortedPresets = timeoutPresets.filter { enabledPresets.contains(it) }
        val targetTimeout = sortedPresets.firstOrNull() ?: timeoutPresets.first()

        startFor(context, targetTimeout)
    }

    private fun registerScreenOffReceiver(context: Context) {
        if (screenOffReceiver != null) return
        val appContext = context.applicationContext
        if (!getAbortOnScreenOff(appContext)) return

        screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    coordinator.onScreenOff(SystemClock.elapsedRealtime())
                    cancelAll(appContext)
                }
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(
            appContext,
            screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun unregisterScreenOffReceiver(context: Context) {
        val appContext = context.applicationContext
        screenOffReceiver?.let {
            try {
                appContext.unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        screenOffReceiver = null
    }

    fun cycleTimeout(context: Context) {
        if (!coordinator.isStarting && !isStarting.value) return

        val enabledPresets = getEnabledPresets(context)
        val sortedPresets = timeoutPresets.filter { enabledPresets.contains(it) }

        coordinator.transition(CaffeinateEvent.CycleTimeout(sortedPresets, resetCountdownSeconds = 5))

        if (coordinator.isActive) {
            startService(context)
        }

        resetSelectionTimer(context, coordinator.generation)
        refreshTile(context)
    }

    private fun resetSelectionTimer(context: Context, generation: Long) {
        startingJob?.cancel()
        startingJob = scope.launch {
            for (sec in 4 downTo 0) {
                delay(1000L)
                if (coordinator.generation != generation) return@launch
                coordinator.transition(CaffeinateEvent.SelectionTick(sec))
                refreshTile(context)
            }
            if (coordinator.generation == generation) {
                coordinator.transition(CaffeinateEvent.SelectionConfirmed(SystemClock.elapsedRealtime()))
                startService(context)
            }
        }
    }

    private fun startService(context: Context) {
        unregisterScreenOffReceiver(context)
        val timeout = coordinator.snapshot.selectedTimeoutMinutes
        val intent = Intent(context, CaffeinateWakeLockService::class.java).apply {
            putExtra("timeout_minutes", timeout)
            putExtra("generation", coordinator.generation)
        }
        ContextCompat.startForegroundService(context, intent)
        refreshTile(context)
    }

    fun cancelAll(context: Context) {
        unregisterScreenOffReceiver(context)
        startingJob?.cancel()
        coordinator.stop("User requested cancel")
        context.stopService(Intent(context, CaffeinateWakeLockService::class.java))
        refreshTile(context)
    }

    private fun getEnabledPresets(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences("caffeinate_prefs", Context.MODE_PRIVATE)
        val savedPresets = prefs.getStringSet("enabled_presets", timeoutPresets.map { it.toString() }.toSet())
        return savedPresets?.map { it.toInt() }?.toSet() ?: timeoutPresets.toSet()
    }

    private fun isSkipCountdownEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("caffeinate_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("skip_countdown", false)
    }

    private fun getAbortOnScreenOff(context: Context): Boolean {
        val prefs = context.getSharedPreferences("caffeinate_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("abort_screen_off", true)
    }

    @Suppress("DEPRECATION")
    private fun isWakeLockServiceRunning(context: Context): Boolean {
        return try {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
            for (service in manager.getRunningServices(100)) {
                if (CaffeinateWakeLockService::class.java.name == service.service.className) {
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
