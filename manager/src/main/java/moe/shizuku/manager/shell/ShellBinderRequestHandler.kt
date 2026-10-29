package moe.shizuku.manager.shell

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import moe.shizuku.manager.utils.Logger.LOGGER
import rikka.shizuku.Shizuku

object ShellBinderRequestHandler {

    fun handleRequest(context: Context, intent: Intent): Boolean {
        if (intent.action != "rikka.shizuku.intent.action.REQUEST_BINDER") {
            return false
        }

        val binder = intent.getBundleExtra("data")?.getBinder("binder") ?: return false
        val shizukuBinder = Shizuku.getBinder()

        // Defensive: on Realme/OEM devices the Shizuku binder can be null if the service
        // was killed by aggressive power management or has not finished binding yet.
        // Never dereference a null binder - bail out gracefully instead of NPE.
        if (shizukuBinder == null) {
            LOGGER.w("Binder not received or Shizuku service not running")
            return false
        }

        val data = Parcel.obtain()
        return try {
            data.writeStrongBinder(shizukuBinder)
            data.writeString(context.applicationInfo.sourceDir)
            binder.transact(1, data, null, IBinder.FLAG_ONEWAY)
            true
        } catch (e: Throwable) {
            LOGGER.w(e, "ShellBinderRequestHandler: failed to relay binder")
            false
        } finally {
            data.recycle()
        }
    }
}
