package moe.shizuku.manager.ui.component

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import moe.shizuku.manager.home.CpuCounters
import moe.shizuku.manager.home.HomeMetricsCalculator
import moe.shizuku.manager.ui.theme.LocalAmoledTheme
import java.io.File
import java.util.Locale

/**
 * Data class representing real-time hardware telemetry and device metrics.
 */
data class LiveDeviceMetrics(
    // CPU
    val cpuPercent: Double? = null,
    val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
    // RAM
    val ramUsedBytes: Long = 0L,
    val ramTotalBytes: Long = 0L,
    val ramFreeBytes: Long = 0L,
    // Network (Live rates & totals)
    val rxSpeedBps: Double? = null,
    val txSpeedBps: Double? = null,
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
    // Storage
    val storageUsedBytes: Long = 0L,
    val storageFreeBytes: Long = 0L,
    val storageTotalBytes: Long = 0L,
    // Battery
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val batteryStatus: String = "Discharging",
    val batteryPlugged: String = "Battery",
    val batteryTemperatureC: Double? = null,
    val batteryHealth: String = "Good",
    val batteryTechnology: String = "Li-ion",
    val batteryVoltageMv: Int? = null,
    // System & Kernel Specs
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    val kernelRelease: String = System.getProperty("os.version") ?: "Linux",
    val architecture: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
    val uptimeMillis: Long = SystemClock.elapsedRealtime()
)

/**
 * Sampling helper to collect current metrics safely without blocking the UI thread.
 */
private class LiveMetricsCollector(private val context: Context) {
    private var prevCpu: CpuCounters? = null
    private var prevRxBytes: Long? = null
    private var prevTxBytes: Long? = null
    private var prevNetworkTimestamp: Long? = null

    fun reset() {
        prevCpu = null
        prevRxBytes = null
        prevTxBytes = null
        prevNetworkTimestamp = null
    }

    fun sample(): LiveDeviceMetrics {
        val now = SystemClock.elapsedRealtime()

        // 1. CPU
        val currCpu = runCatching {
            HomeMetricsCalculator.parseCpuCounters(File("/proc/stat").readText())
        }.getOrNull()
        val cpuPercent = HomeMetricsCalculator.cpuUtilizationPercent(prevCpu, currCpu)
        prevCpu = currCpu

        // 2. RAM
        val am = context.getSystemService(ActivityManager::class.java)
        val mem = ActivityManager.MemoryInfo().also { am?.getMemoryInfo(it) }
        val ramTotal = mem.totalMem
        val ramFree = mem.availMem
        val ramUsed = (ramTotal - ramFree).coerceAtLeast(0L)

        // 3. Network Speeds
        val rx = TrafficStats.getTotalRxBytes().takeIf { it >= 0L } ?: 0L
        val tx = TrafficStats.getTotalTxBytes().takeIf { it >= 0L } ?: 0L
        val elapsedNetMs = prevNetworkTimestamp?.let { now - it } ?: 0L
        val rxSpeed = HomeMetricsCalculator.bytesPerSecond(prevRxBytes, rx, elapsedNetMs)
        val txSpeed = HomeMetricsCalculator.bytesPerSecond(prevTxBytes, tx, elapsedNetMs)
        prevRxBytes = rx
        prevTxBytes = tx
        prevNetworkTimestamp = now

        // 4. Storage (StatFs)
        val dataDir = Environment.getDataDirectory()
        val stat = runCatching { StatFs(dataDir.path) }.getOrNull()
        val storageTotal = stat?.totalBytes ?: 0L
        val storageFree = stat?.availableBytes ?: 0L
        val storageUsed = (storageTotal - storageFree).coerceAtLeast(0L)

        // 5. Battery
        val batteryIntent = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()

        val rawLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val level = if (scale > 0 && rawLevel in 0..scale) (rawLevel * 100 / scale) else 0

        val statusInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusInt == BatteryManager.BATTERY_STATUS_FULL
        val batteryStatus = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Discharging"
        }

        val pluggedInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
        val batteryPlugged = when (pluggedInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }

        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val tempC = if (rawTemp != null && rawTemp != Int.MIN_VALUE) rawTemp / 10.0 else null

        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthText = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }

        val tech = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val volt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 }

        return LiveDeviceMetrics(
            cpuPercent = cpuPercent,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            ramUsedBytes = ramUsed,
            ramTotalBytes = ramTotal,
            ramFreeBytes = ramFree,
            rxSpeedBps = rxSpeed,
            txSpeedBps = txSpeed,
            totalRxBytes = rx,
            totalTxBytes = tx,
            storageUsedBytes = storageUsed,
            storageFreeBytes = storageFree,
            storageTotalBytes = storageTotal,
            batteryLevel = level,
            isCharging = isCharging,
            batteryStatus = batteryStatus,
            batteryPlugged = batteryPlugged,
            batteryTemperatureC = tempC,
            batteryHealth = healthText,
            batteryTechnology = tech,
            batteryVoltageMv = volt,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}".trim(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            kernelRelease = System.getProperty("os.version") ?: "Linux",
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
            uptimeMillis = SystemClock.elapsedRealtime()
        )
    }
}

