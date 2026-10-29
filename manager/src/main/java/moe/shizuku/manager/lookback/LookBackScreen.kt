package moe.shizuku.manager.lookback

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

private const val PREFS = "lookback"

/** LookBack screen recording preferences. */
object LookBackManager {
    private const val KEY_ENABLED = "lookback_enabled"
    private const val KEY_BITRATE = "lookback_bitrate"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getBitrate(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BITRATE, 4_000_000)

    fun setBitrate(context: Context, bitrate: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_BITRATE, bitrate).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookBackScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(LookBackManager.isEnabled(context)) }
    var bitrate by remember { mutableStateOf(LookBackManager.getBitrate(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LookBack") },
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
            Text("LookBack", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Record screen activity and play it back locally. Recordings are stored on device only.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        LookBackManager.setEnabled(context, it)
                    }
                )
                Column {
                    Text("Enable Screen Recording", style = MaterialTheme.typography.bodyMedium)
                    Text("Record screen to local storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bitrate (bps)", style = MaterialTheme.typography.bodyMedium)
                    Text("${bitrate / 1_000_000} Mbps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Slider(
                    value = bitrate.toFloat(),
                    onValueChange = { bitrate = it.toInt() },
                    valueRange = 1_000_000f..20_000_000f,
                    steps = 19,
                    enabled = enabled
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    LookBackManager.setBitrate(context, bitrate)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) { Text("Apply Recording Settings") }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
