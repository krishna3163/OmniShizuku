package moe.shizuku.manager.audio

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.audio.data.AudioState
import moe.shizuku.manager.audio.lib.ShizukuRunner
import moe.shizuku.manager.ui.component.LabToolScaffold
import moe.shizuku.manager.ui.theme.OmniTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixedAudioScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var appList by remember { mutableStateOf<List<Pair<String, AudioState>>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<Pair<String, AudioState>?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    suspend fun loadAppsAsync() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("media", ignoreCase = true) || it.packageName.contains("music", ignoreCase = true) || it.packageName.contains("audio", ignoreCase = true) || it.packageName.contains("youtube", ignoreCase = true) }

        val muteMap = HashMap<String, Boolean>()
        val focusMap = HashMap<String, AudioState>()

        installed.forEach { appInfo ->
            val label = pm.getApplicationLabel(appInfo).toString()
            focusMap[appInfo.packageName] = AudioState(label, false, AudioState.Focus.ALLOWED)
        }

        // Now query actual states from Shizuku
        ShizukuRunner.command("appops query-op PLAY_AUDIO deny", object : ShizukuRunner.CommandResultListener {
            override fun onCommandResult(output: String, done: Boolean) {
                if (done) {
                    output.split("\n").forEach { pkg ->
                        val clean = pkg.trim()
                        if (clean.isNotBlank()) muteMap[clean] = true
                    }
                }
            }
        })

        ShizukuRunner.command("appops query-op TAKE_AUDIO_FOCUS ignore", object : ShizukuRunner.CommandResultListener {
            override fun onCommandResult(output: String, done: Boolean) {
                if (done) {
                    output.split("\n").forEach { pkg ->
                        val clean = pkg.trim()
                        if (clean.isNotBlank()) {
                            val current = focusMap[clean]
                            val label = current?.name ?: getAppName(context, clean)
                            focusMap[clean] = AudioState(label, muteMap[clean] ?: false, AudioState.Focus.IGNORED)
                        }
                    }
                }
            }
        })

        ShizukuRunner.command("appops query-op TAKE_AUDIO_FOCUS deny", object : ShizukuRunner.CommandResultListener {
            override fun onCommandResult(output: String, done: Boolean) {
                if (done) {
                    output.split("\n").forEach { pkg ->
                        val clean = pkg.trim()
                        if (clean.isNotBlank()) {
                            val current = focusMap[clean]
                            val label = current?.name ?: getAppName(context, clean)
                            focusMap[clean] = AudioState(label, muteMap[clean] ?: false, AudioState.Focus.DENIED)
                        }
                    }
                }
            }
        })

        withContext(Dispatchers.Main) {
            appList = focusMap.toList().sortedBy { it.second.name.lowercase() }
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        loadAppsAsync()
    }

    val filteredApps = remember(appList, searchQuery) {
        if (searchQuery.isBlank()) {
            appList
        } else {
            appList.filter {
                it.first.contains(searchQuery, ignoreCase = true) ||
                it.second.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LabToolScaffold(
        title = "Mixed Audio",
        description = "Configure per-app audio focus and concurrent media playback via AppOps.",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showRestoreDialog = true }) {
                Icon(Icons.Outlined.Restore, contentDescription = "Restore Defaults")
            }
            IconButton(onClick = { refreshTrigger++ }) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps (${appList.size} available)...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = OmniTokens.shapeMedium
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OmniTokens.brandPrimary())
                }
            } else if (filteredApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No apps found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
                ) {
                    items(filteredApps, key = { it.first }) { (packageName, state) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedApp = Pair(packageName, state) },
                            shape = OmniTokens.shapeMedium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val (icon, iconTint) = when {
                                    state.muted -> Pair(Icons.AutoMirrored.Outlined.VolumeOff, MaterialTheme.colorScheme.error)
                                    state.focus == AudioState.Focus.ALLOWED -> Pair(Icons.AutoMirrored.Outlined.VolumeUp, OmniTokens.brandPrimary())
                                    state.focus == AudioState.Focus.IGNORED -> Pair(Icons.Outlined.GraphicEq, Color(0xFF34C759))
                                    else -> Pair(Icons.Outlined.GraphicEq, MaterialTheme.colorScheme.error)
                                }
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        state.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val statusText = (if (state.muted) "Muted • " else "") + when (state.focus) {
                                        AudioState.Focus.ALLOWED -> "Standard (Interrupts media)"
                                        AudioState.Focus.IGNORED -> "Mixed Audio Active (Concurrent)"
                                        AudioState.Focus.DENIED -> "Forced Concurrent Audio"
                                    }
                                    Text(
                                        statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = iconTint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedApp?.let { (pkg, state) ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text(state.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select operation for $pkg:", style = MaterialTheme.typography.bodyMedium)
                    
                    OutlinedButton(
                        onClick = {
                            val cmd = if (state.muted) "appops set $pkg PLAY_AUDIO allow" else "appops set $pkg PLAY_AUDIO deny"
                            ShizukuRunner.command(cmd, object : ShizukuRunner.CommandResultListener {
                                override fun onCommandResult(output: String, done: Boolean) {
                                    if (done) refreshTrigger++
                                }
                            })
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.muted) "Unmute Audio" else "Mute Audio")
                    }

                    OutlinedButton(
                        onClick = {
                            ShizukuRunner.command("appops set $pkg TAKE_AUDIO_FOCUS ignore", object : ShizukuRunner.CommandResultListener {
                                override fun onCommandResult(output: String, done: Boolean) {
                                    if (done) refreshTrigger++
                                }
                            })
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable Mixed Audio (Concurrent)")
                    }

                    OutlinedButton(
                        onClick = {
                            ShizukuRunner.command("appops set $pkg TAKE_AUDIO_FOCUS allow", object : ShizukuRunner.CommandResultListener {
                                override fun onCommandResult(output: String, done: Boolean) {
                                    if (done) refreshTrigger++
                                }
                            })
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disable Mixed Audio (Default)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedApp = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Settings") },
            text = { Text("Reset audio focus and un-mute configurations for all applications?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        appList.filter { it.second.muted }.forEach { (pkg, _) ->
                            ShizukuRunner.command("appops set $pkg PLAY_AUDIO allow", object : ShizukuRunner.CommandResultListener {})
                        }
                        appList.filter { it.second.focus != AudioState.Focus.ALLOWED }.forEach { (pkg, _) ->
                            ShizukuRunner.command("appops set $pkg TAKE_AUDIO_FOCUS allow", object : ShizukuRunner.CommandResultListener {})
                        }
                        refreshTrigger++
                    }
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getAppName(context: Context, packageName: String): String {
    return try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        packageName
    }
}
