package moe.shizuku.manager.doze

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ShizukuSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DozeBlocklistDialog(
    onDismiss: () -> Unit,
    context: Context
) {
    val apps = EnforceDozeManager.getDozeAppBlocklist(context).toList()
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Doze App Blocklist") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Apps to suspend during Doze:")
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(apps, key = { it }) { pkg ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                selected = if (pkg in selected) selected - pkg else selected + pkg 
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = pkg in selected,
                                onCheckedChange = { checked ->
                                    selected = if (checked) selected + pkg else selected - pkg
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = pkg, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                EnforceDozeManager.setDozeAppBlocklist(context, selected)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DozeTunablesDialog(
    onDismiss: () -> Unit,
    context: Context
) {
    val tunables = EnforceDozeManager.getDozeTunables(context)
    val edited by remember { mutableStateOf(tunables.toMutableList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("device_config Tunables") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(edited, key = { it.key }) { tunable ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = tunable.key, style = MaterialTheme.typography.bodyMedium)
                                Text(text = "default: ${tunable.default}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = tunable.value,
                                onValueChange = {
                                    tunable.value = it
                                },
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                for (t in edited) {
                    EnforceDozeManager.setDozeTunable(context, t.key, t.value)
                }
                onDismiss()
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
