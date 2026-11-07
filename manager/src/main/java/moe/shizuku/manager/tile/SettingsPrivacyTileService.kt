package moe.shizuku.manager.tile
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import moe.shizuku.manager.R
@RequiresApi(Build.VERSION_CODES.N)
class SettingsPrivacyTileService : TileService() {
    override fun onStartListening() { super.onStartListening(); val t=qsTile?:return; t.state=Tile.STATE_INACTIVE; t.label=getString(R.string.tile_settings_privacy); if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) t.subtitle=getString(R.string.tile_settings_privacy_subtitle); t.updateTile() }
    override fun onClick() { super.onClick(); SettingsTileUtils.open(this, { Intent(Settings.ACTION_PRIVACY_SETTINGS) }, { Intent(Settings.ACTION_SECURITY_SETTINGS) }) }
}
