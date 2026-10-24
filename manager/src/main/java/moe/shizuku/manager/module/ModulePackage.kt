package moe.shizuku.manager.module

import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Why a module zip was refused, in the words the screen shows. */
enum class ModuleInstallProblem {
    /** No `module.prop` at the root, or an id in it that could not be a directory name. */
    NOT_A_MODULE,

    /** An entry named an absolute path or climbed out of the module with `..`. */
    UNSAFE_PATH,

    /** More entries than a module can have. */
    TOO_MANY_ENTRIES,

    /** More bytes than a module may unpack to. */
    TOO_LARGE,

    /** A script bigger than a script can be. */
    SCRIPT_TOO_LARGE
}

class ModuleInstallException(val problem: ModuleInstallProblem) : Exception(problem.name)

/**
 * What installing a module is allowed to do, and the checks that keep it to that.
 *
 * Everything here treats the zip as hostile, because it is: a module is an arbitrary file from
 * GitHub that we unpack into the app's own storage and then run through Shizuku as shell. The
 * limits are the ones the ADB module format settled on - the same numbers the forks that implement
 * it use - and they are all enforced while reading, before anything is written.
 */
object ModulePackage {

    /** The most entries a module zip may have. */
    const val MAX_ENTRIES = 2048

    /** The most a module may unpack to. */
    const val MAX_TOTAL_BYTES = 200L * 1024 * 1024

    /** The most a script inside a module may weigh. */
    const val MAX_SCRIPT_BYTES = 256L * 1024

    /**
     * Whether [name] may be unpacked to.
     *
     * A zip entry is a name the archive chooses, so an entry called `../../shared_prefs/x.xml` or
     * `/data/data/<package>/files/steal` is a write to a place the module was never given. Only
     * relative names are allowed, and only ones that stay inside: a `..` anywhere in the path
     * climbs out, however it is spelled.
     */
    fun isSafeEntry(name: String): Boolean {
        if (name.isEmpty()) return false
        if (name.startsWith("/") || name.startsWith("\\")) return false
        if (name.contains('\u0000')) return false
        // Windows-style absolute names, and drive letters: not paths this ever means to write.
        if (name.length >= 2 && name[1] == ':') return false

        return name.split('/', '\\').none { it == ".." }
    }

    /** Whether a directory entry of this name is a script, which has its own size limit. */
    fun isScript(name: String): Boolean = name.endsWith(".sh", ignoreCase = true)

    /** The name [install] unpacks into before it knows what the module is called. */
    private const val STAGING = ".installing"

    /**
     * Installs the module in [zip] under [root], and answers with it.
     *
     * Two names are involved and neither is trusted: the zip is unpacked into a staging directory
     * whose name is ours, and only once its `module.prop` has named it is that directory renamed to
     * the id - so a refused zip never leaves something that looks installed, and a module can never
     * choose where it lands. A module with that id already installed is replaced, which is what
     * installing a newer zip over an older one means.
     */
    fun install(root: File, zip: InputStream): AdbModule {
        root.mkdirs()

        val staging = File(root, STAGING)
        staging.deleteRecursively()
        staging.mkdirs()

        try {
            val prop = extract(zip, staging)

            val installed = File(root, prop.id)
            installed.deleteRecursively()

            if (!staging.renameTo(installed)) {
                throw ModuleInstallException(ModuleInstallProblem.NOT_A_MODULE)
            }

            return AdbModule(prop, installed)
        } catch (e: Throwable) {
            staging.deleteRecursively()
            throw e
        }
    }

    /**
     * Unpacks [zip] into [dest], and answers with the module it held.
     *
     * Every refusal throws, and the caller is expected to have [dest] thrown away with it.
     */
    private fun extract(zip: InputStream, dest: File): ModuleProp {
        var entries = 0
        var total = 0L
        var propText: String? = null

        ZipInputStream(zip.buffered()).use { stream ->
            while (true) {
                val entry = stream.nextEntry ?: break

                if (!isSafeEntry(entry.name)) {
                    throw ModuleInstallException(ModuleInstallProblem.UNSAFE_PATH)
                }

                if (++entries > MAX_ENTRIES) {
                    throw ModuleInstallException(ModuleInstallProblem.TOO_MANY_ENTRIES)
                }

                if (entry.isDirectory) {
                    File(dest, entry.name).mkdirs()
                    continue
                }

                val target = File(dest, entry.name)
                target.parentFile?.mkdirs()

                // Read it in, counting as we go: the declared size in the entry header is the
                // archive's claim about itself, and this is the one that cannot be lied about.
                val limit = if (isScript(entry.name)) MAX_SCRIPT_BYTES else Long.MAX_VALUE
                var written = 0L
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = stream.read(buffer)
                        if (read <= 0) break

                        written += read
                        total += read

                        if (written > limit) {
                            throw ModuleInstallException(ModuleInstallProblem.SCRIPT_TOO_LARGE)
                        }
                        if (total > MAX_TOTAL_BYTES) {
                            throw ModuleInstallException(ModuleInstallProblem.TOO_LARGE)
                        }

                        output.write(buffer, 0, read)
                    }
                }

                if (entry.name == ModuleProp.FILE_NAME) {
                    propText = target.readText()
                }
            }
        }

        return propText?.let { ModuleProp.parse(it) }
            ?: throw ModuleInstallException(ModuleInstallProblem.NOT_A_MODULE)
    }
}
