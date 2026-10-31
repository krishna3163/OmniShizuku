package moe.shizuku.manager.dfroot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
    
    val isSamsung = remember {
        val m = Build.MANUFACTURER.lowercase(Locale.ROOT)
        val b = Build.BRAND.lowercase(Locale.ROOT)
        m.contains("samsung") || b.contains("samsung")
    }
    
    var suManagers by remember { mutableStateOf<List<SuManagerEntry>>(emptyList()) }
    var selectedSuManager by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("") }
    val isAlreadyHooked = remember { File("/dev/df").exists() }
    
    val prefs = remember { context.createDeviceProtectedStorageContext().getSharedPreferences("dfroot", Context.MODE_PRIVATE) }
    
    val openUrl = { url: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: $e", Toast.LENGTH_SHORT).show()
        }
    }

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
            
            // Device-tailored SU Manager Download card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Install a SU Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        if (isSamsung) {
                            "Root access lene ke liye Samsung device par niche diya gaya KernelSU manager install karein:"
                        } else {
                            "Root access lene ke liye in teeno mein se kisi ek SU Manager ko install karna hoga:"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSamsung) {
                                "Detected Device: Samsung (${Build.MODEL})"
                            } else {
                                "Detected Device: ${Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} (${Build.MODEL})"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (isSamsung) {
                        // Samsung-only SU Manager
                        SuManagerDownloadItem(
                            name = "diabl0w's KernelSU",
                            description = "KernelSU build tailored specifically for Samsung kernels & Knox",
                            url = "https://github.com/diabl0w/KernelSU/releases/latest",
                            onDownload = { openUrl("https://github.com/diabl0w/KernelSU/releases/latest") }
                        )
                    } else {
                        // Other devices: 3 options
                        SuManagerDownloadItem(
                            name = "KernelSU",
                            description = "Official KernelSU manager app by tiann",
                            url = "https://github.com/tiann/KernelSU/releases/latest",
                            onDownload = { openUrl("https://github.com/tiann/KernelSU/releases/latest") }
                        )
                        SuManagerDownloadItem(
                            name = "KernelSU-Next",
                            description = "Next-generation KernelSU manager with extended features",
                            url = "https://github.com/KernelSU-Next/KernelSU-Next/releases/latest",
                            onDownload = { openUrl("https://github.com/KernelSU-Next/KernelSU-Next/releases/latest") }
                        )
                        SuManagerDownloadItem(
                            name = "KowSU",
                            description = "KernelSU fork by KOWX712 with custom patchset",
                            url = "https://github.com/KOWX712/KernelSU/releases/latest",
                            onDownload = { openUrl("https://github.com/KOWX712/KernelSU/releases/latest") }
                        )
                    }
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

@Composable
private fun SuManagerDownloadItem(
    name: String,
    description: String,
    url: String,
    onDownload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            FilledTonalButton(
                onClick = onDownload,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Get", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
