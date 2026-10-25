package moe.shizuku.manager.tile

import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.PowerManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class CaffeinateTileService : TileService() {

    companion object {
        private var wakeLock: PowerManager.WakeLock? = null
        private var countDownTimer: CountDownTimer? = null
        private var durationMinutes: Int = 0 // 0 = off, 5, 10, 30, -1 = infinite
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        durationMinutes = when (durationMinutes) {
            0 -> 5
            5 -> 10
            10 -> 30
            30 -> -1
            else -> 0
        }
        applyCaffeinate(durationMinutes)
    }

    private fun applyCaffeinate(minutes: Int) {
        countDownTimer?.cancel()
        countDownTimer = null

        if (minutes == 0) {
            runCatching { wakeLock?.release() }
            wakeLock = null
            updateTile("Off", Tile.STATE_INACTIVE)
            return
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (wakeLock == null) {
            @Suppress("DEPRECATION")
            wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "OmniShizuku:CaffeinateLock"
            ).apply { setReferenceCounted(false) }
        }

        if (minutes == -1) {
            wakeLock?.acquire()
            updateTile("Infinite", Tile.STATE_ACTIVE)
        } else {
            val millis = minutes * 60 * 1000L
            wakeLock?.acquire(millis + 1000L)
            updateTile("${minutes}m", Tile.STATE_ACTIVE)

            countDownTimer = object : CountDownTimer(millis, 60000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val remainingMins = (millisUntilFinished / 60000L).toInt() + 1
                    updateTile("${remainingMins}m", Tile.STATE_ACTIVE)
                }

                override fun onFinish() {
                    durationMinutes = 0
                    runCatching { wakeLock?.release() }
                    wakeLock = null
                    updateTile("Off", Tile.STATE_INACTIVE)
                }
            }.start()
        }
    }

    private fun updateTile(subtitle: String = if (durationMinutes == 0) "Off" else "${durationMinutes}m", state: Int = if (durationMinutes == 0) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE) {
        val tile = qsTile ?: return
        tile.state = state
        tile.label = "Caffeinate"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = subtitle
        }
        tile.updateTile()
    }
}
