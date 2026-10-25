package moe.shizuku.manager.battery

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun BatteryCenterScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var stats by remember { mutableStateOf<BatteryStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDumpsys by remember { mutableStateOf(false) }

    fun refreshStats() {
        scope.launch {
            isLoading = true
            stats = BatteryCenterManager.getBatteryStats(context)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Center & Guard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshStats() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentStats = stats
            if (currentStats == null && isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (currentStats != null) {
                // Main Battery Level & Health Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (currentStats.isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryFull,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${currentStats.level}%",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            "${currentStats.status} • ${currentStats.powerSource}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Linear progress bar for level
                        LinearProgressIndicator(
                            progress = { currentStats.level / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = if (currentStats.level <= 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Quick Diagnostics Grid
                Text(
                    "DIAGNOSTICS & HARDWARE STATS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricRow("Battery Health", currentStats.health, Icons.Outlined.FavoriteBorder)
                        HorizontalDivider()
                        MetricRow("Temperature", "${currentStats.temperatureC} °C", Icons.Outlined.Thermostat)
                        HorizontalDivider()
                        MetricRow("Voltage", "${currentStats.voltageMv} mV", Icons.Outlined.Bolt)
                        HorizontalDivider()
                        MetricRow("Technology", currentStats.technology, Icons.Outlined.Memory)
                        if (currentStats.cycleCount != null) {
                            HorizontalDivider()
                            MetricRow("Charge Cycles", "${currentStats.cycleCount} cycles", Icons.Outlined.Loop)
                        }
                    }
                }

                // Charging Guard & Battery Longevity Card
                Text(
                    "BATTERY LONGEVITY GUARD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("80% Charge Limit Guard", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(
                                    "Prevents continuous trickle-charging at 100% to protect lithium-ion cell chemistry.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = currentStats.chargeLimitEnabled,
                                onCheckedChange = { checked ->
                                    BatteryCenterManager.setChargeLimitEnabled(context, checked)
                                    scope.launch {
                                        if (checked) {
                                            BatteryCenterManager.applyChargeThreshold(80)
                                            Toast.makeText(context, "Charge limit guard engaged (80%)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            BatteryCenterManager.resetBatteryEmulation()
                                            Toast.makeText(context, "Charge limit guard disabled", Toast.LENGTH_SHORT).show()
                                        }
                                        refreshStats()
                                    }
                                }
                            )
                        }
                    }
                }

                // Shizuku Battery Controls
                Text(
                    "SHIZUKU RECOVERY ACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val success = BatteryCenterManager.resetBatteryStats()
                                Toast.makeText(
                                    context,
                                    if (success) "Battery stats history reset" else "Reset failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refreshStats()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Stats")
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val success = BatteryCenterManager.resetBatteryEmulation()
                                Toast.makeText(
                                    context,
                                    if (success) "Battery emulated state cleared" else "Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refreshStats()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset State")
                    }
                }

                // Dumpsys toggle
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDumpsys = !showDumpsys }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dumpsys Battery Details", fontWeight = FontWeight.Medium)
                            Icon(
                                if (showDumpsys) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null
                            )
                        }
                        AnimatedVisibility(visible = showDumpsys) {
                            Text(
                                currentStats.rawDumpsys.ifEmpty { "No dumpsys battery output available." },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(bottomPadding + 16.dp))
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
