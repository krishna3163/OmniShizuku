package moe.shizuku.manager.display.smartpixels

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPixelsScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var config by remember { mutableStateOf(SmartPixelsManager.getConfig(context)) }

    fun updateConfig(newConfig: SmartPixelsConfig) {
        config = newConfig
        SmartPixelsManager.saveConfig(context, newConfig)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Pixels AMOLED Saver") },
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
                            "AMOLED Smart Pixels",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (config.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (config.enabled) "Active (${config.pattern.powerSaving})" else "Disabled",
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
                                if (isEnabled) "Smart Pixels enabled" else "Smart Pixels turned off",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Interactive OLED Dither Canvas Preview
            Text(
                "OLED SUBPIXEL DITHER PATTERN PREVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E88E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw black dots representing turned-off OLED pixels
                        val pattern = config.pattern
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val step = when (pattern) {
                                SmartPixelsPattern.DISABLED -> 0
                                SmartPixelsPattern.LIGHT -> 8
                                SmartPixelsPattern.MEDIUM -> 5
                                SmartPixelsPattern.AGGRESSIVE -> 3
                            }
                            if (step > 0 && config.enabled) {
                                val dotSize = 2.5f
                                val width = size.width
                                val height = size.height
                                var x = 0f
                                while (x < width) {
                                    var y = 0f
                                    while (y < height) {
                                        drawCircle(
                                            color = Color.Black,
                                            radius = dotSize,
                                            center = Offset(x, y)
                                        )
                                        y += step * 3
                                    }
                                    x += step * 3
                                }
                            }
                        }

                        Text(
                            if (config.enabled) "Live OLED Mesh (${pattern.percent}% off)" else "Normal Display (100% active)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "On OLED and AMOLED panels, black pixels draw exactly 0mA current. Turning off selective pixel matrices extends battery endurance significantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Intensity Mode Selector
            Text(
                "SAVINGS INTENSITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmartPixelsPattern.entries.filter { it != SmartPixelsPattern.DISABLED }.forEach { pat ->
                        val isSelected = config.pattern == pat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(pat.label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(pat.powerSaving, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    updateConfig(config.copy(pattern = pat))
                                }
                            )
                        }
                    }
                }
            }

            // Burn-in Prevention & Battery Automation
            Text(
                "BURN-IN PROTECTION & AUTOMATION",
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
                            Text("Pixel Shift Interval", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Periodically shifts black subpixel matrix to prevent panel ghosting", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${config.burnInShiftMinutes} mins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-engage on Low Battery", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Automatically activates Smart Pixels when battery falls under 20%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.autoOnLowBattery,
                            onCheckedChange = { checked ->
                                updateConfig(config.copy(autoOnLowBattery = checked))
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(bottomPadding + 16.dp))
        }
    }
}
