package eu.darken.porter.sdk.extras

/** How a command run by `exec` ended, and everything it wrote. */
public class PorterShellResult internal constructor(
    public val exitCode: Int,
    /** Its standard output, as UTF-8. */
    public val output: String,
    /** Its standard error, as UTF-8. */
    public val errors: String,
) {
    override fun toString(): String = "PorterShellResult(exitCode=$exitCode, output=${output.length} chars, errors=${errors.length} chars)"
}