/**
 * Beautiful, Hallmark-compliant device information card with live telemetry.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveDeviceInfoSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var isStarted by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isStarted = true
                Lifecycle.Event.ON_STOP -> isStarted = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val collector = remember(context) { LiveMetricsCollector(context) }
    var metrics by remember { mutableStateOf(LiveDeviceMetrics()) }

    LaunchedEffect(isStarted, collector) {
        if (!isStarted) {
            collector.reset()
            return@LaunchedEffect
        }
        collector.reset()
        while (isActive) {
            val sampled = withContext(Dispatchers.IO) { collector.sample() }
            metrics = sampled
            delay(2000L)
        }
    }

    val isAmoled = LocalAmoledTheme.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isAmoled) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Icon, Title & Live Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Device Performance & Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time hardware telemetry and diagnostics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Live Pulse Badge
                Surface(
                    shape = CircleShape,
                    color = if (metrics.isCharging) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (metrics.isCharging) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                        )
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // 1. Core Performance Grid: CPU, RAM, Network
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Hardware Resources",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // CPU Row
                MetricProgressRow(
                    icon = Icons.Rounded.Memory,
                    label = "CPU Load",
                    headline = metrics.cpuPercent?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Sampling...",
                    supporting = "${metrics.cpuCores} active CPU cores",
                    progress = (metrics.cpuPercent?.toFloat()?.div(100f))?.coerceIn(0f, 1f) ?: 0.05f
                )

                // RAM Row
                val ramFraction = if (metrics.ramTotalBytes > 0) {
                    (metrics.ramUsedBytes.toFloat() / metrics.ramTotalBytes.toFloat()).coerceIn(0f, 1f)
                } else 0f
                MetricProgressRow(
                    icon = Icons.Rounded.DeveloperBoard,
                    label = "RAM Memory",
                    headline = "${formatGigabytes(metrics.ramUsedBytes)} / ${formatGigabytes(metrics.ramTotalBytes)} GB",
                    supporting = "${formatGigabytes(metrics.ramFreeBytes)} GB available (${(ramFraction * 100).toInt()}% used)",
                    progress = ramFraction
                )

                // Network Up / Down Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Download
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Download",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatSpeed(metrics.rxSpeedBps),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total ${formatDataSize(metrics.totalRxBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Upload
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowUpward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Upload",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatSpeed(metrics.txSpeedBps),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total ${formatDataSize(metrics.totalTxBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // 2. Storage Section ("Kitna khali aur bacha hua ek bar me")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Internal Storage",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${formatGigabytes(metrics.storageUsedBytes)} GB used of ${formatGigabytes(metrics.storageTotalBytes)} GB",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dual-Segment Single Storage Bar
                val storageFraction = if (metrics.storageTotalBytes > 0) {
                    (metrics.storageUsedBytes.toDouble() / metrics.storageTotalBytes.toDouble()).toFloat()
                } else 0f
                val animatedStorageFraction by animateFloatAsState(
                    targetValue = storageFraction.coerceIn(0.01f, 1f),
                    animationSpec = tween(durationMillis = 600),
                    label = "storageProgress"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        // Used portion
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedStorageFraction)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Storage detail tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Used: ${formatGigabytes(metrics.storageUsedBytes)} GB (${(storageFraction * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Text(
                                text = "Free: ${formatGigabytes(metrics.storageFreeBytes)} GB (${100 - (storageFraction * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // 3. Battery & Power Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Battery & Power Telemetry",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Charging State
                    TelemetryChip(
                        icon = if (metrics.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                        label = "Status",
                        value = "${metrics.batteryStatus} (${metrics.batteryLevel}%)",
                        highlight = metrics.isCharging
                    )

                    // Power Source
                    TelemetryChip(
                        icon = Icons.Rounded.Power,
                        label = "Power Source",
                        value = metrics.batteryPlugged
                    )

                    // Temperature
                    TelemetryChip(
                        icon = Icons.Rounded.Thermostat,
                        label = "Temperature",
                        value = metrics.batteryTemperatureC?.let { String.format(Locale.US, "%.1f °C", it) } ?: "—"
                    )

                    // Health
                    TelemetryChip(
                        icon = Icons.Rounded.Speed,
                        label = "Health",
                        value = metrics.batteryHealth
                    )

                    // Technology
                    TelemetryChip(
                        icon = Icons.Rounded.DeveloperBoard,
                        label = "Technology",
                        value = metrics.batteryTechnology
                    )

                    // Voltage
                    val voltage = metrics.batteryVoltageMv
                    if (voltage != null) {
                        TelemetryChip(
                            icon = Icons.Rounded.Power,
                            label = "Voltage",
                            value = String.format(Locale.US, "%.2f V", voltage / 1000.0)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // 4. System & Kernel Specs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "System & Kernel Architecture",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoSpecRow(label = "Device Model", value = metrics.deviceModel)
                        InfoSpecRow(label = "Android Release", value = metrics.androidVersion)
                        InfoSpecRow(label = "Kernel Version", value = metrics.kernelRelease)
                        InfoSpecRow(label = "Architecture & ABI", value = "${metrics.architecture} (${metrics.cpuCores} Cores)")
                        InfoSpecRow(label = "System Uptime", value = formatUptime(metrics.uptimeMillis))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricProgressRow(
    icon: ImageVector,
    label: String,
    headline: String,
    supporting: String,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.02f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "metricProgress"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TelemetryChip(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun InfoSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatGigabytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f", gb)
}

private fun formatDataSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024.0) {
        String.format(Locale.US, "%.2f GB", mb / 1024.0)
    } else {
        String.format(Locale.US, "%.1f MB", mb)
    }
}

private fun formatSpeed(bps: Double?): String {
    if (bps == null || bps < 0.0) return "0.0 kB/s"
    return if (bps >= 1_000_000.0) {
        String.format(Locale.US, "%.1f MB/s", bps / 1_000_000.0)
    } else {
        String.format(Locale.US, "%.1f kB/s", bps / 1_000.0)
    }
}

private fun formatUptime(millis: Long): String {
    val minutes = millis / 60_000
    val days = minutes / (60 * 24)
    val hours = (minutes % (60 * 24)) / 60
    val mins = minutes % 60
    return buildString {
        if (days > 0) append("${days}d ")
        if (days > 0 || hours > 0) append("${hours}h ")
        append("${mins}m")
    }
}
