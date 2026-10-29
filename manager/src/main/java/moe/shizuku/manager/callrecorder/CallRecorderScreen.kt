package moe.shizuku.manager.callrecorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import moe.shizuku.manager.ui.theme.ShizukuTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class CallRecorderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShizukuTheme {
                CallRecorderScreen()
            }
        }
    }
}

@Composable
fun CallRecorderScreen(onBack: () -> Unit = {}) {
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf("Record", "Settings", "Recordings", "About")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(steps[step]) },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
        when (step) {
            0 -> RecordingControlsScreen()
            1 -> RecorderSettingsScreen()
            2 -> RecordingsListScreen()
            3 -> AboutScreen()
        }
    }
}

@Composable
fun RecordingControlsScreen() {
    var isRecording by remember { mutableStateOf(false) }
    var durationSeconds by remember { mutableIntStateOf(0) }
    var scope = rememberCoroutineScope()

    LaunchedEffect(isRecording) {
        if (isRecording) {
            scope.launch {
                while (isRecording) {
                    delay(1000)
                    durationSeconds += 1
                }
            }
        }
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(200.dp),
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRecording) "\u25FC" else "\u25CF",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isRecording) "Recording" else "Tap to record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = (durationSeconds % 60).coerceAtLeast(0) / 60f
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { isRecording = !isRecording },
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }
    }
}

@Composable
fun RecorderSettingsScreen() {
    var autoRecord by remember { mutableStateOf(false) }
    var recordIncoming by remember { mutableStateOf(true) }
    var recordOutgoing by remember { mutableStateOf(true) }
    var ignoreAnonymous by remember { mutableStateOf(false) }
    var ignoreContacts by remember { mutableStateOf(false) }
    var codecSelected by remember { mutableStateOf("OPUS") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Auto-Recording",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Switch(checked = autoRecord, onCheckedChange = { autoRecord = it })

        Text(
            text = "Record Incoming Calls",
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(checked = recordIncoming, onCheckedChange = { recordIncoming = it })

        Text(
            text = "Record Outgoing Calls",
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(checked = recordOutgoing, onCheckedChange = { recordOutgoing = it })

        Text(
            text = "Exclusion Rules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = ignoreAnonymous,
                onCheckedChange = { ignoreAnonymous = it }
            )
            Text("Ignore anonymous calls")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = ignoreContacts,
                onCheckedChange = { ignoreContacts = it }
            )
            Text("Ignore contacts")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Codec",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Column {
            listOf("OPUS", "AAC", "AMR-WB").forEach { codec ->
                val isSelected = codecSelected == codec
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { codecSelected = codec }
                    )
                    Text(codec)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Storage",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("\u25A0")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Internal storage /OmniShizuku/Recordings/")
        }
    }
}

@Composable
fun RecordingsListScreen() {
    var recordings by remember { mutableStateOf(listOf<RecordingItem>()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recordings) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("\u25B6")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.number ?: "Unknown", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(item.duration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(item.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {}) { Text("\u25FC", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    if (recordings.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("\u25A0", modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No recordings yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AboutScreen() {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "ShizuCallRecorder",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Call recording powered by Shizuku",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Legal Disclaimer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            "Recording phone calls may be subject to laws in your jurisdiction. " +
            "Ensure you have consent from all parties before recording conversations. " +
            "The developers are not responsible for any misuse or legal consequences.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "License: GPL-3.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Based on ShizuCallRecorder by kitsumed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class RecordingItem(
    val number: String?,
    val duration: String,
    val date: String
)
