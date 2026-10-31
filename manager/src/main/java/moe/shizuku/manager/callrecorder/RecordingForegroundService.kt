package moe.shizuku.manager.callrecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import rikka.shizuku.Shizuku
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingForegroundService : Service() {
    companion object {
        private const val TAG = "SCR:ForegroundService"
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "call_recording_channel"
        private const val CHANNEL_NAME = "Call Recording Active"

        const val ACTION_START = "com.krishna.omnishizuku.action.START_RECORDING"
        const val ACTION_STOP = "com.krishna.omnishizuku.action.STOP_RECORDING"
        const val ACTION_PAUSE = "com.krishna.omnishizuku.action.PAUSE_RECORDING"
        const val ACTION_RESUME = "com.krishna.omnishizuku.action.RESUME_RECORDING"
    }

    private var shellService: IShellService? = null
    private var outputFile: ParcelFileDescriptor? = null
    private var isBound = false
    private var pendingOutputFile: File? = null

    private val recordingCallback = object : IRecordingCallback.Stub() {
        override fun onRecordingStarted() {
            RecordingStateHolder.isRecording.value = true
            RecordingStateHolder.isPaused.value = false
            updateNotification("Recording active...")
        }

        override fun onRecordingStopped() {
            RecordingStateHolder.isRecording.value = false
            RecordingStateHolder.isPaused.value = false
            RecordingStateHolder.durationMs.value = 0L
            RecordingStateHolder.fileSizeBytes.value = 0L
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        override fun onRecordingPaused() {
            RecordingStateHolder.isPaused.value = true
            updateNotification("Recording paused")
        }

        override fun onRecordingResumed() {
            RecordingStateHolder.isPaused.value = false
            updateNotification("Recording active...")
        }

        override fun onRecordingFailure(errorCode: Int, message: String) {
            Log.e(TAG, "Recording failure callback received: [$errorCode] $message")
            RecordingStateHolder.lastError.value = message
            RecordingStateHolder.isRecording.value = false
            RecordingStateHolder.isPaused.value = false
            stopRecordingSession()
        }

        override fun onRecordingProgress(durationMs: Long, fileSizeBytes: Long) {
            RecordingStateHolder.durationMs.value = durationMs
            RecordingStateHolder.fileSizeBytes.value = fileSizeBytes
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / 1000) / 60
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            val sizeMb = String.format(Locale.getDefault(), "%.1f MB", fileSizeBytes / (1024f * 1024f))
            updateNotification("Recording $formattedTime ($sizeMb)")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(TAG, "ShellService connected via Shizuku")
            shellService = IShellService.Stub.asInterface(service)
            shellService?.setRecordingCallback(recordingCallback)
            startRemoteRecording()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.w(TAG, "ShellService disconnected")
            shellService = null
            isBound = false
            stopRecordingSession()
        }
    }

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(packageName, ShellService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("callrecorder")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
            .tag("callrecorder")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecordingSession()
            ACTION_STOP -> stopRecordingSession()
            ACTION_PAUSE -> runCatching { shellService?.pauseRecording() }
            ACTION_RESUME -> runCatching { shellService?.resumeRecording() }
        }
        return START_NOT_STICKY
    }

    private fun startRecordingSession() {
        RecordingStateHolder.lastError.value = null
        if (outputFile != null || RecordingStateHolder.isRecording.value) {
            Log.d(TAG, "Session already active or starting")
            return
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Initializing recorder..."))

        try {
            val recordingsDir = File(getExternalFilesDir(null) ?: filesDir, "Recordings")
            if (!recordingsDir.exists()) recordingsDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val codec = RecordingStateHolder.selectedCodec.value
            val targetFile = File(recordingsDir, "Call_$timestamp${codec.extension}")
            pendingOutputFile = targetFile
            RecordingStateHolder.currentFileName.value = targetFile.name

            val mode = ParcelFileDescriptor.MODE_READ_WRITE or
                    ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE
            outputFile = ParcelFileDescriptor.open(targetFile, mode)

            if (!Shizuku.pingBinder()) {
                Log.e(TAG, "Shizuku binder is not available")
                RecordingStateHolder.lastError.value = "Shizuku is not running"
                stopRecordingSession()
                return
            }

            if (!isBound) {
                Shizuku.bindUserService(userServiceArgs, serviceConnection)
                isBound = true
            } else {
                startRemoteRecording()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize recording session", e)
            RecordingStateHolder.lastError.value = e.message ?: "Failed to start"
            stopRecordingSession()
        }
    }

    private fun startRemoteRecording() {
        val service = shellService ?: return
        val pfd = outputFile ?: return

        val primary = RecordingStateHolder.selectedPrimarySource.value.ordinal
        val secondary = RecordingStateHolder.selectedSecondarySource.value?.ordinal ?: -1
        val channelSplit = RecordingStateHolder.isChannelSplit.value
        val codec = RecordingStateHolder.selectedCodec.value.ordinal
        val bitrate = RecordingStateHolder.selectedBitrate.intValue

        runCatching {
            service.startRecording(pfd, primary, secondary, channelSplit, codec, bitrate)
        }.onFailure {
            Log.e(TAG, "Failed to call service.startRecording", it)
            RecordingStateHolder.lastError.value = it.message
            stopRecordingSession()
        }
    }

    private fun stopRecordingSession() {
        runCatching { shellService?.stopRecording() }
        if (isBound) {
            runCatching { Shizuku.unbindUserService(userServiceArgs, serviceConnection, false) }
            isBound = false
        }
        shellService = null

        runCatching { outputFile?.close() }
        outputFile = null

        RecordingStateHolder.isRecording.value = false
        RecordingStateHolder.isPaused.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Displays live call recording status"
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, RecordingForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OmniShizuku Call Recorder")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_server_ok_24dp)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPending)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }
}
