package moe.shizuku.manager.home

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import rikka.shizuku.Shizuku
import java.io.File
import java.util.Locale

private const val POLL_INTERVAL_MS = 3_000L

private data class UsageMetrics(
    val serviceConnected: Boolean = false,
    val permissionGranted: Boolean = false,
    val cpuPercent: Double? = null,
    val cpuCounterAvailable: Boolean = false,
    val memoryUsedBytes: Long? = null,
    val memoryTotalBytes: Long? = null,
    val batteryTemperatureC: Double? = null,
    val batteryDrainPercentPerHour: Double? = null,
    val batteryLevelAvailable: Boolean = false,
    val receivedBytesPerSecond: Double? = null,
    val sentBytesPerSecond: Double? = null,
    val receiveCounterAvailable: Boolean = false,
    val sendCounterAvailable: Boolean = false
)

private class HomeMetricsRepository(private val context: Context) {
    private var previousCpu: CpuCounters? = null
    private var previousRx: Long? = null
    private var previousTx: Long? = null
    private var previousNetworkAt: Long? = null
    private var batteryBaseline: Pair<Int, Long>? = null

    fun reset() {
        previousCpu = null
        previousRx = null
        previousTx = null
        previousNetworkAt = null
        batteryBaseline = null
    }

    fun sample(): UsageMetrics {
        val connected = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        val permitted = connected && runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
        if (!permitted) {
            reset()
            return UsageMetrics(serviceConnected = connected)
        }

        val cpu = runCatching {
            HomeMetricsCalculator.parseCpuCounters(File("/proc/stat").readText())
        }.getOrNull()
        val cpuPercent = HomeMetricsCalculator.cpuUtilizationPercent(previousCpu, cpu)
        previousCpu = cpu

        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memory = runCatching {
            ActivityManager.MemoryInfo().also { activityManager?.getMemoryInfo(it) }
        }.getOrNull()?.takeIf { it.totalMem > 0L && it.availMem >= 0L }
        val memoryTotal = memory?.totalMem
        val memoryUsed = memory?.let { (it.totalMem - it.availMem).coerceIn(0L, it.totalMem) }

        val batteryIntent = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        val rawTemperature = batteryIntent
            ?.takeIf { it.hasExtra(BatteryManager.EXTRA_TEMPERATURE) }
            ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE }
        val temperatureC = rawTemperature?.div(10.0)
        val level = batteryIntent?.let { intent ->
            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (intent.hasExtra(BatteryManager.EXTRA_LEVEL) && scale > 0 && rawLevel in 0..scale) {
                (rawLevel * 100 / scale).coerceIn(0, 100)
            } else null
        }
        val plugged = batteryIntent
            ?.takeIf { it.hasExtra(BatteryManager.EXTRA_PLUGGED) }
            ?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val now = SystemClock.elapsedRealtime()
        val drainRate = estimateDrain(level, plugged, now)

        val rx = TrafficStats.getTotalRxBytes().takeIf { it >= 0L }
        val tx = TrafficStats.getTotalTxBytes().takeIf { it >= 0L }
        val networkAt = previousNetworkAt
        val rxRate = HomeMetricsCalculator.bytesPerSecond(previousRx, rx, networkAt?.let { now - it } ?: 0L)
        val txRate = HomeMetricsCalculator.bytesPerSecond(previousTx, tx, networkAt?.let { now - it } ?: 0L)
        previousRx = rx
        previousTx = tx
        previousNetworkAt = now.takeIf { rx != null || tx != null }

        return UsageMetrics(
            serviceConnected = true,
            permissionGranted = true,
            cpuPercent = cpuPercent,
            cpuCounterAvailable = cpu != null,
            memoryUsedBytes = memoryUsed,
            memoryTotalBytes = memoryTotal,
            batteryTemperatureC = temperatureC,
            batteryDrainPercentPerHour = drainRate,
            batteryLevelAvailable = level != null,
            receivedBytesPerSecond = rxRate,
            sentBytesPerSecond = txRate,
            receiveCounterAvailable = rx != null,
            sendCounterAvailable = tx != null
        )
    }

    private fun estimateDrain(level: Int?, plugged: Int?, now: Long): Double? {
        if (level == null || plugged != 0) {
            batteryBaseline = null
            return null
        }
        val previous = batteryBaseline
        if (previous == null) {
            batteryBaseline = level to now
            return null
        }
        if (level == previous.first) return null
        val rate = HomeMetricsCalculator.batteryDrainPercentPerHour(
            previous.first,
            level,
            now - previous.second
        )
        batteryBaseline = level to now
        return rate
    }
}

