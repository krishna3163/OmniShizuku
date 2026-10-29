package moe.shizuku.manager.audio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundMasterScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sound Master") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Sound Master", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Independent volume stream control for media, ringtone, alarm, notification, and call.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Media Volume", style = MaterialTheme.typography.bodyMedium)
                            Text("Controls music, video, game audio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            steps = 10
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ringtone Volume", style = MaterialTheme.typography.bodyMedium)
                            Text("Incoming call ringtone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            steps = 10
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alarm Volume", style = MaterialTheme.typography.bodyMedium)
                            Text("Alarm and timer alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            steps = 10
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notification Volume", style = MaterialTheme.typography.bodyMedium)
                            Text("App notifications and alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            steps = 10
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Call Volume", style = MaterialTheme.typography.bodyMedium)
                            Text("Voice call audio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            steps = 10
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
