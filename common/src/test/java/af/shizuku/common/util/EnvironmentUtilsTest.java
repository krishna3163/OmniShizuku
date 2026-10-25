package af.shizuku.common.util;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class EnvironmentUtilsTest {

    @Test
    public void isSamsung_whenManufacturerIsSamsung_returnsTrue() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "samsung");
        assertTrue("Expected isSamsung() to return true when manufacturer is 'samsung'", EnvironmentUtils.isSamsung());
    }

    @Test
    public void isSamsung_whenManufacturerIsSamsungUppercase_returnsTrue() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "SAMSUNG");
        assertTrue("Expected isSamsung() to return true when manufacturer is 'SAMSUNG'", EnvironmentUtils.isSamsung());
    }

    @Test
    public void isSamsung_whenManufacturerIsGoogle_returnsFalse() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "Google");
        assertFalse("Expected isSamsung() to return false when manufacturer is 'Google'", EnvironmentUtils.isSamsung());
    }

    @Test
    public void isSamsung_whenManufacturerIsEmpty_returnsFalse() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "");
        assertFalse("Expected isSamsung() to return false when manufacturer is empty", EnvironmentUtils.isSamsung());
    }
}
