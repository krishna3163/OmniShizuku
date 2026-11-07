package moe.shizuku.manager.quicktiles

import android.os.Build
import android.os.CountDownTimer
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import moe.shizuku.manager.dns.PrivateDnsManager

@RequiresApi(Build.VERSION_CODES.N)
class AdvancedPrivateDnsTileService : TileService() {

    private var revertTimer: CountDownTimer? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val prevMode = QuickTileSettingsManager.getPrivateDnsMode(this)
        val prevSpec = QuickTileSettingsManager.getPrivateDnsSpecifier(this)

        revertTimer?.cancel()
        revertTimer = null

        // Cycle through user selected presets: Off -> Auto -> Cloudflare -> AdGuard -> Quad9
        val candidates = buildList {
            if (QuickTileSettingsManager.isCycleDnsOff(this@AdvancedPrivateDnsTileService)) add("off" to null)
            if (QuickTileSettingsManager.isCycleDnsAuto(this@AdvancedPrivateDnsTileService)) add("opportunistic" to null)
            if (QuickTileSettingsManager.isCycleDnsCloudflare(this@AdvancedPrivateDnsTileService)) add("hostname" to QuickTileSettingsManager.HOSTNAME_CLOUDFLARE)
            if (QuickTileSettingsManager.isCycleDnsAdguard(this@AdvancedPrivateDnsTileService)) add("hostname" to QuickTileSettingsManager.HOSTNAME_ADGUARD)
            if (QuickTileSettingsManager.isCycleDnsQuad9(this@AdvancedPrivateDnsTileService)) add("hostname" to QuickTileSettingsManager.HOSTNAME_QUAD9)
        }

        if (candidates.isEmpty()) {
            Toast.makeText(this, "No DNS cycle states enabled in Settings", Toast.LENGTH_SHORT).show()
            return
        }

        val currentIndex = candidates.indexOfFirst {
            it.first == prevMode && (it.second == null || it.second == prevSpec)
        }
        val nextIndex = if (currentIndex == -1 || currentIndex == candidates.lastIndex) 0 else currentIndex + 1
        val (nextMode, nextSpec) = candidates[nextIndex]

        val ok = QuickTileSettingsManager.setPrivateDnsMode(nextMode, nextSpec)
        if (ok) {
            updateTileState()
            val label = when (nextMode) {
                "off" -> "Off"
                "opportunistic" -> "Automatic"
                else -> nextSpec?.substringBefore('.') ?: "Custom"
            }
            Toast.makeText(this, "Private DNS: $label", Toast.LENGTH_SHORT).show()

            if (QuickTileSettingsManager.isDnsAutoRevert(this)) {
                val delaySec = QuickTileSettingsManager.getDnsRevertDelay(this)
                revertTimer = object : CountDownTimer(delaySec * 1000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        QuickTileSettingsManager.setPrivateDnsMode(prevMode, prevSpec)
                        updateTileState()
                        Toast.makeText(applicationContext, "Private DNS auto-reverted", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
        } else {
            Toast.makeText(this, "Failed to change Private DNS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val mode = QuickTileSettingsManager.getPrivateDnsMode(this)
        val host = QuickTileSettingsManager.getPrivateDnsSpecifier(this)

        val subtitle = when (mode) {
            "off" -> "Off"
            "opportunistic" -> "Automatic"
            else -> host.substringBefore('.').ifEmpty { "Custom" }
        }

        tile.state = if (mode == "off") Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.label = "Private DNS"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = subtitle
        }
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        revertTimer?.cancel()
    }
}

