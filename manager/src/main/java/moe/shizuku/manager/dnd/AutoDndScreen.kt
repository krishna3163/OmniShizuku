package moe.shizuku.manager.dnd

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDndScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var enabled by remember { mutableStateOf(AutoDndManager.isEnabled(context)) }
    var currentZen by remember { mutableIntStateOf(0) }
    var targetZen by remember { mutableIntStateOf(AutoDndManager.getTargetZenMode(context)) }
    var selectedApps by remember { mutableStateOf(AutoDndManager.getTargetApps(context)) }
    var newAppPkg by remember { mutableStateOf("") }

    fun refresh() {
        scope.launch {
            currentZen = AutoDndManager.getCurrentZenMode()
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val zenModeNames = mapOf(
        0 to "Zen Mode Off (All Notifications)",
        1 to "Priority Interruptions Only",
        2 to "Total Silence (No Vibrations/Sounds)",
        3 to "Alarms Only"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto DND & Zen Automator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Switch Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Auto DND Interruption Guard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Active Zen State: ${zenModeNames[currentZen] ?: "Mode $currentZen"}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Switch(
                            checked = enabled,
                            onCheckedChange = { checked ->
                                enabled = checked
                                AutoDndManager.setEnabled(context, checked)
                            }
                        )
                    }
                }
            }

            // Target Zen Mode Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Triggered DND Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(8.dp))

                        listOf(
                            1 to "Priority Only (Permits stars & callers)",
                            2 to "Total Silence (Zero sounds, lights, vibration)",
                            3 to "Alarms Only (Permits morning alarms)"
                        ).forEach { (mode, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        targetZen = mode
                                        AutoDndManager.setTargetZenMode(context, mode)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = targetZen == mode,
                                    onClick = {
                                        targetZen = mode
                                        AutoDndManager.setTargetZenMode(context, mode)
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label, fontSize = 14.sp)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val ok = AutoDndManager.setZenMode(targetZen)
                                        if (ok) {
                                            Toast.makeText(context, "Zen Mode applied!", Toast.LENGTH_SHORT).show()
                                            refresh()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Engage Now")
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val ok = AutoDndManager.setZenMode(0)
                                        if (ok) {
                                            Toast.makeText(context, "Zen Mode turned off", Toast.LENGTH_SHORT).show()
                                            refresh()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Turn Off")
                            }
                        }
                    }
                }
            }

            // Target Apps List
            item {
                Text(
                    "TRIGGER APPS (GAMES, CAMERA, NAVIGATION)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newAppPkg,
                        onValueChange = { newAppPkg = it },
                        placeholder = { Text("Package (e.g. com.dts.freefireth)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newAppPkg.isNotBlank()) {
                                val updated = selectedApps + newAppPkg.trim()
                                selectedApps = updated
                                AutoDndManager.setTargetApps(context, updated)
                                newAppPkg = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }

            items(selectedApps.toList()) { pkg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pkg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        IconButton(
                            onClick = {
                                val updated = selectedApps - pkg
                                selectedApps = updated
                                AutoDndManager.setTargetApps(context, updated)
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}
