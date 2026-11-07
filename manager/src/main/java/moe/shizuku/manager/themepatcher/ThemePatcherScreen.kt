package moe.shizuku.manager.themepatcher

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import moe.shizuku.manager.ui.component.LabToolScaffold
import moe.shizuku.manager.ui.component.LabToolStatus
import moe.shizuku.manager.ui.theme.OmniTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePatcherScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var overlays by remember { mutableStateOf<List<SystemThemeOverlay>>(emptyList()) }
    var oemState by remember { mutableStateOf<OemThemeTrialState?>(null) }
    var loading by remember { mutableStateOf(true) }
    var actionLoading by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val isShizukuRunning = ThemePatcherManager.isShizukuAvailable()

    fun refresh() {
        loading = true
        scope.launch {
            overlays = ThemePatcherManager.getSystemOverlays()
            oemState = ThemePatcherManager.getOemTrialState(context)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val scaffoldStatus = when {
        !isShizukuRunning -> LabToolStatus.Warning(
            title = "Shizuku Not Active",
            message = "Shizuku service is not running. Applying theme overlays and modifying trial settings require Shizuku shell privileges.",
            icon = Icons.Outlined.WarningAmber
        )
        else -> null
    }

    LabToolScaffold(
        title = "Theme Patcher",
        description = "Manage system theme overlays, colors, and icon shapes, or patch OEM Theme Store trial limits.",
        status = scaffoldStatus,
        onBack = onBack,
        bottomPadding = bottomPadding,
        actions = {
            IconButton(
                onClick = { refresh() },
                enabled = !loading && !actionLoading
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("System Overlays (${overlays.size})") },
                    icon = { Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("OEM Theme Store") },
                    icon = { Icon(Icons.Outlined.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Scanning theme overlays and system properties...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (selectedTab == 0) {
                // Tab 0: System Overlays
                SystemOverlaysContent(
                    overlays = overlays,
                    selectedFilter = selectedCategoryFilter,
                    onFilterChange = { selectedCategoryFilter = it },
                    isShizukuRunning = isShizukuRunning,
                    actionLoading = actionLoading,
                    onToggleOverlay = { overlay ->
                        actionLoading = true
                        scope.launch {
                            val ok = if (overlay.isEnabled) {
                                ThemePatcherManager.disableOverlay(overlay)
                            } else {
                                ThemePatcherManager.enableOverlay(overlay, overlays)
                            }
                            if (ok) {
                                Toast.makeText(
                                    context,
                                    if (!overlay.isEnabled) "Applied ${overlay.displayName}" else "Disabled ${overlay.displayName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refresh()
                            } else {
                                Toast.makeText(context, "Failed to update overlay", Toast.LENGTH_SHORT).show()
                            }
                            actionLoading = false
                        }
                    },
                    onResetCategory = { category ->
                        actionLoading = true
                        scope.launch {
                            val ok = ThemePatcherManager.disableAllCategory(category, overlays)
                            if (ok) {
                                Toast.makeText(context, "Reset $category overlays", Toast.LENGTH_SHORT).show()
                                refresh()
                            }
                            actionLoading = false
                        }
                    }
                )
            } else {
                // Tab 1: OEM Theme Store
                OemThemeStoreContent(
                    state = oemState,
                    isShizukuRunning = isShizukuRunning,
                    actionLoading = actionLoading,
                    onApplyTrialFreeze = {
                        actionLoading = true
                        scope.launch {
                            val (ok, msg) = ThemePatcherManager.applyOemTrialFreeze(context)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            refresh()
                            actionLoading = false
                        }
                    },
                    onResetTrialDefaults = {
                        actionLoading = true
                        scope.launch {
                            val (ok, msg) = ThemePatcherManager.resetOemTrialDefaults()
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            refresh()
                            actionLoading = false
                        }
                    },
                    onLaunchThemeStore = {
                        val launched = ThemePatcherManager.launchThemeStore(context)
                        if (!launched) {
                            Toast.makeText(context, "No OEM Theme Store app found on this device", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SystemOverlaysContent(
    overlays: List<SystemThemeOverlay>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    isShizukuRunning: Boolean,
    actionLoading: Boolean,
    onToggleOverlay: (SystemThemeOverlay) -> Unit,
    onResetCategory: (String) -> Unit
) {
    val categories = listOf("All", "Colors", "Icon Shapes", "Icon Packs", "Fonts")
    val filteredOverlays = if (selectedFilter == "All") {
        overlays
    } else {
        overlays.filter { it.category == selectedFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedFilter == category,
                    onClick = { onFilterChange(category) },
                    label = { Text(category) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filteredOverlays.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No theme overlays found for category: $selectedFilter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (selectedFilter != "All") {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$selectedFilter (${filteredOverlays.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { onResetCategory(selectedFilter) },
                                enabled = isShizukuRunning && !actionLoading
                            ) {
                                Text("Reset $selectedFilter")
                            }
                        }
                    }
                }

                items(filteredOverlays, key = { it.packageName }) { overlay ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (overlay.isEnabled) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = overlay.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = overlay.category,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = overlay.packageName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = overlay.isEnabled,
                                onCheckedChange = { onToggleOverlay(overlay) },
                                enabled = isShizukuRunning && !actionLoading
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OemThemeStoreContent(
    state: OemThemeTrialState?,
    isShizukuRunning: Boolean,
    actionLoading: Boolean,
    onApplyTrialFreeze: () -> Unit,
    onResetTrialDefaults: () -> Unit,
    onLaunchThemeStore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = OmniTokens.brandPrimary(),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Theme Store Trial Status",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (state?.isTrialActive == true) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = if (state?.isTrialActive == true) "Trial Active" else "Frozen / Normal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state?.isTrialActive == true) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TrialInfoRow(
                        label = "OEM Theme Store",
                        value = if (state?.isStoreInstalled == true) state.storePackage ?: "Installed" else "Not detected"
                    )
                    TrialInfoRow(
                        label = "persist.sys.trial.theme (System)",
                        value = state?.trialThemeSystem ?: "0"
                    )
                    TrialInfoRow(
                        label = "persist.sys.trial.font (System)",
                        value = state?.trialFontSystem ?: "0"
                    )
                    TrialInfoRow(
                        label = "persist.sys.oplus.theme_uuid",
                        value = state?.oplusThemeUuid ?: "None"
                    )
                }
            }
        }

        // Trial Freeze Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoFixHigh,
                            contentDescription = null,
                            tint = OmniTokens.brandPrimary(),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Trial Limit Freeze",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Oppo / Realme / OnePlus Devices",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Sets trial timer properties to 0 and uuid to -1 across system and secure settings, then kills the Theme Store service. This stops the 5-minute trial countdown and locks in the applied theme or font permanently.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onApplyTrialFreeze,
                            enabled = isShizukuRunning && !actionLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Apply Trial Freeze")
                            }
                        }

                        OutlinedButton(
                            onClick = onResetTrialDefaults,
                            enabled = isShizukuRunning && !actionLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Defaults")
                        }
                    }
                }
            }
        }

        // Theme Store App Launcher Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Launch,
                            contentDescription = null,
                            tint = OmniTokens.brandPrimary(),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Launch Theme Store & Watcher",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Automated Background Trial Watcher",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Launches the OEM Theme Store app and runs the background watcher service. When you apply a 5-minute Free Trial in the store, the watcher automatically freezes the timer.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = onLaunchThemeStore,
                        enabled = isShizukuRunning && !actionLoading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open Theme Store")
                    }
                }
            }
        }
    }
}

@Composable
private fun TrialInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
