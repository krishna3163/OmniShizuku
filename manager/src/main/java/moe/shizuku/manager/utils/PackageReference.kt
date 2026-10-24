package moe.shizuku.manager.utils

import com.reandroid.arsc.chunk.xml.AndroidManifestBlock
import com.reandroid.arsc.chunk.xml.ResXmlElement
import java.util.function.Predicate

/**
 * [value] with a leading [oldPackage] replaced by [newPackage], or null when this value has nothing
 * to do with the old package name.
 *
 * The name an app is called by is written into its manifest in more places than its `package`
 * attribute: intent filter actions, permission names and provider authorities all carry it, and
 * renaming only the package leaves a copy that advertises one name and answers to another. That is
 * issue #86: a hidden install whose `.START` action matched no filter at all, while the action that
 * did match was then rejected by a receiver comparing against the runtime package name.
 *
 * Only a value that *is* the package, or begins with it followed by a dot, is rewritten - so a URL
 * or a path that happens to contain those characters somewhere inside it is left alone.
 */
internal fun rewrittenPackageReference(
    value: String,
    oldPackage: String,
    newPackage: String
): String? = when {
    value == oldPackage -> newPackage
    !value.startsWith("$oldPackage.") -> null
    else -> newPackage + value.removePrefix(oldPackage)
}

/**
 * [this] manifest's label replaced with [requested], and whether a name of its own was written.
 *
 * A hidden copy is a copy of this app's own APK, so its label is this app's label: the manifest
 * points the launcher at a string resource, and that resource says this app's name however the
 * package is called. The name a user types is written over it as a literal, because the point is
 * what the copy is listed as, not a renamed app - the same resource is what the app's own screens
 * read, and a copy whose launcher entry and whose screens disagree is its own kind of tell.
 *
 * Blank is not a name: it leaves the manifest alone, which is what an empty field means.
 */
fun AndroidManifestBlock.renameHiddenCopy(requested: String?): Boolean {
    val label = requested?.trim()?.takeIf { it.isNotEmpty() } ?: return false

    setApplicationLabel(label)
    return true
}

/**
 * The name a hidden copy has to keep across an update, or null when it has none of its own.
 *
 * An update downloads the released APK and, while hidden, renames it to the running package name -
 * which rebuilds the label from this app's own string resource and would undo whatever name the
 * user chose when they hid. [currentLabel] is what the running copy is listed as, which is that
 * name; [appName] is what the resource says in the locale in use. When the two differ a name was
 * chosen and is written again, and when they are the same nothing is written at all - which is what
 * keeps the label translated for everybody who never chose one.
 */
fun displayNameToKeep(currentLabel: String, appName: String): String? =
    currentLabel.takeIf { it != appName }

/**
 * [this] manifest renamed to [newPackageName]: its `package` attribute set, and every value that
 * named [oldPackageName] rewritten to the new name. Returns how many values were rewritten.
 *
 * Both steps are here, in this order, rather than at the call site, because the order is the whole
 * of it. [rewritePackageReferences] walks from the manifest element itself, so `package` is one of
 * the values it visits; writing the new name before the walk made the walk rewrite it a second time
 * whenever the new name begins with the old one followed by a dot - which is every name a hidden
 * copy gets, since it is the old one with a suffix on the end. `moe.shizuku.privileged.api` plus
 * `.v19r8` built a copy named `moe.shizuku.privileged.api.v19r8.v19r8`, whose provider authorities
 * still named the running copy, so the platform refused it as INSTALL_FAILED_CONFLICTING_PROVIDER:
 * issue #94, where a hidden copy could neither be made nor updated. Writing the `package` attribute
 * last is what keeps the name written once.
 *
 * It lives here, beside the rule, rather than in the file that signs and installs the APK, because
 * this is the whole of it: an `AndroidManifestBlock` in and an `AndroidManifestBlock` out, with no
 * Android under it, which is how a test can drive it on the JVM alone.
 */
internal fun AndroidManifestBlock.renamePackage(
    oldPackageName: String,
    newPackageName: String,
): Int {
    val rewritten = rewritePackageReferences(oldPackageName, newPackageName)
    packageName = newPackageName
    return rewritten
}

/**
 * Every value in [this] manifest that names [oldPackage], rewritten to [newPackage], and how many
 * were rewritten.
 *
 * Walks the whole document rather than a list of known tags: the strings that carry a package name
 * sit at every level - the permission at the root, the components under `application`, the actions
 * inside their `intent-filter` children - and a new one added later should not have to be added
 * here as well. Nothing but string attribute values is touched.
 */
private fun AndroidManifestBlock.rewritePackageReferences(oldPackage: String, newPackage: String): Int {
    fun walk(element: ResXmlElement): Int {
        var count = 0

        for (index in 0 until element.attributeCount) {
            val attribute = element.getAttributeAt(index)
            val value = attribute.valueAsString ?: continue

            val rewritten = rewrittenPackageReference(value, oldPackage, newPackage)
                ?: continue
            attribute.setValueAsString(rewritten)
            count++
        }

        // The generic child iterator is the only one that can name what it returns, because the base
        // class holding the no-argument version is not public; a predicate that accepts everything
        // says the same thing.
        val children = ArrayList<ResXmlElement>()
        element.getElements(Predicate { true }).forEachRemaining { children.add(it) }
        return children.fold(count) { total, child -> total + walk(child) }
    }

    // `getElements()` yields the document element itself, the `<manifest>`, and not what it holds -
    // which reads backwards, and is why the walk covers the manifest's own `package` attribute as
    // well. [renamePackage] writes that one last for exactly that reason. The accessor is the raw
    // one, hence the cast.
    val roots = ArrayList<ResXmlElement>()
    getElements().forEachRemaining { element -> roots.add(element as ResXmlElement) }
    return roots.fold(0) { total, root -> total + walk(root) }
}
