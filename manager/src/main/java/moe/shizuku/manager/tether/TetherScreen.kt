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
    var lastStatusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    val isShizukuRunning = TetherManager.isShizukuAvailable()

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

    val scaffoldStatus = when {
        !isShizukuRunning -> LabToolStatus.Warning(
            title = "Shizuku Not Active",
            message = "Shizuku service is not running. Tethering global settings require Shizuku or ADB shell privileges.",
            icon = Icons.Outlined.WarningAmber
        )
        isStatusError && lastStatusMessage != null -> LabToolStatus.Error(
            title = "Action Failed",
            message = lastStatusMessage!!,
            icon = Icons.Outlined.ErrorOutline
        )
        !isStatusError && lastStatusMessage != null -> LabToolStatus.Info(
            title = "Status Update",
            message = lastStatusMessage!!,
            icon = Icons.Outlined.CheckCircle
        )
        else -> null
    }

    LabToolScaffold(
        title = "Carrier Tethering",
        description = "Configure supported tethering options. Carrier/device support varies. Manage DUN requirements and entitlement validation.",
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
                            value = state?.tetherDunRequired ?: "Unknown"
                        )
                        SettingValueRow(
                            label = "tether_entitlement_check_state",
                            value = state?.entitlementCheckState ?: "Unknown"
                        )
                        SettingValueRow(
                            label = "tether_supported",
                            value = state?.tetherSupported ?: "Unknown"
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
                                "Shizuku Shell / ADB Privileges",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Sets tether_dun_required to 0 and entitlement_check_state to 0 in Android global settings. This prevents carrier provision checks on supported networks.",
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
                                    isStatusError = !result.success
                                    lastStatusMessage = result.message
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
                                    isStatusError = !result.success
                                    lastStatusMessage = result.message
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
                                "Kernel Netfilter / Root Required",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Rewrites outgoing tether packets to TTL 64 so mobile carriers identify downstream client traffic as native phone traffic. Note: Modifying kernel iptables mangle tables requires Root permissions on Android 10+.",
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
                                actionInProgress = true
                                scope.launch {
                                    val result = TetherManager.applyTtlBypass(64)
                                    isStatusError = !result.success
                                    lastStatusMessage = result.message
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    actionInProgress = false
                                }
                            },
                            enabled = isShizukuRunning && !actionInProgress
                        ) {
                            Text("Apply TTL 64 Rule")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingValueRow(label: String, value: String) {
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
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
