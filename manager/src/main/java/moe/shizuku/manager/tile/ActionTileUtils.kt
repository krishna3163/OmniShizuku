package moe.shizuku.manager.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.widget.Toast
import moe.shizuku.manager.R

internal object ActionTileUtils {
    fun collapseAndStart(tile: TileService, intent: Intent): Boolean {
        return try {
            val resolved = intent.resolveActivity(tile.packageManager)
            if (resolved == null) {
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pi = PendingIntent.getActivity(
                    tile,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                tile.startActivityAndCollapse(pi)
            } else {
                @Suppress("DEPRECATION")
                tile.startActivityAndCollapse(intent)
            }
            true
        } catch (t: Throwable) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                tile.startActivity(intent)
                true
            } catch (t2: Throwable) {
                Toast.makeText(tile, R.string.tile_action_not_found, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}

