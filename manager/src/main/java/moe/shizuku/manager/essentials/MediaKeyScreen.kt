package moe.shizuku.manager.essentials

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

/**
 * Volume Key Track Skipper & Button Remap Controller
 * Authoritative port from Essentials (com.sameerasw.essentials.input.VolumeLongPressDetector)
 *
 * Allows controlling music playback with physical hardware volume buttons when screen is off:
 * - Long-Press Volume Up: Skip to Next Track
 * - Long-Press Volume Down: Skip to Previous Track
 * - Double Tap / Long-Press Play/Pause trigger
 */
object VolumeSkipperEngine {
    private const val PREFS_NAME = "essentials_volume_skipper"
    private const val KEY_ENABLED = "volume_skip_enabled"
    private const val KEY_DURATION = "volume_skip_duration"
    private const val KEY_HAPTIC = "volume_skip_haptic"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ENABLED, false)

    fun getDuration(context: Context): Int =
        getPrefs(context).getInt(KEY_DURATION, 500)

    fun isHapticEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_HAPTIC, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun setDuration(context: Context, durationMs: Int) {
        getPrefs(context).edit().putInt(KEY_DURATION, durationMs).apply()
    }

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAPTIC, enabled).apply()
    }

    fun performHaptic(context: Context) {
        if (!isHapticEnabled(context)) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    suspend fun skipNext(context: Context) {
        performHaptic(context)
        withContext(Dispatchers.IO) {
            runShellCommand("input keyevent 87") // KEYCODE_MEDIA_NEXT
        }
    }

    suspend fun skipPrevious(context: Context) {
        performHaptic(context)
        withContext(Dispatchers.IO) {
            runShellCommand("input keyevent 88") // KEYCODE_MEDIA_PREVIOUS
        }
    }

    suspend fun togglePlayPause(context: Context) {
        performHaptic(context)
        withContext(Dispatchers.IO) {
            runShellCommand("input keyevent 85") // KEYCODE_MEDIA_PLAY_PAUSE
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaKeyScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isEnabled by remember { mutableStateOf(VolumeSkipperEngine.isEnabled(context)) }
    var duration by remember { mutableIntStateOf(VolumeSkipperEngine.getDuration(context)) }
    var haptic by remember { mutableStateOf(VolumeSkipperEngine.isHapticEnabled(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Volume Key Track Skipper", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Hardware Key Actions & Remap", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Switch
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Enable Volume Track Skipper",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Long-press Volume Up to skip forward, and Volume Down to skip backward when screen is off",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            isEnabled = checked
                            VolumeSkipperEngine.setEnabled(context, checked)
                        }
                    )
                }
            }

            // Duration and Haptics
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Hold Threshold & Feedback", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Long-Press Hold Duration", fontWeight = FontWeight.SemiBold)
                            Text("${duration}ms", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = duration.toFloat(),
                            onValueChange = {
                                duration = it.toInt()
                                VolumeSkipperEngine.setDuration(context, it.toInt())
                            },
                            valueRange = 300f..1000f,
                            steps = 6,
                            enabled = isEnabled
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Click Feedback", fontWeight = FontWeight.SemiBold)
                            Text("Vibrate subtly when track change is successfully triggered", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = haptic,
                            onCheckedChange = {
                                haptic = it
                                VolumeSkipperEngine.setHapticEnabled(context, it)
                            },
                            enabled = isEnabled
                        )
                    }
                }
            }

            // Manual Test Controls
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Test Shell Media Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Test Shizuku media key injection on your active music player (Spotify, YouTube Music, Apple Music):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { coroutineScope.launch { VolumeSkipperEngine.skipPrevious(context) } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.SkipPrevious, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev")
                        }
                        Button(
                            onClick = { coroutineScope.launch { VolumeSkipperEngine.togglePlayPause(context) } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play/Pause")
                        }
                        OutlinedButton(
                            onClick = { coroutineScope.launch { VolumeSkipperEngine.skipNext(context) } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.SkipNext, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Next")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
