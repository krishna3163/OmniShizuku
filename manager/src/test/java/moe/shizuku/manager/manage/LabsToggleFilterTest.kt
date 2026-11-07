package moe.shizuku.manager.manage

import org.junit.Assert.assertEquals
import org.junit.Test

class LabsToggleFilterTest {

    private val sampleApps = listOf(
        LabsToggleAppItem(packageName = "com.android.settings", label = "Settings", isSystemApp = true),
        LabsToggleAppItem(packageName = "org.telegram.messenger", label = "Telegram", isSystemApp = false),
        LabsToggleAppItem(packageName = "com.spotify.music", label = "Spotify", isSystemApp = false),
        LabsToggleAppItem(packageName = "com.google.android.youtube", label = "YouTube", isSystemApp = true)
    )

    @Test
    fun sortsBlockedAppsFirst() {
        val blocked = setOf("com.spotify.music")
        val result = LabsToggleViewModel.filterAndSortApps(
            apps = sampleApps,
            blockedPackages = blocked,
            query = "",
            showSystemApps = true,
            onlyBlocked = false
        )
        assertEquals("Spotify", result.first().label)
    }

    @Test
    fun filtersOutSystemAppsWhenDisabled() {
        val blocked = emptySet<String>()
        val result = LabsToggleViewModel.filterAndSortApps(
            apps = sampleApps,
            blockedPackages = blocked,
            query = "",
            showSystemApps = false,
            onlyBlocked = false
        )
        assertEquals(2, result.size)
        assertEquals(listOf("Spotify", "Telegram"), result.map { it.label })
    }

    @Test
    fun filtersOnlyBlockedWhenEnabled() {
        val blocked = setOf("org.telegram.messenger")
        val result = LabsToggleViewModel.filterAndSortApps(
            apps = sampleApps,
            blockedPackages = blocked,
            query = "",
            showSystemApps = true,
            onlyBlocked = true
        )
        assertEquals(1, result.size)
        assertEquals("Telegram", result.first().label)
    }

    @Test
    fun queryMatchesLabelAndPackageName() {
        val blocked = emptySet<String>()
        val resultByLabel = LabsToggleViewModel.filterAndSortApps(
            apps = sampleApps,
            blockedPackages = blocked,
            query = "tele",
            showSystemApps = true,
            onlyBlocked = false
        )
        assertEquals(listOf("Telegram"), resultByLabel.map { it.label })

        val resultByPackage = LabsToggleViewModel.filterAndSortApps(
            apps = sampleApps,
            blockedPackages = blocked,
            query = "youtube",
            showSystemApps = true,
            onlyBlocked = false
        )
        assertEquals(listOf("YouTube"), resultByPackage.map { it.label })
    }
}
