package af.shizuku.common.util;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34}) // Limit max SDK to avoid Robolectric Target SDK 37 issue
public class EnvironmentUtilsTest {

    private String originalManufacturer;
    private int originalSdkInt;

    @Before
    public void setUp() {
        originalManufacturer = Build.MANUFACTURER;
        originalSdkInt = Build.VERSION.SDK_INT;
    }

    @After
    public void tearDown() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", originalManufacturer);
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", originalSdkInt);
    }

    @Test
    public void getOneUiVersion_notSamsung_returnsMinusOne() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "google");
        assertEquals(-1, EnvironmentUtils.getOneUiVersion());
    }

    @Test
    public void getOneUiVersion_samsung_sdk36_returns8() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "samsung");
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 36);
        assertEquals(8, EnvironmentUtils.getOneUiVersion());
    }

    @Test
    public void getOneUiVersion_samsung_sdk35_returns7() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "samsung");
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 35);
        assertEquals(7, EnvironmentUtils.getOneUiVersion());
    }

    @Test
    public void getOneUiVersion_samsung_sdk34_returns6() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "samsung");
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 34);
        assertEquals(6, EnvironmentUtils.getOneUiVersion());
    }

    @Test
    public void getOneUiVersion_samsung_sdk33_returns5() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "samsung");
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 33);
        assertEquals(5, EnvironmentUtils.getOneUiVersion());
    }

    @Test
    public void getOneUiVersion_samsung_sdk32_returnsMinusOne() {
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", "samsung");
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 32);
        assertEquals(-1, EnvironmentUtils.getOneUiVersion());
    }
}
