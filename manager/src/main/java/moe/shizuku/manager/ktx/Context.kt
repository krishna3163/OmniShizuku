package moe.shizuku.manager.ktx

import android.content.Context
import android.os.Build
import android.os.UserManager
import moe.shizuku.manager.ShizukuApplication

val Context.application: ShizukuApplication
    get() {
        return applicationContext as ShizukuApplication
    }

fun Context.createDeviceProtectedStorageContextCompat(): Context {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        createDeviceProtectedStorageContext()
    } else {
        this
    }
}

fun Context.createDeviceProtectedStorageContextCompatWhenLocked(): Context {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getSystemService(UserManager::class.java)?.isUserUnlocked != true) {
        createDeviceProtectedStorageContext()
    } else {
        this
    }
}

/**
 * Whether credential-encrypted storage is readable yet. False from boot until the first unlock,
 * which is when the app can run (it is direct-boot aware) and WorkManager's database cannot be read.
 */
fun Context.isUserUnlockedCompat(): Boolean =
    getSystemService(UserManager::class.java)?.isUserUnlocked != false
