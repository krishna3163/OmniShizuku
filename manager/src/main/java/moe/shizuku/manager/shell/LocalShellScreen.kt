package moe.shizuku.manager.shell

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ui.component.SegmentedListItem
import moe.shizuku.manager.utils.ShizukuStateMachine

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

private data class QuickPreset(
    val title: String,
    val command: String,
    val description: String
)

private val QUICK_PRESETS = listOf(
    QuickPreset("List Installed Apps", "pm list packages -3", "Show all 3rd-party user installed packages"),
    QuickPreset("Battery Status", "dumpsys battery", "Battery level, health, temperature, and charging status"),
    QuickPreset("Memory Info", "dumpsys meminfo", "System RAM usage, memory buckets, and allocations"),
    QuickPreset("System Properties", "getprop ro.build.version.release && getprop ro.product.model", "Android version and device hardware model"),
    QuickPreset("Storage Space", "df -h", "Disk capacity, free space, and mount points"),
    QuickPreset("Process List", "ps -A", "Running processes, PIDs, and user ownership")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalShellScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val terminalListState = rememberLazyListState()

    var commandText by remember {
        mutableStateOf(LocalShellManager.getDefaultCommand(context) ?: "pm list packages -3")
    }
    var selectedBackend by remember { mutableStateOf(ShellBackend.LOCAL) }
    var isRunning by remember { mutableStateOf(false) }
    var outputLines by remember { mutableStateOf<List<ShellLine>>(emptyList()) }
    var exitCode by remember { mutableStateOf<Int?>(null) }
    var executionDurationMs by remember { mutableStateOf<Long?>(null) }

    val session = remember { ShellSession() }
    val shizukuRunning = remember { ShizukuStateMachine.isRunning() }

    fun executeCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty() || isRunning) return

        isRunning = true
        exitCode = null
        executionDurationMs = null
        outputLines = listOf(ShellLine("$ $trimmed", ShellLine.Kind.COMMAND))

        scope.launch {
            val startTime = System.currentTimeMillis()
            val accumulator = mutableListOf(ShellLine("$ $trimmed", ShellLine.Kind.COMMAND))

            val code = withContext(Dispatchers.IO) {
                session.run(selectedBackend, trimmed) { line ->
                    synchronized(accumulator) {
                        accumulator.add(line)
                        if (accumulator.size > 800) {
                            accumulator.removeAt(1) // Keep prompt, drop oldest
                        }
                    }
                }
            }

            val elapsed = System.currentTimeMillis() - startTime
            outputLines = accumulator.toList()
            exitCode = code
            executionDurationMs = elapsed
            isRunning = false

            if (accumulator.isNotEmpty()) {
                terminalListState.scrollToItem(accumulator.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Shell") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (outputLines.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val text = outputLines.joinToString("\n") { it.text }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("OmniShizuku Shell Output", text))
                                Toast.makeText(context, "Output copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Output")
                        }
                        IconButton(
                            onClick = {
                                outputLines = emptyList()
                                exitCode = null
                                executionDurationMs = null
                            }
                        ) {
                            Icon(Icons.Outlined.ClearAll, contentDescription = "Clear Output")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Command Console",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Run shell commands directly in the local process sandbox or elevate through Shizuku ADB / Root.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Backend Selector
            item {
                Text(
                    text = "Execution Backend",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedBackend == ShellBackend.LOCAL,
                        onClick = { selectedBackend = ShellBackend.LOCAL },
                        label = { Text("Local Process") },
                        leadingIcon = if (selectedBackend == ShellBackend.LOCAL) {
                            { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = selectedBackend == ShellBackend.SHIZUKU,
                        onClick = { selectedBackend = ShellBackend.SHIZUKU },
                        label = { Text("Shizuku ADB") },
                        leadingIcon = if (selectedBackend == ShellBackend.SHIZUKU) {
                            { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = selectedBackend == ShellBackend.ROOT,
                        onClick = { selectedBackend = ShellBackend.ROOT },
                        label = { Text("Root (su)") },
                        leadingIcon = if (selectedBackend == ShellBackend.ROOT) {
                            { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Command Input Field
            item {
                OutlinedTextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text("Shell Command") },
                    placeholder = { Text("e.g. pm list packages, getprop, dumpsys") },
                    singleLine = false,
                    maxLines = 3,
                    trailingIcon = {
                        if (commandText.isNotEmpty()) {
                            IconButton(onClick = { commandText = "" }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear text")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Execution Controls & Save Default
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { executeCommand(commandText) },
                        enabled = !isRunning && commandText.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Running...")
                        } else {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run")
                        }
                    }

                    if (isRunning) {
                        OutlinedButton(
                            onClick = { session.stop() }
                        ) {
                            Icon(Icons.Outlined.Stop, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            LocalShellManager.setDefaultCommand(context, commandText)
                            Toast.makeText(context, "Saved as default command", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }

            // Terminal Output Window
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Terminal Output",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            executionDurationMs?.let { ms ->
                                Text(
                                    text = "${ms}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            exitCode?.let { code ->
                                val chipColor = if (code == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                Text(
                                    text = "exit: $code",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = chipColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (outputLines.isEmpty()) {
                        Text(
                            text = "> Ready. Tap 'Run' or choose a quick preset below.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            state = terminalListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 320.dp)
                        ) {
                            items(outputLines) { line ->
                                val color = when (line.kind) {
                                    ShellLine.Kind.COMMAND -> MaterialTheme.colorScheme.primary
                                    ShellLine.Kind.ERROR -> MaterialTheme.colorScheme.error
                                    ShellLine.Kind.INFO -> MaterialTheme.colorScheme.tertiary
                                    ShellLine.Kind.EXIT -> MaterialTheme.colorScheme.outline
                                    ShellLine.Kind.OUTPUT -> MaterialTheme.colorScheme.onSurface
                                }
                                Text(
                                    text = line.text,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    fontFamily = FontFamily.Monospace,
                                    color = color,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Quick Presets
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any preset to load and execute it immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(QUICK_PRESETS) { preset ->
                SegmentedListItem(
                    headlineContent = { Text(preset.title) },
                    supportingContent = { Text("${preset.command} — ${preset.description}") },
                    trailingContent = {
                        Icon(
                            Icons.Outlined.PlayCircleOutline,
                            contentDescription = "Execute ${preset.title}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        commandText = preset.command
                        executeCommand(preset.command)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(bottomPadding + 16.dp))
            }
        }
    }
}
