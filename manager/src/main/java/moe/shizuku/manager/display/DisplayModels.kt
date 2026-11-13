package moe.shizuku.manager.display

data class DisplayModeInfo(
    val id: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
    val supportedRefreshRates: List<Float> = listOf(refreshRate)
)

data class DisplaySnapshot(
    val size: String?,
    val density: Int?,
    val minRefreshRate: String?,
    val peakRefreshRate: String?,
    val modeId: Int? = null,
    val currentRefreshRate: Float? = null,
    val capturedAt: Long = System.currentTimeMillis()
)

data class DisplayChange(
    val size: String? = null,
    val density: Int? = null,
    val minRefreshRate: String? = null,
    val peakRefreshRate: String? = null
)

data class DisplayProfile(
    val name: String,
    val change: DisplayChange,
    val builtIn: Boolean = false
)

enum class DisplayRollbackState {
    IDLE,
    APPLYING,
    AWAITING_CONFIRMATION,
    CONFIRMED,
    ROLLING_BACK,
    RESTORED,
    FAILED
}

data class PendingDisplayChange(
    val before: DisplaySnapshot,
    val deadlineMillis: Long,
    val state: DisplayRollbackState
)
