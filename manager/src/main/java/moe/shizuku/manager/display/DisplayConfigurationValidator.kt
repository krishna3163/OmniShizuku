package moe.shizuku.manager.display

import kotlin.math.abs

object DisplayConfigurationValidator {

    private val sizePattern = Regex("""(\d{3,5})x(\d{3,5})""")

    fun parseSize(value: String): Pair<Int, Int>? {
        val match = sizePattern.matchEntire(value.trim()) ?: return null
        val width = match.groupValues[1].toIntOrNull() ?: return null
        val height = match.groupValues[2].toIntOrNull() ?: return null
        return width to height
    }

    fun validateSize(value: String, supportedModes: List<DisplayModeInfo>): String? {
        val (width, height) = parseSize(value) ?: return "Resolution must be WIDTHxHEIGHT."
        if (width !in 320..10000 || height !in 320..10000) {
            return "Resolution is outside the safe range."
        }
        if (supportedModes.isEmpty()) return null
        val supportedAspect = supportedModes
            .map { it.width.toFloat() / it.height }
            .minByOrNull { abs(it - width.toFloat() / height) }
            ?: return null
        if (abs(supportedAspect - width.toFloat() / height) > 0.08f) {
            return "Resolution aspect ratio is not compatible with this display."
        }
        return null
    }

    fun validateDensity(value: Int): String? =
        if (value in 100..1000) null else "Density must be between 100 and 1000 DPI."

    fun validateRefreshRate(value: Float, supportedRates: List<Float>): String? {
        if (value !in 1f..240f) return "Refresh rate is outside the supported range."
        if (supportedRates.isNotEmpty() &&
            supportedRates.none { abs(it - value) < 0.6f }
        ) {
            return "This refresh rate is not reported by the display."
        }
        return null
    }

    fun validate(change: DisplayChange, modes: List<DisplayModeInfo>): String? {
        change.size?.let { validateSize(it, modes)?.let { error -> return error } }
        change.density?.let { validateDensity(it)?.let { error -> return error } }
        val rates = modes.flatMap { it.supportedRefreshRates }.distinct()
        change.minRefreshRate?.toFloatOrNull()?.let {
            validateRefreshRate(it, rates)?.let { error -> return error }
        }
        change.peakRefreshRate?.toFloatOrNull()?.let {
            validateRefreshRate(it, rates)?.let { error -> return error }
        }
        return null
    }
}
