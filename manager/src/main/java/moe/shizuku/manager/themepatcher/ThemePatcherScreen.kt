package moe.shizuku.manager.themepatcher

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val PREFS = "theme_patcher"

/** Simple theme patcher that records user-selected overrides and exposes them via preferences. */
object ThemePatcherManager {
    const val KEY_ACCENT = "theme_accent"
    const val KEY_SURFACE = "theme_surface"
    const val KEY_ERROR = "theme_error"

    fun getPref(context: Context, key: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)

    fun setPref(context: Context, key: String, value: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(key, value).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePatcherScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var accent by remember { mutableStateOf(ThemePatcherManager.getPref(context, ThemePatcherManager.KEY_ACCENT) ?: "#000000") }
    var surface by remember { mutableStateOf(ThemePatcherManager.getPref(context, ThemePatcherManager.KEY_SURFACE) ?: "#000000") }
    var error by remember { mutableStateOf(ThemePatcherManager.getPref(context, ThemePatcherManager.KEY_ERROR) ?: "#000000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Patcher") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Theme Overrides", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Override system theme colors. These values are stored locally and can be read by Shizuku-based apps.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Accent Color", style = MaterialTheme.typography.bodyMedium)
                            Text(accent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = accent,
                            onValueChange = { accent = it },
                            placeholder = { Text("#RRGGBB") },
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Surface Color", style = MaterialTheme.typography.bodyMedium)
                            Text(surface, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = surface,
                            onValueChange = { surface = it },
                            placeholder = { Text("#RRGGBB") },
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Error Color", style = MaterialTheme.typography.bodyMedium)
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = error,
                            onValueChange = { error = it },
                            placeholder = { Text("#RRGGBB") },
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    ThemePatcherManager.setPref(context, ThemePatcherManager.KEY_ACCENT, accent)
                    ThemePatcherManager.setPref(context, ThemePatcherManager.KEY_SURFACE, surface)
                    ThemePatcherManager.setPref(context, ThemePatcherManager.KEY_ERROR, error)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Apply Theme Overrides") }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
