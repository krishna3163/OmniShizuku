package moe.shizuku.manager.audio

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.R
import moe.shizuku.manager.audio.data.AudioOutputBase
import moe.shizuku.manager.audio.dialog.SoundMasterDialog
import moe.shizuku.manager.ui.component.LabToolScaffold
import moe.shizuku.manager.ui.theme.OmniTokens
import java.io.File

private const val FILENAME_SOUNDMASTER_PACKAGE_SLIDERS = "soundmaster_package_sliders"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundMasterScreen(
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("soundmaster", Context.MODE_PRIVATE) }
    var showNoti by remember { mutableStateOf(prefs.getBoolean("show_notification", false)) }
    var showOnVolChange by remember { mutableStateOf(prefs.getBoolean("show_on_volume_change", false)) }
    var autoHide by remember { mutableStateOf(prefs.getBoolean("auto_hide", false)) }

    var sliders by remember {
        mutableStateOf(loadPackageSliders(context))
    }

    LabToolScaffold(
        title = "SoundMaster",
        description = "Per-app volume sliders, multi-output routing, and audio equalization.",
        onBack = onBack,
        actions = {
            IconButton(onClick = {
                val intent = Intent(context, SoundMasterActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(intent)
            }) {
                Icon(Icons.Outlined.Tune, contentDescription = "Floating Controls")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OmniTokens.shapeMedium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Equalizer,
                            contentDescription = null,
                            tint = OmniTokens.brandPrimary(),
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Per-App Volume & Routing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Control audio volume of individual applications independently.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val intent = Intent(context, SoundMasterActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = OmniTokens.shapeMedium
                    ) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Floating Mixer Overlay")
                    }
                }
            }

            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OmniTokens.shapeMedium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.show_soundmaster_notification),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Persistent notification for quick access",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showNoti,
                            onCheckedChange = { checked ->
                                showNoti = checked
                                prefs.edit().putBoolean("show_notification", checked).apply()
                                SoundMasterDialog(context).updateControlNotiState(context, checked)
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.show_on_volume_change),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Trigger mixer when hardware volume keys are pressed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showOnVolChange,
                            onCheckedChange = { checked ->
                                showOnVolChange = checked
                                prefs.edit().putBoolean("show_on_volume_change", checked).apply()
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.auto_hide_soundmaster),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Automatically dismiss floating mixer after inactivity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoHide,
                            onCheckedChange = { checked ->
                                autoHide = checked
                                prefs.edit().putBoolean("auto_hide", checked).apply()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 16.dp))
        }
    }
}

private fun loadPackageSliders(context: Context): List<AudioOutputBase> {
    return try {
        val file = File(context.filesDir, FILENAME_SOUNDMASTER_PACKAGE_SLIDERS)
        if (!file.exists()) emptyList()
        else {
            file.readText().split("\n").filter { it.isNotBlank() }.map { line ->
                val splits = line.split("/")
                AudioOutputBase(splits[0], splits[1].toInt(), splits[2].toFloatOrNull() ?: 100f)
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}
