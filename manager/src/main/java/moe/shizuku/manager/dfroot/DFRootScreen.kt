package moe.shizuku.manager.dfroot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DFRootScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var suManagers by remember { mutableStateOf<List<SuManagerEntry>>(emptyList()) }
    var selectedSuManager by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("") }
    val isAlreadyHooked = remember { File("/dev/df").exists() }
    
    val prefs = remember { context.createDeviceProtectedStorageContext().getSharedPreferences("dfroot", Context.MODE_PRIVATE) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val entries = mutableListOf<SuManagerEntry>()
            for (ai in pm.getInstalledApplications(0)) {
                if ((ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue
                if (!hasKsud(ai)) continue
                entries.add(SuManagerEntry(ai.packageName, pm.getApplicationLabel(ai).toString(), pm.getApplicationIcon(ai)))
            }
            entries.sortBy { it.label.lowercase() }
            withContext(Dispatchers.Main) {
                suManagers = entries
                val saved = prefs.getString("su_manager", null)
                selectedSuManager = saved
                if (saved != null && entries.none { it.packageName == saved }) {
                    prefs.edit().remove("su_manager").apply()
                    selectedSuManager = null
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DFRoot (CVE-2026-43284)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "DFRoot exploits CVE-2026-43284 (DirtyFrag) — a kernel page cache write primitive — to load a custom kernel module without requiring an unlocked bootloader. It is intentionally designed to be SU Manager agnostic and works with any KernelSU-compatible SU manager.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "This is GPL-2.0 licensed software. Source code and attribution preserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SU Manager Selection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = suManagers.find { it.packageName == selectedSuManager }?.label ?: "Select a SU Manager",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            suManagers.forEach { manager ->
                                DropdownMenuItem(
                                    text = { Text(manager.label) },
                                    onClick = {
                                        selectedSuManager = manager.packageName
                                        prefs.edit().putString("su_manager", manager.packageName).apply()
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isAlreadyHooked) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = if (isAlreadyHooked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAlreadyHooked) "Already hooked (/dev/df exists)" else "Not hooked")
                    }
                    Button(
                        onClick = {
                            if (selectedSuManager == null || isRunning || isAlreadyHooked) return@Button
                            isRunning = true
                            output = ""
                            scope.launch(Dispatchers.IO) {
                                var resultCode = -1
                                try {
                                    resultCode = DFRootExploit.run(context, object : DFRootExploit.Reporter {
                                        override fun report(msg: String) {
                                            scope.launch(Dispatchers.Main) {
                                                output += msg
                                            }
                                        }
                                    }, selectedSuManager!!)
                                } catch (e: Exception) {
                                    scope.launch(Dispatchers.Main) {
                                        output += "\nException: $e\n"
                                    }
                                    resultCode = -1
                                }
                                withContext(Dispatchers.Main) {
                                    isRunning = false
                                    val msg = when (resultCode) {
                                        0 -> "SUCCESS"
                                        1 -> "Error - ksud nonzero exit"
                                        2 -> "Error - check logcat & dmesg"
                                        else -> "Error - failed to patch files"
                                    }
                                    Toast.makeText(context, "DFRoot: $msg", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = selectedSuManager != null && !isRunning && !isAlreadyHooked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (isRunning) "Running..." else "Run Exploit")
                    }
                }
            }
            
            if (output.isNotEmpty()) {
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            output,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "• Requires a vulnerable kernel (DirtyFrag CVE-2026-43284)\n• Works with KernelSU, KernelSU-Next, and compatible SU managers\n• libksud.so must be present in selected manager\n• Developer Options 'Log buffer size' must not be set to Off\n• This feature is optional and does not affect other OmniShizuku functionality",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class SuManagerEntry(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable?
)

private fun hasKsud(app: ApplicationInfo): Boolean {
    return app.nativeLibraryDir != null && File(app.nativeLibraryDir, "libksud.so").exists()
}
