package moe.shizuku.manager.callrecorder.wrappers

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.IInterface
import java.lang.reflect.Method

/**
 * A wrapper for the Android ServiceManager, allowing access to system services via reflection.
 */
object ServiceManager {

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private val getServiceMethod: Method = try {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        serviceManagerClass.getDeclaredMethod("getService", String::class.java).apply {
            isAccessible = true
        }
    } catch (e: Throwable) {
        throw AssertionError(e)
    }

    fun getService(service: String, type: String): IInterface {
        return try {
            val serviceBinder = getServiceMethod.invoke(null, service) as IBinder
            val serviceInterfaceClass = Class.forName("$type\$Stub")
            val asInterfaceMethod = serviceInterfaceClass.getMethod("asInterface", IBinder::class.java)
            asInterfaceMethod.invoke(null, serviceBinder) as IInterface
        } catch (e: Throwable) {
            throw AssertionError(e)
        }
    }
}
