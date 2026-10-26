package moe.shizuku.manager.utils

/**
 * Validates package names before they are inserted into a privileged shell command.
 *
 * The package grammar and 255-character bound are adapted from ShizukuPlus
 * `InputValidationUtils.isValidPackageName` (thejaustin, Apache-2.0).
 */
internal object PackageNameValidator {

    private val pattern = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$")

    fun isValid(value: String): Boolean =
        value.isNotEmpty() && value.length <= 255 && pattern.matches(value)
}
