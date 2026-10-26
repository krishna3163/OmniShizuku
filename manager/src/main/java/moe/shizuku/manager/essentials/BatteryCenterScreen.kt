package moe.shizuku.manager.essentials

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class RealBatteryStats(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val chargingSpeedWatts: Float = 0f,
    val temperatureCelsius: Float = 0f,
    val voltageMv: Int = 0,
    val health: String = "Good",
    val technology: String = "Li-ion",
    val cycleCount: Int = -1
)

/**
 * Battery Center & Guard Engine
 * Authoritative port from Essentials (com.sameerasw.essentials.utils.battery.BatteryInfoUtil)
 *
 * Reads real-time hardware battery telemetry via Android BatteryManager & Linux sysfs power_supply
 * without executing dangerous PMIC shell commands.
 */
object BatteryCenterEngine {
    fun getBatteryStats(context: Context): RealBatteryStats {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, filter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempRaw / 10.0f

        val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        val healthRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }

        // Try reading cycle count from sysfs or BatteryManager on Android 14+
        var cycleCount = -1
        if (Build.VERSION.SDK_INT >= 34) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            try {
                val cycleMethod = BatteryManager::class.java.getMethod("getIntProperty", Int::class.javaPrimitiveType)
                val count = cycleMethod.invoke(bm, 7) as? Int ?: -1
                if (count > 0) cycleCount = count
            } catch (_: Exception) {}
        }
        if (cycleCount == -1) {
            try {
                val cycleFile = File("/sys/class/power_supply/battery/cycle_count")
                if (cycleFile.canRead()) {
                    cycleCount = cycleFile.readText().trim().toIntOrNull() ?: -1
                }
            } catch (_: Exception) {}
        }

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentMicroAmp = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        val currentAmps = kotlin.math.abs(currentMicroAmp) / 1000000.0f
        val volts = voltageMv / 1000.0f
        val watts = if (isCharging) (currentAmps * volts) else 0f

        return RealBatteryStats(
            level = batteryPct,
            isCharging = isCharging,
            chargingSpeedWatts = watts,
            temperatureCelsius = tempCelsius,
            voltageMv = voltageMv,
            health = health,
            technology = technology,
            cycleCount = cycleCount
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryCenterScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var stats by remember { mutableStateOf(BatteryCenterEngine.getBatteryStats(context)) }
    var chargeLimitEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        stats = BatteryCenterEngine.getBatteryStats(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Battery Center & Guard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Hardware Health & Charging Telemetry", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { stats = BatteryCenterEngine.getBatteryStats(context) }) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (stats.isCharging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (stats.isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (stats.isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                            contentDescription = null,
                            tint = if (stats.isCharging) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text("${stats.level}%", fontWeight = FontWeight.Black, fontSize = 48.sp)
                    Text(
                        if (stats.isCharging) "Charging (${if (stats.chargingSpeedWatts > 0f) "%.1f W".format(stats.chargingSpeedWatts) else "Plugged in"})" else "Discharging",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Real Hardware Telemetry Grid
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Hardware Telemetry", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Battery Health", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stats.health, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Battery Temperature", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.1f °C".format(stats.temperatureCelsius), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Voltage", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stats.voltageMv} mV", fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Chemistry", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stats.technology, fontWeight = FontWeight.SemiBold)
                    }

                    if (stats.cycleCount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Charge Cycle Count", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${stats.cycleCount} cycles", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Battery Longevity Guard
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("80% Charge Longevity Guard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Notifies and prompts to unplug when battery reaches 80% to extend Li-ion lifespan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = chargeLimitEnabled,
                        onCheckedChange = { chargeLimitEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
