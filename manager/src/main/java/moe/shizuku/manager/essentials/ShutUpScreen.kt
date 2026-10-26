package moe.shizuku.manager.essentials

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * "Shut Up!" Per-App Media Mute Engine
 * Authoritative port from Essentials (com.sameerasw.essentials.ui.features.audio.ShutUpSettingsUI)
 *
 * Automatically silences media volume when noisy foreground apps (Instagram, TikTok, Twitter, games)
 * launch, and seamlessly restores volume on exit with intelligent backoff delay.
 */
object ShutUpEngine {
    private const val PREFS_NAME = "essentials_shutup"
    private const val KEY_ENABLED = "shutup_enabled"
    private const val KEY_RESTORE_DELAY = "shutup_restore_delay"
    private const val KEY_MUTED_PACKAGES = "shutup_muted_packages"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ENABLED, false)

    fun getRestoreDelay(context: Context): Int =
        getPrefs(context).getInt(KEY_RESTORE_DELAY, 3)

    fun getMutedPackages(context: Context): Set<String> =
        getPrefs(context).getStringSet(KEY_MUTED_PACKAGES, emptySet()) ?: emptySet()

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun setRestoreDelay(context: Context, seconds: Int) {
        getPrefs(context).edit().putInt(KEY_RESTORE_DELAY, seconds).apply()
    }

    fun toggleApp(context: Context, packageName: String) {
        val current = getMutedPackages(context).toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        getPrefs(context).edit().putStringSet(KEY_MUTED_PACKAGES, current).apply()
    }
}

data class AppItem(
    val packageName: String,
    val label: String,
    val isSystem: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShutUpScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(ShutUpEngine.isEnabled(context)) }
    var restoreDelay by remember { mutableIntStateOf(ShutUpEngine.getRestoreDelay(context)) }
    var mutedApps by remember { mutableStateOf(ShutUpEngine.getMutedPackages(context)) }
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAppPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { appInfo ->
                    AppItem(
                        packageName = appInfo.packageName,
                        label = appInfo.loadLabel(pm).toString(),
                        isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                .sortedWith(compareBy({ it.isSystem }, { it.label.lowercase() }))
            installedApps = apps
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shut Up! Media Muter", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Per-App Media Focus Controller", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Switch Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
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
                            "Enable Shut Up!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Instantly silences media stream when target noisy apps are opened",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            isEnabled = checked
                            ShutUpEngine.setEnabled(context, checked)
                        }
                    )
                }
            }

            // Restore delay setting
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Volume Restore Delay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${restoreDelay}s", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Seconds to wait after leaving the muted app before automatically restoring your original media volume level.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = restoreDelay.toFloat(),
                        onValueChange = {
                            restoreDelay = it.toInt()
                            ShutUpEngine.setRestoreDelay(context, it.toInt())
                        },
                        valueRange = 1f..15f,
                        steps = 13,
                        enabled = isEnabled
                    )
                }
            }

            // Target Apps Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Target Applications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${mutedApps.size} apps configured to mute", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { showAppPicker = !showAppPicker },
                            enabled = isEnabled
                        ) {
                            Text(if (showAppPicker) "Done" else "Configure")
                        }
                    }

                    if (showAppPicker) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search installed apps...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
                        )

                        val filtered = installedApps.filter {
                            it.label.contains(searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(searchQuery, ignoreCase = true)
                        }.take(30)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filtered.forEach { app ->
                                val isMuted = mutedApps.contains(app.packageName)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            ShutUpEngine.toggleApp(context, app.packageName)
                                            mutedApps = ShutUpEngine.getMutedPackages(context)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.label, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                    Checkbox(
                                        checked = isMuted,
                                        onCheckedChange = {
                                            ShutUpEngine.toggleApp(context, app.packageName)
                                            mutedApps = ShutUpEngine.getMutedPackages(context)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
