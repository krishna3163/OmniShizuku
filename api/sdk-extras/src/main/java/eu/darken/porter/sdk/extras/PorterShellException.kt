package eu.darken.porter.sdk.extras

/**
 * The shell service could not run a command: it could not start one at all, as in a working
 * directory that does not exist, or it stopped answering while the command ran. A command that is
 * not found is not this: it exits with 127, as in a shell. The SDK's own `PorterException`s pass
 * through unchanged, such as the one for a permission that was not granted.
 */
public class PorterShellException internal constructor(message: String, cause: Throwable?) : RuntimeException(message, cause)
