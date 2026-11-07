package moe.shizuku.manager.tether

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import moe.shizuku.manager.ui.component.LabToolScaffold
import moe.shizuku.manager.ui.component.LabToolStatus
import moe.shizuku.manager.ui.theme.OmniTokens

@Composable
fun TetherScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<TetherSettingsState?>(null) }
    var loading by remember { mutableStateOf(true) }
    var actionInProgress by remember { mutableStateOf(false) }
    var showRootInfoDialog by remember { mutableStateOf(false) }

    val isShizukuRunning = TetherManager.isShizukuAvailable()
    val isRoot = remember(isShizukuRunning) { TetherManager.isRootAvailable() }

    fun refresh() {
        loading = true
        scope.launch {
            state = TetherManager.getTetherState()
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val isBypassActive = state?.tetherDunRequired == "0" && state?.entitlementCheckState == "0"

    val scaffoldStatus = when {
        !isShizukuRunning -> LabToolStatus.Warning(
            title = "Shizuku Not Active",
            message = "Shizuku service is not running. Carrier tethering global settings require Shizuku or ADB shell privileges.",
            icon = Icons.Outlined.WarningAmber
        )
        else -> null
    }

    if (showRootInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRootInfoDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = OmniTokens.brandPrimary()
                )
            },
            title = {
                Text(
                    "Root Required for Kernel TTL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Kernel netfilter (iptables mangle) requires Linux Root privileges (UID 0).\n\n" +
                    "OmniShizuku is currently running under Shizuku ADB Shell (UID 2000).\n\n" +
                    "The Entitlement & DUN Bypass above is already active and works rootlessly without needing Root.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showRootInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    LabToolScaffold(
        title = "Carrier Tethering",
        description = "Configure carrier tethering entitlements, DUN requirements, and hotspot restrictions.",
        status = scaffoldStatus,
        onBack = onBack,
        bottomPadding = bottomPadding,
        actions = {
            IconButton(
                onClick = { refresh() },
                enabled = !loading && !actionInProgress
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // State Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = OmniTokens.brandPrimary(),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Carrier Entitlement Status",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!loading) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBypassActive) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = if (isBypassActive) "Bypass Active" else "Default / Restricted",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBypassActive) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Querying system global settings...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        SettingValueRow(
                            label = "tether_dun_required",
                            value = state?.tetherDunRequired ?: "Unknown",
                            isHighlight = state?.tetherDunRequired == "0"
                        )
                        SettingValueRow(
                            label = "tether_entitlement_check_state",
                            value = state?.entitlementCheckState ?: "Unknown",
                            isHighlight = state?.entitlementCheckState == "0"
                        )
                        SettingValueRow(
                            label = "tether_supported",
                            value = state?.tetherSupported ?: "Unknown",
                            isHighlight = state?.tetherSupported == "1"
                        )
                    }
                }
            }

            // 1-Tap Bypass Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WifiTethering,
                            contentDescription = null,
                            tint = OmniTokens.brandPrimary(),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Entitlement & DUN Bypass",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Shizuku Shell / ADB Privileges (Rootless)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Disables carrier hotspot validation checks (DUN requirement and entitlement check) in Android global settings. Enables tethering without restrictions.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                actionInProgress = true
                                scope.launch {
                                    val result = TetherManager.bypassCarrierRestrictions()
                                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                    refresh()
                                    actionInProgress = false
                                }
                            },
                            enabled = isShizukuRunning && !actionInProgress,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Bypass Restrictions")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                actionInProgress = true
                                scope.launch {
                                    val result = TetherManager.restoreDefaultRestrictions()
                                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                    refresh()
                                    actionInProgress = false
                                }
                            },
                            enabled = isShizukuRunning && !actionInProgress,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore Defaults")
                        }
                    }
                }
            }

            // TTL Hop Limit Rewriting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Speed,
                                contentDescription = null,
                                tint = OmniTokens.brandPrimary(),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "TTL Hop-Limit Camouflage (TTL 64)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    if (isRoot) "Kernel Netfilter / Root Active" else "Kernel Netfilter (Root Only)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRoot) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = if (isRoot) "Root (UID 0)" else "Root Required",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isRoot) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Rewrites outgoing tether packet TTL to 64 so mobile carriers identify downstream client traffic as native device traffic. Note: Packet mangle requires Linux root privileges.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (!isRoot) {
                                    showRootInfoDialog = true
                                } else {
                                    actionInProgress = true
                                    scope.launch {
                                        val result = TetherManager.applyTtlBypass(64)
                                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                        actionInProgress = false
                                    }
                                }
                            },
                            enabled = isShizukuRunning && !actionInProgress
                        ) {
                            Icon(
                                if (isRoot) Icons.Outlined.Check else Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isRoot) "Apply TTL 64 Rule" else "View Root Requirement")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingValueRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isHighlight) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
