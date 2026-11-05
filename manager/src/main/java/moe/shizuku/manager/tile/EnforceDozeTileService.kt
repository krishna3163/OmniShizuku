package moe.shizuku.manager.tile

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.doze.EnforceDozeManager
import moe.shizuku.manager.service.EnforceDozeServiceStarter

@RequiresApi(api = Build.VERSION_CODES.N)
class EnforceDozeTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileState()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val enabled = ShizukuSettings.getPreferences().getBoolean("enforcedoze_serviceEnabled", false)
        if (enabled) {
            ShizukuSettings.getPreferences().edit().putBoolean("enforcedoze_serviceEnabled", false).apply()
            EnforceDozeServiceStarter.stopService(this)
        } else {
            ShizukuSettings.getPreferences().edit().putBoolean("enforcedoze_serviceEnabled", true).apply()
            EnforceDozeServiceStarter.startService(this)
        }
    }

    fun updateTileState() {
        Handler(Looper.getMainLooper()).postDelayed({
            val tile = qsTile
            val active = ShizukuSettings.getPreferences().getBoolean("enforcedoze_serviceEnabled", false)
            tile?.label = if (active) "EnforceDoze on" else "EnforceDoze off"
            tile?.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile?.updateTile()
        }, 150)
    }
}
