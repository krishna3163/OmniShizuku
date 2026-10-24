package eu.darken.porter.porsh

import android.annotation.SuppressLint
import android.system.ErrnoException
import android.system.Os
import java.io.FileDescriptor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@SuppressLint("DiscouragedPrivateApi")
internal object FileDescriptors {

    private val getInt: Method? = try {
        FileDescriptor::class.java.getDeclaredMethod("getInt$").also { it.isAccessible = true }
    } catch (ignored: ReflectiveOperationException) {
        null
    }

    fun getFd(fileDescriptor: FileDescriptor?): Int = try {
        getInt!!.invoke(fileDescriptor) as Int
    } catch (ignored: IllegalAccessException) {
        -1
    } catch (ignored: InvocationTargetException) {
        -1
    }

    fun closeSilently(fileDescriptor: FileDescriptor?) {
        if (fileDescriptor == null) return

        try {
            Os.close(fileDescriptor)
        } catch (ignored: ErrnoException) {
        }
    }
}
