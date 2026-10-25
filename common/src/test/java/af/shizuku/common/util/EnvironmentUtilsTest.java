package af.shizuku.common.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class EnvironmentUtilsTest {

    private Context mockContext;
    private UiModeManager mockUiModeManager;

    @Before
    public void setUp() {
        mockContext = Mockito.mock(Context.class);
        mockUiModeManager = Mockito.mock(UiModeManager.class);

        // Mock getSystemService(Class) which is added in API 23
        Mockito.when(mockContext.getSystemService(UiModeManager.class)).thenReturn(mockUiModeManager);
    }

    @Test
    public void isWatch_withWatchMode_returnsTrue() {
        Mockito.when(mockUiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_WATCH);
        assertTrue(EnvironmentUtils.isWatch(mockContext));
    }

    @Test
    public void isWatch_withNormalMode_returnsFalse() {
        Mockito.when(mockUiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_NORMAL);
        assertFalse(EnvironmentUtils.isWatch(mockContext));
    }

    @Test
    public void isWatch_withOtherModes_returnsFalse() {
        Mockito.when(mockUiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_DESK);
        assertFalse(EnvironmentUtils.isWatch(mockContext));

        Mockito.when(mockUiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_TELEVISION);
        assertFalse(EnvironmentUtils.isWatch(mockContext));

        Mockito.when(mockUiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_CAR);
        assertFalse(EnvironmentUtils.isWatch(mockContext));

        Mockito.when(mockUiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_VR_HEADSET);
        assertFalse(EnvironmentUtils.isWatch(mockContext));
    }

    @Test(expected = NullPointerException.class)
    public void isWatch_withNullContext_throwsNullPointerException() {
        EnvironmentUtils.isWatch(null);
    }

    @Test(expected = NullPointerException.class)
    public void isWatch_withNullUiModeManager_throwsNullPointerException() {
        Context contextWithoutUiModeManager = Mockito.mock(Context.class);
        Mockito.when(contextWithoutUiModeManager.getSystemService(UiModeManager.class)).thenReturn(null);
        EnvironmentUtils.isWatch(contextWithoutUiModeManager);
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
