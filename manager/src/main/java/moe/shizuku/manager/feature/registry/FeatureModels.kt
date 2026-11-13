package moe.shizuku.manager.feature.registry

import moe.shizuku.manager.ui.Detail

enum class FeatureCategory {
    APP_MANAGEMENT,
    SYSTEM_CONTROLS,
    UTILITIES,
    COMPANION_APPS
}

enum class FeatureProvider {
    ANDROID_API,
    SHIZUKU,
    ADB,
    ROOT,
    DHIZUKU
}

enum class FeatureAvailability {
    AVAILABLE,
    REQUIRES_SHIZUKU,
    REQUIRES_ROOT,
    REQUIRES_ADB,
    UNSUPPORTED_ANDROID_VERSION,
    UNSUPPORTED_DEVICE,
    EXPERIMENTAL
}

data class FeatureDefinition(
    val id: String,
    val title: String,
    val detail: Detail,
    val category: FeatureCategory,
    val description: String,
    val searchKeywords: Set<String>,
    val minApi: Int = 26,
    val requiredProviders: Set<FeatureProvider> = setOf(FeatureProvider.ANDROID_API),
    val reversible: Boolean = true,
    val experimental: Boolean = false
)

data class FeatureCapability(
    val definition: FeatureDefinition,
    val availability: FeatureAvailability,
    val availableProviders: Set<FeatureProvider>
) {
    val isAvailable: Boolean
        get() = availability == FeatureAvailability.AVAILABLE ||
            availability == FeatureAvailability.EXPERIMENTAL
}
