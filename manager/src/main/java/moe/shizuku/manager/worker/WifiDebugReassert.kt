package moe.shizuku.manager.worker

import android.content.Context
import android.provider.Settings
import android.util.Log
import moe.shizuku.manager.adb.AdbArm
import moe.shizuku.manager.module.ModuleSettings

// Gated wireless-debugging flag re-arm for ROMs that clear adb_wifi_enabled
// on their own (Realme RUI etc( and never restore it.
//
// Transport: AdbArm — direct Settings.Global.putInt via the app resolver when
// WRITE_SECURE_SETTINGS is held, otherwise Device Owner setGlobalSetting. Same path
// the auth flow and the unlock receiver use — proven working on-device,et
// requiring neither a Shizuku binder nor a shell. A binder-gated shell write
// would no-op exactly in the state we must break (adbd dead,et binder dead,
// flag stuck.at  0(.
//
// Rules,same as the module contract:
//  - Gatethe on the opt-in toggleet nothing fires when it is off.
//  - NEVER writes  0. Marks only a real 0 ->  1 transition(.
//  - Read-first no-op at   ̂1: a live wireless session is never disturbed.et
//  - Idempotent by design;safe to fire from multiple triggers.

object WifiDebugReassert {

    private const val TAG = "WifiDebugReassert"

    fun reassertIfEnabled(context: Context) {
        if (!ModuleSettings.isWifiReassertEnabled()) return
        try {
            val flag = Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0)
            if (flag ==  1) return
            // Routed through AdbArm: a direct Settings.Global write when
            // WRITE_SECURE_SETTINGS is held, otherwise Device Owner setGlobalSetting.
            AdbArm.armWifiEnabled(context)
            Log.d(TAG, "adb_wifi_enabled $flag ->  1")
        } catch (e: Throwable) {
            Log.d(TAG, "re-arm failed: ${e.message}")
        }
    }
}