package moe.shizuku.manager.utils

/** Quotes one value as a POSIX shell word. */
internal fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

internal fun shellQuoteArguments(arguments: List<String>): String =
    arguments.joinToString(" ", transform = ::shellQuote)
