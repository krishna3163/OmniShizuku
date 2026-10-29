package moe.shizuku.manager;

import android.app.ActivityThread;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import java.lang.annotation.Retention;
import java.util.Locale;

import moe.shizuku.manager.utils.EmptySharedPreferencesImpl;
import moe.shizuku.manager.utils.EnvironmentUtils;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class ShizukuSettings {

    public static final String NAME = "settings";
    public static final String NIGHT_MODE = "night_mode";
    public static final String LANGUAGE = "language";
    public static final String KEEP_START_ON_BOOT = "start_on_boot";
    public static final String TCP_MODE = "tcp_mode";
    public static final String SUPPRESS_TCP_MODE_PROMPT = "suppress_tcp_mode_prompt";
    public static final String LAST_ADB_PORT = "last_adb_port";
    public static final String AUTO_DISABLE_USB_DEBUGGING = "auto_disable_usb_debugging";
    public static final String START_ON_BOOT_ADB = "start_on_boot_adb";
    public static final String USE_CLASSIC_NAV = "use_classic_nav";


    private static SharedPreferences sPreferences;

    public static SharedPreferences getPreferences() {
        if (sPreferences == null) {
            return new EmptySharedPreferencesImpl();
        }
        return sPreferences;
    }

    @NonNull
    private static Context getSettingsStorageContext(@NonNull Context context) {
        Context storageContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            storageContext = context.createDeviceProtectedStorageContext();
        } else {
            storageContext = context;
        }

        storageContext = new ContextWrapper(storageContext) {
            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                try {
                    return super.getSharedPreferences(name, mode);
                } catch (IllegalStateException e) {
                    // SharedPreferences in credential encrypted storage are not available until after user is unlocked
                    return new EmptySharedPreferencesImpl();
                }
            }
        };

        return storageContext;
    }

    public static void initialize(Context context) {
        if (sPreferences == null) {
            sPreferences = getSettingsStorageContext(context)
                    .getSharedPreferences(NAME, Context.MODE_PRIVATE);
        }
    }

    @IntDef({
            LaunchMethod.UNKNOWN,
            LaunchMethod.ROOT,
            LaunchMethod.ADB,
            LaunchMethod.DHIZUKU,
    })
    @Retention(SOURCE)
    public @interface LaunchMethod {
        int UNKNOWN = -1;
        int ROOT = 0;
        int ADB = 1;
        int DHIZUKU = 3;
    }

    @LaunchMethod
    public static int getLastLaunchMode() {
        return getPreferences().getInt("mode", LaunchMethod.UNKNOWN);
    }

    public static void setLastLaunchMode(@LaunchMethod int method) {
        getPreferences().edit().putInt("mode", method).apply();
    }

    @AppCompatDelegate.NightMode
    public static int getNightMode() {
        int defValue = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (EnvironmentUtils.isWatch(ActivityThread.currentActivityThread().getApplication())) {
            defValue = AppCompatDelegate.MODE_NIGHT_YES;
        }
        return getPreferences().getInt(NIGHT_MODE, defValue);
    }

    public static void setNightMode(@AppCompatDelegate.NightMode int mode) {
        getPreferences().edit().putInt(NIGHT_MODE, mode).apply();
    }

    public static Locale getLocale() {
        String tag = getPreferences().getString(LANGUAGE, null);
        if (TextUtils.isEmpty(tag) || "SYSTEM".equals(tag)) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(tag);
    }

    public static boolean isTcpMode() {
        return getPreferences().getBoolean(TCP_MODE, false);
    }

    public static void setTcpMode(boolean enabled) {
        getPreferences().edit().putBoolean(TCP_MODE, enabled).apply();
    }

    public static boolean getAutoDisableUsbDebugging() {
        return getPreferences().getBoolean(AUTO_DISABLE_USB_DEBUGGING, false);
    }

    public static void setAutoDisableUsbDebugging(boolean enabled) {
        getPreferences().edit().putBoolean(AUTO_DISABLE_USB_DEBUGGING, enabled).apply();
    }

    public static boolean getAutoDisableWirelessDebugging() {
        return getPreferences().getBoolean(Keys.KEY_AUTO_DISABLE_WIRELESS_DEBUGGING, false);
    }

    public static void setAutoDisableWirelessDebugging(boolean enabled) {
        getPreferences().edit().putBoolean(Keys.KEY_AUTO_DISABLE_WIRELESS_DEBUGGING, enabled).apply();
    }

    public static boolean getStartOnBootAdb() {
        return getPreferences().getBoolean(START_ON_BOOT_ADB, false);
    }

    public static void setStartOnBootAdb(boolean enabled) {
        getPreferences().edit().putBoolean(START_ON_BOOT_ADB, enabled).apply();
    }

    public static boolean getStartOnBoot() {
        return getPreferences().getBoolean(KEEP_START_ON_BOOT, false);
    }

    public static void setStartOnBoot(boolean enabled) {
        getPreferences().edit().putBoolean(KEEP_START_ON_BOOT, enabled).apply();
    }

    public static void setStartOnBoot(Context context, boolean enabled) {
        getPreferences().edit().putBoolean(KEEP_START_ON_BOOT, enabled).apply();
        updateBootReceiver(context);
    }

    public static void updateBootReceiver(Context context) {
        if (context == null) return;
        boolean needed = getPreferences().getBoolean(KEEP_START_ON_BOOT, false);
        android.content.ComponentName bootCompleteReceiver = new android.content.ComponentName(
            context.getPackageName(),
            "moe.shizuku.manager.receiver.BootCompleteReceiver"
        );
        try {
            context.getPackageManager().setComponentEnabledSetting(
                bootCompleteReceiver,
                needed ? android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED : android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            );
        } catch (Throwable ignored) {
        }
    }

    public static boolean isTcpModePromptSuppressed() {
        return getPreferences().getBoolean(SUPPRESS_TCP_MODE_PROMPT, false);
    }

    public static void setTcpModePromptSuppressed(boolean suppressed) {
        getPreferences().edit().putBoolean(SUPPRESS_TCP_MODE_PROMPT, suppressed).apply();
    }

    public static boolean isClassicNav() {
        return getPreferences().getBoolean(USE_CLASSIC_NAV, false);
    }

    public static void setClassicNav(boolean enabled) {
        getPreferences().edit().putBoolean(USE_CLASSIC_NAV, enabled).apply();
    }

    public static final String SWIPE_BETWEEN_TABS = "swipe_between_tabs";
    public static final String MOTION_BLUR_ENABLED = "motion_blur_enabled";
    public static final String MOTION_BLUR_INTENSITY = "motion_blur_intensity";
    public static final String MOTION_BLUR_STRENGTH = "motion_blur_strength";

    public static boolean isSwipeBetweenTabs() {
        return getPreferences().getBoolean(SWIPE_BETWEEN_TABS, true);
    }

    public static void setSwipeBetweenTabs(boolean enabled) {
        getPreferences().edit().putBoolean(SWIPE_BETWEEN_TABS, enabled).apply();
    }

    public static boolean isMotionBlurEnabled() {
        return getPreferences().getBoolean(MOTION_BLUR_ENABLED, false);
    }

    public static void setMotionBlurEnabled(boolean enabled) {
        getPreferences().edit().putBoolean(MOTION_BLUR_ENABLED, enabled).apply();
    }

    public static int getMotionBlurIntensity() {
        return getPreferences().getInt(MOTION_BLUR_INTENSITY, 60);
    }

    public static void setMotionBlurIntensity(int value) {
        getPreferences().edit().putInt(MOTION_BLUR_INTENSITY, Math.max(0, Math.min(100, value))).apply();
    }

    public static int getMotionBlurStrength() {
        return getPreferences().getInt(MOTION_BLUR_STRENGTH, 40);
    }

    public static void setMotionBlurStrength(int value) {
        getPreferences().edit().putInt(MOTION_BLUR_STRENGTH, Math.max(0, Math.min(100, value))).apply();
    }

    public static final String SHIZUKU_WHITELIST_MODE = "shizuku_whitelist_mode";
    public static final String DEVICE_OWNER_WHITELIST_MODE = "device_owner_whitelist_mode";

    public static boolean isShizukuWhitelistEnabled() {
        return getPreferences().getBoolean(SHIZUKU_WHITELIST_MODE, false);
    }

    public static void setShizukuWhitelistEnabled(boolean enabled) {
        getPreferences().edit().putBoolean(SHIZUKU_WHITELIST_MODE, enabled).apply();
    }

    public static boolean isDeviceOwnerWhitelistEnabled() {
        return getPreferences().getBoolean(DEVICE_OWNER_WHITELIST_MODE, false);
    }

    public static void setDeviceOwnerWhitelistEnabled(boolean enabled) {
        getPreferences().edit().putBoolean(DEVICE_OWNER_WHITELIST_MODE, enabled).apply();
    }

    public static final String AUTH_TOKEN = "auth_token";

    public static String getAuthToken() {
        String token = getPreferences().getString(AUTH_TOKEN, null);
        if (token == null || token.isEmpty()) {
            token = generateAuthToken();
        }
        return token;
    }

    public static String generateAuthToken() {
        String token = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        getPreferences().edit().putString(AUTH_TOKEN, token).apply();
        return token;
    }

    public static class Keys {
        public static final String KEY_START_ON_BOOT = "start_on_boot";
        public static final String KEY_ADB_WITHOUT_DEVELOPER_OPTIONS = "adb_without_developer_options";
        public static final String KEY_WATCHDOG = "watchdog";
        public static final String KEY_SERVER_RAN_SINCE_BOOT = "server_ran_since_boot";
        public static final String KEY_BOOT_COUNT = "boot_count";
        public static final String KEY_TCP_MODE = "tcp_mode";
        public static final String KEY_TCP_PORT = "tcp_port";
        public static final String KEY_AUTO_DISABLE_USB_DEBUGGING = "auto_disable_usb_debugging";
        public static final String KEY_AUTO_DISABLE_WIRELESS_DEBUGGING = "auto_disable_wireless_debugging";
        public static final String KEY_LANGUAGE = "language";
        public static final String KEY_NIGHT_MODE = "night_mode";
        public static final String KEY_MANUALLY_STOPPED = "manually_stopped";
        public static final String KEY_LAST_ADB_PORT = "last_adb_port";
        public static final String KEY_PERSIST_ADB_PORT = "persist_adb_port";
        public static final String KEY_REQUIRE_INTENT_TOKEN = "require_intent_token";
        public static final String KEY_FORCE_WIRELESS_DEBUGGING = "force_wireless_debugging";
        public static final String KEY_START_METHOD = "start_method";
        public static final String KEY_RUNNING_START_METHOD = "running_start_method";
        public static final String KEY_CATEGORY_ADVANCED = "category_advanced";
        public static final String KEY_LAST_ADB_TRANSPORT = "last_adb_transport";
        public static final String KEY_LAST_ADB_PORT_BOOT = "last_adb_port_boot";
        public static final String KEY_WAIT_FOR_WIFI = "wait_for_wifi";
        public static final String KEY_SYSTEM_START_METHOD = "system_start_method";
        public static final String KEY_LEGACY_PAIRING = "legacy_pairing";
        public static final String KEY_TRANSLATION = "translation";
        public static final String KEY_TRANSLATION_CONTRIBUTORS = "translation_contributors";
        public static final String KEY_LIGHT_THEME = "light_theme";
        public static final String KEY_BLACK_NIGHT_THEME = "black_night_theme";
        public static final String KEY_USE_SYSTEM_COLOR = "use_system_color";
        public static final String KEY_UPDATE_MODE = "update_mode";
    }

    public static final String KEY_HELP = "help";
    public static final String KEY_REPORT_BUG = "report_bug";
    public static final String KEY_FIRST_LAUNCH = "first_launch";
    public static final String KEY_ACTIVATION_METHOD = "activation_method";

    public static final int ACTIVATION_WIRELESS = 0;
    public static final int ACTIVATION_ROOT = 1;
    public static final int ACTIVATION_COMPUTER = 2;

    public static int getActivationMethod() {
        return getPreferences().getInt(KEY_ACTIVATION_METHOD, ACTIVATION_WIRELESS);
    }

    public static void setActivationMethod(int method) {
        getPreferences().edit().putInt(KEY_ACTIVATION_METHOD, method).apply();
    }

    public static boolean isFirstLaunch() {
        return getPreferences().getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public static void setFirstLaunch(boolean first) {
        getPreferences().edit().putBoolean(KEY_FIRST_LAUNCH, first).apply();
    }

    public static class UpdateMode {
        public static final int OFF = 0;
        public static final int STABLE = 1;
        public static final int BETA = 2;
    }

    public static final int ADB_TRANSPORT_UNKNOWN = 0;
    public static final int ADB_TRANSPORT_TLS = 1;
    public static final int ADB_TRANSPORT_TCP = 2;

    public static final String SYSTEM_START_EXPLOIT = "exploit";
    public static final String SYSTEM_START_CUSTOM = "custom";

    @IntDef({
        StartMethod.WIRELESS,
        StartMethod.USB,
        StartMethod.SYSTEM,
        StartMethod.ROOT,
        StartMethod.WIRELESS_NO_NETWORK,
    })
    @Retention(SOURCE)
    public @interface StartMethod {
        int WIRELESS = 0;
        int USB = 1;
        int SYSTEM = 2;
        int ROOT = 3;
        int WIRELESS_NO_NETWORK = 4;
    }

    @StartMethod
    public static int getStartMethod() {
        int defaultMethod = EnvironmentUtils.isRooted()
                ? StartMethod.ROOT
                : StartMethod.WIRELESS;
        return getPreferences().getInt(Keys.KEY_START_METHOD, defaultMethod);
    }

    public static void setStartMethod(@StartMethod int method) {
        getPreferences().edit().putInt(Keys.KEY_START_METHOD, method).apply();
    }

    public static int getRunningStartMethod() {
        return getPreferences().getInt(Keys.KEY_RUNNING_START_METHOD, -1);
    }

    public static void setRunningStartMethod(@StartMethod int method) {
        getPreferences().edit().putInt(Keys.KEY_RUNNING_START_METHOD, method).apply();
    }

    public static boolean getRequireIntentToken() {
        return getPreferences().getBoolean(Keys.KEY_REQUIRE_INTENT_TOKEN, true);
    }

    public static void setRequireIntentToken(boolean require) {
        getPreferences().edit().putBoolean(Keys.KEY_REQUIRE_INTENT_TOKEN, require).apply();
    }

    public static boolean getAdbWithoutDeveloperOptions() {
        return getPreferences().getBoolean(Keys.KEY_ADB_WITHOUT_DEVELOPER_OPTIONS, false);
    }

    public static void setAdbWithoutDeveloperOptions(Context context, boolean enable) {
        getPreferences().edit().putBoolean(Keys.KEY_ADB_WITHOUT_DEVELOPER_OPTIONS, enable).apply();
    }

    public static boolean getWatchdog() {
        return getPreferences().getBoolean(Keys.KEY_WATCHDOG, false);
    }

    public static void setWatchdog(boolean enabled) {
        getPreferences().edit().putBoolean(Keys.KEY_WATCHDOG, enabled).apply();
    }

    public static boolean getManuallyStopped() {
        return getPreferences().getBoolean(Keys.KEY_MANUALLY_STOPPED, false);
    }

    public static void setManuallyStopped(boolean stopped) {
        getPreferences().edit().putBoolean(Keys.KEY_MANUALLY_STOPPED, stopped).apply();
    }

    public static boolean getPersistAdbPort() {
        return getPreferences().getBoolean(Keys.KEY_PERSIST_ADB_PORT, false);
    }

    public static void setPersistAdbPort(boolean enable) {
        getPreferences().edit().putBoolean(Keys.KEY_PERSIST_ADB_PORT, enable).apply();
    }

    public static int getTcpPort() {
        try {
            return Integer.parseInt(getPreferences().getString(Keys.KEY_TCP_PORT, "5555"));
        } catch (Exception e) {
            return 5555;
        }
    }

    public static void setTcpPort(Integer port) {
        if (port != null) {
            getPreferences().edit().putString(Keys.KEY_TCP_PORT, Integer.toString(port)).apply();
        }
    }

    public static boolean getStartOnBoot(Context context) {
        return getStartOnBoot();
    }

    public static boolean getServerRanSinceBoot() {
        return getPreferences().getBoolean(Keys.KEY_SERVER_RAN_SINCE_BOOT, false);
    }

    public static void setServerRanSinceBoot(boolean ran) {
        getPreferences().edit().putBoolean(Keys.KEY_SERVER_RAN_SINCE_BOOT, ran).apply();
    }

    public static boolean isWatchdogStartAllowed() {
        return getPreferences().getBoolean(Keys.KEY_START_ON_BOOT, false) || getServerRanSinceBoot();
    }

    public static void noteBootIfNeeded(Context context) {
        int bootCount = android.provider.Settings.Global.getInt(
                context.getContentResolver(), android.provider.Settings.Global.BOOT_COUNT, -1);
        if (bootCount <= 0) return;

        int stored = getPreferences().getInt(Keys.KEY_BOOT_COUNT, -1);
        if (bootCount == stored) return;

        getPreferences().edit()
                .putInt(Keys.KEY_BOOT_COUNT, bootCount)
                .putBoolean(Keys.KEY_SERVER_RAN_SINCE_BOOT, false)
                .apply();
    }

    public static boolean getForceWirelessDebugging() {
        return getPreferences().getBoolean(Keys.KEY_FORCE_WIRELESS_DEBUGGING, false);
    }

    public static int getLastAdbTransport() {
        return getPreferences().getInt(Keys.KEY_LAST_ADB_TRANSPORT, ADB_TRANSPORT_UNKNOWN);
    }

    public static void setLastAdbTransport(int transport) {
        getPreferences().edit().putInt(Keys.KEY_LAST_ADB_TRANSPORT, transport).apply();
    }

    public static int getLastAdbPort() {
        return getPreferences().getInt(Keys.KEY_LAST_ADB_PORT, -1);
    }

    public static void setLastAdbPort(int port) {
        getPreferences().edit().putInt(Keys.KEY_LAST_ADB_PORT, port).apply();
    }

    public static int getLastAdbPortBoot() {
        return getPreferences().getInt(Keys.KEY_LAST_ADB_PORT_BOOT, -1);
    }

    public static void setLastAdbPortBoot(int bootCount) {
        getPreferences().edit().putInt(Keys.KEY_LAST_ADB_PORT_BOOT, bootCount).apply();
    }

    public static boolean getWaitForWifi() {
        return getPreferences().getBoolean(Keys.KEY_WAIT_FOR_WIFI, true);
    }

    public static void setWaitForWifi(boolean enable) {
        getPreferences().edit().putBoolean(Keys.KEY_WAIT_FOR_WIFI, enable).apply();
    }

    public static void setForceWirelessDebugging(boolean enable) {
        getPreferences().edit().putBoolean(Keys.KEY_FORCE_WIRELESS_DEBUGGING, enable).apply();
    }

    public static String getSystemStartMethod() {
        return getPreferences().getString(Keys.KEY_SYSTEM_START_METHOD, SYSTEM_START_CUSTOM);
    }

    public static void setSystemStartMethod(String method) {
        getPreferences().edit().putString(Keys.KEY_SYSTEM_START_METHOD, method).apply();
    }

    public static boolean getTcpMode() {
        return isTcpMode();
    }

    public static boolean getLegacyPairing() {
        return getPreferences().getBoolean(Keys.KEY_LEGACY_PAIRING, false);
    }

    public static String getLanguageTag() {
        String tag = getPreferences().getString(Keys.KEY_LANGUAGE, null);
        return TextUtils.isEmpty(tag) ? moe.shizuku.manager.utils.AppLocale.SYSTEM : tag;
    }

    public static void setLanguageTag(String tag) {
        getPreferences().edit().putString(Keys.KEY_LANGUAGE, tag).apply();
    }

    public static int getUpdateMode() {
        return getPreferences().getInt(Keys.KEY_UPDATE_MODE, UpdateMode.STABLE);
    }

    public static String getLastPromptedVersion() {
        return getPreferences().getString("lastPromptedVersion", "");
    }

    public static void setLastPromptedVersion(String version) {
        getPreferences().edit().putString("lastPromptedVersion", version).apply();
    }

    public static void setWatchdog(Context context, boolean enable) {
        setWatchdog(enable);
        try {
            if (enable) {
                moe.shizuku.manager.service.WatchdogService.start(context);
                moe.shizuku.manager.receiver.WatchdogAlarmReceiver.schedule(context);
            } else {
                moe.shizuku.manager.service.WatchdogService.stop(context);
                moe.shizuku.manager.receiver.WatchdogAlarmReceiver.cancel(context);
            }
        } catch (Throwable ignored) {}
    }
}
