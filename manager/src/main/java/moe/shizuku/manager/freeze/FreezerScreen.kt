package moe.shizuku.manager.freeze

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreezerScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apps by remember { mutableStateOf<List<FreezableApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }
    var selectedAppForTags by remember { mutableStateOf<FreezableApp?>(null) }
    var autoFreeze by remember { mutableStateOf(FreezerManager.isAutoFreezeEnabled(context)) }

    fun refresh() {
        loading = true
        scope.launch {
            apps = FreezerManager.loadApps(context)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val availableTags = remember(apps) {
        listOf("All", "Frozen", "Active") + FreezerManager.getAllCustomTags(context)
    }

    val filteredApps = remember(apps, query, selectedTag) {
        apps.filter { app ->
            val matchQuery = query.isEmpty() ||
                    app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchTag = when (selectedTag) {
                "All" -> true
                "Frozen" -> app.isFrozen
                "Active" -> !app.isFrozen
                else -> selectedTag in app.tags
            }

            matchQuery && matchTag
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Freezer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search and Controls
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps or packages...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Auto-freeze banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auto-Freeze on Screen Off",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Freezes tagged apps when screen locks to save RAM & battery",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoFreeze,
                        onCheckedChange = {
                            autoFreeze = it
                            FreezerManager.setAutoFreezeEnabled(context, it)
                        }
                    )
                }
            }

            // Tag Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableTags) { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag) }
                    )
                }
            }

            // Batch Action Buttons for Selected Tag
            if (selectedTag !in listOf("All", "Frozen", "Active") && filteredApps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                val count = FreezerManager.freezeTag(context, selectedTag)
                                Toast.makeText(context, "Froze $count apps in $selectedTag", Toast.LENGTH_SHORT).show()
                                refresh()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Outlined.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Freeze Tag")
                    }

                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                val count = FreezerManager.unfreezeTag(context, selectedTag)
                                Toast.makeText(context, "Unfroze $count apps in $selectedTag", Toast.LENGTH_SHORT).show()
                                refresh()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Unfreeze Tag")
                    }
                }
            }

            // App List
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        FreezerAppCard(
                            app = app,
                            onToggleFreeze = {
                                scope.launch {
                                    val success = if (app.isFrozen) {
                                        FreezerManager.unfreezeApp(app.packageName)
                                    } else {
                                        FreezerManager.freezeApp(app.packageName)
                                    }
                                    if (success) {
                                        refresh()
                                    } else {
                                        Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onManageTags = {
                                selectedAppForTags = app
                            }
                        )
                    }
                }
            }
        }
    }

    // Tag Editor Dialog
    selectedAppForTags?.let { app ->
        TagEditorDialog(
            app = app,
            onDismiss = { selectedAppForTags = null },
            onSave = { updatedTags ->
                FreezerManager.setAppTags(context, app.packageName, updatedTags)
                selectedAppForTags = null
                refresh()
            }
        )
    }
}

@Composable
private fun FreezerAppCard(
    app: FreezableApp,
    onToggleFreeze: () -> Unit,
    onManageTags: () -> Unit
) {
    val context = LocalContext.current
    val iconBitmap = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName).toBitmap(96, 96).asImageBitmap()
        }.getOrNull()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isFrozen)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Android, contentDescription = null)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isFrozen) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "FROZEN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (app.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        app.tags.forEach { tag ->
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onManageTags) {
                Icon(Icons.Outlined.Label, contentDescription = "Tags")
            }

            FilledIconButton(
                onClick = onToggleFreeze,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (app.isFrozen)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (app.isFrozen)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    if (app.isFrozen) Icons.Outlined.WbSunny else Icons.Outlined.AcUnit,
                    contentDescription = if (app.isFrozen) "Unfreeze" else "Freeze"
                )
            }
        }
    }
}

@Composable
private fun TagEditorDialog(
    app: FreezableApp,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    val currentTags = remember { mutableStateListOf(*app.tags.toTypedArray()) }
    var newTagInput by remember { mutableStateOf("") }
    val allTags = remember { FreezerManager.getAllCustomTags(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tags for ${app.label}") },
        text = {
            Column {
                Text("Select tags to assign:", fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))

                allTags.forEach { tag ->
                    val checked = tag in currentTags
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checked) currentTags.remove(tag) else currentTags.add(tag)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) currentTags.add(tag) else currentTags.remove(tag)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(tag)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("New Tag") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTagInput.isNotBlank()) {
                                val t = newTagInput.trim()
                                FreezerManager.addCustomTag(context, t)
                                currentTags.add(t)
                                newTagInput = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(currentTags.toSet()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
