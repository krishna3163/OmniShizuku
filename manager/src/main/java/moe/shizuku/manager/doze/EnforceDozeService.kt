package moe.shizuku.manager.doze

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.runShellCommand
import java.util.Date
import java.util.Locale

class EnforceDozeService : Service() {

    private val CHANNEL_STATS = "enforcedoze_channel_stats"
    private val CHANNEL_SILENT = "enforcedoze_channel_silent"
    private val PERSISTENT_NOTIF_ID = 1234

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var enterDozeJob: Job? = null
    private var enableSensorsJob: Job? = null
    private var disableSensorsJob: Job? = null

    private var pm: PowerManager? = null
    private var tempWakeLock: PowerManager.WakeLock? = null

    private var dozeAppBlocklist: Set<String> = emptySet()
    private var dozeNotificationBlocklist: Set<String> = emptySet()
    private var dozeUsageData: Set<String> = emptySet()

    var wasBatterySaverOn = false
    var wasWiFiTurnedOn = false
    var wasMobileDataTurnedOn = false
    var wasAirplaneOn = false
    var wasBluetoothOn = false
    var wasGPSOn = false
    var wasHotSpotTurnedOn = false

    private var state = ""
    private var timeEnterDoze: Long = 0
    private var timeExitDoze: Long = 0
    private var lastScreenOff = "Unknown"
    private var maintenance = false

