package moe.shizuku.manager.essentials

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.widget.FrameLayout
import android.view.WindowManager

class PocketModeService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var proximity: Sensor? = null
    
    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        proximity?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_PROXIMITY) {
                val maxRange = proximity?.maximumRange ?: 5f
                val isNear = sensorEvent.values[0] < maxRange
                
                if (isNear) {
                    showPocketOverlay()
                } else {
                    hidePocketOverlay()
                }
            }
        }
    }

    private fun showPocketOverlay() {
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            setOnTouchListener { _, _ -> true }
        }

        try {
            wm.addView(overlay, params)
            overlayView = overlay
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hidePocketOverlay() {
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return
        overlayView?.let { view ->
            try {
                wm.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    companion object {
        private var overlayView: FrameLayout? = null
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
