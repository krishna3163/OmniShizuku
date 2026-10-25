package moe.shizuku.manager.watermark

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var config by remember { mutableStateOf(WatermarkManager.getConfig(context)) }

    fun updateConfig(newConfig: WatermarkConfig) {
        config = newConfig
        WatermarkManager.saveConfig(context, newConfig)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Watermark & EXIF") },
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
                            "Auto Photo Watermark Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (config.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (config.enabled) "Luxury branding active" else "Disabled",
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
                                if (isEnabled) "Watermark studio enabled" else "Watermark disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Live Banner Preview
            Text(
                "LUXURY WATERMARK PREVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Simulated photo with bottom watermark strip
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        // Simulated photo body
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color(0xFF2B3A4A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Sample Photo",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Watermark bottom banner
                        val isDarkFrame = config.theme == FrameTheme.DARK
                        val frameBg = if (isDarkFrame) Color(0xFF121212) else Color(0xFFFFFFFF)
                        val textPrimary = if (isDarkFrame) Color.White else Color.Black
                        val textSecondary = if (isDarkFrame) Color(0xFFAAAAAA) else Color(0xFF666666)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(frameBg)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left side: Device & Artist
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (config.style == WatermarkStyle.LEICA_FRAME) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(0xFFE53935), CircleShape)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Text(
                                        config.deviceName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                }
                                Text(
                                    config.customArtistText,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            }

                            // Right side: EXIF & Brand
                            if (config.showExif) {
                                Column(horizontalAlignment = Alignment.End) {
                                    val exifParts = mutableListOf<String>()
                                    if (config.showFocalLength) exifParts.add("24mm")
                                    if (config.showAperture) exifParts.add("f/1.8")
                                    if (config.showShutterSpeed) exifParts.add("1/1250s")
                                    if (config.showIso) exifParts.add("ISO 50")

                                    Text(
                                        exifParts.joinToString("  "),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary
                                    )
                                    if (config.showTimestamp) {
                                        Text(
                                            "2026.10.25 15:30",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Style Selector
            Text(
                "WATERMARK STYLE & BRANDING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WatermarkStyle.entries.forEach { style ->
                        val isSelected = config.style == style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(style.label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            RadioButton(
                                selected = isSelected,
                                onClick = { updateConfig(config.copy(style = style)) }
                            )
                        }
                    }
                }
            }

            // Theme Selector
            Text(
                "FRAME COLOR THEME",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrameTheme.entries.forEach { theme ->
                        val isSelected = config.theme == theme
                        FilterChip(
                            selected = isSelected,
                            onClick = { updateConfig(config.copy(theme = theme)) },
                            label = { Text(theme.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Metadata & Exif Toggles
            Text(
                "EXIF PARAMETERS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataToggleRow("Show EXIF Metadata Bar", config.showExif) {
                        updateConfig(config.copy(showExif = it))
                    }
                    if (config.showExif) {
                        HorizontalDivider()
                        MetadataToggleRow("Focal Length (mm)", config.showFocalLength) {
                            updateConfig(config.copy(showFocalLength = it))
                        }
                        HorizontalDivider()
                        MetadataToggleRow("Aperture (f-stop)", config.showAperture) {
                            updateConfig(config.copy(showAperture = it))
                        }
                        HorizontalDivider()
                        MetadataToggleRow("Shutter Speed", config.showShutterSpeed) {
                            updateConfig(config.copy(showShutterSpeed = it))
                        }
                        HorizontalDivider()
                        MetadataToggleRow("ISO Sensitivity", config.showIso) {
                            updateConfig(config.copy(showIso = it))
                        }
                        HorizontalDivider()
                        MetadataToggleRow("Date & Timestamp", config.showTimestamp) {
                            updateConfig(config.copy(showTimestamp = it))
                        }
                    }
                }
            }

            Spacer(Modifier.height(bottomPadding + 16.dp))
        }
    }
}

@Composable
private fun MetadataToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
