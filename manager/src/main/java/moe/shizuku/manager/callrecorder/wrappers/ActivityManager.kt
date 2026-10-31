package moe.shizuku.manager.callrecorder.wrappers

import android.annotation.SuppressLint
import android.content.Context
import android.content.IContentProvider
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import moe.shizuku.manager.callrecorder.FakeContext
import java.lang.reflect.Method

/**
 * A wrapper for Android system-level ActivityManagerService.
 */
@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
object ActivityManager {
    private const val TAG = "SCR:ActivityManager"

    private val manager: IInterface by lazy {
        ServiceManager.getService(Context.ACTIVITY_SERVICE, "android.app.IActivityManager")
    }

    private val getContentProviderExternalMethod: Method by lazy {
        manager.javaClass.getMethod(
            "getContentProviderExternal",
            String::class.java,
            Int::class.javaPrimitiveType,
            IBinder::class.java,
            String::class.java
        )
    }

    fun getContentProviderExternal(name: String?, token: IBinder): IContentProvider? {
        return try {
            val providerHolder = getContentProviderExternalMethod.invoke(
                manager,
                name,
                0,
                token,
                FakeContext.PACKAGE_NAME
            ) ?: return null
            val providerField = providerHolder.javaClass.getDeclaredField("provider").apply {
                isAccessible = true
            }
            providerField.get(providerHolder) as? IContentProvider
        } catch (e: ReflectiveOperationException) {
            Log.e(TAG, "Could not invoke getContentProviderExternal", e)
            null
        }
    }

    private val getStartActivityAsUserWithFeatureMethod: Method by lazy {
        val iApplicationThreadClass = Class.forName("android.app.IApplicationThread")
        val profilerInfoClass = Class.forName("android.app.ProfilerInfo")
        manager.javaClass.getMethod(
            "startActivityAsUserWithFeature",
            iApplicationThreadClass,
            String::class.java,
            String::class.java,
            Intent::class.java,
            String::class.java,
            IBinder::class.java,
            String::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            profilerInfoClass,
            Bundle::class.java,
            Int::class.javaPrimitiveType
        )
    }

    fun startActivity(intent: Intent, options: Bundle? = null): Int {
        return try {
            getStartActivityAsUserWithFeatureMethod.invoke(
                manager,
                null,
                FakeContext.PACKAGE_NAME,
                null,
                intent,
                null,
                null,
                null,
                0,
                0,
                null,
                options,
                -2
            ) as Int
        } catch (e: Throwable) {
            Log.e(TAG, "Could not invoke startActivityAsUserWithFeatureMethod", e)
            0
        }
    }

    private val forceStopPackageMethod: Method by lazy {
        manager.javaClass.getMethod(
            "forceStopPackage",
            String::class.java,
            Int::class.javaPrimitiveType
        )
    }

    fun forceStopPackage(packageName: String) {
        try {
            forceStopPackageMethod.invoke(
                manager,
                packageName,
                -2
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Could not invoke forceStopPackage", e)
        }
    }
}
