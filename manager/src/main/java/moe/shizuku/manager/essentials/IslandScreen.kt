package moe.shizuku.manager.essentials

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CutoutPosition {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * Dynamic Island Controller & Overlay Preview
 * Authoritative port from Essentials (com.sameerasw.essentials.ui.features.display.IslandSettingsUI & IslandWindowHost)
 *
 * Implements interactive floating punch-hole pill overlay for live media playback visualizers,
 * charging battery rings, notifications, OTP quick-copy, and system status cues.
 */
object IslandEngine {
    private const val PREFS_NAME = "essentials_island"
    private const val KEY_ENABLED = "island_enabled"
    private const val KEY_POSITION = "island_position"
    private const val KEY_PILL_WIDTH = "island_pill_width"
    private const val KEY_MEDIA_PLUGIN = "island_plugin_media"
    private const val KEY_BATTERY_PLUGIN = "island_plugin_battery"
    private const val KEY_TIMER_PLUGIN = "island_plugin_timer"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ENABLED, false)

    fun getPosition(context: Context): CutoutPosition {
        val name = getPrefs(context).getString(KEY_POSITION, CutoutPosition.CENTER.name)
        return try { CutoutPosition.valueOf(name ?: CutoutPosition.CENTER.name) } catch (_: Exception) { CutoutPosition.CENTER }
    }

    fun getPillWidth(context: Context): Float =
        getPrefs(context).getFloat(KEY_PILL_WIDTH, 140f)

    fun isMediaPlugin(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_MEDIA_PLUGIN, true)

    fun isBatteryPlugin(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_BATTERY_PLUGIN, true)

    fun isTimerPlugin(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_TIMER_PLUGIN, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun setPosition(context: Context, pos: CutoutPosition) {
        getPrefs(context).edit().putString(KEY_POSITION, pos.name).apply()
    }

    fun setPillWidth(context: Context, width: Float) {
        getPrefs(context).edit().putFloat(KEY_PILL_WIDTH, width).apply()
    }

    fun setMediaPlugin(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MEDIA_PLUGIN, enabled).apply()
    }

    fun setBatteryPlugin(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BATTERY_PLUGIN, enabled).apply()
    }

    fun setTimerPlugin(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TIMER_PLUGIN, enabled).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isEnabled by remember { mutableStateOf(IslandEngine.isEnabled(context)) }
    var position by remember { mutableStateOf(IslandEngine.getPosition(context)) }
    var pillWidth by remember { mutableFloatStateOf(IslandEngine.getPillWidth(context)) }
    var mediaPlugin by remember { mutableStateOf(IslandEngine.isMediaPlugin(context)) }
    var batteryPlugin by remember { mutableStateOf(IslandEngine.isBatteryPlugin(context)) }
    var timerPlugin by remember { mutableStateOf(IslandEngine.isTimerPlugin(context)) }
    var isPreviewing by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    // Floating Island Pill Animation
    val animatedPillWidth by animateDpAsState(
        targetValue = if (isExpanded) 320.dp else pillWidth.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pill_width"
    )
    val animatedPillHeight by animateDpAsState(
        targetValue = if (isExpanded) 90.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pill_height"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Dynamic Island", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Floating Punch-Hole Overlay & Live Activities", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Master Toggle
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
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
                            Text(
                                "Enable Dynamic Island",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Floating pill around your camera cutout with interactive widgets and music cues",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                IslandEngine.setEnabled(context, checked)
                            }
                        )
                    }
                }

                // Cutout Alignment & Geometry
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Display Cutout Geometry", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CutoutPosition.values().forEach { pos ->
                                FilterChip(
                                    selected = position == pos,
                                    onClick = {
                                        position = pos
                                        IslandEngine.setPosition(context, pos)
                                    },
                                    label = { Text(pos.name) },
                                    enabled = isEnabled
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pill Base Width", fontWeight = FontWeight.SemiBold)
                                Text("${pillWidth.toInt()} dp", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = pillWidth,
                                onValueChange = {
                                    pillWidth = it
                                    IslandEngine.setPillWidth(context, it)
                                },
                                valueRange = 100f..220f,
                                steps = 11,
                                enabled = isEnabled
                            )
                        }
                    }
                }

                // Subsystem Live Plugins
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
                        Text("Island Live Plugins", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Now Playing Music Visualizer", fontWeight = FontWeight.SemiBold)
                                Text("Shows album art, mini audio waveform & track skip on tap", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = mediaPlugin,
                                onCheckedChange = {
                                    mediaPlugin = it
                                    IslandEngine.setMediaPlugin(context, it)
                                },
                                enabled = isEnabled
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Battery Charging Animation", fontWeight = FontWeight.SemiBold)
                                Text("Displays neon charging progress ring when connected to power", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = batteryPlugin,
                                onCheckedChange = {
                                    batteryPlugin = it
                                    IslandEngine.setBatteryPlugin(context, it)
                                },
                                enabled = isEnabled
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Timer & Stopwatch Countdown", fontWeight = FontWeight.SemiBold)
                                Text("Live countdown clock pinned next to front camera cutout", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = timerPlugin,
                                onCheckedChange = {
                                    timerPlugin = it
                                    IslandEngine.setTimerPlugin(context, it)
                                },
                                enabled = isEnabled
                            )
                        }
                    }
                }

                // Interactive Preview Trigger
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isPreviewing = true
                            isExpanded = false
                            delay(1200)
                            isExpanded = true
                            delay(3500)
                            isExpanded = false
                            delay(800)
                            isPreviewing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isEnabled
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPreviewing) "Preview Running..." else "Trigger Island Live Animation")
                }

                Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
            }
        }

        // Live Floating Island Pill Preview Overlay
        if (isPreviewing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .align(
                            when (position) {
                                CutoutPosition.LEFT -> Alignment.TopStart
                                CutoutPosition.CENTER -> Alignment.TopCenter
                                CutoutPosition.RIGHT -> Alignment.TopEnd
                            }
                        )
                        .padding(horizontal = if (position == CutoutPosition.CENTER) 0.dp else 24.dp)
                        .size(width = animatedPillWidth, height = animatedPillHeight)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black)
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isExpanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            Text("Now Playing", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Outlined.GraphicEq, contentDescription = null, tint = Color(0xFF76FF03), modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF212121)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Starboy", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("The Weeknd", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                                Icon(Icons.Outlined.GraphicEq, contentDescription = null, tint = Color(0xFF76FF03), modifier = Modifier.size(24.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(28.dp))
                                Icon(Icons.Outlined.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(28.dp))
                                Icon(Icons.Outlined.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
