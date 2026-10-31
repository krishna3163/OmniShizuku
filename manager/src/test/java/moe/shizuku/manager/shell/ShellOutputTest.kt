package moe.shizuku.manager.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellOutputTest {

    @Test
    fun returnsEmptyRangesWhenInputOrQueryIsEmpty() {
        assertTrue(ShellOutput.matchRangesOf("", "test").isEmpty())
        assertTrue(ShellOutput.matchRangesOf("some text", "").isEmpty())
        assertTrue(ShellOutput.matchRangesOf("", "").isEmpty())
    }

    @Test
    fun findsNonOverlappingCaseInsensitiveMatchRanges() {
        val ranges = ShellOutput.matchRangesOf("User 0: id=com.example.user_app", "user")
        assertEquals(2, ranges.size)
        assertEquals(0 until 4, ranges[0]) // "User"
        assertEquals(23 until 27, ranges[1]) // "user"

        val repeatingRanges = ShellOutput.matchRangesOf("aaaa", "aa")
        assertEquals(2, repeatingRanges.size)
        assertEquals(0 until 2, repeatingRanges[0])
        assertEquals(2 until 4, repeatingRanges[1])
    }

    @Test
    fun findsMatchingLinesByIndices() {
        val lines = listOf(
            "UID 1000: system",
            "UID 2000: shell",
            "UID 10150: com.google.android.gms",
            "UID 10151: moe.shizuku.manager"
        )
        val matching = ShellOutput.matchingLines(lines, "shizuku")
        assertEquals(listOf(3), matching)

        val uidMatches = ShellOutput.matchingLines(lines, "UID")
        assertEquals(listOf(0, 1, 2, 3), uidMatches)

        val noMatches = ShellOutput.matchingLines(lines, "nonexistent")
        assertTrue(noMatches.isEmpty())

        assertTrue(ShellOutput.matchingLines(lines, "").isEmpty())
    }

    @Test
    fun calculatesTotalMatchCountAcrossLines() {
        val lines = listOf(
            "Service: shizuku_server",
            "Active binder for shizuku client",
            "shizuku-ready"
        )
        assertEquals(3, ShellOutput.matchCount(lines, "shizuku"))
        assertEquals(0, ShellOutput.matchCount(lines, "absent"))
    }
}
