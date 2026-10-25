package af.shizuku.manager.database

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
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
                }
            }
        """.trimIndent()

        AppContextManager.updateDatabase(json2)
        AppContextManager.getMetadata("com.test.app")?.description shouldBe "Version 2"
        AppContextManager.getMetadata("com.test2.app")?.description shouldBe "New app"
    }
}
