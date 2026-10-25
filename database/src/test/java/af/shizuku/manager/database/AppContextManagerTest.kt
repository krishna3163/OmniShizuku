package af.shizuku.manager.database

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.After
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class AppContextManagerTest {

    private lateinit var fakeSettings: FakeAppContextSettings

    class FakeAppContextSettings : AppContextSettings {
        var storedRemoteDbJson: String? = null
        var lastDbUpdateTime: Long = 0

        override fun getRemoteDbJson(): String? = storedRemoteDbJson

        override fun setRemoteDbJson(json: String) {
            storedRemoteDbJson = json
        }

        override fun setLastDbUpdate(time: Long) {
            lastDbUpdateTime = time
        }
    }

    @Before
    fun setup() {
        fakeSettings = FakeAppContextSettings()
        // clear dynamic db before each test
        val dynamicDatabaseField: Field = AppContextManager::class.java.getDeclaredField("dynamicDatabase")
        dynamicDatabaseField.isAccessible = true
        val dynamicDatabase = dynamicDatabaseField.get(AppContextManager) as MutableMap<*, *>
        dynamicDatabase.clear()

        AppContextManager.initialize(fakeSettings)
    }

    @After
    fun tearDown() {
        val dynamicDatabaseField: Field = AppContextManager::class.java.getDeclaredField("dynamicDatabase")
        dynamicDatabaseField.isAccessible = true
        val dynamicDatabase = dynamicDatabaseField.get(AppContextManager) as MutableMap<*, *>
        dynamicDatabase.clear()
    }

    @Test
    fun getMetadata_returnsStaticData_whenDynamicDataUnavailable() {
        // AppContextManager initialized with empty fakeSettings (no remote JSON)
        val metadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        metadata shouldNotBe null
        metadata!!.description shouldBe "Magisk: The systemless root solution. Shizuku+ spoofs its presence to other apps."
        metadata.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
        metadata.isVerified shouldBe true
    }

    @Test
    fun getMetadata_returnsNull_whenPackageUnknown() {
        val metadata = AppContextManager.getMetadata("com.unknown.app")
        metadata.shouldBeNull()
    }

    @Test
    fun getDescription_returnsDescription_whenPackageKnown() {
        val description = AppContextManager.getDescription("org.lsposed.manager")
        description shouldBe "LSPosed: Xposed framework modern implementation. Shizuku+ successfully masks its presence."
    }

    @Test
    fun getRootLegacyPackages_returnsCorrectStructure() {
        val legacyPackages = AppContextManager.getRootLegacyPackages()
        legacyPackages.keys shouldContain "Backup & Cleaning"
        legacyPackages.keys shouldContain "System Customization"

        legacyPackages["Backup & Cleaning"]!! shouldContain "com.keramidas.TitaniumBackup"
        legacyPackages["File Management"]!! shouldContain "com.speedsoftware.rootexplorer"
    }

    @Test
    fun updateDatabase_loadsDynamicDataAndTakesPrecedence() {

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
                        "description": "Updated Magisk description from remote.",
                        "enhancements": ["shell_interceptor"],
                        "verified": false,
                        "root_support": "full",
                        "shizuku_aware": true
                    },
                    "com.new.app": {
                        "description": "A completely new app.",
                        "enhancements": [],
                        "verified": true,
                        "root_support": "partial",
                        "shizuku_aware": false
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

        // Check settings updated
        fakeSettings.storedRemoteDbJson shouldBe json
        fakeSettings.lastDbUpdateTime shouldNotBe 0L

        // Verify dynamic data takes precedence over static data
        val magiskMetadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        magiskMetadata shouldNotBe null
        magiskMetadata!!.description shouldBe "Updated Magisk description from remote."
        magiskMetadata.isVerified shouldBe false
        magiskMetadata.rootSupportLevel shouldBe RootSupportLevel.FULL
        magiskMetadata.supportsShizukuNatively shouldBe true
        magiskMetadata.potentialEnhancements shouldHaveSize 1
        magiskMetadata.potentialEnhancements[0].key shouldBe "shell_interceptor"

        // Verify new app is loaded
        val newAppMetadata = AppContextManager.getMetadata("com.new.app")
        newAppMetadata shouldNotBe null
        newAppMetadata!!.description shouldBe "A completely new app."
        newAppMetadata.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
    }

    @Test
    fun initialize_loadsFromCacheIfAvailable() {
        val json = """
            {
                "apps": {
                    "com.cached.app": {
                        "description": "Cached app description."
                    }
                }
            }
        """.trimIndent()

        fakeSettings.storedRemoteDbJson = json

        // Clear and re-initialize
        val dynamicDatabaseField: Field = AppContextManager::class.java.getDeclaredField("dynamicDatabase")
        dynamicDatabaseField.isAccessible = true
        val dynamicDatabase = dynamicDatabaseField.get(AppContextManager) as MutableMap<*, *>
        dynamicDatabase.clear()

        AppContextManager.initialize(fakeSettings)

        val metadata = AppContextManager.getMetadata("com.cached.app")
        metadata shouldNotBe null
        metadata!!.description shouldBe "Cached app description."
        metadata.rootSupportLevel shouldBe RootSupportLevel.FULL // Default
    }

    @Test
    fun loadFromCache_handlesInvalidJsonGracefully() {
        // First set a valid dynamic db
        val validJson = """
            {
                "apps": {
                    "com.test.app": {
                        "description": "Valid"
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
        AppContextManager.updateDatabase(validJson)

        // Now try updating with invalid JSON
        val invalidJson = "{ invalid_json "
        AppContextManager.updateDatabase(invalidJson)

        // It clears the dynamic database at the start of updateDatabase, and loadFromCache will fail and catch the exception.
        // Therefore dynamic db should be empty, and static db should fallback.
        val metadata = AppContextManager.getMetadata("com.test.app")
        metadata.shouldBeNull()

        // But static fallback should still work
        val magiskMetadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        magiskMetadata shouldNotBe null
    }

    @Test
    fun loadFromCache_handlesEmptyJson() {
        AppContextManager.updateDatabase("")
        val magiskMetadata = AppContextManager.getMetadata("com.topjohnwu.magisk")
        magiskMetadata shouldNotBe null // Static fallback works
    }

    @Test
    fun repeatedlyUpdateDatabase_replacesOldDynamicData() {
        val json1 = """
            {
                "apps": {
                    "com.test.app": {
                        "description": "Version 1"
                    }
                }
            }
        """.trimIndent()

        AppContextManager.updateDatabase(json1)
        AppContextManager.getMetadata("com.test.app")?.description shouldBe "Version 1"

        val json2 = """
            {
                "apps": {
                    "com.test.app": {
                        "description": "Version 2"
                    },
                    "com.test2.app": {
                        "description": "New app"
                    }

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

        AppContextManager.updateDatabase(json2)
        AppContextManager.getMetadata("com.test.app")?.description shouldBe "Version 2"
        AppContextManager.getMetadata("com.test2.app")?.description shouldBe "New app"
        AppContextManager.updateDatabase(json)

        AppContextManager.getMetadata("app.partial")?.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
        AppContextManager.getMetadata("app.required")?.rootSupportLevel shouldBe RootSupportLevel.ROOT_REQUIRED
        AppContextManager.getMetadata("app.full")?.rootSupportLevel shouldBe RootSupportLevel.FULL
        AppContextManager.getMetadata("app.upper")?.rootSupportLevel shouldBe RootSupportLevel.PARTIAL
    }
}
