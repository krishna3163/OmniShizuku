package moe.shizuku.manager.essentials

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Settings surface for Essentials' Caffeinate controller and foreground service. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaffeinateScreen(bottomPadding: Dp, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("caffeinate_prefs", 0) }
    val active by CaffeinateController.isActive
    val starting by CaffeinateController.isStarting
    val secondsLeft by CaffeinateController.startingTimeLeft
    val selectedTimeout by CaffeinateController.selectedTimeout
    var abortOnScreenOff by remember { mutableStateOf(prefs.getBoolean("abort_screen_off", true)) }
    var skipCountdown by remember { mutableStateOf(prefs.getBoolean("skip_countdown", false)) }
    var timeoutMinutes by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        CaffeinateController.check(context)
        while (true) {
            CaffeinateController.check(context)
            delay(1000)
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Column {
                    Text("Caffeinate", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Keep the screen awake", style = MaterialTheme.typography.bodySmall)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
        )
    }) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Coffee, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text(if (active) "Caffeinate is active" else if (starting) "Starting in ${secondsLeft}s" else "Caffeinate is off", fontWeight = FontWeight.Bold)
                            Text(if (active && selectedTimeout == -1) "No timeout" else if (active) "$selectedTimeout minute timeout" else "Choose how long to keep the display awake", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = active || starting,
                            onCheckedChange = { enabled ->
                                if (enabled) CaffeinateController.startFor(context, timeoutMinutes)
                                else CaffeinateController.cancelAll(context)
                            },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 30, 60, -1).forEach { value ->
                            FilterChip(
                                selected = timeoutMinutes == value,
                                onClick = { timeoutMinutes = value },
                                label = { Text(if (value == -1) "∞" else if (value == 60) "1h" else "${value}m") },
                            )
                        }
                    }
                    Button(
                        onClick = { CaffeinateController.startFor(context, timeoutMinutes) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !active && !starting,
                    ) { Text("Start Caffeinate") }
                    if (active || starting) {
                        Button(onClick = { CaffeinateController.cancelAll(context) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Stop")
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Stop when the screen turns off", fontWeight = FontWeight.SemiBold)
                            Text("Automatically release the wake lock on screen off", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = abortOnScreenOff, onCheckedChange = {
                            abortOnScreenOff = it
                            prefs.edit().putBoolean("abort_screen_off", it).apply()
                            if (active) context.startService(Intent(context, CaffeinateWakeLockService::class.java).setAction("UPDATE_PREFS"))
                        })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Skip start countdown", fontWeight = FontWeight.SemiBold)
                            Text("Start the selected timeout immediately", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = skipCountdown, onCheckedChange = {
                            skipCountdown = it
                            prefs.edit().putBoolean("skip_countdown", it).apply()
                        })
                    }
                }
            }
            Spacer(Modifier.height(bottomPadding + 24.dp))
        }
    }
}
