package moe.shizuku.manager.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.runShellCommand
import android.util.Log

class IntentReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_EXECUTE = "com.krishna.omnishizuku.execute"
        const val ACTION_EXECUTE_LEGACY = "com.legendsayantan.adbtools.execute"
        const val EXTRA_KEY = "key"
        const val EXTRA_COMMAND = "command"
        const val EXTRA_CMD = "cmd"
        const val EXTRA_RESPONSE = "response"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_EXECUTE && action != ACTION_EXECUTE_LEGACY) return

        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences("execution", Context.MODE_PRIVATE)
        val defaultPrefs = ShizukuSettings.getPreferences()
        val key = intent.getStringExtra(EXTRA_KEY)?.replace("-", "") ?: return

        val enabled = prefs.getBoolean("enabled", defaultPrefs.getBoolean("intent_shell_enabled", false))
        val lockUntil = prefs.getLong("intent_shell_lock_until", defaultPrefs.getLong("intent_shell_lock_until", 0L))
        if (!enabled || lockUntil > System.currentTimeMillis()) return

        val storedKey = (prefs.getString("key", null) ?: defaultPrefs.getString("intent_shell_key", null))?.replace("-", "")
        if (storedKey == null || storedKey != key) {
            val lockTime = System.currentTimeMillis() + 300000
            prefs.edit().putLong("intent_shell_lock_until", lockTime).apply()
            defaultPrefs.edit().putLong("intent_shell_lock_until", lockTime).apply()
            return
        }

        val command = intent.getStringExtra(EXTRA_COMMAND)
            ?: intent.getStringExtra(EXTRA_CMD)
            ?: return
        val responseAction = intent.getStringExtra(EXTRA_RESPONSE)

        val pendingResult = goAsync()
        Thread {
            try {
                val output = runShellCommand(command)
                if (responseAction != null && output != null) {
                    appCtx.sendBroadcast(Intent(responseAction).apply {
                        putExtra("response", output)
                        putExtra("command", command)
                    })
                }
            } catch (e: Exception) {
                postErrorNotification(appCtx, "Command Execution Error", e.message ?: "Unknown error")
                Log.e("IntentReceiver", Log.getStackTraceString(e))
            } finally {
                pendingResult.finish()
            }
        }.apply { isDaemon = true }.start()
    }

    private fun postErrorNotification(context: Context, title: String, message: String) {
        val channelId = "intent_shell_errors"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(channelId, "Intent Shell", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(context, IntentReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_warning_24)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(0x7F040010, builder.build())
    }
}
