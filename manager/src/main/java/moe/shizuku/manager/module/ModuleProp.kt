package moe.shizuku.manager.module

/**
 * What a module says about itself, read from the `module.prop` at the root of its zip.
 *
 * The format is the one the ADB module format uses across the forks that implement it, so a module
 * written for one of those installs here unchanged: `id`, `name` and `versionCode` are what the
 * manager keys on, `banner` and `webui` name files inside the module, and `action` overrides the
 * script the module card runs.
 *
 * Parsing is deliberately not [java.util.Properties]: a properties file treats `:`, `!` and
 * backslashes as syntax and unescapes what it reads, and a description holding any of those would
 * come out different from what the author wrote.
 */
data class ModuleProp(
    val id: String,
    val name: String,
    val version: String?,
    val versionCode: Long?,
    val author: String?,
    val description: String?,
    /** The banner file the module named, or null to fall back to the conventional names. */
    val banner: String?,
    /** The WebUI directory the module named, or null to fall back to the conventional names. */
    val webUi: String?,
    /** The script an action runs, relative to the module directory. Defaults to `action.sh`. */
    val action: String,
    /** Whether the module asks for the WebUI's shell bridge; a module without it gets no bridge. */
    val usesShellBridge: Boolean,
) {
    companion object {
        const val FILE_NAME = "module.prop"

        const val DEFAULT_ACTION = "action.sh"
        const val SERVICE_SCRIPT = "service.sh"

        /**
         * What an id may be: a letter, then letters, digits, dots, underscores or hyphens. The one
         * place a module's id is used as a path, so it is the one place that has to be strict.
         */
        val ID_PATTERN = Regex("^[A-Za-z][A-Za-z0-9._-]{1,63}$")

        /** Banner files tried in this order when the module does not name one. */
        val BANNER_NAMES = listOf("banner.png", "banner.jpg", "banner.jpeg", "banner.webp")

        /** WebUI directories tried in this order when the module does not name one. */
        val WEB_UI_NAMES = listOf("webroot", "webui", "web")

        val WEB_UI_ENTRY = "index.html"

        /**
         * [text] as a module, or null when it is not one.
         *
         * Null covers both a missing id and one that could not be a directory name: a module zip is
         * an arbitrary file from the internet, and the id is what its contents are written under.
         */
        fun parse(text: String): ModuleProp? {
            val values = linkedMapOf<String, String>()
            text.lineSequence().forEach { raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) return@forEach

                val separator = line.indexOf('=')
                if (separator <= 0) return@forEach

                val key = line.substring(0, separator).trim().lowercase()
                values[key] = line.substring(separator + 1).trim()
            }

            val id = values["id"].orEmpty()
            val name = values["name"].orEmpty()
            if (!ID_PATTERN.matches(id) || name.isEmpty()) return null

            return ModuleProp(
                id = id,
                name = name,
                version = values["version"],
                versionCode = values["versioncode"]?.toLongOrNull(),
                author = values["author"],
                description = values["description"],
                banner = values["banner"],
                webUi = values["webui"],
                action = values["action"] ?: DEFAULT_ACTION,
                usesShellBridge = values["usesshellbridge"]?.lowercase() == "true"
            )
        }
    }
}
