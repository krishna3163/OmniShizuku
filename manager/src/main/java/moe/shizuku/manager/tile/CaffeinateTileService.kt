package moe.shizuku.manager.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import moe.shizuku.manager.essentials.CaffeinateController

@RequiresApi(Build.VERSION_CODES.N)
class CaffeinateTileService : TileService() {
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            updateTile()
            if (CaffeinateController.isStarting.value) handler.postDelayed(this, 1000L)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        CaffeinateController.check(this)
        if (CaffeinateController.isStarting.value) {
            handler.removeCallbacks(refreshRunnable)
            handler.post(refreshRunnable)
        }
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onClick() {
        super.onClick()
        if (CaffeinateController.isStarting.value) {
            CaffeinateController.cycleTimeout(this)
        } else {
            CaffeinateController.toggle(this)
        }
        handler.removeCallbacks(refreshRunnable)
        handler.post(refreshRunnable)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val active = CaffeinateController.isActive.value
        val starting = CaffeinateController.isStarting.value
        tile.state = if (active || starting) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Caffeinate"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = when {
                starting -> "Starting ${CaffeinateController.startingTimeLeft.value}s"
                active && CaffeinateController.selectedTimeout.value == -1 -> "Infinite"
                active -> "${CaffeinateController.selectedTimeout.value}m"
                else -> "Off"
            }
        }
        tile.updateTile()
    }
}
