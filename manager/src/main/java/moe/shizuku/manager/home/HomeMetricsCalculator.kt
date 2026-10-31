package moe.shizuku.manager.home

import kotlin.math.max

internal data class CpuCounters(val totalTicks: Long, val idleTicks: Long)

internal object HomeMetricsCalculator {
    fun parseCpuCounters(stat: String): CpuCounters? {
        val cpuLine = stat.lineSequence().firstOrNull { it.startsWith("cpu ") } ?: return null
        val values = cpuLine.trim().split(Regex("\\s+")).drop(1).map { it.toLongOrNull() ?: return null }
        if (values.size < 4 || values.any { it < 0L }) return null
        // Guest and guest_nice are already included in user and nice respectively.
        val total = values.take(8).fold(0L) { sum, value -> Math.addExact(sum, value) }
        val idle = Math.addExact(values[3], values.getOrElse(4) { 0L })
        if (total <= 0L || idle > total) return null
        return CpuCounters(total, idle)
    }

    fun cpuUtilizationPercent(previous: CpuCounters?, current: CpuCounters?): Double? {
        if (previous == null || current == null) return null
        val totalDelta = current.totalTicks - previous.totalTicks
        val idleDelta = current.idleTicks - previous.idleTicks
        if (totalDelta <= 0L || idleDelta < 0L || idleDelta > totalDelta) return null
        return ((totalDelta - idleDelta).toDouble() / totalDelta * 100.0)
            .takeIf { it.isFinite() && it in 0.0..100.0 }
    }

    fun bytesPerSecond(previousBytes: Long?, currentBytes: Long?, elapsedMillis: Long): Double? {
        if (previousBytes == null || currentBytes == null || previousBytes < 0L ||
            currentBytes < previousBytes || elapsedMillis <= 0L
        ) return null
        val delta = currentBytes - previousBytes
        return (delta.toDouble() * 1_000.0 / elapsedMillis)
            .takeIf { it.isFinite() && it >= 0.0 }
    }

    fun batteryDrainPercentPerHour(
        previousPercent: Int?,
        currentPercent: Int?,
        elapsedMillis: Long
    ): Double? {
        if (previousPercent !in 0..100 || currentPercent !in 0..100 ||
            previousPercent!! <= currentPercent!! || elapsedMillis < MIN_DRAIN_SAMPLE_MS ||
            elapsedMillis > MAX_DRAIN_SAMPLE_MS
        ) return null
        val rate = (previousPercent - currentPercent) * 3_600_000.0 / elapsedMillis
        return rate.takeIf { it.isFinite() && it in 0.0..100.0 }
    }

    private const val MIN_DRAIN_SAMPLE_MS = 60_000L
    private const val MAX_DRAIN_SAMPLE_MS = 24 * 60 * 60 * 1_000L
}
