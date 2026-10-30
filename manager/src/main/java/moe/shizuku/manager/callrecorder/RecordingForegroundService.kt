package moe.shizuku.manager.callrecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.R
import rikka.shizuku.Shizuku
import java.io.File

class RecordingForegroundService : Service() {

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "call_recorder_recording"
    private val CHANNEL_NAME = "Call Recording"

    private var shellService: IShellService? = null
    private var outputFile: ParcelFileDescriptor? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            shellService = IShellService.Stub.asInterface(service)
            startShellRecording()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            shellService = null
            bound = false
            stopRecordingSession()
        }
    }

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
        if (outputFile != null) return
        createNotificationChannel()
        val notification = buildNotification("Recording call...")
        startForeground(NOTIFICATION_ID, notification)

        val recordingDir = getExternalFilesDir(null) ?: filesDir
        val file = File(recordingDir, "call_${System.currentTimeMillis()}.m4a")
        val mode = ParcelFileDescriptor.MODE_READ_WRITE or
            ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
        outputFile = ParcelFileDescriptor.open(file, mode)
        bindToShellService()
    }

    private fun stopRecordingSession() {
        runCatching { shellService?.stopRecording() }
        shellService = null
        if (bound) {
            runCatching { Shizuku.unbindUserService(userServiceArgs, serviceConnection, false) }
            bound = false
        }
        outputFile?.close()
        outputFile = null
        stopForeground(true)
        stopSelf()
    }

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(packageName, ShellService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("callrecorder")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
            .tag("omnishizuku-call-recorder")
    }

    private fun bindToShellService() {
        if (!Shizuku.pingBinder()) {
            stopRecordingSession()
            return
        }
        if (runCatching { Shizuku.bindUserService(userServiceArgs, serviceConnection) }.isFailure) {
            stopRecordingSession()
            return
        }
        bound = true
    }

    private fun startShellRecording() {
        val service = shellService ?: return
        val output = outputFile ?: return
        runCatching {
            service.startRecording(
                output,
                AudioSource.VOICE_COMMUNICATION.ordinal,
                -1,
                false,
                RecordingCodec.AAC.ordinal,
                RecordingCodec.AAC.defaultBitrate
            )
        }.onFailure {
            stopRecordingSession()
        }
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
