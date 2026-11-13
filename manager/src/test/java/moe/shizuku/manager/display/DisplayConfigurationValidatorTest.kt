package moe.shizuku.manager.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayConfigurationValidatorTest {

    private val modes = listOf(
        DisplayModeInfo(1, 1080, 2400, 60f, listOf(60f, 90f)),
        DisplayModeInfo(2, 1080, 2400, 90f, listOf(60f, 90f))
    )

    @Test
    fun parsesAndValidatesCompatibleResolution() {
        assertEquals(1080 to 2400, DisplayConfigurationValidator.parseSize("1080x2400"))
        assertNull(DisplayConfigurationValidator.validateSize("720x1600", modes))
    }

    @Test
    fun rejectsMalformedOrIncompatibleResolution() {
        assertNotNull(DisplayConfigurationValidator.validateSize("0x2400", modes))
        assertNotNull(DisplayConfigurationValidator.validateSize("1000x1000", modes))
    }

    @Test
    fun validatesDensityAndReportedRefreshRates() {
        assertNull(DisplayConfigurationValidator.validateDensity(420))
        assertNotNull(DisplayConfigurationValidator.validateDensity(42))
        assertNull(DisplayConfigurationValidator.validateRefreshRate(90f, listOf(60f, 90f)))
        assertNotNull(DisplayConfigurationValidator.validateRefreshRate(120f, listOf(60f, 90f)))
    }

    @Test
    fun rejectsInvalidProfileChange() {
        assertNull(
            DisplayConfigurationValidator.validate(
                DisplayChange(size = "720x1600", density = 420, peakRefreshRate = "90"),
                modes
            )
        )
        assertNotNull(
            DisplayConfigurationValidator.validate(
                DisplayChange(minRefreshRate = "120"),
                modes
            )
        )
    }
}
