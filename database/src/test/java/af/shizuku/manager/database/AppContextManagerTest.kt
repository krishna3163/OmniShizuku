package af.shizuku.manager.database

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [34])
class AppContextManagerTest {

    private class FakeAppContextSettings : AppContextSettings {
        private var _remoteDbJson: String? = null
        private var _lastDbUpdate: Long = 0

        override fun getRemoteDbJson(): String? = _remoteDbJson

        override fun setRemoteDbJson(json: String) {
            _remoteDbJson = json
        }

        override fun setLastDbUpdate(time: Long) {
            _lastDbUpdate = time
        }
    }

    private lateinit var settings: FakeAppContextSettings

    @Before
    fun setup() {
        settings = FakeAppContextSettings()
        // Reset dynamic database for isolated tests
        AppContextManager.updateDatabase("")
        AppContextManager.initialize(settings)
    }

    @Test
    fun testStaticDatabaseIsLoaded() {
        val metadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        metadata.shouldNotBeNull()
        metadata.description shouldBe "Magisk: The systemless root solution. Shizuku+ spoofs its presence to other apps."
        metadata.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
    }

    @Test
    fun testGetDescriptionUsesStaticDatabase() {
        val description = AppContextManager.getDescription("com.topjohnwu.magisk")
        description shouldBe "Magisk: The systemless root solution. Shizuku+ spoofs its presence to other apps."
    }

    @Test
    fun testGetDescriptionReturnsNullForUnknownPackage() {
        val description = AppContextManager.getDescription("com.unknown.app")
        description.shouldBeNull()
    }

    @Test
    fun testDynamicDataOverridesStaticData() {
        val json = """
            {
                "apps": {
                    "com.topjohnwu.magisk": {
                        "description": "Updated Magisk description",
                        "enhancements": ["shell_interceptor"],
                        "verified": true,
                        "root_support": "full",
                        "shizuku_aware": true
                    }
                }
            }
        """.trimIndent()

        AppContextManager.updateDatabase(json)

        val metadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        metadata.shouldNotBeNull()
        metadata.description shouldBe "Updated Magisk description"
        metadata.potentialEnhancements.map { it.key } shouldContainExactly listOf("shell_interceptor")
        metadata.isVerified shouldBe true
        metadata.rootSupportLevel shouldBe RootSupportLevel.FULL
        metadata.supportsShizukuNatively shouldBe true
    }

    @Test
    fun testJsonParsingWithUnknownKeys() {
        val json = """
            {
                "apps": {
                    "com.test.app": {
                        "description": "Test App",
                        "unknown_key": "some value",
                        "another_unknown": 123
                    }
                },
                "global_unknown": true
            }
        """.trimIndent()

        AppContextManager.updateDatabase(json)
        val metadata = AppContextManager.getMetadata("com.test.app")
        metadata.shouldNotBeNull()
        metadata.description shouldBe "Test App"
    }

    @Test
    fun testJsonParsingWithMissingFieldsAndInvalidEnums() {
        val json = """
            {
                "apps": {
                    "com.test.app": {
                        "description": "Test App",
                        "enhancements": ["unknown_enhancement", "shell_interceptor"],
                        "root_support": "unknown_level"
                    }
                }
            }
        """.trimIndent()

        AppContextManager.updateDatabase(json)
        val metadata = AppContextManager.getMetadata("com.test.app")

        metadata.shouldNotBeNull()
        // verify unknown enhancement is ignored but known one is kept
        metadata.potentialEnhancements.map { it.key } shouldContainExactly listOf("shell_interceptor")

        // unknown root_support should fallback to FULL based on loadFromCache logic
        metadata.rootSupportLevel shouldBe RootSupportLevel.FULL

        // missing fields should take defaults
        metadata.isVerified shouldBe false
        metadata.supportsShizukuNatively shouldBe false
    }

    @Test
    fun testEmptyJsonHandling() {
        val json = ""
        AppContextManager.updateDatabase(json)

        // Should not crash, and static DB should still be accessible
        val metadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        metadata.shouldNotBeNull()
        metadata.description shouldBe "Magisk: The systemless root solution. Shizuku+ spoofs its presence to other apps."
    }

    @Test
    fun testMalformedJsonHandling() {
        val json = "{ invalid json"
        // Update database catches the exception and logs it via Timber, so it shouldn't crash
        AppContextManager.updateDatabase(json)

        val metadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        metadata.shouldNotBeNull()
        metadata.description shouldBe "Magisk: The systemless root solution. Shizuku+ spoofs its presence to other apps."
    }

    @Test
    fun testRootSupportLevelParsing() {
        val json = """
            {
                "apps": {
                    "app.partial": { "description": "1", "root_support": "partial" },
                    "app.required": { "description": "2", "root_support": "required" },
                    "app.full": { "description": "3", "root_support": "full" },
                    "app.upper": { "description": "4", "root_support": "PARTIAL" }
                }
            }
        """.trimIndent()

        AppContextManager.updateDatabase(json)

        AppContextManager.getMetadata("app.partial")?.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
        AppContextManager.getMetadata("app.required")?.rootSupportLevel shouldBe RootSupportLevel.ROOT_REQUIRED
        AppContextManager.getMetadata("app.full")?.rootSupportLevel shouldBe RootSupportLevel.FULL
        AppContextManager.getMetadata("app.upper")?.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
    }
}
