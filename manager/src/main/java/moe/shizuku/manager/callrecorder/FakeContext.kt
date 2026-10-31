package moe.shizuku.manager.callrecorder

import android.annotation.SuppressLint
import android.content.AttributionSource
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi

/**
 * Spoofs system identity as `com.android.shell` (UID 2000).
 * Required for modern Android versions (12+) so AudioRecord permission checks
 * identify the caller as the privileged shell process.
 */
@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
class FakeContext private constructor() : ContextWrapper(ShellWorkarounds.getSystemContext()) {

    companion object {
        const val PACKAGE_NAME = "com.android.shell"
        val instance: FakeContext by lazy { FakeContext() }
        fun get(): FakeContext = instance
    }

    override fun getPackageName(): String = PACKAGE_NAME
    override fun getOpPackageName(): String = PACKAGE_NAME

    @RequiresApi(Build.VERSION_CODES.S)
    override fun getAttributionSource(): AttributionSource {
        return AttributionSource.Builder(Process.SHELL_UID)
            .setPackageName(PACKAGE_NAME)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun getDeviceId(): Int {
        return Context.DEVICE_ID_DEFAULT
    }

    override fun getApplicationContext(): Context = this
    override fun createPackageContext(packageName: String, flags: Int): Context = this
}