    private val localDozeReceiver: BroadcastReceiver = ServiceBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            localDozeReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )

        reloadSettings()

        val isScreenOn = EnforceDozeManager.isScreenOn(this)
        val isCharging = EnforceDozeManager.isCharging(this)
        EnforceDozeManager.coordinator.transition(
            DozeEvent.ServiceStarted(isScreenOn = isScreenOn, isCharging = isCharging)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(localDozeReceiver) } catch (_: Exception) {}

        enterDozeJob?.cancel()
        enableSensorsJob?.cancel()
        disableSensorsJob?.cancel()

        try {
            tempWakeLock?.let { if (it.isHeld) it.release() }
        } catch (_: Exception) {}
        tempWakeLock = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                runShellCommand("dumpsys sensorservice enable")
                exitDoze(getDeviceIdleState())
            } catch (_: Exception) {
            } finally {
                EnforceDozeManager.coordinator.stopService()
            }
        }
        serviceScope.cancel()

        if (!ShizukuSettings.getPreferences().getBoolean("enforcedoze_serviceEnabled", false)) {
            showDisabledNotification(applicationContext)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (EnforceDozeManager.isShowPersistentNotif(this)) {
                showPersistentNotification()
            } else {
                showSilentNotification()
            }
        } else {
            if (EnforceDozeManager.isShowPersistentNotif(this)) {
                showPersistentNotification()
            } else {
                hidePersistentNotification()
            }
        }

        serviceScope.launch(Dispatchers.IO) {
            addSelfToDozeWhitelist()
            if (!EnforceDozeManager.isScreenOn(this@EnforceDozeService)) {
                scheduleEnterDoze(0L)
            }
        }

        state = getDeviceIdleState()
        hideDisabledNotification(applicationContext)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun reloadSettings() {
        val p = ShizukuSettings.getPreferences()
        dozeAppBlocklist = p.getStringSet("enforcedoze_dozeAppBlockList", emptySet()) ?: emptySet()
        dozeNotificationBlocklist = p.getStringSet("enforcedoze_notificationBlockList", emptySet()) ?: emptySet()
        dozeUsageData = p.getStringSet("enforcedoze_dozeUsageDataAdvanced", emptySet()) ?: emptySet()
    }

    fun addSelfToDozeWhitelist() {
        val pkg = packageName
        runShellCommand("dumpsys deviceidle whitelist +$pkg")
    }

    fun applyDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runShellCommand("dumpsys deviceidle force-idle deep")
        } else {
            runShellCommand("dumpsys deviceidle force-idle")
        }
    }

    fun leaveDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runShellCommand("device_config reset trusted_defaults device_idle")
            runShellCommand("dumpsys deviceidle step")
        } else {
            runShellCommand("dumpsys deviceidle unforce")
        }
    }

    fun scheduleEnterDoze(delayMs: Long) {
        enterDozeJob?.cancel()
        val gen = EnforceDozeManager.coordinator.generation
        enterDozeJob = serviceScope.launch(Dispatchers.IO) {
            if (delayMs > 0) {
                delay(delayMs)
            }
            EnforceDozeManager.coordinator.transition(DozeEvent.PendingDelayElapsed(gen))
            enterDoze(this@EnforceDozeService)
        }
    }

    fun enterDoze(context: Context) {
        if (!EnforceDozeManager.isInsideCustomDozePeriod(context)) return

        val currentState = getDeviceIdleState()
        if (!currentState.equals("IDLE", ignoreCase = true)) {
            if (!EnforceDozeManager.isScreenOn(context)) {
                try {
                    val wl = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OmniShizuku:EnforceDoze")
                    wl?.acquire(5000L)
                    tempWakeLock = wl
                } catch (_: Exception) {}

                try {
                    val modified = computeModifiedSettings(context)
                    val snapshot = DeviceRadioSnapshot(
                        wifiEnabled = isWiFiEnabled(context),
                        mobileDataEnabled = isMobileDataEnabled(context),
                        airplaneModeEnabled = isAirplaneEnabled(),
                        bluetoothEnabled = isBluetoothEnabled(),
                        locationModeEnabled = isLocationEnabled(),
                        batterySaverEnabled = isBatterySaverEnabled(),
                        hotspotEnabled = isHotspotEnabled(context),
                        sensorsRestricted = false,
                        biometricsDisabled = false,
                        blockedAppPackages = dozeAppBlocklist,
                        blockedNotificationPackages = dozeNotificationBlocklist,
                        modifiedSettings = modified,
                        timestampMonotonicMs = SystemClock.elapsedRealtime()
                    )
                    EnforceDozeManager.coordinator.transition(
                        DozeEvent.RadioStateCaptured(EnforceDozeManager.coordinator.generation, snapshot)
                    )

                    if (dozeAppBlocklist.isNotEmpty()) {
                        for (pkg in dozeAppBlocklist) {
                            if (EnforceDozeManager.isWhitelistCurrentApp(context)) {
                                val focused = EnforceDozeManager.getNonRootFocusedPackageName(context)
                                if (focused != null && focused == pkg) continue
                            }
                            setPackageState(context, pkg, enabled = false)
                        }
                    }

                    if (dozeNotificationBlocklist.isNotEmpty()) {
                        for (pkg in dozeNotificationBlocklist) {
                            if (pkg !in dozeAppBlocklist) {
                                setNotificationEnabledForPackage(pkg, enabled = false)
                            }
                        }
                    }

                    timeEnterDoze = System.currentTimeMillis()
                    lastScreenOff = java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                        .format(Date(System.currentTimeMillis()))

                    if (!EnforceDozeManager.isDisableStats(context)) {
                        val charging = if (EnforceDozeManager.isCharging(context)) 0.0f else EnforceDozeManager.getBatteryLevel(context).toFloat()
                        dozeUsageData += "${System.currentTimeMillis()},$charging,ENTER"
                        saveDozeDataStats()
                    }

                    applyDoze()

                    if (EnforceDozeManager.isDisableMotionSensors(context)) {
                        disableSensorsJob?.cancel()
                        disableSensorsJob = serviceScope.launch(Dispatchers.IO) {
                            delay(2000L)
                            val whitelist = EnforceDozeManager.getSensorWhitelistPackage(context)
                            val target = if (whitelist.isNotEmpty()) whitelist else ""
                            runShellCommand("dumpsys sensorservice restrict $target")
                        }
                    }

                    enterDozeHandleNetwork(context)

                    val newIdleState = getDeviceIdleState()
                    EnforceDozeManager.coordinator.transition(
                        DozeEvent.DozeApplied(EnforceDozeManager.coordinator.generation, newIdleState)
                    )
                } finally {
                    try {
                        tempWakeLock?.let { if (it.isHeld) it.release() }
                    } catch (_: Exception) {}
                    tempWakeLock = null
                }
            }
        }
    }

    private fun computeModifiedSettings(context: Context): Set<String> {
        val modified = mutableSetOf<String>()
        val wasHotspot = isHotspotEnabled(context)
        val wasWifi = isWiFiEnabled(context)
        val wasData = isMobileDataEnabled(context)
        val wasAirplane = isAirplaneEnabled()
        val wasBt = isBluetoothEnabled()
        val wasGps = isLocationEnabled()

        if (EnforceDozeManager.isTurnOffAllSensorsInDoze(context)) modified.add(DeviceRadioSnapshot.SETTING_SENSORS)
        if (EnforceDozeManager.isTurnOffBiometricsInDoze(context)) modified.add(DeviceRadioSnapshot.SETTING_BIOMETRICS)
        if (EnforceDozeManager.isTurnOnBatterySaverInDoze(context)) modified.add(DeviceRadioSnapshot.SETTING_BATTERY_SAVER)
        if (EnforceDozeManager.isTurnOnAirplaneInDoze(context) &&
            (EnforceDozeManager.isIgnoreIfHotspot(context) || !wasHotspot) && !wasAirplane) {
            modified.add(DeviceRadioSnapshot.SETTING_AIRPLANE)
        }
        if (EnforceDozeManager.isTurnOffBluetoothInDoze(context) && wasBt) modified.add(DeviceRadioSnapshot.SETTING_BLUETOOTH)
        if (EnforceDozeManager.isTurnOffGPSInDoze(context) && wasGps) modified.add(DeviceRadioSnapshot.SETTING_LOCATION)
        if (EnforceDozeManager.isTurnOffWiFiInDoze(context) &&
            (EnforceDozeManager.isIgnoreIfHotspot(context) || !wasHotspot) && wasWifi) {
            modified.add(DeviceRadioSnapshot.SETTING_WIFI)
        }
        if (EnforceDozeManager.isTurnOffDataInDoze(context) && wasData &&
            (EnforceDozeManager.isIgnoreIfHotspot(context) || !wasHotspot)) {
            modified.add(DeviceRadioSnapshot.SETTING_DATA)
        }
        if (dozeAppBlocklist.isNotEmpty()) modified.add(DeviceRadioSnapshot.SETTING_BLOCKED_APPS)
        if (dozeNotificationBlocklist.isNotEmpty()) modified.add(DeviceRadioSnapshot.SETTING_BLOCKED_NOTIFS)
        return modified
    }

    fun captureCurrentRadioSnapshot(context: Context): DeviceRadioSnapshot = DeviceRadioSnapshot(
        wifiEnabled = isWiFiEnabled(context),
        mobileDataEnabled = isMobileDataEnabled(context),
        airplaneModeEnabled = isAirplaneEnabled(),
        bluetoothEnabled = isBluetoothEnabled(),
        locationModeEnabled = isLocationEnabled(),
        batterySaverEnabled = isBatterySaverEnabled(),
        hotspotEnabled = isHotspotEnabled(context),
        sensorsRestricted = false,
        biometricsDisabled = false,
        blockedAppPackages = emptySet(),
        blockedNotificationPackages = emptySet(),
        modifiedSettings = emptySet(),
        timestampMonotonicMs = SystemClock.elapsedRealtime()
    )

    private fun reEnableBlockedAppsAndNotifications(context: Context) {
        if (dozeAppBlocklist.isNotEmpty()) {
            for (pkg in dozeAppBlocklist) {
                setPackageState(context, pkg, enabled = true)
            }
        }
        if (dozeNotificationBlocklist.isNotEmpty()) {
            for (pkg in dozeNotificationBlocklist) {
                if (pkg !in dozeAppBlocklist) {
                    setNotificationEnabledForPackage(pkg, enabled = true)
                }
            }
        }
    }

    fun exitDoze(newDeviceIdleState: String) {
        enterDozeJob?.cancel()
        timeExitDoze = System.currentTimeMillis()
        leaveDoze()
        state = "ACTIVE"

        if (!EnforceDozeManager.isDisableStats(this)) {
            val charging = if (EnforceDozeManager.isCharging(this)) 0.0f else EnforceDozeManager.getBatteryLevel(this).toFloat()
            dozeUsageData += "${System.currentTimeMillis()},$charging,EXIT"
            saveDozeDataStats()
        }

        val snapshot = EnforceDozeManager.coordinator.snapshot.initialSnapshot
        val currentLive = captureCurrentRadioSnapshot(this)
        val toRestore = snapshot?.computeRequiredRestorations(currentLive) ?: setOf(
            DeviceRadioSnapshot.SETTING_AIRPLANE,
            DeviceRadioSnapshot.SETTING_BLUETOOTH,
            DeviceRadioSnapshot.SETTING_LOCATION,
            DeviceRadioSnapshot.SETTING_WIFI,
            DeviceRadioSnapshot.SETTING_DATA,
            DeviceRadioSnapshot.SETTING_BATTERY_SAVER,
            DeviceRadioSnapshot.SETTING_SENSORS,
            DeviceRadioSnapshot.SETTING_BIOMETRICS
        )

        leaveDozeHandleNetworkSelective(this, toRestore)
        reEnableBlockedAppsAndNotifications(this)

        if (EnforceDozeManager.isDisableMotionSensors(this)) {
            enableSensorsJob?.cancel()
            enableSensorsJob = serviceScope.launch(Dispatchers.IO) {
                delay(2000L)
                runShellCommand("dumpsys sensorservice enable")
                autoRotateBrightnessFix()
            }
        }

        EnforceDozeManager.coordinator.transition(
            DozeEvent.RestoringCompleted(EnforceDozeManager.coordinator.generation, toRestore)
        )
    }

    fun enterDozeHandleNetwork(context: Context) {
        actualEnterDozeHandleNetwork(context, null)
    }

    fun actualEnterDozeHandleNetwork(context: Context, packageName: String?) {
        wasWiFiTurnedOn = isWiFiEnabled(context)
        wasMobileDataTurnedOn = isMobileDataEnabled(context)
        wasAirplaneOn = isAirplaneEnabled()
        wasBluetoothOn = isBluetoothEnabled()
        wasGPSOn = isLocationEnabled()
        wasHotSpotTurnedOn = isHotspotEnabled(context)
        wasBatterySaverOn = isBatterySaverEnabled()

        if (EnforceDozeManager.isTurnOffAllSensorsInDoze(context)) {
            val whitelist = EnforceDozeManager.getSensorWhitelistPackage(context)
            val target = if (whitelist.isNotEmpty()) whitelist else ""
            runShellCommand("dumpsys sensorservice restrict $target")
        }
        if (EnforceDozeManager.isTurnOffBiometricsInDoze(context)) {
            runShellCommand("settings put secure biometric_keyguard_enabled 0")
        }
        if (EnforceDozeManager.isTurnOnBatterySaverInDoze(context)) {
            runShellCommand("settings put global low_power 1")
        }
        if (EnforceDozeManager.isTurnOnAirplaneInDoze(context) &&
            (EnforceDozeManager.isIgnoreIfHotspot(context) || !wasHotSpotTurnedOn) && !wasAirplaneOn && packageName == null) {
            runShellCommand("settings put global airplane_mode_on 1")
            runShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
        }
        if (EnforceDozeManager.isTurnOffBluetoothInDoze(context) && wasBluetoothOn && packageName == null) {
            runShellCommand("svc bluetooth disable")
        }
        if (EnforceDozeManager.isTurnOffGPSInDoze(context) && wasGPSOn && packageName == null) {
            runShellCommand("settings put secure location_mode 0")
        }
        if (EnforceDozeManager.isTurnOffWiFiInDoze(context) &&
            (EnforceDozeManager.isIgnoreIfHotspot(context) || !wasHotSpotTurnedOn) && wasWiFiTurnedOn && packageName == null) {
            runShellCommand("svc wifi disable")
        }
        if (EnforceDozeManager.isTurnOffDataInDoze(context) && wasMobileDataTurnedOn &&
            (EnforceDozeManager.isIgnoreIfHotspot(context) || !wasHotSpotTurnedOn) &&
            (packageName == null || wasWiFiTurnedOn)) {
            runShellCommand("svc data disable")
        }
    }

    fun leaveDozeHandleNetwork(context: Context) {
        val snapshot = EnforceDozeManager.coordinator.snapshot.initialSnapshot
        val currentLive = captureCurrentRadioSnapshot(context)
        val toRestore = snapshot?.computeRequiredRestorations(currentLive) ?: setOf(
            DeviceRadioSnapshot.SETTING_AIRPLANE,
            DeviceRadioSnapshot.SETTING_BLUETOOTH,
            DeviceRadioSnapshot.SETTING_LOCATION,
            DeviceRadioSnapshot.SETTING_WIFI,
            DeviceRadioSnapshot.SETTING_DATA,
            DeviceRadioSnapshot.SETTING_BATTERY_SAVER,
            DeviceRadioSnapshot.SETTING_SENSORS,
            DeviceRadioSnapshot.SETTING_BIOMETRICS
        )
        leaveDozeHandleNetworkSelective(context, toRestore)
    }

    fun leaveDozeHandleNetworkSelective(context: Context, toRestore: Set<String>) {
        if (DeviceRadioSnapshot.SETTING_AIRPLANE in toRestore) {
            runShellCommand("settings put global airplane_mode_on 0")
            runShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")
        }
        if (DeviceRadioSnapshot.SETTING_BLUETOOTH in toRestore) {
            runShellCommand("svc bluetooth enable")
        }
        if (DeviceRadioSnapshot.SETTING_LOCATION in toRestore) {
            runShellCommand("settings put secure location_mode 1")
        }
        if (DeviceRadioSnapshot.SETTING_WIFI in toRestore) {
            runShellCommand("svc wifi enable")
        }
        if (DeviceRadioSnapshot.SETTING_SENSORS in toRestore) {
            runShellCommand("dumpsys sensorservice enable")
        }
        if (DeviceRadioSnapshot.SETTING_BATTERY_SAVER in toRestore) {
            runShellCommand("settings put global low_power 0")
        }
        if (DeviceRadioSnapshot.SETTING_DATA in toRestore) {
            runShellCommand("svc data enable")
        }
        if (DeviceRadioSnapshot.SETTING_BIOMETRICS in toRestore) {
            runShellCommand("settings put secure biometric_keyguard_enabled 1")
        }
    }

    fun handleScreenOn(context: Context) {
        enterDozeJob?.cancel()
        try {
            tempWakeLock?.let { if (it.isHeld) it.release() }
        } catch (_: Exception) {}
        tempWakeLock = null

        leaveDozeHandleNetwork(context)
        wasWiFiTurnedOn = false
        wasBatterySaverOn = false
        wasMobileDataTurnedOn = false
        wasAirplaneOn = false
        wasBluetoothOn = false
        wasGPSOn = false

        val newState = getDeviceIdleState()
        if (!newState.equals("ACTIVE", ignoreCase = true) || !state.equals("ACTIVE", ignoreCase = true)) {
            exitDoze(newState)
        } else {
            reEnableBlockedAppsAndNotifications(context)
        }
        if (EnforceDozeManager.isTurnOffBiometricsInDoze(context)) {
            serviceScope.launch(Dispatchers.IO) {
                runShellCommand("settings put secure biometric_keyguard_enabled 1")
            }
        }
    }

    fun autoRotateBrightnessFix() {
        if (!EnforceDozeManager.isUseAutoRotateAndBrightnessFix(this)) return
        try {
            val ar = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
            Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (ar == 1) 0 else 1)
            Thread.sleep(100)
            Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, ar)
            Thread.sleep(100)
            val ab = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, if (ab == 1) 0 else 1)
            Thread.sleep(100)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, ab)
        } catch (_: Exception) {}
    }

    fun setPackageState(context: Context, packageName: String, enabled: Boolean) {
        val cmd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            "pm ${if (enabled) "unsuspend" else "suspend"} $packageName"
        } else {
            "pm ${if (enabled) "enable" else "disable"} $packageName"
        }
        runShellCommand(cmd)
    }

    fun setNotificationEnabledForPackage(packageName: String, enabled: Boolean) {
        try {
            val cls = Class.forName("android.app.INotificationManager")
            val inner = cls.declaredClasses.firstOrNull() ?: return
            val field = inner.getDeclaredField("TRANSACTION_setNotificationsEnabledForPackage")
            field.isAccessible = true
            val cmd = field.getInt(null).toString()
            val pkgInfo = packageManager.getPackageInfo(packageName, 0)
            val uid = pkgInfo.applicationInfo?.uid ?: 0
            runShellCommand("service call notification $cmd s16 $packageName i32 $uid i32 ${if (enabled) 1 else 0}")
        } catch (_: Exception) {}
    }

    fun getDeviceIdleState(): String {
        val output = runShellCommand("dumpsys deviceidle") ?: return "ACTIVE"
        return when {
            output.contains("mState=ACTIVE") -> "ACTIVE"
            output.contains("mState=IDLE") -> "IDLE"
            output.contains("mState=IDLE_PENDING") -> "IDLE_PENDING"
            output.contains("mState=SENSING") -> "SENSING"
            output.contains("mState=LOCATING") -> "LOCATING"
            output.contains("mState=IDLE_MAINTENANCE") -> "IDLE_MAINTENANCE"
            output.contains("mState=PRE_IDLE") -> "PRE_IDLE"
            else -> if (pm?.isDeviceIdleMode == true) "IDLE" else "ACTIVE"
        }
    }

    private fun isWiFiEnabled(context: Context): Boolean {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        return wifi.isWifiEnabled
    }

    private fun isMobileDataEnabled(context: Context): Boolean {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        return tm.getSimState() == android.telephony.TelephonyManager.SIM_STATE_READY &&
            tm.getDataState() != android.telephony.TelephonyManager.DATA_DISCONNECTED
    }

    private fun isAirplaneEnabled(): Boolean =
        Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

    private fun isBluetoothEnabled(): Boolean =
        Settings.Global.getInt(contentResolver, "bluetooth_on", 0) != 0

    private fun isLocationEnabled(): Boolean =
        Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF

    private fun isHotspotEnabled(context: Context): Boolean {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        return try {
            val method = wifi.javaClass.getDeclaredMethod("getWifiApState")
            method.isAccessible = true
            val state = method.invoke(wifi) as Int
            state == 12 || state == 13
        } catch (_: Exception) {
            false
        }
    }

    private fun isBatterySaverEnabled(): Boolean =
        Settings.Global.getInt(contentResolver, "low_power", 0) >= 1

    private fun saveDozeDataStats() {
        val p = ShizukuSettings.getPreferences()
        p.edit().putStringSet("enforcedoze_dozeUsageDataAdvanced", dozeUsageData).apply()
    }

    private fun showPersistentNotification() {
        createNotificationChannels()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL_STATS)
            .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.enforcedoze_stats_no_data)))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pi)
            .setOngoing(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(PERSISTENT_NOTIF_ID, builder.build())
        } else {
            startForeground(PERSISTENT_NOTIF_ID, builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }
    }

    fun updatePersistentNotification(timeSpent: Int, batteryUsage: Int) {
        createNotificationChannels()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL_STATS)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.enforcedoze_stats_long_text, lastScreenOff, timeSpent, batteryUsage))
                .setSummaryText(getString(R.string.enforcedoze_stats_summary_text, batteryUsage)))
            .setShowWhen(false)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pi)
            .setOngoing(true)

        startForeground(PERSISTENT_NOTIF_ID, builder.build())
    }

    private fun showSilentNotification() {
        createNotificationChannels()
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_SILENT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, CHANNEL_SILENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.enforcedoze_silent_notif_title))
            .setContentText(getString(R.string.enforcedoze_silent_notif_text))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(PERSISTENT_NOTIF_ID, builder.build())
        } else {
            startForeground(PERSISTENT_NOTIF_ID, builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }
    }

    private fun hidePersistentNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CHANNEL_STATS, "EnforceDoze Stats", NotificationManager.IMPORTANCE_MIN).apply {
            description = "Persistent stats notification for EnforceDoze"
        })
        nm.createNotificationChannel(NotificationChannel(CHANNEL_SILENT, "EnforceDoze Silent", NotificationManager.IMPORTANCE_MIN).apply {
            description = "Silent foreground service notification"
            setSound(null, null)
        })
    }

    private fun showDisabledNotification(context: Context) {
        val p = ShizukuSettings.getPreferences()
        if (!p.getBoolean("enforcedoze_showDisabledNotification", false)) return
        val channel = "enforcedoze_channel_disabled"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(channel, "EnforceDoze Disabled", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Notification when EnforceDoze is disabled"
            })
        }
        val intent = Intent(this, EnableEnforceDozeReceiver::class.java).apply {
            action = "moe.shizuku.manager.enforcedoze.ENABLE"
        }
        val pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.enforcedoze_disabled_notif_title))
            .setContentText(getString(R.string.enforcedoze_disabled_notif_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pi)
            .setAutoCancel(true)
        val id = 9876
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, builder.build())
    }

    private fun hideDisabledNotification(context: Context) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(9876)
    }

    inner class ServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            serviceScope.launch(Dispatchers.IO) {
                when (action) {
                    Intent.ACTION_USER_PRESENT -> {
                        EnforceDozeManager.coordinator.transition(
                            DozeEvent.ScreenTurnedOn(SystemClock.elapsedRealtime())
                        )
                        withContext(Dispatchers.Main) {
                            handleScreenOn(context)
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                        val isLocked = km?.isKeyguardLocked == true
                        if (!isLocked || !EnforceDozeManager.isWaitForUnlock(context)) {
                            EnforceDozeManager.coordinator.transition(
                                DozeEvent.ScreenTurnedOn(SystemClock.elapsedRealtime())
                            )
                            withContext(Dispatchers.Main) {
                                handleScreenOn(context)
                            }
                        }
                        if (EnforceDozeManager.isTurnOffBiometricsInDoze(context)) {
                            runShellCommand("settings put secure biometric_keyguard_enabled 1")
                        }
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        val isCharging = EnforceDozeManager.isCharging(context)
                        val inComm = isUserInCommunicationCall(context)
                        val inCall = isUserInCall(context)
                        val callActive = inComm || inCall

                        if (EnforceDozeManager.isDisableWhenCharging(context) && isCharging) {
                            return@launch
                        }
                        if (callActive) {
                            return@launch
                        }

                        val delayMs = if (EnforceDozeManager.isIgnoreLockscreenTimeout(context)) {
                            EnforceDozeManager.getDozeEnterDelay(context) * 1000L
                        } else {
                            val time = Settings.Secure.getInt(context.contentResolver, "lock_screen_lock_after_timeout", 5000)
                            val delay = EnforceDozeManager.getDozeEnterDelay(context) * 1000L
                            time + delay
                        }

                        EnforceDozeManager.coordinator.transition(
                            DozeEvent.ScreenTurnedOff(
                                isCharging = isCharging,
                                isCallActive = callActive,
                                delayMs = delayMs,
                                currentTimeMs = SystemClock.elapsedRealtime()
                            )
                        )
                        scheduleEnterDoze(delayMs)
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        EnforceDozeManager.coordinator.transition(
                            DozeEvent.PowerConnected(SystemClock.elapsedRealtime())
                        )
                        if (EnforceDozeManager.isDisableWhenCharging(context)) {
                            enterDozeJob?.cancel()
                            withContext(Dispatchers.Main) {
                                exitDoze(getDeviceIdleState())
                            }
                        }
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        EnforceDozeManager.coordinator.transition(
                            DozeEvent.PowerDisconnected(SystemClock.elapsedRealtime())
                        )
                        if (!EnforceDozeManager.isScreenOn(context)) {
                            scheduleEnterDoze(0L)
                        }
                    }
                    PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED,
                    "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED" -> {
                        val idleMode = getDeviceIdleState()
                        EnforceDozeManager.coordinator.transition(
                            DozeEvent.IdleModeChanged(idleMode)
                        )
                    }
                }
            }
        }
    }
}

private fun isUserInCommunicationCall(context: Context): Boolean {
    val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    return am.mode == android.media.AudioManager.MODE_IN_CALL || am.mode == android.media.AudioManager.MODE_IN_COMMUNICATION
}

private fun isUserInCall(context: Context): Boolean {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
    return tm.callState == android.telephony.TelephonyManager.CALL_STATE_OFFHOOK ||
        tm.callState == android.telephony.TelephonyManager.CALL_STATE_RINGING
}