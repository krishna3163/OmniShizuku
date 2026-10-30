package moe.shizuku.manager.wallyou

import android.content.Context
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallYouSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var intervalHours by remember { mutableStateOf(6) }
    var changeHome by remember { mutableStateOf(true) }
    var changeLock by remember { mutableStateOf(false) }
    var useCombined by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallpaper Changer Settings") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Change Interval", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$intervalHours hours")
                        Row {
                            IconButton(onClick = { if (intervalHours > 1) intervalHours-- }) {
                                Icon(Icons.Outlined.Remove, contentDescription = null)
                            }
                            IconButton(onClick = { intervalHours++ }) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                            }
                        }
                    }
                    Button(
                        onClick = { WallYouAutoChanger.schedule(context, (intervalHours * 60).toLong()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Auto Changer")
                    }
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Wallpaper Target", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change Home Screen")
                        Switch(checked = changeHome, onCheckedChange = { changeHome = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change Lock Screen")
                        Switch(checked = changeLock, onCheckedChange = { changeLock = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Use Combined Wallpaper")
                        Switch(checked = useCombined, onCheckedChange = { useCombined = it })
                    }
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sources", style = MaterialTheme.typography.titleMedium)
                    Text("Wallhaven, Pixabay, Reddit, Picsum, Bing, Owalls, Wikimedia", 
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
