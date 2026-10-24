package eu.darken.porter.porsh

/**
 * Decides whether the client's environment is handed to the forked shell.
 *
 * Termux points PATH and LD_PRELOAD at its own internal directories, which a shell
 * running as adb is not permitted to read. So under adb the client has to opt in, and
 * under root the environment is kept unless the client opts out.
 *
 * Both spellings are accepted because this server also serves Shizuku's own rish
 * client through the compatibility companion, and that one sets RISH_PRESERVE_ENV.
 */
internal class EnvPolicy(private val rootDefault: Boolean) {

    /**
     * The environment to pass on, or null when it has to be dropped.
     */
    fun resolve(env: Array<String>): Array<String>? = if (allows(env)) env else null

    private fun allows(env: Array<String>): Boolean {
        var porsh: Boolean? = null
        var rish: Boolean? = null

        for (e in env) {
            if (porsh == null && PORSH_ON == e) {
                porsh = true
            } else if (porsh == null && PORSH_OFF == e) {
                porsh = false
            } else if (rish == null && RISH_ON == e) {
                rish = true
            } else if (rish == null && RISH_OFF == e) {
                rish = false
            }
        }

        if (porsh != null) return porsh
        if (rish != null) return rish
        return rootDefault
    }

    private companion object {
        const val PORSH_ON = "PORSH_PRESERVE_ENV=1"
        const val PORSH_OFF = "PORSH_PRESERVE_ENV=0"
        const val RISH_ON = "RISH_PRESERVE_ENV=1"
        const val RISH_OFF = "RISH_PRESERVE_ENV=0"
    }
}
