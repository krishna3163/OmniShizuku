package moe.shizuku.manager.themepatcher

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.button.MaterialButton
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.utils.runShellCommand

class ThemePatcherActivity : AppActivity() {

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

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private lateinit var pkgName: String
    private lateinit var pkgManager: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_patcher)

        pkgName = packageName
        pkgManager = packageManager

        val themeStoreBtn = findViewById<MaterialButton>(R.id.launchThemeStore)

        runShellCommand("pm grant $pkgName android.permission.WRITE_SETTINGS")
        runShellCommand("pm grant $pkgName android.permission.WRITE_SECURE_SETTINGS")

        themeStoreBtn.setOnClickListener {
            themeStoreBtn.isEnabled = false
            val serviceIntent = Intent(this@ThemePatcherActivity, ThemePatcherService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            Thread {
                var themeStores =
                    pkgManager.getInstalledApplications(PackageManager.GET_META_DATA).filter {
                        it.packageName.contains("theme") || it.loadLabel(pkgManager).contains("theme")
                    }
                if (themeStores.any { it.packageName.contains("store") }) {
                    themeStores = themeStores.filter { it.packageName.contains("store") }
                }
                runOnUiThread {
                    val intent = pkgManager.getLaunchIntentForPackage(themeStores.firstOrNull()?.packageName ?: "")
                    intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent?.let { startActivity(it) }
                }
            }.start()
        }
    }

    fun shell(cmd: String): String? = runShellCommand(cmd)

    companion object {
        const val NOTI_ID = 2
    }
}
