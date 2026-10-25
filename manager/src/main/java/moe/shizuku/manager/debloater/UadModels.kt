package moe.shizuku.manager.debloater

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color

enum class UadRemoval(
    val displayName: String,
    val color: Color,
    val description: String
) {
    RECOMMENDED(
        "Recommended",
        Color(0xFF2E7D32),
        "Safe to remove. No system side-effects or easily replaced by better alternatives."
    ),
    ADVANCED(
        "Advanced",
        Color(0xFFF57F17),
        "Safe if you do not use the feature or have an alternative app installed."
    ),
    EXPERT(
        "Expert",
        Color(0xFFD84315),
        "Disables OEM/carrier specific features. May break dependent functions."
    ),
    UNSAFE(
        "Unsafe",
        Color(0xFFC62828),
        "Critical framework or system component. High risk of bootloop or crash!"
    ),
    SYSTEM(
        "System",
        Color(0xFF546E7A),
        "Pre-installed system application not specifically profiled in UAD database."
    ),
    UNKNOWN(
        "User App",
        Color(0xFF78909C),
        "User-installed application."
    );

    companion object {
        fun fromString(value: String?): UadRemoval = when (value?.trim()?.lowercase()) {
            "recommended" -> RECOMMENDED
            "advanced" -> ADVANCED
            "expert" -> EXPERT
            "unsafe" -> UNSAFE
            else -> SYSTEM
        }
    }
}

data class UadEntry(
    val packageName: String,
    val list: String = "General",
    val description: String = "",
    val removal: UadRemoval = UadRemoval.SYSTEM,
    val dependencies: List<String> = emptyList(),
    val neededBy: List<String> = emptyList()
)

enum class AppState(val label: String, val color: Color) {
    ACTIVE("Active", Color(0xFF1E88E5)),
    DISABLED("Disabled", Color(0xFFE53935)),
    UNINSTALLED("Uninstalled (User 0)", Color(0xFF8E24AA))
}

data class DebloatApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val state: AppState,
    val uadEntry: UadEntry?,
    val icon: Drawable? = null
)
