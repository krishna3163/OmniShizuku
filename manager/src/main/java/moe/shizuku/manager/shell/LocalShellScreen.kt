package moe.shizuku.manager.shell

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

private const val PREFS = "local_shell"

/** Local shell preferences that do not require Shizuku. */
object LocalShellManager {
    private const val KEY_DEFAULT_CMD = "shell_default_cmd"

    fun getDefaultCommand(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DEFAULT_CMD, null)

    fun setDefaultCommand(context: Context, cmd: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_DEFAULT_CMD, cmd).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalShellScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var defaultCmd by remember { mutableStateOf(LocalShellManager.getDefaultCommand(context) ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Shell") },
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
            Text("Local Shell", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Run local shell commands without Shizuku. Requires the app's own process context.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Default Command", style = MaterialTheme.typography.bodyMedium)
                    Text(defaultCmd.ifEmpty { "(none set)" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = defaultCmd,
                    onValueChange = { defaultCmd = it },
                    placeholder = { Text("e.g. ls, dumpsys, pm list packages") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    LocalShellManager.setDefaultCommand(context, defaultCmd)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Default Command") }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Quick Commands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SegmentedListItem(
                        headlineContent = { Text("List Packages") },
                        supportingContent = { Text("pm list packages") },
                        onClick = {}
                    )
                }
                item {
                    SegmentedListItem(
                        headlineContent = { Text("Show Battery") },
                        supportingContent = { Text("dumpsys battery") },
                        onClick = {}
                    )
                }
                item {
                    SegmentedListItem(
                        headlineContent = { Text("Show Memory") },
                        supportingContent = { Text("dumpsys meminfo") },
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
