package moe.shizuku.manager.essentials

import android.content.Context
import android.os.BatteryManager
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sameerasw.essentials.utils.battery.BatteryInfoUtil
import com.sameerasw.essentials.utils.DeviceUtils
import com.sameerasw.essentials.utils.battery.ChargingMode
import com.sameerasw.essentials.utils.battery.ChargingModeUtil

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
        val basic = BatteryInfoUtil.getBasicDetails(context)
        val details = BatteryInfoUtil.fetchAdvancedDetails(context, basic)
        val level = if (details.scale > 0) (details.level * 100 / details.scale).coerceAtLeast(0) else 0
        val isCharging = details.status == BatteryManager.BATTERY_STATUS_CHARGING ||
            details.status == BatteryManager.BATTERY_STATUS_FULL
        val health = when (details.health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }
        val currentMa = details.currentNowMa ?: 0
        val watts = if (isCharging) kotlin.math.abs(currentMa) * details.voltage / 1_000_000f else 0f

        return RealBatteryStats(
            level = level,
            isCharging = isCharging,
            chargingSpeedWatts = watts,
            temperatureCelsius = details.temperature / 10f,
            voltageMv = details.voltage,
            health = health,
            technology = details.technology,
            cycleCount = details.cycleCount ?: -1
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
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf(RealBatteryStats()) }
    var chargeLimitEnabled by remember { mutableStateOf(false) }
    var chargeModeAllowed by remember { mutableStateOf(false) }
    val supportsPixelChargingMode = remember { DeviceUtils.isGoogleDevice() }

    fun refreshStats() {
        scope.launch {
            stats = withContext(Dispatchers.IO) { BatteryCenterEngine.getBatteryStats(context) }
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
        val chargeModeState = withContext(Dispatchers.IO) {
            ChargingModeUtil.hasPermission(context) to
                (ChargingModeUtil.getMode(context) == ChargingMode.LIMITED)
        }
        chargeModeAllowed = chargeModeState.first
        chargeLimitEnabled = chargeModeState.second
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

            if (supportsPixelChargingMode) {
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
                            Text("Pixel charging limit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Uses the device's system charging optimization setting. Requires Shizuku permission and a supported Pixel device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = chargeLimitEnabled,
                            enabled = chargeModeAllowed,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    val requestedMode = if (enabled) ChargingMode.LIMITED else ChargingMode.OFF
                                    val isLimited = withContext(Dispatchers.IO) {
                                        ChargingModeUtil.setMode(context, requestedMode)
                                        ChargingModeUtil.getMode(context) == ChargingMode.LIMITED
                                    }
                                    chargeLimitEnabled = isLimited
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
