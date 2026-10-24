package eu.darken.porter.porsh

import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class EnvPolicyTest {

    @Test
    fun underRoot_theEnvironmentIsKept() {
        val env = arrayOf(TERMUX_PATH)

        assertSame(env, EnvPolicy(true).resolve(env))
    }

    @Test
    fun underAdb_theEnvironmentIsDropped() {
        val env = arrayOf(TERMUX_PATH)

        assertNull(EnvPolicy(false).resolve(env))
    }

    @Test
    fun porshPreserveEnvZero_overridesTheRootDefault() {
        val env = arrayOf(TERMUX_PATH, "PORSH_PRESERVE_ENV=0")

        assertNull(EnvPolicy(true).resolve(env))
    }

    @Test
    fun porshPreserveEnvOne_overridesTheAdbDefault() {
        val env = arrayOf(TERMUX_PATH, "PORSH_PRESERVE_ENV=1")

        assertSame(env, EnvPolicy(false).resolve(env))
    }

    @Test
    fun rishPreserveEnvOne_isStillHonoured() {
        val env = arrayOf(TERMUX_PATH, "RISH_PRESERVE_ENV=1")

        assertSame(env, EnvPolicy(false).resolve(env))
    }

    @Test
    fun rishPreserveEnvZero_isStillHonoured() {
        val env = arrayOf(TERMUX_PATH, "RISH_PRESERVE_ENV=0")

        assertNull(EnvPolicy(true).resolve(env))
    }

    @Test
    fun porshPreserveEnv_winsOverRishPreserveEnv() {
        val rishFirst = arrayOf("RISH_PRESERVE_ENV=0", "PORSH_PRESERVE_ENV=1")
        assertSame(rishFirst, EnvPolicy(false).resolve(rishFirst))

        val porshFirst = arrayOf("PORSH_PRESERVE_ENV=0", "RISH_PRESERVE_ENV=1")
        assertNull(EnvPolicy(true).resolve(porshFirst))
    }

    private companion object {
        const val TERMUX_PATH = "PATH=/data/data/com.termux/files/usr/bin"
    }
}
