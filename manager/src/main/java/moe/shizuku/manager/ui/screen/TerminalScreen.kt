package moe.shizuku.manager.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.agy.AgyCliEngine
import moe.shizuku.manager.ui.component.ScrollToTopButton
import moe.shizuku.manager.ui.component.SegmentedColumn
import moe.shizuku.manager.ui.component.SegmentedListItem
import moe.shizuku.manager.utils.CustomTabsHelper
import rikka.compatibility.DeviceCompatibility
import rikka.shizuku.Shizuku

private const val SH_NAME = "rish"
private const val DEX_NAME = "rish_shizuku.dex"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { tree ->
        if (tree != null) writeRish(context, tree)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.tools_terminal)) },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Embedded AGY Shell") },
                icon = { Icon(Icons.Rounded.Terminal, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Termux Export (rish)") }
            )
        }

        if (selectedTab == 0) {
            EmbeddedAgyTerminal(modifier = Modifier.fillMaxSize())
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    item {
                        SegmentedColumn(modifier = Modifier.fillMaxWidth()) {
                            item {
                                SegmentedListItem(
                                    headlineContent = { Text(stringResource(R.string.terminal_tutorial_1)) },
                                    supportingContent = {
                                        Text(
                                            stringResource(
                                                R.string.terminal_tutorial_1_description,
                                                SH_NAME,
                                                DEX_NAME
                                            )
                                        )
                                    },
                                    onClick = { picker.launch(null) }
                                )
                            }
                            item {
                                SegmentedListItem(
                                    headlineContent = { Text(stringResource(R.string.terminal_tutorial_2)) },
                                    supportingContent = {
                                        Column {
                                            Text(
                                                stringResource(
                                                    R.string.terminal_tutorial_2_description,
                                                    "Termux",
                                                    "PKG",
                                                    "com.termux",
                                                    "com.termux"
                                                )
                                            )
                                            Text(
                                                text = "cp /sdcard/chosen-folder/* /data/data/terminal.package.name/files",
                                                fontFamily = FontFamily.Monospace,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                )
                            }
                            item {
                                SegmentedListItem(
                                    headlineContent = { Text(stringResource(R.string.terminal_tutorial_3)) },
                                    supportingContent = {
                                        Text(
                                            text = "sh /path/to/$SH_NAME",
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                            item {
                                SegmentedListItem(
                                    headlineContent = { Text(stringResource(R.string.settings_help)) },
                                    onClick = {
                                        CustomTabsHelper.launchUrlOrCopy(context, Helps.RISH.get())
                                    }
                                )
                            }
                        }
                    }

                    if (runCatching { DeviceCompatibility.isMiui() }.getOrNull() == true) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.terminal_tutorial_miui),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.terminal_tutorial_miui_2),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                ScrollToTopButton(
                    listState = listState,
                    bottomPadding = 16.dp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
private fun EmbeddedAgyTerminal(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var commandInput by rememberSaveable { mutableStateOf("agy help") }
    var outputText by rememberSaveable {
        mutableStateOf(
            "⚡ OmniShizuku AGY Shell Initialized.\nPrivilege: Autonomous (UID 2000 / UID 0)\nPre-installed: AGY CLI & Gemini Assistant.\n\nType 'agy help' or select a quick action below to begin."
        )
    }
    var isRunning by remember { mutableStateOf(false) }
    var lastFailed by remember { mutableStateOf(false) }
    var lastExecutedCmd by remember { mutableStateOf("") }

    val isBinderAlive = remember { Shizuku.pingBinder() }
    val uid = runCatching { if (isBinderAlive) Shizuku.getUid() else -1 }.getOrDefault(-1)

    val quickChips = remember {
        listOf(
            "agy help",
            "agy info",
            "agy fix",
            "agy optimize",
            "agy bug-report",
            "pm list packages -3",
            "dumpsys battery",
            "df -h"
        )
    }

    fun runCmd(targetCmd: String) {
        val trimmed = targetCmd.trim()
        if (trimmed.isBlank() || isRunning) return

        isRunning = true
        lastFailed = false
        lastExecutedCmd = trimmed
        outputText = "Executing: $trimmed ...\n"

        scope.launch {
            val (result, failed) = AgyCliEngine.runAgyOrShell(context, trimmed)
            outputText = result
            lastFailed = failed
            isRunning = false
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBinderAlive) Icons.Rounded.CheckCircle else Icons.Rounded.BugReport,
                        contentDescription = null,
                        tint = if (isBinderAlive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBinderAlive) "Shizuku Active (UID $uid)" else "Shizuku Not Running",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "AGY Pre-installed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick action chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickChips.forEach { chip ->
                FilterChip(
                    selected = commandInput == chip,
                    onClick = {
                        commandInput = chip
                        runCmd(chip)
                    },
                    label = {
                        Text(
                            text = chip,
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            maxLines = 1
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // Input Field & Run Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("e.g. agy help, agy fix, pm list packages") },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { runCmd(commandInput) }),
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = { runCmd(commandInput) },
                enabled = !isRunning && commandInput.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(54.dp)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Run")
                }
            }
        }

        // Failure Action Row
        if (lastFailed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        commandInput = "agy fix"
                        runCmd("agy fix")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AGY Auto-Fix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val reportCmd = "agy bug-report Command '$lastExecutedCmd' failed"
                        commandInput = reportCmd
                        runCmd(reportCmd)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Report Bug", fontSize = 12.sp)
                }
            }
        }

        // Terminal Output Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header bar of terminal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSOLE OUTPUT",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row {
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("Terminal Output", outputText))
                                Toast.makeText(context, "Output copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = { outputText = "Console cleared." },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Output text container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    val terminalScroll = rememberScrollState()
                    SelectionContainer {
                        Text(
                            text = outputText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = if (lastFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(terminalScroll)
                        )
                    }
                }
            }
        }
    }
}

private fun writeRish(context: Context, tree: Uri) {
    val cr = context.contentResolver
    val doc = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
    val child = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))

    runCatching {
        cr.query(
            child,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val name = cursor.getString(1)
                if (name == SH_NAME || name == DEX_NAME) {
                    runCatching {
                        DocumentsContract.deleteDocument(
                            cr,
                            DocumentsContract.buildDocumentUriUsingTree(tree, id)
                        )
                    }
                }
            }
        }
    }

    fun writeToDocument(name: String) {
        runCatching {
            val created = DocumentsContract.createDocument(cr, doc, "application/octet-stream", name) ?: return
            cr.openOutputStream(created)?.use { output ->
                context.assets.open(name).use { input ->
                    if (name == SH_NAME) {
                        input.bufferedReader().use {
                            output.write(it.readText().replace("MANAGER_PKG", context.packageName).toByteArray())
                        }
                    } else {
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    writeToDocument(SH_NAME)
    writeToDocument(DEX_NAME)
}
