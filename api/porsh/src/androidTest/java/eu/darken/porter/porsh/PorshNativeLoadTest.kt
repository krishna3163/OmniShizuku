package eu.darken.porter.porsh

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Assembling the module never loads the shared object, so a JNI class string that no
 * longer matches its Java class still compiles and links. JNI_OnLoad returns JNI_ERR when
 * either registerNatives fails, and both do a FindClass on their renamed class first, so a
 * load that succeeds proves both strings resolve.
 */
@RunWith(AndroidJUnit4::class)
class PorshNativeLoadTest {

    @Test
    fun loadingTheLibrary_registersTheRenamedClasses() {
        System.loadLibrary("porsh")

        // Touches no binder; only isatty on this process's own descriptors.
        PorshTerminal.prepare()
    }
}
