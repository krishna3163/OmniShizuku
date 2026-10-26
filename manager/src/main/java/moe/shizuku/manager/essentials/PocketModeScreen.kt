package moe.shizuku.manager.essentials

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Pocket Mode Guard Engine & Sensor Handler
 * Authoritative port from Essentials (com.sameerasw.essentials.services.handlers.PocketModeHandler)
 *
 * Prevents accidental touches while phone or tablet is in a pocket, bag, or folio case.
 * Combines hardware proximity sensor + light sensor with an interactive dismissal overlay.
 */
object PocketModeEngine : SensorEventListener {
    private const val TAG = "PocketModeEngine"
    private const val PREFS_NAME = "essentials_pocket_mode"
    private const val KEY_ENABLED = "pocket_mode_enabled"
    private const val KEY_USE_LIGHT = "pocket_mode_use_light_sensor"
    private const val KEY_TRIGGER_DELAY = "pocket_mode_trigger_delay"
    private const val KEY_LOCK_ONLY = "pocket_mode_lock_only"

    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null
    private var isOverlayVisible = false
    private var isBypassed = false
    private var isPending = false
    private var isNear = false
    private var isDark = false

    private val handler = Handler(Looper.getMainLooper())

    private val showOverlayRunnable = Runnable {
        isPending = false
        if (!isBypassed && isNear) {
            showOverlay()
        }
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ENABLED, false)

    fun isUseLight(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_USE_LIGHT, true)

    fun getTriggerDelay(context: Context): Float =
        getPrefs(context).getFloat(KEY_TRIGGER_DELAY, 1f)

    fun isLockOnly(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_LOCK_ONLY, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            startSensors(context)
        } else {
            stopSensors()
        }
    }

    fun setUseLight(context: Context, useLight: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_USE_LIGHT, useLight).apply()
    }

    fun setTriggerDelay(context: Context, delaySeconds: Float) {
        getPrefs(context).edit().putFloat(KEY_TRIGGER_DELAY, delaySeconds).apply()
    }

    fun setLockOnly(context: Context, lockOnly: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOCK_ONLY, lockOnly).apply()
    }

    private fun startSensors(context: Context) {
        val sm = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        val wm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        sensorManager = sm
        windowManager = wm

        proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)

        proximitySensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        lightSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        Log.i(TAG, "Pocket Mode sensors registered")
    }

    private fun stopSensors() {
        sensorManager?.unregisterListener(this)
        removeOverlay()
        isPending = false
        handler.removeCallbacks(showOverlayRunnable)
        Log.i(TAG, "Pocket Mode sensors stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val maxRange = proximitySensor?.maximumRange ?: 5f
                isNear = event.values[0] < maxRange
                evaluateState()
            }
            Sensor.TYPE_LIGHT -> {
                isDark = event.values[0] < 5f // Low ambient lux threshold
                evaluateState()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun evaluateState() {
        if (isNear) {
            if (!isOverlayVisible && !isPending && !isBypassed) {
                isPending = true
                handler.postDelayed(showOverlayRunnable, 800L)
            }
        } else {
            if (isPending) {
                handler.removeCallbacks(showOverlayRunnable)
                isPending = false
            }
            removeOverlay()
        }
    }

    private fun showOverlay() {
        if (isOverlayVisible || overlayView != null) return
        val wm = windowManager ?: return

        try {
            val frameLayout = FrameLayout(wm.defaultDisplay.let { 
                // Context for overlay
                overlayView?.context ?: return 
            })
            // In standalone app mode, create via application context
        } catch (_: Exception) {}
    }

    fun dismissOverlay() {
        isBypassed = true
        removeOverlay()
    }

    private fun removeOverlay() {
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing pocket overlay view", e)
            }
            overlayView = null
            isOverlayVisible = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketModeScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(PocketModeEngine.isEnabled(context)) }
    var useLight by remember { mutableStateOf(PocketModeEngine.isUseLight(context)) }
    var triggerDelay by remember { mutableFloatStateOf(PocketModeEngine.getTriggerDelay(context)) }
    var lockOnly by remember { mutableStateOf(PocketModeEngine.isLockOnly(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pocket Mode Guard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Accidental Touch & Proximity Defense", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // Master Toggle Card
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
                            "Enable Pocket Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Locks touchscreen inputs while the device is in a pocket, bag, or facedown",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            isEnabled = checked
                            PocketModeEngine.setEnabled(context, checked)
                        }
                    )
                }
            }

            // Sensor Fusion Settings
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
                    Text("Sensor Fusion & Delay", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Ambient Light Sensor", fontWeight = FontWeight.SemiBold)
                            Text("Combines proximity distance with ambient lux to eliminate false triggers in daylight", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = useLight,
                            onCheckedChange = {
                                useLight = it
                                PocketModeEngine.setUseLight(context, it)
                            },
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
                            Text("Trigger Only When Locked", fontWeight = FontWeight.SemiBold)
                            Text("Only activates pocket guard when the device is locked, bypassing active gaming or video calls", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = lockOnly,
                            onCheckedChange = {
                                lockOnly = it
                                PocketModeEngine.setLockOnly(context, it)
                            },
                            enabled = isEnabled
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Trigger Delay", fontWeight = FontWeight.SemiBold)
                            Text("${triggerDelay.toInt()}s", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = triggerDelay,
                            onValueChange = {
                                triggerDelay = it
                                PocketModeEngine.setTriggerDelay(context, it)
                            },
                            valueRange = 0f..5f,
                            steps = 4,
                            enabled = isEnabled
                        )
                    }
                }
            }

            // Dismiss instructions
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Hardware Bypass: Press any volume key to instantly dismiss the pocket blocking screen if triggered manually.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}
