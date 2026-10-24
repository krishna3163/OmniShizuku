package eu.darken.porter.sdk.extras

import android.os.IBinder
import android.util.Log
import androidx.annotation.VisibleForTesting
import java.util.concurrent.ConcurrentHashMap

/**
 * The system service binders of this process, looked up locally. Nothing here reaches the server.
 *
 * example:
 * ```
 * val binder = PorterSystemServices.getSystemService("package") ?: return
 * val pm = IPackageManager.Stub.asInterface(connection.wrap(binder))
 * ```
 *
 * The lookup reflects into a platform class outside the SDK. An Android release that refuses it
 * leaves every call here throwing [IllegalStateException].
 */
public object PorterSystemServices {

    private const val TAG = "PorterSystemServices"

    private val cache = ConcurrentHashMap<String, IBinder>()

    @VisibleForTesting
    internal fun interface ServiceLookup {
        @Throws(ReflectiveOperationException::class)
        fun get(name: String): IBinder?
    }

    @VisibleForTesting
    internal var lookup: ServiceLookup? = reflectiveLookup()

    @VisibleForTesting
    internal fun reflectiveLookup(): ServiceLookup? = try {
        val serviceManager = Class.forName("android.os.ServiceManager")
        val getService = serviceManager.getMethod("getService", String::class.java)
        ServiceLookup { name -> getService.invoke(null, name) as IBinder? }
    } catch (e: ClassNotFoundException) {
        Log.w(TAG, Log.getStackTraceString(e))
        null
    } catch (e: NoSuchMethodException) {
        Log.w(TAG, Log.getStackTraceString(e))
        null
    }

    /**
     * @param name the name of the service, such as `"package"` for
     * `android.content.pm.IPackageManager`
     * @return the binder, or null if no service goes by that name
     * @throws IllegalStateException if the lookup itself could not be performed, which says nothing
     * about whether the service exists
     */
    public fun getSystemService(name: String): IBinder? {
        val current = lookup ?: throw IllegalStateException("android.os.ServiceManager.getService is unreachable")
        cache[name]?.let { return it }

        val binder = try {
            current.get(name)
        } catch (e: ReflectiveOperationException) {
            throw IllegalStateException("Failed to look up the system service '$name'", e)
        }
        // ConcurrentHashMap forbids a null value, and a missing service is worth asking about again.
        if (binder != null) cache[name] = binder
        return binder
    }

    @VisibleForTesting
    internal fun clearCacheForTest() {
        cache.clear()
    }
}
