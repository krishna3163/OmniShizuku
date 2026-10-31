package moe.shizuku.manager.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.doze.DozeManager

@RequiresApi(Build.VERSION_CODES.N)
class DozeTileService : TileService() {
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val state = DozeManager.getDeepDozeState()
            if (state.equals("IDLE", true)) {
                DozeManager.unforceIdle()
            } else {
                DozeManager.forceDeepIdle()
            }
            updateTile()
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        scope.launch {
            val state = DozeManager.getDeepDozeState()
            val isIdle = state.equals("IDLE", true)
            tile.state = if (isIdle) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = "Deep Sleep"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = state
            }
            tile.updateTile()
        }
    }
}
