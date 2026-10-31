package moe.shizuku.manager.debloater

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch

private enum class FilterTab(val label: String) {
    ALL("All"),
    RECOMMENDED("Recommended"),
    ADVANCED("Advanced"),
    EXPERT("Expert"),
    UNSAFE("Unsafe"),
    SYSTEM("System"),
    DISABLED("Disabled"),
    UNINSTALLED("Uninstalled")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebloaterScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apps by remember { mutableStateOf<List<DebloatApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterTab.ALL) }
    var selectedApp by remember { mutableStateOf<DebloatApp?>(null) }
    var updatingDatabase by remember { mutableStateOf(false) }

    fun refreshApps() {
        scope.launch {
            loading = true
            apps = DebloaterManager.getInstalledApps(context)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    val filteredApps = remember(apps, searchQuery, selectedFilter) {
        apps.filter { app ->
            val matchesQuery = searchQuery.isBlank() ||
                    app.label.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true) ||
                    (app.uadEntry?.description?.contains(searchQuery, ignoreCase = true) == true)

            val matchesFilter = when (selectedFilter) {
                FilterTab.ALL -> true
                FilterTab.RECOMMENDED -> app.uadEntry?.removal == UadRemoval.RECOMMENDED
                FilterTab.ADVANCED -> app.uadEntry?.removal == UadRemoval.ADVANCED
                FilterTab.EXPERT -> app.uadEntry?.removal == UadRemoval.EXPERT
                FilterTab.UNSAFE -> app.uadEntry?.removal == UadRemoval.UNSAFE
                FilterTab.SYSTEM -> app.uadEntry?.removal == UadRemoval.SYSTEM
                FilterTab.DISABLED -> app.state == AppState.DISABLED
                FilterTab.UNINSTALLED -> app.state == AppState.UNINSTALLED
            }

            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Universal Debloater (UAD)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                updatingDatabase = true
                                Toast.makeText(context, "Updating UAD list from GitHub...", Toast.LENGTH_SHORT).show()
                                val result = UadRepository.updateFromRemote(context)
                                updatingDatabase = false
                                result.onSuccess { count ->
                                    Toast.makeText(context, "Updated $count UAD definitions", Toast.LENGTH_SHORT).show()
                                    refreshApps()
                                }.onFailure { e ->
                                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !updatingDatabase
                    ) {
                        if (updatingDatabase) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.CloudDownload, contentDescription = "Update UAD Database")
                        }
                    }
                    IconButton(onClick = { refreshApps() }, enabled = !loading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(bottom = bottomPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps, packages, or descriptions...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FilterTab.entries) { tab ->
                    FilterChip(
                        selected = selectedFilter == tab,
                        onClick = { selectedFilter = tab },
                        label = { Text(tab.label) }
                    )
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Auditing installed packages with UAD database...")
                    }
                }
            } else {
                Text(
                    text = "${filteredApps.size} packages listed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        DebloatAppCard(
                            app = app,
                            onClick = { selectedApp = app }
                        )
                    }
                }
            }
        }
    }

    selectedApp?.let { app ->
        AppActionBottomSheet(
            app = app,
            onDismiss = { selectedApp = null },
            onRefresh = {
                selectedApp = null
                refreshApps()
            }
        )
    }
}

@Composable
private fun DebloatAppCard(
    app: DebloatApp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bitmap = remember(app.icon) {
                app.icon?.let { runCatching { it.toBitmap(96, 96).asImageBitmap() }.getOrNull() }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (app.isSystem) Icons.Outlined.Android else Icons.Outlined.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val removal = app.uadEntry?.removal ?: if (app.isSystem) UadRemoval.SYSTEM else UadRemoval.UNKNOWN
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = removal.color.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = removal.displayName,
                            color = removal.color,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!app.uadEntry?.description.isNullOrBlank()) {
                    Text(
                        text = app.uadEntry!!.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (app.state != AppState.ACTIVE) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = app.state.color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = app.state.label,
                        color = app.state.color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppActionBottomSheet(
    app: DebloatApp,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var executing by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            val removal = app.uadEntry?.removal ?: if (app.isSystem) UadRemoval.SYSTEM else UadRemoval.UNKNOWN
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = removal.color.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Safety Level: ${removal.displayName} (${app.uadEntry?.list ?: "General"})",
                        fontWeight = FontWeight.Bold,
                        color = removal.color
                    )
                    Text(
                        text = removal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!app.uadEntry?.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Description & Advice:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = app.uadEntry!!.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!app.uadEntry?.dependencies.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dependencies: ${app.uadEntry!!.dependencies.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (app.state == AppState.UNINSTALLED) {
                    Button(
                        onClick = {
                            scope.launch {
                                executing = true
                                val res = DebloaterManager.restoreUser0(app.packageName)
                                executing = false
                                res.onSuccess {
                                    Toast.makeText(context, "Restored for User 0", Toast.LENGTH_SHORT).show()
                                    onRefresh()
                                }.onFailure {
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !executing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore User 0")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                executing = true
                                val res = if (app.state == AppState.DISABLED) {
                                    DebloaterManager.enablePackage(app.packageName)
                                } else {
                                    DebloaterManager.disablePackage(app.packageName)
                                }
                                executing = false
                                res.onSuccess {
                                    Toast.makeText(context, "State updated", Toast.LENGTH_SHORT).show()
                                    onRefresh()
                                }.onFailure {
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !executing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (app.state == AppState.DISABLED) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (app.state == AppState.DISABLED) "Enable" else "Disable")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                executing = true
                                val res = DebloaterManager.uninstallUser0(app.packageName)
                                executing = false
                                res.onSuccess {
                                    Toast.makeText(context, "Uninstalled for User 0 (Data Kept)", Toast.LENGTH_SHORT).show()
                                    onRefresh()
                                }.onFailure {
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !executing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Debloat")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        executing = true
                        val res = DebloaterManager.clearData(app.packageName)
                        executing = false
                        res.onSuccess {
                            Toast.makeText(context, "Cleared data", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !executing && app.state != AppState.UNINSTALLED,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.CleaningServices, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear App Data & Cache")
            }
        }
    }
}
