package moe.shizuku.manager.doze

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import moe.shizuku.manager.ui.theme.OmniTokens
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
import moe.shizuku.manager.service.EnforceDozeServiceStarter

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

    // EnforceDoze Service and advanced toggles
    var enforceDozeServiceEnabled by remember { mutableStateOf(EnforceDozeManager.isServiceEnabled(context)) }
    var disableWhenCharging by remember { mutableStateOf(EnforceDozeManager.isDisableWhenCharging(context)) }
    var turnOffAllSensors by remember { mutableStateOf(EnforceDozeManager.isTurnOffAllSensorsInDoze(context)) }
    var turnOffBiometrics by remember { mutableStateOf(EnforceDozeManager.isTurnOffBiometricsInDoze(context)) }
    var turnOnBatterySaver by remember { mutableStateOf(EnforceDozeManager.isTurnOnBatterySaverInDoze(context)) }
    var turnOnAirplane by remember { mutableStateOf(EnforceDozeManager.isTurnOnAirplaneInDoze(context)) }
    var turnOffBluetooth by remember { mutableStateOf(EnforceDozeManager.isTurnOffBluetoothInDoze(context)) }
    var turnOffGps by remember { mutableStateOf(EnforceDozeManager.isTurnOffGPSInDoze(context)) }
    var turnOffWifi by remember { mutableStateOf(EnforceDozeManager.isTurnOffWiFiInDoze(context)) }
    var turnOffData by remember { mutableStateOf(EnforceDozeManager.isTurnOffDataInDoze(context)) }
    var waitForUnlock by remember { mutableStateOf(EnforceDozeManager.isWaitForUnlock(context)) }
    var showPersistentNotif by remember { mutableStateOf(EnforceDozeManager.isShowPersistentNotif(context)) }

    var showBlocklistDialog by remember { mutableStateOf(false) }
    var showTunablesDialog by remember { mutableStateOf(false) }

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
                title = {
                    Column {
                        Text("Enforce Doze (Deep Sleep)")
                        Text("Configure Doze behavior. May delay background work.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                },
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
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = OmniTokens.brandPrimary(),
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
                                color = if (deepState.equals("IDLE", true)) Color(0xFF2E7D32) else OmniTokens.brandPrimary()
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

            // Master EnforceDoze Service Switch
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = if (enforceDozeServiceEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "EnforceDoze Background Service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (enforceDozeServiceEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Runs background monitor that forces deep sleep upon screen-off, manages battery & sensors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enforceDozeServiceEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enforceDozeServiceEnabled,
                        onCheckedChange = { checked ->
                            enforceDozeServiceEnabled = checked
                            EnforceDozeManager.setServiceEnabled(context, checked)
                            if (checked) {
                                EnforceDozeServiceStarter.startService(context)
                                Toast.makeText(context, "EnforceDoze Service started", Toast.LENGTH_SHORT).show()
                            } else {
                                EnforceDozeServiceStarter.stopService(context)
                                Toast.makeText(context, "EnforceDoze Service stopped", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Switch 1: Aggressive Doze
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
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
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
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

            // Additional EnforceDoze Features
            Text(
                text = "EnforceDoze Advanced Profiles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Disable when charging
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Disable When Charging", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Skips entering Doze when connected to AC/USB power.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = disableWhenCharging,
                        onCheckedChange = {
                            disableWhenCharging = it
                            EnforceDozeManager.setDisableWhenCharging(context, it)
                        }
                    )
                }
            }

            // Turn off biometric unlock during doze
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Turn Off Biometrics in Doze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Temporarily disables fingerprint/face wake while dozing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = turnOffBiometrics,
                        onCheckedChange = {
                            turnOffBiometrics = it
                            EnforceDozeManager.setTurnOffBiometricsInDoze(context, it)
                        }
                    )
                }
            }

            // Battery Saver in Doze
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Turn On Battery Saver in Doze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Automatically triggers system Battery Saver mode during deep sleep.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = turnOnBatterySaver,
                        onCheckedChange = {
                            turnOnBatterySaver = it
                            EnforceDozeManager.setTurnOnBatterySaverInDoze(context, it)
                        }
                    )
                }
            }

            // Persistent Notification
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Persistent Stats Notification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Shows Doze statistics and quick controls in notification shade.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showPersistentNotif,
                        onCheckedChange = {
                            showPersistentNotif = it
                            EnforceDozeManager.setShowPersistentNotif(context, it)
                        }
                    )
                }
            }

            // Dialog Triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showBlocklistDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Block, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("App Blocklist")
                }

                OutlinedButton(
                    onClick = { showTunablesDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tunables Editor")
                }
            }

            if (showBlocklistDialog) {
                DozeBlocklistDialog(
                    onDismiss = { showBlocklistDialog = false },
                    context = context
                )
            }

            if (showTunablesDialog) {
                DozeTunablesDialog(
                    onDismiss = { showTunablesDialog = false },
                    context = context
                )
            }

            // Doze Constants
            Text(
                text = "Doze Tunable Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
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
