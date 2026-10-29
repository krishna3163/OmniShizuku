package moe.shizuku.manager.audio

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
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

private const val PREFS = "mixed_audio"

/** Audio mix / routing preferences. */
object MixedAudioManager {
    private const val KEY_MIX = "mix_enabled"
    private const val KEY_ROUTE = "mix_route"

    fun isMixEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_MIX, false)

    fun setMixEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_MIX, enabled).apply()
    }

    fun getMixRoute(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ROUTE, null)

    fun setMixRoute(context: Context, route: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ROUTE, route).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixedAudioScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var mixEnabled by remember { mutableStateOf(MixedAudioManager.isMixEnabled(context)) }
    var mixRoute by remember { mutableStateOf(MixedAudioManager.getMixRoute(context) ?: "default") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mixed Audio") },
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
            Text("Audio Mix", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Mix and route audio streams across output devices. Requires Shizuku to modify system audio policy.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = mixEnabled,
                    onCheckedChange = {
                        mixEnabled = it
                        MixedAudioManager.setMixEnabled(context, it)
                    }
                )
                Column {
                    Text("Enable Audio Mix", style = MaterialTheme.typography.bodyMedium)
                    Text("Combine multiple audio streams into one output", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Output Route", style = MaterialTheme.typography.bodyMedium)
                    Text(mixRoute, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = mixRoute,
                    onValueChange = { mixRoute = it },
                    placeholder = { Text("e.g. speaker, bluetooth, wired") },
                    singleLine = true,
                    modifier = Modifier.width(160.dp),
                    enabled = mixEnabled
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    MixedAudioManager.setMixRoute(context, mixRoute)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mixEnabled
            ) { Text("Apply Mix Route") }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
