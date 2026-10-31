package moe.shizuku.manager.pip

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

private const val PREFS = "universal_pip"

/** Universal Picture-in-Picture preferences. */
object UniversalPiPManager {
    private const val KEY_ENABLED = "pip_enabled"
    private const val KEY_ASPECT = "pip_aspect"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getAspectRatio(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_ASPECT, 16f / 9f)

    fun setAspectRatio(context: Context, ratio: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_ASPECT, ratio).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalPiPScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(UniversalPiPManager.isEnabled(context)) }
    var aspect by remember { mutableStateOf(UniversalPiPManager.getAspectRatio(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Universal PiP") },
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
            Text("Universal PiP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Force picture-in-picture mode for apps that do not natively support it.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        UniversalPiPManager.setEnabled(context, it)
                    }
                )
                Column {
                    Text("Enable Universal PiP", style = MaterialTheme.typography.bodyMedium)
                    Text("Force PiP for all apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Aspect Ratio", style = MaterialTheme.typography.bodyMedium)
                    Text("%.2f".format(aspect), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Slider(
                    value = aspect,
                    onValueChange = { aspect = it },
                    valueRange = 1f..3f,
                    steps = 20,
                    enabled = enabled
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    UniversalPiPManager.setAspectRatio(context, aspect)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) { Text("Apply PiP Settings") }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
