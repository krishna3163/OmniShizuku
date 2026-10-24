package moe.shizuku.manager.module

import kotlinx.serialization.Serializable

/**
 * The manifest a module points at to offer a newer version of itself, in the shape the format
 * defines.
 *
 * A module that wants to be updatable ships an `update.json` whose contents are the URL of this
 * manifest; the manager fetches it, and if it names a version code above the installed one, the
 * card says so and offers the install. Nothing here is trusted: the zip that comes out of it goes
 * through the same unpacking and the same limits as one installed by hand.
 */
@Serializable
data class ModuleUpdate(
    val version: String = "",
    val versionCode: Long = 0,
    val zipUrl: String = "",
    val changelog: String = "",
) {
    /**
     * Whether this is newer than [module].
     *
     * The version code is what decides, the way the format says it does; a manifest without a zip
     * to install is not an update however new it claims to be.
     */
    fun isNewerThan(module: AdbModule): Boolean =
        zipUrl.isNotEmpty() && versionCode > (module.prop.versionCode ?: 0L)
}
