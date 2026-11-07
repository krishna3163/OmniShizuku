package moe.shizuku.manager.quicktiles

import android.graphics.drawable.Icon
import android.os.Build
import android.os.CountDownTimer
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import moe.shizuku.manager.R

@RequiresApi(Build.VERSION_CODES.N)
class UsbDebuggingTileService : TileService() {

    private var revertTimer: CountDownTimer? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val currentEnabled = QuickTileSettingsManager.isUsbDebuggingEnabled(this)
        val target = !currentEnabled

        revertTimer?.cancel()
        revertTimer = null

        val ok = QuickTileSettingsManager.setUsbDebuggingEnabled(target)
        if (ok) {
            updateTileState()
            Toast.makeText(this, "USB Debugging: ${if (target) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()

            if (QuickTileSettingsManager.isUsbAutoRevert(this)) {
                val delaySec = QuickTileSettingsManager.getUsbRevertDelay(this)
                revertTimer = object : CountDownTimer(delaySec * 1000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        QuickTileSettingsManager.setUsbDebuggingEnabled(currentEnabled)
                        updateTileState()
                        Toast.makeText(applicationContext, "USB Debugging auto-reverted", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
        } else {
            Toast.makeText(this, "Failed to toggle USB debugging. Check Shizuku/ADB permissions.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val enabled = QuickTileSettingsManager.isUsbDebuggingEnabled(this)
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "USB Debugging"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (enabled) "Enabled" else "Disabled"
        }
        tile.icon = Icon.createWithResource(this, if (enabled) R.drawable.ic_usb_on else R.drawable.ic_usb_off)
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        revertTimer?.cancel()
    }
}