@Composable
internal fun HomeCurrentUsageCard(isVisible: Boolean) {
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var started by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> started = true
                Lifecycle.Event.ON_STOP -> started = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val repository = remember(context) { HomeMetricsRepository(context) }
    var metrics by remember { mutableStateOf(UsageMetrics()) }
    LaunchedEffect(isVisible, started, repository) {
        if (!isVisible || !started) {
            repository.reset()
            metrics = UsageMetrics()
            return@LaunchedEffect
        }
        repository.reset()
        while (isActive) {
            val next = withContext(Dispatchers.IO) { repository.sample() }
            metrics = next
            delay(POLL_INTERVAL_MS)
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.home_usage_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            when {
                !isVisible || !started -> Text(
                    stringResource(R.string.home_usage_paused),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                !metrics.serviceConnected -> Text(
                    stringResource(R.string.home_usage_service_unavailable),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                !metrics.permissionGranted -> Text(
                    stringResource(R.string.home_usage_permission_missing),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {
                    val memoryUsedBytes = metrics.memoryUsedBytes
                    val memoryTotalBytes = metrics.memoryTotalBytes
                    val batteryTemperatureC = metrics.batteryTemperatureC
                    val batteryDrainRate = metrics.batteryDrainPercentPerHour
                    val receivedRate = metrics.receivedBytesPerSecond
                    val sentRate = metrics.sentBytesPerSecond

                    UsageMetricRow(
                        stringResource(R.string.home_usage_cpu),
                        metrics.cpuPercent?.let { String.format(Locale.getDefault(), "%.0f%%", it) }
                            ?: unavailableOrWaiting(metrics.cpuCounterAvailable)
                    )
                    UsageMetricRow(
                        stringResource(R.string.home_usage_memory),
                        if (memoryUsedBytes != null && memoryTotalBytes != null) {
                            "${formatBytes(memoryUsedBytes)} / ${formatBytes(memoryTotalBytes)}"
                        } else stringResource(R.string.home_usage_unavailable)
                    )
                    UsageMetricRow(
                        stringResource(R.string.home_usage_battery_temperature),
                        batteryTemperatureC?.let {
                            String.format(Locale.getDefault(), "%.1f °C", it)
                        } ?: stringResource(R.string.home_usage_unavailable)
                    )
                    UsageMetricRow(
                        stringResource(R.string.home_usage_battery_drain),
                        batteryDrainRate?.let {
                            stringResource(R.string.home_usage_percent_per_hour, it)
                        } ?: stringResource(
                            if (metrics.batteryLevelAvailable) R.string.home_usage_sampling
                            else R.string.home_usage_unavailable
                        )
                    )
                    UsageMetricRow(
                        stringResource(R.string.home_usage_download),
                        formatRate(receivedRate, metrics.receiveCounterAvailable)
                    )
                    UsageMetricRow(
                        stringResource(R.string.home_usage_upload),
                        formatRate(sentRate, metrics.sendCounterAvailable)
                    )
                    Text(
                        stringResource(R.string.home_usage_network_scope),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun HomeDeviceInfoCard(onClick: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                stringResource(R.string.home_device_info_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.home_device_info_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UsageMetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun unavailableOrWaiting(counterAvailable: Boolean): String =
    stringResource(if (counterAvailable) R.string.home_usage_sampling else R.string.home_usage_unavailable)

@Composable
private fun formatRate(rate: Double?, countersAvailable: Boolean): String =
    when {
        !countersAvailable -> stringResource(R.string.home_usage_unavailable)
        rate == null -> stringResource(R.string.home_usage_sampling)
        rate < 1_000.0 -> String.format(Locale.getDefault(), "%.0f B/s", rate)
        rate < 1_000_000.0 -> String.format(Locale.getDefault(), "%.1f kB/s", rate / 1_000.0)
        else -> String.format(Locale.getDefault(), "%.1f MB/s", rate / 1_000_000.0)
    }

private fun formatBytes(bytes: Long): String {
    val mib = bytes / (1024.0 * 1024.0)
    return if (mib >= 1024.0) String.format(Locale.getDefault(), "%.1f GiB", mib / 1024.0)
    else String.format(Locale.getDefault(), "%.0f MiB", mib)
}
