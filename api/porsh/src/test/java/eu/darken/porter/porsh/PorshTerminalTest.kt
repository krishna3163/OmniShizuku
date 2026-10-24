package eu.darken.porter.porsh

import eu.darken.porter.porsh.PorshConstants.ATTY_ERR
import eu.darken.porter.porsh.PorshConstants.ATTY_IN
import eu.darken.porter.porsh.PorshConstants.ATTY_OUT
import org.junit.Assert.assertEquals
import org.junit.Test

class PorshTerminalTest {

    @Test
    fun ttyStderrWithPipedStdout_sendsStderrAsAPipe() {
        assertEquals(ATTY_IN, route(ATTY_IN or ATTY_ERR))
        assertEquals(0, route(ATTY_ERR))
    }

    @Test
    fun ttyStdout_keepsEveryBit() {
        assertEquals(ATTY_IN or ATTY_OUT or ATTY_ERR, route(ATTY_IN or ATTY_OUT or ATTY_ERR))
        assertEquals(ATTY_OUT or ATTY_ERR, route(ATTY_OUT or ATTY_ERR))
        assertEquals(ATTY_OUT, route(ATTY_OUT))
    }

    @Test
    fun noTtyStderr_isUnchanged() {
        assertEquals(ATTY_IN, route(ATTY_IN))
        assertEquals(0, route(0))
    }

    private fun route(tty: Int): Int = PorshTerminal.routable(tty.toByte()).toInt()
}
