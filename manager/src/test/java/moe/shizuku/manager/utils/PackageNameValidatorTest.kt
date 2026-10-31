package moe.shizuku.manager.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNameValidatorTest {

    @Test
    fun acceptsAndroidPackageNameSegments() {
        listOf(
            "android",
            "com.example.app",
            "com.example_app2.feature3",
            "android.permission.POST_NOTIFICATIONS"
        ).forEach {
            assertTrue(it, PackageNameValidator.isValid(it))
        }
    }

    @Test
    fun rejectsShellSyntaxAndMalformedSegments() {
        listOf(
            "",
            ".com.example",
            "com..example",
            "com.example.",
            "com.example;reboot",
            "com.example && id",
            "com.example/app",
            "--user",
            "com.exämple.app",
            "a".repeat(256)
        ).forEach {
            assertFalse(it, PackageNameValidator.isValid(it))
        }
    }
}
