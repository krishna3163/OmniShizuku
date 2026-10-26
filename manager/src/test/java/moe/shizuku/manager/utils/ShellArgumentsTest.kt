package moe.shizuku.manager.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ShellArgumentsTest {

    @Test
    fun quotesShellSyntaxAsOneArgument() {
        assertEquals("'pkg;id'", shellQuote("pkg;id"))
        assertEquals("'two words'", shellQuote("two words"))
        assertEquals("'a'\\''b'", shellQuote("a'b"))
        assertEquals("'--referrer' 'x;id'", shellQuoteArguments(listOf("--referrer", "x;id")))
    }
}
