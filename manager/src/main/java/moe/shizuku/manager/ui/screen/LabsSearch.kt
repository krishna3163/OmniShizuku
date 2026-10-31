package moe.shizuku.manager.ui.screen

import java.util.Locale

/**
 * Search text for one Labs entry. The normalized text is built once with the item and reused as
 * the user types, leaving each query to one normalization and a short list of substring checks.
 */
internal class LabsSearchItem<T>(
    val value: T,
    name: String,
    description: String,
    aliases: String,
    category: String
) {
    private val searchableText = listOf(name, description, aliases, category)
        .joinToString(" ")
        .lowercase(Locale.ROOT)

    internal fun matches(normalizedQuery: String): Boolean =
        searchableText.contains(normalizedQuery)
}

internal fun <T> filterLabsSearchItems(
    items: List<LabsSearchItem<T>>,
    query: String
): List<LabsSearchItem<T>> {
    val normalizedQuery = query.trim().lowercase(Locale.ROOT)
    return if (normalizedQuery.isEmpty()) items
    else items.filter { it.matches(normalizedQuery) }
}
