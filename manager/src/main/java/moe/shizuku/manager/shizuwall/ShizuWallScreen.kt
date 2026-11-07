@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package moe.shizuku.manager.shizuwall

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.manage.InstalledPackages
import moe.shizuku.manager.manage.PackageTools
import moe.shizuku.manager.ui.component.AppIcon
import moe.shizuku.manager.utils.ShizukuStateMachine

private enum class ShizuWallFilter {
    ALL, BLOCKED, ALLOWED, TRACKERS, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizuWallScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val scope = rememberCoroutineScope()

    var isMasterActive by remember { mutableStateOf(ShizuWallManager.isMasterEnabled(context)) }
    var apps by remember { mutableStateOf<List<PackageInfo>>(emptyList()) }
    var blockedSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var trackersMap by remember { mutableStateOf<Map<String, List<Tracker>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf(ShizuWallFilter.ALL) }
    var selectedAppForTrackers by remember { mutableStateOf<PackageInfo?>(null) }
    var scanningTrackersFor by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    fun refreshState() {
        scope.launch {
            loading = true
            val (pkgList, blockedList) = withContext(Dispatchers.IO) {
                val installed = InstalledPackages.all(context, 0)
                val blocked = installed.filter {
                    PackageTools.readNetworkBlocked(context, it.packageName) == true
                }.map { it.packageName }.toSet()
                Pair(installed, blocked)
            }
            apps = pkgList
            blockedSet = blockedList
            isMasterActive = ShizuWallManager.isMasterEnabled(context)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshState()
    }

    val filteredApps = remember(apps, blockedSet, trackersMap, searchQuery, activeFilter) {
        apps.filter { pkg ->
            val appInfo = pkg.applicationInfo
            val isSystem = (appInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
            val label = appInfo?.loadLabel(pm)?.toString().orEmpty()
            val matchesQuery = searchQuery.isBlank() ||
                label.contains(searchQuery, ignoreCase = true) ||
                pkg.packageName.contains(searchQuery, ignoreCase = true)

            if (!matchesQuery) return@filter false

            when (activeFilter) {
                ShizuWallFilter.ALL -> true
                ShizuWallFilter.BLOCKED -> pkg.packageName in blockedSet
                ShizuWallFilter.ALLOWED -> pkg.packageName !in blockedSet
                ShizuWallFilter.TRACKERS -> trackersMap[pkg.packageName]?.isNotEmpty() == true
                ShizuWallFilter.SYSTEM -> isSystem
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ShizuWall Firewall", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            if (isMasterActive) "Firewall Active · ${blockedSet.size} apps blocked"
                            else "Firewall Inactive · Rules preserved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = isMasterActive,
                        onCheckedChange = { enable ->
                            scope.launch {
                                val ok = withContext(Dispatchers.IO) {
                                    ShizuWallManager.setMasterEnabled(context, enable)
                                }
                                if (ok) {
                                    isMasterActive = enable
                                } else {
                                    Toast.makeText(context, "Failed to toggle firewall chain", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            // Master banner / Mode Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMasterActive) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isMasterActive) Icons.Outlined.Security else Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = if (isMasterActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isMasterActive) "Chain 3 Firewall Protection Enabled"
                            else "Firewall Protection Disabled",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isMasterActive) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Direct kernel/connectivity socket denial without battery-draining VPN loops.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMasterActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search apps or packages…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeFilter == ShizuWallFilter.ALL,
                    onClick = { activeFilter = ShizuWallFilter.ALL },
                    label = { Text("All (${apps.size})") }
                )
                FilterChip(
                    selected = activeFilter == ShizuWallFilter.BLOCKED,
                    onClick = { activeFilter = ShizuWallFilter.BLOCKED },
                    label = { Text("Blocked (${blockedSet.size})") }
                )
                FilterChip(
                    selected = activeFilter == ShizuWallFilter.ALLOWED,
                    onClick = { activeFilter = ShizuWallFilter.ALLOWED },
                    label = { Text("Allowed (${apps.size - blockedSet.size})") }
                )
                FilterChip(
                    selected = activeFilter == ShizuWallFilter.SYSTEM,
                    onClick = { activeFilter = ShizuWallFilter.SYSTEM },
                    label = { Text("System") }
                )
            }

            // App list
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { pkg ->
                        val isBlocked = pkg.packageName in blockedSet
                        val label = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                        val detectedTrackers = trackersMap[pkg.packageName]
                        val isScanning = scanningTrackersFor == pkg.packageName

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 3.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isBlocked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            tonalElevation = if (isBlocked) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val targetBlocked = !isBlocked
                                        scope.launch {
                                            val success = withContext(Dispatchers.IO) {
                                                PackageTools.setNetworkBlocked(context, pkg.packageName, targetBlocked)
                                            }
                                            if (success) {
                                                blockedSet = if (targetBlocked) blockedSet + pkg.packageName
                                                else blockedSet - pkg.packageName
                                            } else {
                                                Toast.makeText(context, "Failed to update ${pkg.packageName}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                ) {
                                    AppIcon(pkg)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = pkg.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (detectedTrackers != null && detectedTrackers.isNotEmpty()) {
                                        Text(
                                            text = "⚠️ ${detectedTrackers.size} trackers found",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                // Tracker inspect button
                                IconButton(
                                    onClick = {
                                        if (detectedTrackers == null && !isScanning) {
                                            scanningTrackersFor = pkg.packageName
                                            scope.launch {
                                                val res = TrackerScanner.scan(context, pkg.packageName)
                                                if (res is TrackerScanner.ScanResult.Success) {
                                                    trackersMap = trackersMap + (pkg.packageName to res.trackers)
                                                    selectedAppForTrackers = pkg
                                                } else {
                                                    Toast.makeText(context, "No trackers detected", Toast.LENGTH_SHORT).show()
                                                }
                                                scanningTrackersFor = null
                                            }
                                        } else {
                                            selectedAppForTrackers = pkg
                                        }
                                    }
                                ) {
                                    if (isScanning) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            Icons.Outlined.BugReport,
                                            contentDescription = "Scan Trackers",
                                            tint = if (detectedTrackers?.isNotEmpty() == true) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.width(4.dp))

                                // Toggle Switch
                                Switch(
                                    checked = isBlocked,
                                    onCheckedChange = { targetBlocked ->
                                        scope.launch {
                                            val success = withContext(Dispatchers.IO) {
                                                PackageTools.setNetworkBlocked(context, pkg.packageName, targetBlocked)
                                            }
                                            if (success) {
                                                blockedSet = if (targetBlocked) blockedSet + pkg.packageName
                                                else blockedSet - pkg.packageName
                                            } else {
                                                Toast.makeText(context, "Failed to update ${pkg.packageName}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Tracker details dialog
    selectedAppForTrackers?.let { pkg ->
        val trackers = trackersMap[pkg.packageName].orEmpty()
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedAppForTrackers = null },
            title = {
                Text(
                    text = "${pkg.applicationInfo?.loadLabel(pm) ?: pkg.packageName} Trackers",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (trackers.isEmpty()) {
                        Text("No known advertising or telemetry trackers detected in this APK.")
                    } else {
                        Text(
                            text = "Found ${trackers.size} tracker signatures embedded:",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(trackers) { t ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(t.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    if (t.categories.isNotEmpty()) {
                                        Text(t.categories.joinToString(", "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAppForTrackers = null }) {
                    Text("Close")
                }
            }
        )
    }
}

