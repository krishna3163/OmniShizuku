package moe.shizuku.manager.utils

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeIntentUtilsTest {

    @Test
    fun trustedInstallerPackageRecognition() {
        val current = "com.krishna.omnishizuku"

        // Host package
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage(current, false, false, current))

        // System packages
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage("com.android.settings", true, false, current))
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage("com.android.shell", false, true, current))

        // Known package installers
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage("com.android.packageinstaller", false, false, current))
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage("com.google.android.packageinstaller", false, false, current))
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage("com.android.permissioncontroller", false, false, current))
        assertTrue(SafeIntentUtils.isTrustedInstallerPackage("com.google.android.permissioncontroller", false, false, current))

        // Untrusted 3rd party packages
        assertFalse(SafeIntentUtils.isTrustedInstallerPackage("com.evil.malware", false, false, current))
        assertFalse(SafeIntentUtils.isTrustedInstallerPackage("org.example.phishing", false, false, current))
        assertFalse(SafeIntentUtils.isTrustedInstallerPackage(null, false, false, current))
        assertFalse(SafeIntentUtils.isTrustedInstallerPackage("", false, false, current))
    }

    @Test
    fun computeSanitizedFlagsStripsUriGrantsAndAddsNewTask() {
        val originalFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION or
                Intent.FLAG_ACTIVITY_CLEAR_TOP

        val sanitized = SafeIntentUtils.computeSanitizedFlags(originalFlags)

        val unsafeMask = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        assertEquals(0, sanitized and unsafeMask)

        // Verifies FLAG_ACTIVITY_NEW_TASK is set
        assertTrue((sanitized and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)

        // Verifies safe flags are preserved
        assertTrue((sanitized and Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0)
    }
}
