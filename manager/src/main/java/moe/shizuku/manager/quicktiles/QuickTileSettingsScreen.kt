@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package moe.shizuku.manager.quicktiles

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTileSettingsScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // USB Debugging state
    var usbDebuggingEnabled by remember { mutableStateOf(QuickTileSettingsManager.isUsbDebuggingEnabled(context)) }
    var devOptionsEnabled by remember { mutableStateOf(QuickTileSettingsManager.isDevOptionsEnabled(context)) }
    var usbAutoRevert by remember { mutableStateOf(QuickTileSettingsManager.isUsbAutoRevert(context)) }
    var usbDelayText by remember { mutableStateOf(QuickTileSettingsManager.getUsbRevertDelay(context).toString()) }

    // DNS state
    var dnsMode by remember { mutableStateOf(QuickTileSettingsManager.getPrivateDnsMode(context)) }
    var dnsHost by remember { mutableStateOf(QuickTileSettingsManager.getPrivateDnsSpecifier(context)) }
    var dnsAutoRevert by remember { mutableStateOf(QuickTileSettingsManager.isDnsAutoRevert(context)) }
    var dnsDelayText by remember { mutableStateOf(QuickTileSettingsManager.getDnsRevertDelay(context).toString()) }

    // DNS Cycle toggles
    var cycleOff by remember { mutableStateOf(QuickTileSettingsManager.isCycleDnsOff(context)) }
    var cycleAuto by remember { mutableStateOf(QuickTileSettingsManager.isCycleDnsAuto(context)) }
    var cycleCloudflare by remember { mutableStateOf(QuickTileSettingsManager.isCycleDnsCloudflare(context)) }
    var cycleAdguard by remember { mutableStateOf(QuickTileSettingsManager.isCycleDnsAdguard(context)) }
    var cycleQuad9 by remember { mutableStateOf(QuickTileSettingsManager.isCycleDnsQuad9(context)) }

    fun refreshState() {
        usbDebuggingEnabled = QuickTileSettingsManager.isUsbDebuggingEnabled(context)
        devOptionsEnabled = QuickTileSettingsManager.isDevOptionsEnabled(context)
        dnsMode = QuickTileSettingsManager.getPrivateDnsMode(context)
        dnsHost = QuickTileSettingsManager.getPrivateDnsSpecifier(context)
    }

    LaunchedEffect(Unit) {
        refreshState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quick Settings Tiles", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Customize Quick Settings Tile behavior and Auto-Revert",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshState() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("USB Debugging") },
                    icon = { Icon(Icons.Outlined.Usb, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Private DNS") },
                    icon = { Icon(Icons.Outlined.Dns, contentDescription = null) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    // USB Debugging section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (usbDebuggingEnabled) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Usb,
                                    contentDescription = null,
                                    tint = if (usbDebuggingEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (usbDebuggingEnabled) "USB Debugging is Active"
                                        else "USB Debugging is Disabled",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (usbDebuggingEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        if (devOptionsEnabled) "Developer options are enabled"
                                        else "Developer options are disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (usbDebuggingEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Switch(
                                    checked = usbDebuggingEnabled,
                                    onCheckedChange = { target ->
                                        scope.launch {
                                            val ok = withContext(Dispatchers.IO) {
                                                QuickTileSettingsManager.setUsbDebuggingEnabled(target)
                                            }
                                            if (ok) {
                                                usbDebuggingEnabled = target
                                            } else {
                                                Toast.makeText(context, "Failed to toggle USB debugging", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Auto-revert after delay", fontWeight = FontWeight.SemiBold)
                                        Text("Automatically turn off USB debugging after a delay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = usbAutoRevert,
                                        onCheckedChange = {
                                            usbAutoRevert = it
                                            QuickTileSettingsManager.setUsbAutoRevert(context, it)
                                        }
                                    )
                                }

                                AnimatedVisibility(visible = usbAutoRevert) {
                                    OutlinedTextField(
                                        value = usbDelayText,
                                        onValueChange = {
                                            usbDelayText = it
                                            it.toIntOrNull()?.let { sec ->
                                                QuickTileSettingsManager.setUsbRevertDelay(context, sec)
                                            }
                                        },
                                        label = { Text("Revert delay (seconds)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Tip: Add the 'USB Debugging' tile to your quick settings panel to toggle it with single tap anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Private DNS section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (dnsMode != "off") MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Dns,
                                        contentDescription = null,
                                        tint = if (dnsMode != "off") MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            when (dnsMode) {
                                                "off" -> "Private DNS is Off"
                                                "opportunistic" -> "Private DNS: Automatic"
                                                else -> "Private DNS: $dnsHost"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (dnsMode != "off") MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Quick Settings tile cycles through chosen DNS presets.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (dnsMode != "off") MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Tile Cycle States", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("Choose which DNS options to include when cycling the tile:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    cycleOff = !cycleOff
                                    QuickTileSettingsManager.setCycleDnsOff(context, cycleOff)
                                }) {
                                    Checkbox(checked = cycleOff, onCheckedChange = {
                                        cycleOff = it
                                        QuickTileSettingsManager.setCycleDnsOff(context, it)
                                    })
                                    Text("DNS Off")
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    cycleAuto = !cycleAuto
                                    QuickTileSettingsManager.setCycleDnsAuto(context, cycleAuto)
                                }) {
                                    Checkbox(checked = cycleAuto, onCheckedChange = {
                                        cycleAuto = it
                                        QuickTileSettingsManager.setCycleDnsAuto(context, it)
                                    })
                                    Text("DNS Automatic (System Default)")
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    cycleCloudflare = !cycleCloudflare
                                    QuickTileSettingsManager.setCycleDnsCloudflare(context, cycleCloudflare)
                                }) {
                                    Checkbox(checked = cycleCloudflare, onCheckedChange = {
                                        cycleCloudflare = it
                                        QuickTileSettingsManager.setCycleDnsCloudflare(context, it)
                                    })
                                    Column {
                                        Text("Cloudflare (1.1.1.1)")
                                        Text("one.one.one.one · Performance & Privacy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    cycleAdguard = !cycleAdguard
                                    QuickTileSettingsManager.setCycleDnsAdguard(context, cycleAdguard)
                                }) {
                                    Checkbox(checked = cycleAdguard, onCheckedChange = {
                                        cycleAdguard = it
                                        QuickTileSettingsManager.setCycleDnsAdguard(context, it)
                                    })
                                    Column {
                                        Text("AdGuard DNS")
                                        Text("dns.adguard-dns.com · Ad & Tracker Blocking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    cycleQuad9 = !cycleQuad9
                                    QuickTileSettingsManager.setCycleDnsQuad9(context, cycleQuad9)
                                }) {
                                    Checkbox(checked = cycleQuad9, onCheckedChange = {
                                        cycleQuad9 = it
                                        QuickTileSettingsManager.setCycleDnsQuad9(context, it)
                                    })
                                    Column {
                                        Text("Quad9")
                                        Text("dns.quad9.net · Malware & Phishing Protection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Auto-revert Private DNS", fontWeight = FontWeight.SemiBold)
                                        Text("Revert back to the previous DNS mode after delay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = dnsAutoRevert,
                                        onCheckedChange = {
                                            dnsAutoRevert = it
                                            QuickTileSettingsManager.setDnsAutoRevert(context, it)
                                        }
                                    )
                                }

                                AnimatedVisibility(visible = dnsAutoRevert) {
                                    OutlinedTextField(
                                        value = dnsDelayText,
                                        onValueChange = {
                                            dnsDelayText = it
                                            it.toIntOrNull()?.let { sec ->
                                                QuickTileSettingsManager.setDnsRevertDelay(context, sec)
                                            }
                                        },
                                        label = { Text("Revert delay (seconds)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

