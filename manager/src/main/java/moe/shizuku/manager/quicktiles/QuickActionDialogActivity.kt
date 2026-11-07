package moe.shizuku.manager.quicktiles

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.ui.theme.ShizukuTheme

class QuickActionDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)

        val componentName = intent?.getStringExtra(Intent.EXTRA_COMPONENT_NAME)
            ?: intent?.component?.className.orEmpty()

        setContent {
            ShizukuTheme {
                val isUsb = componentName.contains("Usb", ignoreCase = true)
                QuickActionDialog(
                    title = if (isUsb) "USB Debugging Actions" else "Private DNS Actions",
                    actions = if (isUsb) getUsbActions() else getDnsActions(),
                    onDismiss = { finish() },
                    onOpenApp = {
                        startActivity(Intent(this@QuickActionDialogActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                        finish()
                    },
                    onAction = { action ->
                        action.invoke()
                        finish()
                    }
                )
            }
        }
    }

    private fun getUsbActions(): List<Pair<String, () -> Unit>> {
        return listOf(
            "Enable USB Debugging" to {
                QuickTileSettingsManager.setUsbDebuggingEnabled(true)
                Toast.makeText(this@QuickActionDialogActivity, "USB Debugging Enabled", Toast.LENGTH_SHORT).show()
            },
            "Disable USB Debugging" to {
                QuickTileSettingsManager.setUsbDebuggingEnabled(false)
                Toast.makeText(this@QuickActionDialogActivity, "USB Debugging Disabled", Toast.LENGTH_SHORT).show()
            },
            "Toggle Wireless Debugging" to {
                val current = QuickTileSettingsManager.isWirelessDebuggingEnabled(this@QuickActionDialogActivity)
                QuickTileSettingsManager.setWirelessDebuggingEnabled(!current)
                Toast.makeText(this@QuickActionDialogActivity, "Wireless Debugging: " + if (!current) "ON" else "OFF", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun getDnsActions(): List<Pair<String, () -> Unit>> {
        return listOf(
            "DNS Off" to {
                QuickTileSettingsManager.setPrivateDnsMode("off", null)
                Toast.makeText(this@QuickActionDialogActivity, "Private DNS: Off", Toast.LENGTH_SHORT).show()
            },
            "DNS Automatic" to {
                QuickTileSettingsManager.setPrivateDnsMode("opportunistic", null)
                Toast.makeText(this@QuickActionDialogActivity, "Private DNS: Automatic", Toast.LENGTH_SHORT).show()
            },
            "Cloudflare (1.1.1.1)" to {
                QuickTileSettingsManager.setPrivateDnsMode("hostname", QuickTileSettingsManager.HOSTNAME_CLOUDFLARE)
                Toast.makeText(this@QuickActionDialogActivity, "Private DNS: Cloudflare", Toast.LENGTH_SHORT).show()
            },
            "AdGuard DNS" to {
                QuickTileSettingsManager.setPrivateDnsMode("hostname", QuickTileSettingsManager.HOSTNAME_ADGUARD)
                Toast.makeText(this@QuickActionDialogActivity, "Private DNS: AdGuard", Toast.LENGTH_SHORT).show()
            },
            "Quad9" to {
                QuickTileSettingsManager.setPrivateDnsMode("hostname", QuickTileSettingsManager.HOSTNAME_QUAD9)
                Toast.makeText(this@QuickActionDialogActivity, "Private DNS: Quad9", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun QuickActionDialog(
    title: String,
    actions: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onAction: (() -> Unit) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(actions) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(item.second) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(item.first, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenApp) {
                Text("Open App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
