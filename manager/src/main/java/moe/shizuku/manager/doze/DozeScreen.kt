package moe.shizuku.manager.doze

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DozeScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var deepState by remember { mutableStateOf("Querying...") }
    var lightState by remember { mutableStateOf("Querying...") }
    var aggressiveDoze by remember { mutableStateOf(DozeManager.isAggressiveDozeEnabled(context)) }
    var motionKill by remember { mutableStateOf(DozeManager.isMotionKillEnabled(context)) }
    var dozeConstants by remember { mutableStateOf("Loading...") }
    var executing by remember { mutableStateOf(false) }

    fun refreshStates() {
        scope.launch {
            deepState = DozeManager.getDeepDozeState()
            lightState = DozeManager.getLightDozeState()
            dozeConstants = DozeManager.getDozeSettingsRaw()
        }
    }

    LaunchedEffect(Unit) {
        refreshStates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enforce Doze (Deep Sleep)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshStates() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(bottom = bottomPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Device Idle Status",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Deep Doze State", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = deepState,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (deepState.equals("IDLE", true)) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                        }

                        Column {
                            Text("Light Doze State", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = lightState,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            executing = true
                            val res = DozeManager.forceDeepIdle()
                            executing = false
                            Toast.makeText(context, "Deep Doze Forced: $res", Toast.LENGTH_SHORT).show()
                            refreshStates()
                        }
                    },
                    enabled = !executing,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Force Deep Doze")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            executing = true
                            val res = DozeManager.unforceIdle()
                            executing = false
                            Toast.makeText(context, "Doze Reset: $res", Toast.LENGTH_SHORT).show()
                            refreshStates()
                        }
                    },
                    enabled = !executing,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Doze")
                }
            }

            Text(
                text = "Automation Rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Switch 1: Aggressive Doze
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aggressive Screen-Off Doze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Bypasses Android's 30-minute idle delay and triggers Deep Doze immediately when the screen turns off.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = aggressiveDoze,
                        onCheckedChange = { checked ->
                            aggressiveDoze = checked
                            DozeManager.setAggressiveDozeEnabled(context, checked)
                            Toast.makeText(context, if (checked) "Aggressive Doze enabled" else "Aggressive Doze disabled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Switch 2: Motion Sensor Kill
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Disable Motion Sensors in Doze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Prevents device movement (e.g. while walking or in vehicle) from waking the phone out of deep sleep.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = motionKill,
                        onCheckedChange = { checked ->
                            motionKill = checked
                            DozeManager.setMotionKillEnabled(context, checked)
                            Toast.makeText(context, if (checked) "Motion Sensor kill enabled" else "Motion Sensor kill disabled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Doze Constants
            Text(
                text = "Doze Tunable Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = dozeConstants,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
