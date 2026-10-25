package moe.shizuku.manager.tuner

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class TunerTab(val label: String) {
    STATUS_BAR("Status Bar"),
    IMMERSIVE("Immersive"),
    EXPLORER("Settings Explorer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemUiTunerScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(TunerTab.STATUS_BAR) }
    var blacklistedIcons by remember { mutableStateOf<Set<String>>(emptySet()) }
    var immersiveMode by remember { mutableStateOf(SystemSettingsManager.ImmersiveMode.NORMAL) }

    // Settings explorer state
    var selectedTable by remember { mutableStateOf(SystemSettingsManager.Table.GLOBAL) }
    var settingsList by remember { mutableStateOf<List<SystemSettingsManager.SettingItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<SystemSettingsManager.SettingItem?>(null) }
    var editValue by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    fun refreshState() {
        scope.launch {
            loading = true
            blacklistedIcons = SystemSettingsManager.getBlacklistedIcons()
            immersiveMode = SystemSettingsManager.getImmersiveMode()
            if (selectedTab == TunerTab.EXPLORER) {
                settingsList = SystemSettingsManager.listSettings(selectedTable)
            }
            loading = false
        }
    }

    LaunchedEffect(selectedTab, selectedTable) {
        refreshState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SystemUI Tuner") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshState() }) {
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
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                TunerTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }

            when (selectedTab) {
                TunerTab.STATUS_BAR -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Toggle status bar icon visibility using Android's native icon blacklist.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(SystemSettingsManager.STATUS_BAR_ICONS, key = { it.slot }) { icon ->
                            val isVisible = !blacklistedIcons.contains(icon.slot)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(icon.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Slot: ${icon.slot}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = isVisible,
                                        onCheckedChange = { visible ->
                                            scope.launch {
                                                SystemSettingsManager.setIconVisibility(icon.slot, visible)
                                                blacklistedIcons = SystemSettingsManager.getBlacklistedIcons()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                TunerTab.IMMERSIVE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Force immersive full screen across all applications or selectively hide system bars.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        SystemSettingsManager.ImmersiveMode.entries.forEach { mode ->
                            val isSelected = immersiveMode == mode
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            SystemSettingsManager.setImmersiveMode(mode)
                                            immersiveMode = mode
                                            Toast.makeText(context, "Immersive mode applied", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (mode.value != null) {
                                            Text(
                                                text = "Policy: ${mode.value}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TunerTab.EXPLORER -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SystemSettingsManager.Table.entries.forEach { table ->
                                FilterChip(
                                    selected = selectedTable == table,
                                    onClick = { selectedTable = table },
                                    label = { Text(table.arg.uppercase()) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            placeholder = { Text("Search ${selectedTable.arg} keys & values...") },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        val filtered = remember(settingsList, searchQuery) {
                            if (searchQuery.isBlank()) settingsList
                            else settingsList.filter {
                                it.key.contains(searchQuery, ignoreCase = true) ||
                                        it.value.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        if (loading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filtered, key = { "${it.table}_${it.key}" }) { item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                editingItem = item
                                                editValue = item.value
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = item.key,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.value,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit ${item.key}") },
            text = {
                Column {
                    Text("Table: ${item.table.arg}", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Value") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = SystemSettingsManager.putSetting(item.table, item.key, editValue)
                            if (success) {
                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                editingItem = null
                                refreshState()
                            } else {
                                Toast.makeText(context, "Failed to save", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
