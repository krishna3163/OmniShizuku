package moe.shizuku.manager.island

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(IslandManager.getConfig(context)) }
    var isExpanded by remember { mutableStateOf(false) }

    val pillWidthAnim by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else config.pillWidthDp.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pill_width"
    )

    val pillHeightAnim by animateDpAsState(
        targetValue = if (isExpanded) 80.dp else config.pillHeightDp.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pill_height"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic Island Floating Overlay") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Interactive Floating Pill Preview Stage
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = config.topMarginDp.dp),
                    contentAlignment = when (config.position) {
                        CutoutPosition.CENTER -> Alignment.TopCenter
                        CutoutPosition.LEFT -> Alignment.TopStart
                        CutoutPosition.RIGHT -> Alignment.TopEnd
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(width = pillWidthAnim, height = pillHeightAnim)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isExpanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Now Playing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("OmniShizuku Sound Engine", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                                Icon(
                                    Icons.Outlined.Equalizer,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF00E5FF), CircleShape)
                                )
                                Text(
                                    "Tap to expand",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Outlined.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD600),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Master Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Dynamic Island Overlay", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Streams live music playback, charging status, and notification banners into a hardware-accelerated punch-hole pill.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = config.enabled,
                        onCheckedChange = {
                            val updated = config.copy(enabled = it)
                            config = updated
                            IslandManager.setConfig(context, updated)
                            Toast.makeText(context, if (it) "Dynamic Island Activated" else "Island Disabled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Camera Cutout Position
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Display Cutout Alignment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CutoutPosition.entries.forEach { pos ->
                            FilterChip(
                                selected = config.position == pos,
                                onClick = {
                                    val updated = config.copy(position = pos)
                                    config = updated
                                    IslandManager.setConfig(context, updated)
                                },
                                label = { Text(pos.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Subsystem Feature Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Island Activity Providers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Media Visualizer & Track Info", fontSize = 14.sp)
                        Checkbox(
                            checked = config.showMedia,
                            onCheckedChange = {
                                val updated = config.copy(showMedia = it)
                                config = updated
                                IslandManager.setConfig(context, updated)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Battery Charging Ring Pulse", fontSize = 14.sp)
                        Checkbox(
                            checked = config.showBattery,
                            onCheckedChange = {
                                val updated = config.copy(showBattery = it)
                                config = updated
                                IslandManager.setConfig(context, updated)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notification Hub Banners", fontSize = 14.sp)
                        Checkbox(
                            checked = config.showNotifications,
                            onCheckedChange = {
                                val updated = config.copy(showNotifications = it)
                                config = updated
                                IslandManager.setConfig(context, updated)
                            }
                        )
                    }
                }
            }
        }
    }
}
