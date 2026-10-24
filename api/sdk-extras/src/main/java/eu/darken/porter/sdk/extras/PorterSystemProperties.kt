@file:JvmName("PorterSystemProperties")

package eu.darken.porter.sdk.extras

import eu.darken.porter.sdk.PorterConnection

/*
 * Typed reads of the system properties, as the server sees them, parsed the way
 * android.os.SystemProperties parses them. Every call reaches the server, and a value that is
 * missing or does not parse reads as the default.
 */

/** Decimal, `0x` hexadecimal and `0` octal, as `Integer.decode` reads them. */
public suspend fun PorterConnection.getSystemPropertyInt(key: String, default: Int): Int =
    getSystemProperty(key, default.toString())?.let { runCatching { Integer.decode(it) }.getOrNull() } ?: default

/** Decimal, `0x` hexadecimal and `0` octal, as `Long.decode` reads them. */
public suspend fun PorterConnection.getSystemPropertyLong(key: String, default: Long): Long =
    getSystemProperty(key, default.toString())?.let { runCatching { java.lang.Long.decode(it) }.getOrNull() } ?: default

/** `1`, `y`, `yes`, `on` and `true` read as true; `0`, `n`, `no`, `off` and `false` as false. */
public suspend fun PorterConnection.getSystemPropertyBoolean(key: String, default: Boolean): Boolean =
    when (getSystemProperty(key, default.toString())) {
        "1", "y", "yes", "on", "true" -> true
        "0", "n", "no", "off", "false" -> false
        else -> default
    }
