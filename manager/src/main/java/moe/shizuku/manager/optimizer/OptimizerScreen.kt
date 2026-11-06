package moe.shizuku.manager.optimizer

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import moe.shizuku.manager.ui.theme.OmniTokens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizerScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeProfile by remember { mutableStateOf(OptimizerManager.getActiveProfile(context)) }
    var confirmingProfile by remember { mutableStateOf<PerformanceProfile?>(null) }
    var confirmingAction by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Axeron Gaming & CPU Optimizer")
                        Text(
                            "Apply supported profiles. Results vary by device; no FPS guarantee.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "SYSTEM PERFORMANCE PROFILES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OmniTokens.brandPrimary()
            )

            PerformanceProfile.entries.forEach { profile ->
                val isSelected = activeProfile == profile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (activeProfile != profile) confirmingProfile = profile },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
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
                            onClick = { if (activeProfile != profile) confirmingProfile = profile }
                        )

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                profile.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                profile.desc,
                                fontSize = 13.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "MEMORY & KERNEL RECLAMATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OmniTokens.brandPrimary()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1-Tap Memory Reclaimer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Forces system cache trimming across all background apps and purges inactive filesystem page caches to immediately free RAM before launching intensive 3D games.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { confirmingAction = "trim" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Trim Caches")
                        }

                        OutlinedButton(
                            onClick = { confirmingAction = "purge" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Purge Buffers")
                        }
                    }
                }
            }
        }
    }

    confirmingProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { confirmingProfile = null },
            title = { Text("Apply profile?") },
            text = { Text("Apply ${profile.title}? Results vary by device and backend; this does not guarantee performance gains.") },
            confirmButton = { TextButton(onClick = { scope.launch { activeProfile = profile; OptimizerManager.applyProfile(context, profile); Toast.makeText(context, "${profile.title} activated", Toast.LENGTH_SHORT).show(); confirmingProfile = null } }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { confirmingProfile = null }) { Text("Cancel") } }
        )
    }
    confirmingAction?.let { action ->
        val (title, msg, runAction) = if (action == "trim") Triple("Trim system caches?", "This requests system cache trimming. Effects depend on device and backend.", suspend { OptimizerManager.clearSystemCaches(); Toast.makeText(context, "System caches trimmed", Toast.LENGTH_SHORT).show() }) else Triple("Purge page buffers?", "This requests kernel buffer purging where supported. Effects depend on device and backend.", suspend { OptimizerManager.dropLinuxBuffers(); Toast.makeText(context, "Page buffers purged", Toast.LENGTH_SHORT).show() })
        AlertDialog(
            onDismissRequest = { confirmingAction = null },
            title = { Text(title) },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { scope.launch { runAction(); confirmingAction = null } }) { Text("Continue") } },
            dismissButton = { TextButton(onClick = { confirmingAction = null }) { Text("Cancel") } }
        )
    }
}
