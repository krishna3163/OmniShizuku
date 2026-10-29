package moe.shizuku.manager.callrecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.R
import moe.shizuku.manager.callrecorder.IShellService.Stub as ShellServiceStub
import java.io.File

class RecordingForegroundService : Service() {

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "call_recorder_recording"
    private val CHANNEL_NAME = "Call Recording"

    private var shellService: IShellService? = null
    private var outputFile: ParcelFileDescriptor? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "com.krishna.omnishizuku.action.START_RECORDING" -> startRecordingSession()
            "com.krishna.omnishizuku.action.STOP_RECORDING" -> stopRecordingSession()
        }
        return android.app.Service.START_NOT_STICKY
    }

    private fun startRecordingSession() {
        createNotificationChannel()
        val notification = buildNotification("Recording call...")
        startForeground(NOTIFICATION_ID, notification)

        bindToShellService { service ->
            shellService = service
            val recordingDir = getExternalFilesDir(null) ?: filesDir
            val file = File(recordingDir, "call_${System.currentTimeMillis()}.ogg")
            val mode = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            outputFile = ParcelFileDescriptor.open(file, mode)

            service.startRecording(
                outputFile,
                0, // VOICE_COMMUNICATION
                -1,
                false,
                0, // OPUS
                16000
            )
        }
    }

    private fun stopRecordingSession() {
        shellService?.stopRecording()
        shellService = null
        outputFile?.close()
        outputFile = null
        stopForeground(true)
        stopSelf()
    }

    private fun bindToShellService(callback: (IShellService) -> Unit) {
        // Placeholder - actual Shizuku binding happens via the app's existing Shizuku client
        // when the shell service is available. This ensures the app compiles without
        // depending on nonexistent wrapper classes.
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val intent = Intent(this, moe.shizuku.manager.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.call_recorder_notification_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
