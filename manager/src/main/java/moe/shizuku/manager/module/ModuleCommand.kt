package moe.shizuku.manager.module

import java.io.File

/** How much a module is allowed to do, as the user set it. */
enum class ModuleAccessMode(val value: String) {
    /** Actions only: services are refused, whatever else is switched on. */
    SAFE("safe"),

    /** Services too, once the user also allows background actions. */
    FULL("full");

    companion object {
        fun fromValue(value: String?): ModuleAccessMode =
            entries.firstOrNull { it.value == value } ?: SAFE
    }
}

/** A single quote for the shell, so a path or a value with a space in it survives intact. */
fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

/**
 * The command a module's script is run as.
 *
 * The contract is the ADB module format's, and it is written into this one string rather than passed
 * as a process environment because Shizuku's process creation takes a single command line: a script
 * that sets its own environment is the same thing, and `MODDIR` is what tells a module where it
 * lives, so that none of them have to guess at a path.
 *
 * `ASH_STANDALONE` is the one that is not obvious: modules are written for Magisk's environment,
 * where `/system/bin/sh` is busybox ash, and a module that says `ash -c` or uses a busybox builtin
 * wants that shell to be standalone rather than to look for its own files somewhere it will not
 * find them.
 */
fun moduleScriptCommand(
    module: AdbModule,
    script: File,
    mode: ModuleAccessMode,
    trusted: Boolean,
    background: Boolean,
): String {
    val directory = module.directory.absolutePath

    return buildString {
        append("cd ").append(shellQuote(directory)).append(" && ")
        append("MODDIR=").append(shellQuote(directory)).append(' ')
        append("ASH_STANDALONE=1 ")
        append("SHIZUKU_MODULE_ID=").append(shellQuote(module.id)).append(' ')
        append("SHIZUKU_MODULE_MODE=").append(shellQuote(mode.value)).append(' ')
        append("SHIZUKU_MODULE_TRUSTED=").append(if (trusted) "1" else "0").append(' ')
        append("SHIZUKU_MODULE_BACKGROUND=").append(if (background) "1" else "0").append(' ')
        append("sh ").append(shellQuote(script.absolutePath))
    }
}

/**
 * The limits a module script is run under.
 *
 * A script that never ends would hold a Shizuku process for as long as the phone is on, and one
 * that prints forever would fill the manager's memory with a log nobody can read, so both ends are
 * capped. The exit code is not invented here: `124` is what `timeout` returns, and modules in this
 * format already read it as "you ran out of time".
 */
object ModuleRunLimits {
    const val SCRIPT_TIMEOUT_SECONDS = 120
    const val TIMEOUT_EXIT_CODE = 124
    const val MAX_OUTPUT_BYTES = 64 * 1024

    /**
     * What a page may ask for with `timeoutSeconds`, and the range the format clamps it to.
     *
     * A page is the one place a module can ask for a shorter or longer clock than a script gets,
     * so the range is the format's rather than this app's: less than a second is not a command and
     * ten minutes is the longest anything is meant to take.
     */
    const val MIN_WEB_UI_TIMEOUT_SECONDS = 1
    const val MAX_WEB_UI_TIMEOUT_SECONDS = 600
}
