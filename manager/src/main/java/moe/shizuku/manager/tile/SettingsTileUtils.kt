package moe.shizuku.manager.tile

import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService

internal object SettingsTileUtils {
    fun open(tile: TileService, primary: () -> Intent, vararg fallbacks: () -> Intent) {
        val intents = listOf(primary) + fallbacks.toList()
        for (factory in intents) {
            val i = runCatching { factory() }.getOrNull() ?: continue
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (ActionTileUtils.collapseAndStart(tile, i)) return
        }
        ActionTileUtils.collapseAndStart(tile, Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
