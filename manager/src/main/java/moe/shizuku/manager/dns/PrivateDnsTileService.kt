package moe.shizuku.manager.dns

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class PrivateDnsTileService : TileService() {
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val nextName = PrivateDnsManager.cycleNextDns()
            updateTileState(nextName)
        }
    }

    private fun updateTileState(forcedSubtitle: String? = null) {
        val tile = qsTile ?: return
        scope.launch {
            val mode = PrivateDnsManager.getCurrentMode()
            val host = PrivateDnsManager.getCurrentHostname()

            val subtitle = forcedSubtitle ?: when (mode) {
                PrivateDnsManager.DnsMode.OFF -> "Off"
                PrivateDnsManager.DnsMode.AUTOMATIC -> "Automatic"
                PrivateDnsManager.DnsMode.CUSTOM -> host?.substringBefore('.') ?: "Custom"
            }

            tile.state = if (mode == PrivateDnsManager.DnsMode.OFF) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.label = "Private DNS"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = subtitle
            }
            tile.updateTile()
        }
    }
}
