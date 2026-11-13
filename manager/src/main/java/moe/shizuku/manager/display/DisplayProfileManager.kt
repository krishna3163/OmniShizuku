package moe.shizuku.manager.display

object DisplayProfileManager {

    fun builtIns(snapshot: DisplaySnapshot): List<DisplayProfile> {
        val currentRate = snapshot.peakRefreshRate ?: snapshot.minRefreshRate
        return listOf(
            DisplayProfile("Default", DisplayChange(), builtIn = true),
            DisplayProfile(
                "Battery Saver",
                DisplayChange(
                    minRefreshRate = "30",
                    peakRefreshRate = "60"
                ),
                builtIn = true
            ),
            DisplayProfile(
                "Balanced",
                DisplayChange(peakRefreshRate = currentRate ?: "60"),
                builtIn = true
            ),
            DisplayProfile(
                "Smooth",
                DisplayChange(
                    minRefreshRate = currentRate ?: "60",
                    peakRefreshRate = currentRate ?: "60"
                ),
                builtIn = true
            ),
            DisplayProfile("Reading", DisplayChange(density = snapshot.density), builtIn = true)
        )
    }

    fun merge(saved: List<DisplayProfile>, snapshot: DisplaySnapshot): List<DisplayProfile> =
        builtIns(snapshot) + saved.filterNot { it.builtIn }

    fun canApply(profile: DisplayProfile, modes: List<DisplayModeInfo>): String? =
        DisplayConfigurationValidator.validate(profile.change, modes)
}
