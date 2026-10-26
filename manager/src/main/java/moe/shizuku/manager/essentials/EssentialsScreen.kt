package moe.shizuku.manager.essentials

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.shizuku.manager.ui.Detail

data class EssentialCategory(
    val title: String,
    val icon: ImageVector,
    val items: List<EssentialItem>
)

data class EssentialItem(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val detail: Detail
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssentialsScreen(
    bottomPadding: Dp,
    onBack: () -> Unit,
    onOpenDetail: (Detail) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    val categories = remember {
        listOf(
            EssentialCategory(
                title = "Display & Island",
                icon = Icons.Outlined.AspectRatio,
                items = listOf(
                    EssentialItem(
                        title = "Dynamic Island",
                        summary = "Floating punch-hole pill overlay for live media, battery & alerts",
                        icon = Icons.Outlined.NotificationsActive,
                        detail = Detail.ISLAND
                    ),
                    EssentialItem(
                        title = "Smart Pixels (AMOLED Saver)",
                        summary = "Disables 25%-75% OLED subpixel grid to save 15-44% display power",
                        icon = Icons.Outlined.GridOn,
                        detail = Detail.SMART_PIXELS
                    ),
                    EssentialItem(
                        title = "Display Tweaker & Refresh Rate",
                        summary = "Resolution, screen density (DPI), and refresh rate overrides",
                        icon = Icons.Outlined.Speed,
                        detail = Detail.DISPLAY
                    ),
                    EssentialItem(
                        title = "Force Dark Mode",
                        summary = "Forces dark themes across all installed applications",
                        icon = Icons.Outlined.DarkMode,
                        detail = Detail.FORCE_DARK
                    )
                )
            ),
            EssentialCategory(
                title = "Protection & Battery",
                icon = Icons.Outlined.Security,
                items = listOf(
                    EssentialItem(
                        title = "Battery Center & Guard",
                        summary = "Real-time battery health, cycle count, temperature & longevity guard",
                        icon = Icons.Outlined.BatteryChargingFull,
                        detail = Detail.BATTERY_CENTER
                    ),
                    EssentialItem(
                        title = "Pocket Mode Guard",
                        summary = "Proximity & light sensor protection against accidental touches in pocket",
                        icon = Icons.Outlined.ScreenLockPortrait,
                        detail = Detail.POCKET_MODE
                    ),
                    EssentialItem(
                        title = "Screen Security & DRM Unlock",
                        summary = "Bypasses FLAG_SECURE for screenshots in banking and streaming apps",
                        icon = Icons.Outlined.LockOpen,
                        detail = Detail.SECURITY
                    ),
                    EssentialItem(
                        title = "Permissions & AppOps",
                        summary = "Privileged permission controller and background service limits",
                        icon = Icons.Outlined.VerifiedUser,
                        detail = Detail.PERMISSIONS
                    )
                )
            ),
            EssentialCategory(
                title = "System & Optimization",
                icon = Icons.Outlined.SettingsSuggest,
                items = listOf(
                    EssentialItem(
                        title = "SystemUI Tuner",
                        summary = "Status bar icon blacklist, battery icon styles & immersive mode",
                        icon = Icons.Outlined.Tune,
                        detail = Detail.SYSTEMUI_TUNER
                    ),
                    EssentialItem(
                        title = "Axeron Gaming & CPU Optimizer",
                        summary = "Axeron performance profiles, zero animation latency & cache trim",
                        icon = Icons.Outlined.RocketLaunch,
                        detail = Detail.OPTIMIZER
                    ),
                    EssentialItem(
                        title = "Enforce Doze (Deep Sleep)",
                        summary = "Aggressive standby deep idle automation on screen off",
                        icon = Icons.Outlined.Bedtime,
                        detail = Detail.ENFORCE_DOZE
                    ),
                    EssentialItem(
                        title = "Advanced Power Controls",
                        summary = "Reboot, recovery, bootloader, shutdown & sleep key via shell",
                        icon = Icons.Outlined.PowerSettingsNew,
                        detail = Detail.POWER
                    )
                )
            ),
            EssentialCategory(
                title = "Connectivity & Network",
                icon = Icons.Outlined.Wifi,
                items = listOf(
                    EssentialItem(
                        title = "Private DNS Controller",
                        summary = "Encrypted zero-VPN DNS presets (Cloudflare, AdGuard, Quad9, Google)",
                        icon = Icons.Outlined.Dns,
                        detail = Detail.PRIVATE_DNS
                    ),
                    EssentialItem(
                        title = "Carrier Tethering Bypass",
                        summary = "Unlocks carrier-restricted hotspot without tether provisioning checks",
                        icon = Icons.Outlined.WifiTethering,
                        detail = Detail.TETHER
                    ),
                    EssentialItem(
                        title = "Saved Wi-Fi Passwords",
                        summary = "Reads and displays saved Wi-Fi access points and passkeys",
                        icon = Icons.Outlined.Password,
                        detail = Detail.WIFI_PASSWORDS
                    )
                )
            ),
            EssentialCategory(
                title = "Audio & Interaction",
                icon = Icons.Outlined.VolumeUp,
                items = listOf(
                    EssentialItem(
                        title = "Shut Up! Media Muter",
                        summary = "Per-app media audio muting when noisy apps become active",
                        icon = Icons.Outlined.VolumeOff,
                        detail = Detail.SHUT_UP
                    ),
                    EssentialItem(
                        title = "Audio Stream Master",
                        summary = "Independent volume stream control for media, ring, alarm, call",
                        icon = Icons.Outlined.GraphicEq,
                        detail = Detail.AUDIO_MASTER
                    ),
                    EssentialItem(
                        title = "Volume Key Track Skipper",
                        summary = "Skip tracks using volume up/down buttons when screen is off",
                        icon = Icons.Outlined.MusicNote,
                        detail = Detail.MEDIA_KEYS
                    )
                )
            ),
            EssentialCategory(
                title = "Interface & Visual Alerts",
                icon = Icons.Outlined.Highlight,
                items = listOf(
                    EssentialItem(
                        title = "Notification Edge Lighting",
                        summary = "Neon edge glow animation along display perimeter on notifications",
                        icon = Icons.Outlined.LightMode,
                        detail = Detail.EDGE_LIGHTING
                    ),
                    EssentialItem(
                        title = "Auto DND (Zen Mode)",
                        summary = "Automated Do-Not-Disturb scheduling based on app focus and schedules",
                        icon = Icons.Outlined.DoNotDisturbOn,
                        detail = Detail.AUTODND
                    )
                )
            ),
            EssentialCategory(
                title = "Power & Display",
                icon = Icons.Outlined.PowerSettingsNew,
                items = listOf(
                    EssentialItem(
                        title = "Caffeinate",
                        summary = "Keep the display awake for a chosen duration, with a stop notification and Quick Settings tile",
                        icon = Icons.Outlined.Coffee,
                        detail = Detail.CAFFEINATE
                    )
                )
            ),
            EssentialCategory(
                title = "Tools & Studio",
                icon = Icons.Outlined.Build,
                items = listOf(
                    EssentialItem(
                        title = "Photo Watermark & EXIF Studio",
                        summary = "Luxury Leica frame & Hasselblad minimal branding with live EXIF strip",
                        icon = Icons.Outlined.PhotoCamera,
                        detail = Detail.WATERMARK
                    ),
                    EssentialItem(
                        title = "Universal Android Debloater (UAD)",
                        summary = "1.6MB community safety-ranked package removal & 1-tap restore",
                        icon = Icons.Outlined.Delete,
                        detail = Detail.DEBLOATER
                    ),
                    EssentialItem(
                        title = "App Freezer",
                        summary = "Instantly freezes background apps without uninstalling",
                        icon = Icons.Outlined.AcUnit,
                        detail = Detail.FREEZER
                    ),
                    EssentialItem(
                        title = "Privileged File Explorer",
                        summary = "Browse protected system paths and Android/data directories",
                        icon = Icons.Outlined.FolderOpen,
                        detail = Detail.FILES
                    ),
                    EssentialItem(
                        title = "Package Installer Pro",
                        summary = "Installs split APKs, APKS, XAPK with downgrade & low-SDK flags",
                        icon = Icons.Outlined.InstallMobile,
                        detail = Detail.INSTALL
                    )
                )
            )
        )
    }

    // Default expand all categories
    LaunchedEffect(categories) {
        categories.forEach { cat ->
            if (!expandedStates.containsKey(cat.title)) {
                expandedStates[cat.title] = true
            }
        }
    }

    val filteredCategories = remember(searchQuery, categories) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            val q = searchQuery.trim().lowercase()
            categories.mapNotNull { cat ->
                val matchingItems = cat.items.filter {
                    it.title.lowercase().contains(q) || it.summary.lowercase().contains(q)
                }
                if (matchingItems.isNotEmpty()) {
                    cat.copy(items = matchingItems)
                } else null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Essentials Hub") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Essentials features…") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Categories
            items(filteredCategories, key = { it.title }) { category ->
                val isExpanded = expandedStates[category.title] ?: true
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ChevronRotation"
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Category Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .clickable {
                                    expandedStates[category.title] = !isExpanded
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        category.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = category.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${category.items.size} features",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                Icons.Outlined.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Category Items
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                category.items.forEach { item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOpenDetail(item.detail) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        item.icon,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = item.summary,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 15.sp,
                                                        maxLines = 2
                                                    )
                                                }
                                            }

                                            Icon(
                                                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                                contentDescription = "Open",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(bottomPadding + 16.dp))
            }
        }
    }
}
