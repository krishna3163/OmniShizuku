package moe.shizuku.manager.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class LabsSearchTest {

    private data class Entry(val id: String)

    private val items = listOf(
        LabsSearchItem(
            Entry("firewall"),
            name = "Firewall",
            description = "Block app network access",
            aliases = "internet deny",
            category = "App management"
        ),
        LabsSearchItem(
            Entry("private-dns"),
            name = "Private DNS",
            description = "Configure domain name servers",
            aliases = "dns resolver",
            category = "System controls"
        ),
        LabsSearchItem(
            Entry("terminal"),
            name = "Terminal",
            description = "Run commands",
            aliases = "shell command line",
            category = "Utilities"
        )
    )

    @Test
    fun matchesNameDescriptionAliasesAndCategoryWithoutCaseSensitivity() {
        assertEquals(listOf("firewall"), filterLabsSearchItems(items, "FIRE").map { it.value.id })
        assertEquals(listOf("firewall"), filterLabsSearchItems(items, "network access").map { it.value.id })
        assertEquals(listOf("firewall"), filterLabsSearchItems(items, "internet").map { it.value.id })
        assertEquals(listOf("private-dns"), filterLabsSearchItems(items, "SYSTEM CONTROLS").map { it.value.id })
    }

    @Test
    fun blankQueryReturnsAllItemsAndNoMatchReturnsEmptyList() {
        assertEquals(items, filterLabsSearchItems(items, "  "))
        assertEquals(emptyList<Entry>(), filterLabsSearchItems(items, "not a feature").map { it.value })
    }
}
