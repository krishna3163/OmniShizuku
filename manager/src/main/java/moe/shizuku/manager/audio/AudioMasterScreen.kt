package moe.shizuku.manager.audio

import android.widget.Toast
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMasterScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var streams by remember { mutableStateOf<List<AudioStreamInfo>>(emptyList()) }
    var devices by remember { mutableStateOf<List<AudioOutputDevice>>(emptyList()) }
    var grantPackage by remember { mutableStateOf("") }

    fun refresh() {
        streams = AudioMasterManager.getStreamVolumes(context)
        devices = AudioMasterManager.getOutputDevices(context)
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Stream Master") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stream Volumes Card
            item {
                Text(
                    "STREAM VOLUME CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(streams) { streamInfo ->
                var currentVal by remember(streamInfo.currentVolume) { mutableFloatStateOf(streamInfo.currentVolume.toFloat()) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(streamInfo.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                "${currentVal.toInt()} / ${streamInfo.maxVolume}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            value = currentVal,
                            onValueChange = { currentVal = it },
                            onValueChangeFinished = {
                                scope.launch {
                                    AudioMasterManager.setStreamVolume(context, streamInfo.streamType, currentVal.toInt())
                                }
                            },
                            valueRange = 0f..streamInfo.maxVolume.toFloat(),
                            steps = if (streamInfo.maxVolume > 1) streamInfo.maxVolume - 1 else 0
                        )
                    }
                }
            }

            // Output Devices Card
            item {
                Text(
                    "DETECTED AUDIO OUTPUT HARDWARE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(devices) { dev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Speaker,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(dev.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(dev.typeName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Call Recording & Audio Permission Bypass Helper
            item {
                Text(
                    "CALL RECORDING & AUDIO STREAM TAPPER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Bypass Android 10+ Call Recording Restrictions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Grants READ_PHONE_STATE, RECORD_AUDIO, and overrides AppOps under Shizuku shell to allow non-system call recording apps full hardware audio access.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = grantPackage,
                            onValueChange = { grantPackage = it },
                            placeholder = { Text("Target package (e.g. com.recorder.app)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (grantPackage.isNotBlank()) {
                                    scope.launch {
                                        val ok = AudioMasterManager.grantCallRecordingAppOps(grantPackage.trim())
                                        if (ok) {
                                            Toast.makeText(context, "Permissions & AppOps granted successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to grant AppOps", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Grant Tapper Privileges")
                        }
                    }
                }
            }
        }
    }
}
