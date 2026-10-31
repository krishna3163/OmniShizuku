package moe.shizuku.manager.callrecorder

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.ui.theme.ShizukuTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PREFS_CALL_RECORDER = "call_recorder_preferences"
private const val KEY_AUTO_RECORD = "auto_record"
private const val KEY_RECORD_INCOMING = "record_incoming"
private const val KEY_RECORD_OUTGOING = "record_outgoing"
private const val KEY_IGNORE_ANONYMOUS = "ignore_anonymous"
private const val KEY_IGNORE_CONTACTS = "ignore_contacts"
private const val KEY_SELECTED_CODEC = "selected_codec"

@OptIn(ExperimentalMaterial3Api::class)
class CallRecorderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShizukuTheme {
                CallRecorderScreen(onBack = { finish() })
            }
        }
    }
}

data class RecordingItem(
    val id: String,
    val title: String,
    val duration: String,
    val date: String,
    val filePath: String,
    val sizeBytes: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallRecorderScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Record", "Settings", "Recordings", "About")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Recorder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> RecordingControlsScreen()
                1 -> RecorderSettingsScreen()
                2 -> RecordingsListScreen()
                3 -> AboutScreen()
            }
        }
    }
}

@Composable
fun RecordingControlsScreen() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var durationSeconds by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRecording) {
        if (isRecording) {
            durationSeconds = 0
            while (isRecording) {
                delay(1000)
                durationSeconds += 1
            }
        } else {
            durationSeconds = 0
        }
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(180.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isRecording) "Recording Active" else "Ready to Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "%02d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isRecording) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val action = if (isRecording) {
                    "com.krishna.omnishizuku.action.STOP_RECORDING"
                } else {
                    "com.krishna.omnishizuku.action.START_RECORDING"
                }
                val intent = Intent(context, RecordingForegroundService::class.java).setAction(action)
                try {
                    if (isRecording) {
                        context.startService(intent)
                    } else {
                        context.startForegroundService(intent)
                    }
                    isRecording = !isRecording
                } catch (e: Exception) {
                    Toast.makeText(context, "Service error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Outlined.Stop else Icons.Outlined.FiberManualRecord,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }
    }
}

@Composable
fun RecorderSettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_CALL_RECORDER, Context.MODE_PRIVATE) }

    var autoRecord by remember { mutableStateOf(prefs.getBoolean(KEY_AUTO_RECORD, false)) }
    var recordIncoming by remember { mutableStateOf(prefs.getBoolean(KEY_RECORD_INCOMING, true)) }
    var recordOutgoing by remember { mutableStateOf(prefs.getBoolean(KEY_RECORD_OUTGOING, true)) }
    var ignoreAnonymous by remember { mutableStateOf(prefs.getBoolean(KEY_IGNORE_ANONYMOUS, false)) }
    var ignoreContacts by remember { mutableStateOf(prefs.getBoolean(KEY_IGNORE_CONTACTS, false)) }
    var codecSelected by remember { mutableStateOf(prefs.getString(KEY_SELECTED_CODEC, "OPUS") ?: "OPUS") }

    fun updatePref(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Capture Triggers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Recording", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Automatically record eligible phone calls in background", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoRecord,
                            onCheckedChange = {
                                autoRecord = it
                                updatePref(KEY_AUTO_RECORD, it)
                            }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Record Incoming Calls", style = MaterialTheme.typography.bodyLarge)
                            Text("Capture calls received from external callers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = recordIncoming,
                            onCheckedChange = {
                                recordIncoming = it
                                updatePref(KEY_RECORD_INCOMING, it)
                            }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Record Outgoing Calls", style = MaterialTheme.typography.bodyLarge)
                            Text("Capture calls initiated from this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = recordOutgoing,
                            onCheckedChange = {
                                recordOutgoing = it
                                updatePref(KEY_RECORD_OUTGOING, it)
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Exclusion Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = ignoreAnonymous,
                            onCheckedChange = {
                                ignoreAnonymous = it
                                updatePref(KEY_IGNORE_ANONYMOUS, it)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Ignore unknown / private callers", style = MaterialTheme.typography.bodyMedium)
                            Text("Skip calls with suppressed or private Caller ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = ignoreContacts,
                            onCheckedChange = {
                                ignoreContacts = it
                                updatePref(KEY_IGNORE_CONTACTS, it)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Ignore contacts in address book", style = MaterialTheme.typography.bodyMedium)
                            Text("Only record calls from unsaved numbers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Audio Encoding Format",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("OPUS", "AAC", "AMR-WB").forEach { codec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    codecSelected = codec
                                    prefs.edit().putString(KEY_SELECTED_CODEC, codec).apply()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = codecSelected == codec,
                                onClick = {
                                    codecSelected = codec
                                    prefs.edit().putString(KEY_SELECTED_CODEC, codec).apply()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(codec, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingsListScreen() {
    val context = LocalContext.current
    var recordings by remember { mutableStateOf<List<RecordingItem>>(emptyList()) }
    var activePlaybackPath by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun refreshRecordings() {
        val list = mutableListOf<RecordingItem>()
        val dirs = listOfNotNull(
            context.getExternalFilesDir(null),
            File(context.filesDir, "recordings")
        )
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        for (dir in dirs) {
            if (dir.exists()) {
                val files = dir.listFiles { file ->
                    file.isFile && (file.name.endsWith(".m4a") || file.name.endsWith(".aac") ||
                        file.name.endsWith(".opus") || file.name.endsWith(".wav"))
                } ?: emptyArray()

                for (file in files) {
                    val sizeKb = file.length() / 1024
                    val sizeText = if (sizeKb > 1024) "%.1f MB".format(sizeKb / 1024f) else "$sizeKb KB"
                    list.add(
                        RecordingItem(
                            id = file.absolutePath,
                            title = file.nameWithoutExtension.removePrefix("call_"),
                            duration = sizeText,
                            date = dateFormat.format(Date(file.lastModified())),
                            filePath = file.absolutePath,
                            sizeBytes = file.length()
                        )
                    )
                }
            }
        }
        recordings = list.sortedByDescending { File(it.filePath).lastModified() }
    }

    LaunchedEffect(Unit) {
        refreshRecordings()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    if (recordings.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No recordings yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Saved audio files will appear here once recorded.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(recordings, key = { it.id }) { item ->
                val isPlaying = activePlaybackPath == item.filePath

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    activePlaybackPath = null
                                } else {
                                    try {
                                        mediaPlayer?.release()
                                        val player = MediaPlayer().apply {
                                            setDataSource(item.filePath)
                                            prepare()
                                            start()
                                            setOnCompletionListener {
                                                activePlaybackPath = null
                                            }
                                        }
                                        mediaPlayer = player
                                        activePlaybackPath = item.filePath
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Playback error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = item.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = item.duration,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    activePlaybackPath = null
                                }
                                val file = File(item.filePath)
                                if (file.exists()) {
                                    file.delete()
                                }
                                refreshRecordings()
                                Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete recording",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ShizuCallRecorder",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hardware and system-level call recording powered by Shizuku Binder IPC.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Legal Compliance Disclaimer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recording telephone conversations is subject to local, state, federal, and national laws. In many jurisdictions, two-party or all-party consent is legally mandated. The developers and contributors of OmniShizuku accept no liability for non-compliant or unlawful usage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "License & Attribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Licensed under GNU General Public License v3.0 (GPL-3.0). Based on ShizuCallRecorder by kitsumed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
