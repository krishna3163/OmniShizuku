package moe.shizuku.manager.hardware.pocket

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketModeScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var config by remember { mutableStateOf(PocketModeManager.getConfig(context)) }

    // Live Sensor Reading
    var proximityDistance by remember { mutableStateOf(-1f) }
    var proximityMaxRange by remember { mutableStateOf(5f) }
    var isCovered by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor != null) {
            proximityMaxRange = proximitySensor.maximumRange
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null && event.sensor.type == Sensor.TYPE_PROXIMITY) {
                        val distance = event.values[0]
                        proximityDistance = distance
                        isCovered = distance < proximitySensor.maximumRange
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_UI)
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        } else {
            onDispose {}
        }
    }

    fun updateConfig(newConfig: PocketModeConfig) {
        config = newConfig
        PocketModeManager.saveConfig(context, newConfig)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pocket Mode Guard") },
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (config.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Accidental Touch Guard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (config.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (config.enabled) "Active • Sensor monitoring armed" else "Disabled",
                            fontSize = 13.sp,
                            color = if (config.enabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = { isEnabled ->
                            updateConfig(config.copy(enabled = isEnabled))
                            Toast.makeText(
                                context,
                                if (isEnabled) "Pocket Mode armed" else "Pocket Mode disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Real-time Sensor Visualizer Card
            Text(
                "HARDWARE SENSOR LIVE STATUS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (isCovered) Color(0xFFE53935) else Color(0xFF43A047),
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Proximity State", fontWeight = FontWeight.Medium)
                        }
                        Text(
                            if (isCovered) "BLOCKED (In Pocket)" else "CLEAR (Uncovered)",
                            fontWeight = FontWeight.Bold,
                            color = if (isCovered) Color(0xFFE53935) else Color(0xFF43A047)
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sensor Distance", fontSize = 14.sp)
                        Text(
                            if (proximityDistance >= 0) "$proximityDistance cm / max $proximityMaxRange cm" else "Not detected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Prevention Options
            Text(
                "GUARD BEHAVIOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Lock Screen", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Immediately sleep/lock device if covered for more than trigger delay", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.lockScreenOnDetection,
                            onCheckedChange = { updateConfig(config.copy(lockScreenOnDetection = it)) }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Feedback on Trigger", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Vibrate gently when pocket mode is activated", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.vibrateOnTrigger,
                            onCheckedChange = { updateConfig(config.copy(vibrateOnTrigger = it)) }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Combine with Light Sensor", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Verifies both low ambient lux and proximity cover before engaging", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.useAmbientLightSensor,
                            onCheckedChange = { updateConfig(config.copy(useAmbientLightSensor = it)) }
                        )
                    }
                }
            }

            // Test Shizuku Lock Action
            Button(
                onClick = {
                    scope.launch {
                        PocketModeManager.lockScreenPrivileged()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Test Privileged Screen Lock")
            }

            Spacer(Modifier.height(bottomPadding + 16.dp))
        }
    }
}
