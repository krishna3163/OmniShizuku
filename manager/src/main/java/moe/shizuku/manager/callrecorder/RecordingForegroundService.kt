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
    @Volatile private var activeRecordingGeneration: Long = -1L
    @Volatile private var activeCallback: IRecordingCallback.Stub? = null

    private fun isCurrentSession(generation: Long): Boolean =
        generation != -1L &&
            generation == activeRecordingGeneration &&
            generation == RecordingStateHolder.generation

    private fun createRecordingCallback(sessionGeneration: Long): IRecordingCallback.Stub =
        object : IRecordingCallback.Stub() {
            private fun matches(): Boolean = isCurrentSession(sessionGeneration)

            override fun onRecordingStarted() {
                if (!matches()) return
                RecordingStateHolder.setRecording()
                if (matches()) updateNotification("Recording active...")
            }

            override fun onRecordingStopped() {
                if (sessionGeneration != activeRecordingGeneration) return
                if (sessionGeneration != RecordingStateHolder.generation) return
                RecordingStateHolder.setStopped()
                cleanupSession(setIdle = false, forGeneration = sessionGeneration)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            override fun onRecordingPaused() {
                if (!matches()) return
                RecordingStateHolder.setPaused()
                if (matches()) updateNotification("Recording paused")
            }

            override fun onRecordingResumed() {
                if (!matches()) return
                RecordingStateHolder.setResumed()
                if (matches()) updateNotification("Recording active...")
            }

            override fun onRecordingFailure(errorCode: Int, message: String) {
                if (sessionGeneration != activeRecordingGeneration) return
                if (sessionGeneration != RecordingStateHolder.generation) return
                Log.e(TAG, "Recording failure callback received: [$errorCode] gen=$sessionGeneration")
                RecordingStateHolder.setError(message)
                cleanupSession(setIdle = false, forGeneration = sessionGeneration)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            override fun onRecordingProgress(durationMs: Long, fileSizeBytes: Long) {
                if (!matches() || !RecordingStateHolder.isRecording) return
                RecordingStateHolder.onProgress(durationMs, fileSizeBytes)
                if (!matches()) return
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
            activeCallback?.let { shellService?.setRecordingCallback(it) }
            startRemoteRecording()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.w(TAG, "ShellService disconnected")
            shellService = null
            isBound = false
            val gen = activeRecordingGeneration
            if (isCurrentSession(gen) &&
                (RecordingStateHolder.isRecording || RecordingStateHolder.sessionState.value == RecordingSessionState.Starting)
            ) {
                RecordingStateHolder.setError("Recording service disconnected")
            }
            cleanupSession(setIdle = false, forGeneration = gen)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
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
            ACTION_PAUSE -> {
                if (isCurrentSession(activeRecordingGeneration) &&
                    RecordingStateHolder.sessionState.value == RecordingSessionState.Recording
                ) {
                    runCatching { shellService?.pauseRecording() }
                }
            }
            ACTION_RESUME -> {
                if (isCurrentSession(activeRecordingGeneration) &&
                    RecordingStateHolder.sessionState.value == RecordingSessionState.Paused
                ) {
                    runCatching { shellService?.resumeRecording() }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecordingSession() {
        val state = RecordingStateHolder.sessionState.value
        if (outputFile != null || RecordingStateHolder.isRecording || state == RecordingSessionState.Starting || state == RecordingSessionState.Stopping) {
            Log.d(TAG, "Session already active or transitioning: $state")
            return
        }

        try {
            val recordingsDir = File(getExternalFilesDir(null) ?: filesDir, "Recordings")
            if (!recordingsDir.exists()) recordingsDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val codec = RecordingStateHolder.selectedCodec.value
            val targetFile = File(recordingsDir, "Call_$timestamp${codec.extension}")
            pendingOutputFile = targetFile
            // Single authoritative transition that allocates a unique generation for this session.
            RecordingStateHolder.setStarting(targetFile.name)
            activeRecordingGeneration = RecordingStateHolder.generation
            activeCallback = createRecordingCallback(activeRecordingGeneration)

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification("Initializing recorder..."))

            val mode = ParcelFileDescriptor.MODE_READ_WRITE or
                    ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE
            outputFile = ParcelFileDescriptor.open(targetFile, mode)

            if (!Shizuku.pingBinder()) {
                Log.e(TAG, "Shizuku binder is not available")
                if (isCurrentSession(activeRecordingGeneration)) RecordingStateHolder.setError("Shizuku is not running")
                cleanupSession(setIdle = false, forGeneration = activeRecordingGeneration)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
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
            if (isCurrentSession(activeRecordingGeneration)) RecordingStateHolder.setError(e.message ?: "Failed to start")
            cleanupSession(setIdle = false, forGeneration = activeRecordingGeneration)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startRemoteRecording() {
        val service = shellService ?: return
        val pfd = outputFile ?: return
        val gen = activeRecordingGeneration
        if (!isCurrentSession(gen)) return

        val primary = RecordingStateHolder.selectedPrimarySource.value.ordinal
        val secondary = RecordingStateHolder.selectedSecondarySource.value?.ordinal ?: -1
        val channelSplit = RecordingStateHolder.isChannelSplit.value
        val codec = RecordingStateHolder.selectedCodec.value.ordinal
        val bitrate = RecordingStateHolder.selectedBitrate.intValue

        runCatching {
            service.startRecording(pfd, primary, secondary, channelSplit, codec, bitrate)
        }.onFailure {
            Log.e(TAG, "Failed to call service.startRecording", it)
            if (isCurrentSession(gen)) RecordingStateHolder.setError(it.message)
            cleanupSession(setIdle = false, forGeneration = gen)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopRecordingSession() {
        val gen = activeRecordingGeneration
        val state = RecordingStateHolder.sessionState.value
        if (state == RecordingSessionState.Idle) {
            cleanupSession(setIdle = true, forGeneration = gen)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        if (state == RecordingSessionState.Stopping) return

        if (isCurrentSession(gen) && (RecordingStateHolder.isRecording || state == RecordingSessionState.Starting)) {
            RecordingStateHolder.setStopping()
            updateNotification("Stopping recording...")
        }

        val stopResult = runCatching { shellService?.stopRecording() }
        if (stopResult.isFailure && isCurrentSession(gen) && !RecordingStateHolder.isRecording && state == RecordingSessionState.Starting) {
            // No active remote session to stop; finalize locally.
            RecordingStateHolder.setStopped()
            cleanupSession(setIdle = false, forGeneration = gen)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        // onRecordingStopped/onRecordingFailure callback finalizes service for active sessions.
    }

    private fun cleanupSession(setIdle: Boolean, forGeneration: Long = activeRecordingGeneration) {
        if (isBound) {
            runCatching { Shizuku.unbindUserService(userServiceArgs, serviceConnection, false) }
            isBound = false
        }
        shellService = null
        runCatching { outputFile?.close() }
        outputFile = null
        pendingOutputFile = null
        if (setIdle && (forGeneration == -1L || forGeneration == RecordingStateHolder.generation)) {
            RecordingStateHolder.setStopped()
        }
        if (forGeneration == activeRecordingGeneration || activeRecordingGeneration == -1L) {
            activeRecordingGeneration = -1L
            activeCallback = null
        }
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
