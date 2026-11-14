package moe.shizuku.manager.themepatcher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import moe.shizuku.manager.utils.runShellCommand

class ThemePatcherService : Service() {

    private val zeroByDefault = listOf(
        "persist.sys.trial.theme",
        "persist.sys.trial_theme",
        "persist.sys.trial.font",
        "persist.sys.trial.live_wp",
    )
    private val negativeOneByDefault = listOf(
        "persist.sys.oplus.theme_uuid",
        "persist.sys.oppo.theme.uuid",
        "persist.sys.oppo.theme_uuid"
    )
    private val otherDefaults = mapOf(
        "persist.sys.oplus.live_wp_uuid" to "default_live_wp_package_name",
        "persist.sys.oppo.live_wp_uuid" to "default_live_wp_package_name"
    )

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var patcherJob: Job? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notifications", "ThemePatcher",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, "notifications")
            .setContentTitle(getString(R.string.themepatcher))
            .setContentText(getString(R.string.themepatcher_initial_noti))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setTimeoutAfter(1000 * 60 * 5)
            .setWhen(System.currentTimeMillis() + 1000 * 60 * 5)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                ThemePatcherActivity.NOTI_ID,
                builder.build(),
                1024
            )
        } else {
            startForeground(ThemePatcherActivity.NOTI_ID, builder.build())
        }

        val themeStores =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filter {
                it.packageName.contains("theme") || it.loadLabel(packageManager).contains("theme")
            }
        val filteredStores = if (themeStores.any { it.packageName.contains("store") }) {
            themeStores.filter { it.packageName.contains("store") }
        } else {
            themeStores
        }

        startPatcherCoroutines(filteredStores)
    }

    override fun onDestroy() {
        super.onDestroy()
        patcherJob?.cancel()
        ThemePatcherManager.coordinator.cancel()
        serviceScope.cancel()
    }

    private fun startPatcherCoroutines(storePackages: List<ApplicationInfo>) {
        patcherJob?.cancel()
        patcherJob = serviceScope.launch {
            val gen = ThemePatcherManager.coordinator.nextGeneration()
            val timeoutDeadlineMs = SystemClock.elapsedRealtime() + (5 * 60 * 1000L) // 5 minutes monotonic timeout

            ThemePatcherManager.coordinator.transition(
                ThemePatcherEvent.StartRequested(
                    generation = gen,
                    storePackages = storePackages.map { it.packageName },
                    timeoutDeadlineMs = timeoutDeadlineMs,
                    currentTimeMs = SystemClock.elapsedRealtime()
                )
            )

            // Initial cleanup patch
            patchAllSettings()

            // Polling loop
            while (isActive && ThemePatcherManager.coordinator.state == ThemePatcherState.WATCHING_FOR_TRIAL) {
                delay(3500L)
                val trial = trialItems()
                val hasTrial = trial[3].isNotEmpty()
                val detected = trial.take(3).filter { it.isNotEmpty() }

                ThemePatcherManager.coordinator.transition(
                    ThemePatcherEvent.TrialPolled(
                        generation = gen,
                        hasTrial = hasTrial,
                        items = detected,
                        currentTimeMs = SystemClock.elapsedRealtime()
                    )
                )
            }

            // If trial was detected, proceed through termination, patching, and verification
            if (isActive && ThemePatcherManager.coordinator.state == ThemePatcherState.TRIAL_DETECTED) {
                val detectedItems = ThemePatcherManager.coordinator.snapshot.detectedItems
                val notiMessage = "Applying on " + detectedItems.joinToString(", ")
                handler.post {
                    postNotification(
                        getString(R.string.themepatcher_patching),
                        notiMessage,
                        success = false,
                        id = ThemePatcherActivity.NOTI_ID
                    )
                }

                delay(12000L)
                killStores(storePackages)
                delay(3000L)
                killStores(storePackages)

                ThemePatcherManager.coordinator.transition(
                    ThemePatcherEvent.StoresTerminated(gen)
                )

                val patchError = patchAllSettings()
                val success = patchError == null
                ThemePatcherManager.coordinator.transition(
                    ThemePatcherEvent.PatchApplied(gen, success, patchError)
                )

                if (success) {
                    val verified = verifyTrialFrozen()
                    ThemePatcherManager.coordinator.transition(
                        ThemePatcherEvent.VerificationCompleted(gen, verified)
                    )
                }
            }

            // Process terminal outcomes
            when (ThemePatcherManager.coordinator.state) {
                ThemePatcherState.COMPLETED -> {
                    handler.post {
                        postPatchNotification(ThemePatcherManager.coordinator.snapshot.detectedItems)
                    }
                }
                ThemePatcherState.TIMED_OUT -> {
                    handler.post {
                        postNotification(
                            getString(R.string.themepatcher),
                            "No trial theme detected within 5 minutes.",
                            success = false,
                            id = ThemePatcherActivity.NOTI_ID
                        )
                    }
                }
                ThemePatcherState.FAILED -> {
                    val err = ThemePatcherManager.coordinator.snapshot.lastError ?: "Unknown error"
                    handler.post {
                        postNotification(
                            getString(R.string.themepatcher),
                            "Error: $err",
                            success = false,
                            id = ThemePatcherActivity.NOTI_ID
                        )
                    }
                }
                else -> {}
            }

            // Allow user to view status before closing foreground service
            delay(5000L)
            try {
                stopSelf()
            } catch (_: Exception) {
                stopForeground(Service.STOP_FOREGROUND_DETACH)
            }
        }
    }

    private fun killStores(storepackages: List<ApplicationInfo>) {
        if (storepackages.isNotEmpty()) {
            storepackages.forEach { sPackage ->
                shell("am force-stop ${sPackage.packageName}")
            }
        }
        shell("am force-stop com.heytap.themestore")
        shell("am force-stop com.nearme.themestore")
    }

    private fun patchAllSettings(): String? {
        val tables = listOf("system", "secure")
        return try {
            tables.forEach { table ->
                zeroByDefault.forEach {
                    shell("settings put $table $it 0")
                }
                negativeOneByDefault.forEach {
                    shell("settings put $table $it -1")
                }
                otherDefaults.forEach { (key, value) ->
                    shell("settings put $table $key $value")
                }
            }
            null
        } catch (e: Exception) {
            e.message ?: "Failed to write settings"
        }
    }

    private fun verifyTrialFrozen(): Boolean {
        zeroByDefault.forEach {
            val system = Settings.System.getInt(contentResolver, it, 0)
            val secure = Settings.Secure.getInt(contentResolver, it, 0)
            if (system != 0 || secure != 0) return false
        }
        return true
    }

    fun patchAll(done: (String) -> Unit) {
        serviceScope.launch {
            val err = patchAllSettings() ?: ""
            handler.post { done(err) }
        }
    }

    fun trialItems(): Array<String> {
        val items = Array(4) { "" }
        zeroByDefault.forEach {
            val system = Settings.System.getInt(contentResolver, it, 0)
            val secure = Settings.Secure.getInt(contentResolver, it, 0)
            if (system != 0 || secure != 0) {
                items[3] = "Trial"
                if (it.contains("theme")) {
                    items[0] = "Theme : " + extractNameFor(false)
                } else if (it.contains("font")) {
                    items[1] = "Font : " + extractNameFor(true)
                } else if (it.contains("live_wp")) {
                    items[2] = "Live Wallpaper : On Trial"
                }
            }
        }
        negativeOneByDefault.forEach {
            val system = Settings.System.getInt(contentResolver, it, -1)
            val secure = Settings.Secure.getInt(contentResolver, it, -1)
            if (system != -1 || secure != -1) {
                items[3] = "Trial"
            }
        }
        otherDefaults.forEach { (key, value) ->
            val system = Settings.System.getString(contentResolver, key)
            val secure = Settings.Secure.getString(contentResolver, key)
            if (system != value || secure != value) {
                items[3] = "Trial"
            }
        }
        return items
    }

    private fun extractNameFor(isFont: Boolean): String {
        return if (isFont) {
            Settings.System.getString(contentResolver, "current_typeface_name") ?: ""
        } else {
            Settings.System.getString(contentResolver, "current_wallpaper_name")
                ?.split(";")?.firstOrNull()
                ?.replace("InnerTheme:", "")
                ?: ""
        }.trim()
    }

    private fun postPatchNotification(items: List<String>) {
        postNotification(
            getString(R.string.themepatcher),
            getString(R.string.patched) + " " + items.take(3).filter { it.isNotEmpty() }
                .joinToString(separator = ", "),
            success = true,
            id = ThemePatcherActivity.NOTI_ID
        )
    }

    fun postPatchNotification(items: Array<String>) {
        postPatchNotification(items.toList())
    }

    private fun shell(cmd: String): String? = runShellCommand(cmd)

    private fun postNotification(title: String, message: String, success: Boolean, id: Int) {
        val channelId = "notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(if (success) android.R.drawable.ic_dialog_info else android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        NotificationManagerCompat.from(applicationContext).notify(id, builder.build())
    }
}
