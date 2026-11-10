package moe.shizuku.manager.themepatcher

import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        "persist.sys.oppo.theme.uuid",
        "persist.sys.oppo.theme_uuid"
    )
    private val otherDefaults = mapOf(
        "persist.sys.oplus.live_wp_uuid" to "default_live_wp_package_name",
        "persist.sys.oppo.live_wp_uuid" to "default_live_wp_package_name"
    )

    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "notifications", "ThemePatcher",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(android.app.NotificationManager::class.java)
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

        startPatcher(filteredStores) { items ->
            postPatchNotification(items)
        }
    }

    private fun startPatcher(
        storepackages: List<ApplicationInfo>,
        patched: (Array<String>) -> Unit
    ) {
        patchAll {}
        Thread {
            var trialStates = trialItems()
            while (trialStates[3].isEmpty()) {
                trialStates = trialItems()
                Thread.sleep(3500)
            }
            trialStates = trialItems()
            val notiMessage = "Applying on " + trialStates.take(3).filter { it.isNotEmpty() }
                .joinToString(separator = ", ")
            handler.post {
                postNotification(
                    getString(R.string.themepatcher_patching),
                    notiMessage, success = false, id = ThemePatcherActivity.NOTI_ID * 10
                )
            }
            Thread.sleep(12000)
            killStores(storepackages)
            Thread.sleep(3000)
            killStores(storepackages)
            patchAll {
                if (it.isEmpty()) {
                    handler.post { patched(trialStates) }
                } else {
                    handler.post {
                        postNotification(
                            getString(R.string.themepatcher),
                            "Error: $it", success = false, id = ThemePatcherActivity.NOTI_ID * 10
                        )
                    }
                }
                Thread.sleep(10000)
                try {
                    stopSelf()
                } catch (_: Exception) {
                    stopForeground(Service.STOP_FOREGROUND_DETACH)
                }
            }
        }.start()
    }

    private fun killStores(storepackages: List<ApplicationInfo>) {
        if (storepackages.isNotEmpty()) {
            storepackages.forEach { sPackage ->
                shell("am force-stop ${sPackage.packageName}")
            }
        }
    }

    private fun patchAll(done: (String) -> Unit) {
        if (trialItems()[3].isNotEmpty()) {
            val tables = listOf("system", "secure")
            tables.forEachIndexed { index, table ->
                zeroByDefault.forEach {
                    shell("settings put $table $it 0")
                }
                negativeOneByDefault.forEach {
                    shell("settings put $table $it -1")
                }
                otherDefaults.forEach { (key, value) ->
                    shell("settings put $table $key $value")
                    if (index == tables.size - 1) {
                        handler.post { done("") }
                    }
                }
            }
        } else {
            handler.post { done("") }
        }
    }

    private fun trialItems(): Array<String> {
        val items = Array<String>(4) { "" }
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
            Settings.System.getString(contentResolver, "current_typeface_name")
        } else {
            Settings.System.getString(contentResolver, "current_wallpaper_name")
                ?.split(";")?.firstOrNull()
                ?.replace("InnerTheme:", "")
                ?: ""
        }.trim()
    }

    private fun postPatchNotification(items: Array<String>) {
        postNotification(
            getString(R.string.themepatcher),
            getString(R.string.patched) + " " + items.take(3).filter { it.isNotEmpty() }
                .joinToString(separator = ", "),
            success = true, id = ThemePatcherActivity.NOTI_ID * 10
        )
    }

    private fun shell(cmd: String): String? = runShellCommand(cmd)

    private fun postNotification(title: String, message: String, success: Boolean, id: Int) {
        val channelId = "notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId, "Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val nm = getSystemService(android.app.NotificationManager::class.java)
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
