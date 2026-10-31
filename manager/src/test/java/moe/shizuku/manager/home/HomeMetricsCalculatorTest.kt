package moe.shizuku.manager.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeMetricsCalculatorTest {

    @Test
    fun parsesAggregateCpuCountersAndUsesIdlePlusIoWait() {
        val counters = HomeMetricsCalculator.parseCpuCounters(
            "cpu 100 0 50 800 50 0 0 0\ncpu0 50 0 25 400 25 0 0 0"
        )

        assertEquals(CpuCounters(totalTicks = 1_000, idleTicks = 850), counters)
    }

    @Test
    fun computesCpuUsageOnlyFromPositiveMonotonicDeltas() {
        assertEquals(
            50.0,
            HomeMetricsCalculator.cpuUtilizationPercent(
                CpuCounters(totalTicks = 100, idleTicks = 80),
                CpuCounters(totalTicks = 200, idleTicks = 130)
            )!!,
            0.001
        )
        assertNull(
            HomeMetricsCalculator.cpuUtilizationPercent(
                CpuCounters(totalTicks = 200, idleTicks = 130),
                CpuCounters(totalTicks = 190, idleTicks = 130)
            )
        )
    }

    @Test
    fun computesTrafficRateOnlyFromValidCounterDeltasAndElapsedTime() {
        assertEquals(500.0, HomeMetricsCalculator.bytesPerSecond(1_000, 2_000, 2_000)!!, 0.001)
        assertNull(HomeMetricsCalculator.bytesPerSecond(2_000, 1_999, 1_000))
        assertNull(HomeMetricsCalculator.bytesPerSecond(1_000, 2_000, 0))
    }

    @Test
    fun estimatesBatteryDrainOnlyForAValidTimedDischargeSample() {
        assertEquals(
            2.0,
            HomeMetricsCalculator.batteryDrainPercentPerHour(80, 78, 3_600_000)!!,
            0.001
        )
        assertNull(HomeMetricsCalculator.batteryDrainPercentPerHour(78, 80, 3_600_000))
        assertNull(HomeMetricsCalculator.batteryDrainPercentPerHour(80, 79, 10_000))
    }
}
