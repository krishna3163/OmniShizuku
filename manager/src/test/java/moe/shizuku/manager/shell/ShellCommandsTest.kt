package moe.shizuku.manager.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellCommandsTest {

    @Test
    fun extractsVariablesFromPlaceholders() {
        val vars = ShellCommands.variablesOf("pm grant <package> <permission>")
        assertEquals(listOf("package", "permission"), vars)

        val multipleVars = ShellCommands.variablesOf("input tap <x> <y>")
        assertEquals(listOf("x", "y"), multipleVars)

        val duplicates = ShellCommands.variablesOf("echo <name> and <name>")
        assertEquals(listOf("name"), duplicates)

        val noVars = ShellCommands.variablesOf("getprop ro.build.version.release")
        assertTrue(noVars.isEmpty())
    }

    @Test
    fun identifiesPackageVariables() {
        assertTrue(ShellCommands.isPackageVariable("package"))
        assertTrue(ShellCommands.isPackageVariable("packages"))
        assertTrue(ShellCommands.isPackageVariable("pkg"))
        assertTrue(ShellCommands.isPackageVariable("app"))
        assertTrue(ShellCommands.isPackageVariable("PACKAGE"))
        assertTrue(ShellCommands.isPackageVariable("App"))

        assertFalse(ShellCommands.isPackageVariable("permission"))
        assertFalse(ShellCommands.isPackageVariable("user"))
        assertFalse(ShellCommands.isPackageVariable("path"))
    }

    @Test
    fun fillsCommandPlaceholdersWithValues() {
        val command = "pm grant <package> <permission>"
        val filled = ShellCommands.filled(
            command,
            mapOf("package" to "com.example.app", "permission" to "android.permission.RECORD_AUDIO")
        )
        assertEquals("pm grant com.example.app android.permission.RECORD_AUDIO", filled)

        val partial = ShellCommands.filled(command, mapOf("package" to "com.example.app"))
        assertEquals("pm grant com.example.app <permission>", partial)

        val emptyValues = ShellCommands.filled(command, mapOf("package" to "   "))
        assertEquals("pm grant <package> <permission>", emptyValues)
    }

    @Test
    fun searchesCommandsByTextDescriptionOrTags() {
        val all = ShellCommands.search("")
        assertEquals(ShellCommands.LIBRARY.size, all.size)

        val packageResults = ShellCommands.search("package")
        assertTrue(packageResults.isNotEmpty())
        assertTrue(packageResults.all { entry ->
            entry.command.contains("package", ignoreCase = true) ||
                entry.description.contains("package", ignoreCase = true) ||
                entry.tags.any { it.contains("package", ignoreCase = true) }
        })

        val nonExistent = ShellCommands.search("xyz_totally_unmatched_query_123")
        assertTrue(nonExistent.isEmpty())
    }

    @Test
    fun libraryCommandsAreValidAndWellFormed() {
        assertTrue(ShellCommands.LIBRARY.isNotEmpty())
        for (cmd in ShellCommands.LIBRARY) {
            assertTrue("Command string must not be blank: ${cmd.command}", cmd.command.isNotBlank())
            assertTrue("Description must not be blank for: ${cmd.command}", cmd.description.isNotBlank())
            assertTrue("Tags must not be empty for: ${cmd.command}", cmd.tags.isNotEmpty())
        }
    }
}

