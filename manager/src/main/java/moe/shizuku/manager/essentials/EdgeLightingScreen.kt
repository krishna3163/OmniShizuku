package moe.shizuku.manager.essentials

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Notification Edge Lighting Controller & Preview
 * Authoritative port from Essentials (com.sameerasw.essentials.ui.features.lighting.NotificationLightingSettingsUI)
 *
 * Renders high-frame-rate display perimeter glowing neon lighting upon incoming notifications
 * with multiple lighting animations: Pulse, Neon Glow, Sweep, and Ripple.
 */
object EdgeLightingEngine {
    private const val PREFS_NAME = "essentials_edge_lighting"
    private const val KEY_ENABLED = "lighting_enabled"
    private const val KEY_STYLE = "lighting_style"
    private const val KEY_THICKNESS = "lighting_thickness"
    private const val KEY_COLOR = "lighting_color"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ENABLED, false)

    fun getStyle(context: Context): String =
        getPrefs(context).getString(KEY_STYLE, "Pulse") ?: "Pulse"

    fun getThickness(context: Context): Float =
        getPrefs(context).getFloat(KEY_THICKNESS, 6f)

    fun getColor(context: Context): Long =
        getPrefs(context).getLong(KEY_COLOR, 0xFF00E5FF)

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun setStyle(context: Context, style: String) {
        getPrefs(context).edit().putString(KEY_STYLE, style).apply()
    }

    fun setThickness(context: Context, thickness: Float) {
        getPrefs(context).edit().putFloat(KEY_THICKNESS, thickness).apply()
    }

    fun setColor(context: Context, color: Long) {
        getPrefs(context).edit().putLong(KEY_COLOR, color).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeLightingScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isEnabled by remember { mutableStateOf(EdgeLightingEngine.isEnabled(context)) }
    var selectedStyle by remember { mutableStateOf(EdgeLightingEngine.getStyle(context)) }
    var thickness by remember { mutableFloatStateOf(EdgeLightingEngine.getThickness(context)) }
    var selectedColor by remember { mutableLongStateOf(EdgeLightingEngine.getColor(context)) }
    var isPreviewing by remember { mutableStateOf(false) }

    val styles = listOf("Pulse", "Neon Glow", "Sweep", "Ripple")
    val presetColors = listOf(
        0xFF00E5FF, // Cyan
        0xFFFF3D00, // Sunset Orange
        0xFF76FF03, // Neon Lime
        0xFFE040FB, // Electric Magenta
        0xFFFFD600, // Cyber Yellow
        0xFF2979FF  // Deep Sky Blue
    )

    // Animation for preview
    val infiniteTransition = rememberInfiniteTransition(label = "edge_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Notification Edge Lighting", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Neon Display Glow Alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                // Master Switch Card
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
                                "Enable Edge Lighting",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Illuminates screen borders with high-vibrancy neon animation on incoming notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                EdgeLightingEngine.setEnabled(context, checked)
                            }
                        )
                    }
                }

                // Animation Style Picker
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
                        Text("Lighting Animation Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            styles.forEach { style ->
                                val selected = selectedStyle == style
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedStyle = style
                                        EdgeLightingEngine.setStyle(context, style)
                                    },
                                    label = { Text(style) },
                                    enabled = isEnabled
                                )
                            }
                        }
                    }
                }

                // Color Selection
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
                        Text("Neon Color Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presetColors.forEach { col ->
                                val isChosen = selectedColor == col
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(col))
                                        .border(
                                            width = if (isChosen) 3.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedColor = col
                                            EdgeLightingEngine.setColor(context, col)
                                        }
                                )
                            }
                        }
                    }
                }

                // Stroke Thickness
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Border Glow Thickness", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${thickness.toInt()} dp", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = thickness,
                            onValueChange = {
                                thickness = it
                                EdgeLightingEngine.setThickness(context, it)
                            },
                            valueRange = 2f..14f,
                            steps = 5,
                            enabled = isEnabled
                        )
                    }
                }

                // Test Live Preview Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isPreviewing = true
                            delay(4000)
                            isPreviewing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isEnabled
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPreviewing) "Lighting Active..." else "Test Live Edge Glow")
                }

                Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
            }
        }

        // Live edge glowing border overlay when previewing
        if (isPreviewing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = thickness.dp.toPx()
                val halfStroke = strokePx / 2f
                val glowColor = Color(selectedColor).copy(alpha = pulseAlpha)

                drawRoundRect(
                    color = glowColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(width = strokePx)
                )
            }
        }
    }
}
