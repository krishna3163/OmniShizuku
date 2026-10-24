package af.shizuku.manager.settings

import af.shizuku.core.ui.AppBarActivity
import af.shizuku.manager.R
import af.shizuku.manager.adb.AdbPairingAccessibilityService
import af.shizuku.manager.adb.LocalNetworkPermission
import af.shizuku.manager.database.RootCompatHelper
import af.shizuku.manager.databinding.ActivityServiceDoctorBinding
import af.shizuku.manager.databinding.ItemDoctorCheckBinding
import af.shizuku.manager.home.tryEnableAccessibilityAutomatically
import af.shizuku.manager.ktx.themeColor
import af.shizuku.manager.utils.DeviceOptimizer
import af.shizuku.manager.utils.EnvironmentUtils
import af.shizuku.manager.utils.SettingsHelper
import af.shizuku.manager.utils.SettingsPage
import af.shizuku.manager.utils.ShizukuStateMachine
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import timber.log.Timber

class ServiceDoctorActivity : AppBarActivity() {
    private lateinit var binding: ActivityServiceDoctorBinding
    private lateinit var checkListAdapter: CheckListAdapter
    private lateinit var tipsTextView: TextView
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val batteryOptimizationListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            runDiagnostics()
        }

    private val localNetworkPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            runDiagnostics()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServiceDoctorBinding.inflate(layoutInflater, rootView, true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tipsTextView = binding.tipsText

        binding.btnRerun.setOnClickListener {
            af.shizuku.manager.utils.HapticUtils
                .segmentTick(it)
            runDiagnostics()
        }

        binding.checkList.clipToPadding = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.checkList) { view, insets ->
            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
                )
            view.setPadding(bars.left, view.paddingTop, bars.right, bars.bottom)
            insets
        }

        binding.checkList.layoutManager = LinearLayoutManager(this)
        checkListAdapter = CheckListAdapter()
        binding.checkList.adapter = checkListAdapter

        runDiagnostics()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /** Sets a secure setting to 0 (needs WRITE_SECURE_SETTINGS) and confirms it stuck. */
    private fun trySecureSettingOff(key: String): Boolean {
        if (!SettingsHelper.hasWriteSecureSettings(this)) return false
        return runCatching {
            android.provider.Settings.Secure
                .putInt(contentResolver, key, 0)
            android.provider.Settings.Secure
                .getInt(contentResolver, key, 1) == 0
        }.getOrDefault(false)
    }

    private fun runDiagnostics() {
        val checks = mutableListOf<DoctorCheck>()
        val tips = mutableListOf<String>()

        // 1. Background protection (battery optimization + Samsung sleeping apps)
        val bgState = DeviceOptimizer.backgroundState(this)
        val isIgnoring = bgState == DeviceOptimizer.BackgroundState.PROTECTED
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_battery),
                getString(
                    when (bgState) {
                        DeviceOptimizer.BackgroundState.PROTECTED -> R.string.doctor_status_ok
                        DeviceOptimizer.BackgroundState.RESTRICTED -> R.string.doctor_status_samsung_sleeping_restricted
                        DeviceOptimizer.BackgroundState.UNPROTECTED -> R.string.doctor_status_optimized
                    },
                ),
                isIgnoring,
                onFix =
                    if (!isIgnoring) {
                        {
                            // Apply the exemptions through Shizuku/root; if that is not enough
                            // (or unavailable) fall back to the system or Samsung screen.
                            serviceScope.launch {
                                if (DeviceOptimizer.protectSelf(this@ServiceDoctorActivity)) {
                                    Toast.makeText(this@ServiceDoctorActivity, R.string.doctor_fix_applied, Toast.LENGTH_SHORT).show()
                                    runDiagnostics()
                                } else if (EnvironmentUtils.isSamsung()) {
                                    DeviceOptimizer.snoozeNag(7)
                                    SettingsPage.Samsung.BackgroundUsageLimits.launch(this@ServiceDoctorActivity)
                                } else {
                                    SettingsHelper.requestIgnoreBatteryOptimizations(this@ServiceDoctorActivity, batteryOptimizationListener)
                                }
                            }
                        }
                    } else {
                        null
                    },
            ),
        )
        if (!isIgnoring) tips.add("• " + getString(R.string.doctor_tip_battery))

        // 2. Wireless ADB
        val adbPort = EnvironmentUtils.getAdbTcpPort()
        val adbOk = adbPort > 0
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_adb),
                if (adbOk) "${getString(R.string.doctor_status_ok)} ($adbPort)" else getString(R.string.doctor_status_not_enabled),
                adbOk,
                onFix =
                    if (!adbOk && SettingsHelper.hasWriteSecureSettings(this)) {
                        {
                            // Turn on USB + Wireless debugging directly; the port appears a moment later.
                            runCatching {
                                android.provider.Settings.Global
                                    .putInt(contentResolver, android.provider.Settings.Global.ADB_ENABLED, 1)
                                android.provider.Settings.Global
                                    .putInt(contentResolver, "adb_wifi_enabled", 1)
                            }
                            Toast.makeText(this, R.string.doctor_fix_applied, Toast.LENGTH_SHORT).show()
                            serviceScope.launch {
                                kotlinx.coroutines.delay(2000)
                                runDiagnostics()
                            }
                        }
                    } else {
                        null
                    },
            ),
        )
        if (!adbOk && !EnvironmentUtils.isRooted()) tips.add("• " + getString(R.string.doctor_tip_adb))

        // 2b. Local Network Permission (Android 16+ / 17+)
        // ACCESS_LOCAL_NETWORK (API 37) and NEARBY_WIFI_DEVICES (API 36) gate mDNS discovery of
        // the wireless-debugging service. Without them Shizuku silently fails to start in ADB mode.
        if (Build.VERSION.SDK_INT >= 36) {
            val lnpGranted = LocalNetworkPermission.granted(this)
            val lnpPermName = if (Build.VERSION.SDK_INT >= 37) "ACCESS_LOCAL_NETWORK" else "NEARBY_WIFI_DEVICES"
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_local_network),
                    if (lnpGranted) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_not_enabled),
                    lnpGranted,
                    onFix =
                        if (!lnpGranted) {
                            {
                                val perm = LocalNetworkPermission.required()
                                if (perm != null) {
                                    serviceScope.launch {
                                        if (DeviceOptimizer.grantSelf(this@ServiceDoctorActivity, perm)) {
                                            Toast.makeText(this@ServiceDoctorActivity, R.string.doctor_fix_applied, Toast.LENGTH_SHORT).show()
                                            runDiagnostics()
                                        } else {
                                            localNetworkPermissionLauncher.launch(perm)
                                        }
                                    }
                                }
                            }
                        } else {
                            null
                        },
                ),
            )
            if (!lnpGranted) tips.add("• " + getString(R.string.doctor_tip_local_network, lnpPermName))
        }

        // 3. Root
        val isRooted = EnvironmentUtils.isRooted()
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_root),
                if (isRooted) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_not_enabled),
                isRooted,
            ),
        )

        // 4. Shizuku Server
        val isRunning = ShizukuStateMachine.isRunning()
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_server),
                if (isRunning) getString(R.string.doctor_status_running) else getString(R.string.doctor_status_stopped),
                isRunning,
                onFix =
                    if (!isRunning) {
                        {
                            Toast.makeText(this, R.string.doctor_fix_starting, Toast.LENGTH_SHORT).show()
                            af.shizuku.manager.receiver.ShizukuReceiverStarter
                                .start(this, forceStart = true)
                            serviceScope.launch {
                                kotlinx.coroutines.delay(4000)
                                runDiagnostics()
                            }
                        }
                    } else {
                        null
                    },
            ),
        )

        // 5. Secure Settings (WRITE_SECURE_SETTINGS)
        val hasSecureSettings = SettingsHelper.hasWriteSecureSettings(this)
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_secure_settings),
                if (hasSecureSettings) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_not_enabled),
                hasSecureSettings,
                onFix =
                    if (!hasSecureSettings) {
                        {
                            SettingsHelper.promptWriteSecureSettings(this)
                            runDiagnostics()
                        }
                    } else {
                        null
                    },
            ),
        )

        // 5b. Accessibility (for AI automation features)
        val isAccessibilityEnabled = SettingsHelper.isAccessibilityServiceEnabled(this, AdbPairingAccessibilityService::class.java)
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_accessibility),
                if (isAccessibilityEnabled) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_not_enabled),
                isAccessibilityEnabled,
                onFix =
                    if (!isAccessibilityEnabled) {
                        {
                            // Enable it ourselves (direct write, Shizuku or root); only if none of
                            // those work do we send the user to the Accessibility screen.
                            serviceScope.launch {
                                val ok = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { tryEnableAccessibilityAutomatically() }
                                if (ok) {
                                    Toast.makeText(this@ServiceDoctorActivity, R.string.doctor_fix_applied, Toast.LENGTH_SHORT).show()
                                    runDiagnostics()
                                } else {
                                    SettingsPage.Accessibility.launch(this@ServiceDoctorActivity)
                                }
                            }
                        }
                    } else {
                        null
                    },
            ),
        )

        // 5c. Device Hardening & Doze Whitelisting
        val isHardeningActive = isIgnoring && hasSecureSettings
        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_device_hardening),
                if (isHardeningActive) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_fix_available),
                isHardeningActive,
                onFix =
                    if (!isHardeningActive) {
                        {
                            serviceScope.launch {
                                DeviceOptimizer.applyFixes(this@ServiceDoctorActivity)
                                runDiagnostics()
                            }
                        }
                    } else {
                        null
                    },
            ),
        )

        // 5d. Google Wallet & Play Integrity Security Check
        val hasTmpSu =
            java.io.File("/data/local/tmp/su").exists() ||
                java.io.File("/data/local/su").exists() ||
                java.io.File("/data/local/bin/su").exists() ||
                java.io.File("/data/local/xbin/su").exists()
        val isMagiskMocking =
            af.shizuku.manager.ShizukuSettings
                .isRootMagiskMockingEnabled()
        val isBusyboxMocking =
            af.shizuku.manager.ShizukuSettings
                .isRootBusyboxMockingEnabled()
        val isSpoofing =
            af.shizuku.manager.ShizukuSettings
                .isSpoofDeviceEnabled()
        val walletSecurityOk = !hasTmpSu && !isMagiskMocking && !isBusyboxMocking && !isSpoofing

        val walletStatus =
            when {
                hasTmpSu -> getString(R.string.doctor_wallet_risk_tmp_su)
                isMagiskMocking -> getString(R.string.doctor_wallet_risk_magisk)
                isBusyboxMocking -> getString(R.string.doctor_wallet_risk_busybox)
                isSpoofing -> getString(R.string.doctor_wallet_risk_spoof)
                else -> getString(R.string.doctor_wallet_ok)
            }

        checks.add(
            DoctorCheck(
                getString(R.string.doctor_check_wallet_integrity),
                walletStatus,
                walletSecurityOk,
                onFix =
                    if (!walletSecurityOk) {
                        {
                            MaterialAlertDialogBuilder(this@ServiceDoctorActivity)
                                .setTitle(R.string.doctor_check_wallet_integrity)
                                .setMessage(R.string.doctor_tip_wallet_integrity)
                                .setPositiveButton(R.string.action_continue) { _, _ ->
                                    serviceScope.launch {
                                        val cleaned = RootCompatHelper.cleanupBridgeFromTmp(this@ServiceDoctorActivity)

                                        if (isMagiskMocking) {
                                            af.shizuku.manager.ShizukuSettings
                                                .setRootMagiskMockingEnabled(false)
                                        }
                                        if (isBusyboxMocking) {
                                            af.shizuku.manager.ShizukuSettings
                                                .setRootBusyboxMockingEnabled(false)
                                        }
                                        if (af.shizuku.manager.ShizukuSettings
                                                .isSuBridgeEnabled()
                                        ) {
                                            af.shizuku.manager.ShizukuSettings
                                                .setSuBridgeEnabled(false)
                                        }
                                        if (isSpoofing) {
                                            af.shizuku.manager.ShizukuSettings
                                                .setSpoofDeviceEnabled(false)
                                        }
                                        af.shizuku.manager.ShizukuSettings
                                            .syncAllPlusFeaturesToServer()

                                        val refreshResult = RootCompatHelper.refreshGoogleWalletAttestation(this@ServiceDoctorActivity)

                                        if (cleaned) {
                                            val successMsg =
                                                when (refreshResult) {
                                                    RootCompatHelper.WalletRefreshResult.CACHE_CLEARED_ROOT ->
                                                        getString(R.string.doctor_fix_wallet_success)
                                                    RootCompatHelper.WalletRefreshResult.WALLET_CLEARED_PROCESSES_KILLED ->
                                                        getString(R.string.doctor_fix_wallet_success_adb_mode)
                                                    RootCompatHelper.WalletRefreshResult.FORCE_STOPPED_ONLY ->
                                                        getString(R.string.doctor_fix_wallet_success_no_shizuku)
                                                }
                                            MaterialAlertDialogBuilder(this@ServiceDoctorActivity)
                                                .setTitle(R.string.doctor_check_wallet_integrity)
                                                .setMessage(successMsg)
                                                .setPositiveButton(R.string.doctor_action_open_wallet) { _, _ ->
                                                    try {
                                                        val pm = packageManager
                                                        val intent = pm.getLaunchIntentForPackage("com.google.android.apps.walletnfcrel")
                                                        if (intent != null) startActivity(intent)
                                                    } catch (_: Exception) {
                                                    }
                                                    runDiagnostics()
                                                }.apply {
                                                    // Show the GMS data clear shortcut whenever root didn't
                                                    // delete the files — it's the guaranteed immediate fallback.
                                                    if (refreshResult != RootCompatHelper.WalletRefreshResult.CACHE_CLEARED_ROOT) {
                                                        setNeutralButton(R.string.doctor_action_clear_gms_cache) { _, _ ->
                                                            try {
                                                                val intent =
                                                                    Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                                        data = android.net.Uri.parse("package:com.google.android.gms")
                                                                    }
                                                                startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Timber.w(e, "Could not open GMS settings")
                                                            }
                                                            runDiagnostics()
                                                        }
                                                    }
                                                }.show()
                                        } else {
                                            MaterialAlertDialogBuilder(this@ServiceDoctorActivity)
                                                .setTitle(R.string.doctor_check_wallet_integrity)
                                                .setMessage(getString(R.string.doctor_fix_wallet_failed) + "\n\n" + RootCompatHelper.ADB_CLEANUP_COMMAND)
                                                .setPositiveButton(R.string.doctor_action_copy_adb_cmd) { _, _ ->
                                                    val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("ADB Cleanup", RootCompatHelper.ADB_CLEANUP_COMMAND))
                                                    Toast.makeText(this@ServiceDoctorActivity, R.string.doctor_adb_cmd_copied, Toast.LENGTH_SHORT).show()
                                                    runDiagnostics()
                                                }.setNegativeButton(android.R.string.cancel) { _, _ -> runDiagnostics() }
                                                .show()
                                        }
                                    }
                                }.setNegativeButton(android.R.string.cancel, null)
                                .show()
                        }
                    } else {
                        null
                    },
            ),
        )
        if (!walletSecurityOk) {
            tips.add("• " + getString(R.string.doctor_tip_wallet_integrity))
        }

        // 6. Xiaomi Restricted ADB + HyperOS background kill
        if (EnvironmentUtils.isXiaomi()) {
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_xiaomi_adb),
                    getString(R.string.doctor_status_xiaomi_adb_unknown),
                    false,
                    onFix = { SettingsPage.Developer.Options.launch(this) },
                ),
            )
            // HyperOS kills background processes at the Doze interval unless the app has
            // "No restrictions" set in PowerKeeper. Provide a direct deep-link to that page.
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_xiaomi_autostart),
                    getString(R.string.doctor_status_xiaomi_autostart_unknown),
                    false,
                    onFix = { SettingsPage.Xiaomi.BatterySettings.launch(this) },
                ),
            )
            tips.add("• " + getString(R.string.doctor_tip_xiaomi))
            tips.add("• " + getString(R.string.doctor_tip_xiaomi_battery))
        }

        // 6b. Oppo/OnePlus Restricted ADB (ColorOS/OxygenOS)
        if (EnvironmentUtils.isOppo() || EnvironmentUtils.isOnePlus()) {
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_oppo_permission),
                    getString(R.string.doctor_status_manual_check),
                    false,
                    onFix = { SettingsPage.Developer.Options.launch(this) },
                ),
            )
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_oppo_battery),
                    getString(R.string.doctor_status_oppo_battery_unknown),
                    false,
                    onFix = { SettingsPage.Oppo.BatterySettings.launch(this) },
                ),
            )
            tips.add("• " + getString(R.string.doctor_tip_oppo_permission))
            tips.add("• " + getString(R.string.doctor_tip_oppo_battery))
        }

        // 6c. TCL Device Polish
        if (EnvironmentUtils.isTCL()) {
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_tcl_autostart),
                    getString(R.string.doctor_status_tcl_autostart_unknown),
                    false,
                    onFix = { SettingsPage.TCL.AutoStart.launch(this) },
                ),
            )
            tips.add("• " + getString(R.string.doctor_tip_tcl_background))
        }

        // 7. Samsung Auto Blocker (One UI 6.1+)
        if (EnvironmentUtils.isSamsung()) {
            val oneUi = EnvironmentUtils.getOneUiVersion()
            val isAutoBlockerOff = SettingsHelper.isSamsungAutoBlockerDisabled(this)
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_samsung_autoblocker),
                    if (isAutoBlockerOff) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_autoblocker_on),
                    isAutoBlockerOff,
                    onFix =
                        if (!isAutoBlockerOff || oneUi >= 6) {
                            {
                                if (!isAutoBlockerOff && trySecureSettingOff("rampart_enabled")) {
                                    Toast.makeText(this, R.string.doctor_fix_applied, Toast.LENGTH_SHORT).show()
                                    runDiagnostics()
                                } else {
                                    SettingsPage.Samsung.AutoBlocker.launch(this)
                                }
                            }
                        } else {
                            null
                        },
                ),
            )

            // OneUI 8+ specific check for "Maximum Restrictions"
            if (oneUi >= 8) {
                val isMaxRestrictionsOff = SettingsHelper.isSamsungMaxRestrictionsDisabled(this)
                checks.add(
                    DoctorCheck(
                        getString(R.string.doctor_check_samsung_max_restrictions),
                        if (isMaxRestrictionsOff) getString(R.string.doctor_status_ok) else getString(R.string.doctor_status_samsung_max_restrictions_on),
                        isMaxRestrictionsOff,
                        onFix =
                            if (!isMaxRestrictionsOff) {
                                {
                                    if (trySecureSettingOff("rampart_max_restrictions_enabled")) {
                                        Toast.makeText(this, R.string.doctor_fix_applied, Toast.LENGTH_SHORT).show()
                                        runDiagnostics()
                                    } else {
                                        SettingsPage.Samsung.AutoBlocker.launch(this)
                                    }
                                }
                            } else {
                                null
                            },
                    ),
                )
            }

            // Sleeping-apps protection is the single "Battery optimization" row above: it reads the
            // system's actual restrictions, so a separate Samsung row would only repeat it.

            if (oneUi >= 6) {
                tips.add("• " + getString(R.string.doctor_tip_samsung_autoblocker))
                if (oneUi >= 8) {
                    tips.add("• " + getString(R.string.doctor_tip_oneui8_max_restrictions))
                }
                tips.add("• " + getString(R.string.doctor_tip_oneui_connectivity))
                tips.add("• " + getString(R.string.doctor_tip_s22_ultra))
            }
        }

        // 8. Secure Folder / Secondary User detection
        if (EnvironmentUtils.isSecondaryUser()) {
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_secondary_user),
                    getString(R.string.doctor_status_detected),
                    false,
                ),
            )
            tips.add("• " + getString(R.string.doctor_tip_secure_folder))
        }

        // 8b. SU Bridge — Android 16+ ADB mode limitation
        // On Android 16+ (API 36), the shell/ADB process (uid 2000) can no longer write to
        // /data/local/tmp due to SELinux policy tightening. The SU Bridge falls back to the
        // user-exported path; show a targeted fix if that path isn't set yet.
        if (Build.VERSION.SDK_INT >= 36) {
            val isAdbMode =
                try {
                    Shizuku.pingBinder() && Shizuku.getUid() == 2000
                } catch (_: Exception) {
                    false
                }
            if (isAdbMode) {
                val hasExportedPath =
                    af.shizuku.manager.ShizukuSettings
                        .getExportDirUri() != null
                checks.add(
                    DoctorCheck(
                        getString(R.string.doctor_check_su_bridge_a16),
                        if (hasExportedPath) {
                            getString(R.string.doctor_status_ok) + " — " + getString(R.string.doctor_status_su_bridge_path_set)
                        } else {
                            getString(R.string.doctor_status_su_bridge_no_path)
                        },
                        hasExportedPath,
                        onFix =
                            if (!hasExportedPath) {
                                {
                                    startActivity(android.content.Intent(this, RootCompatibilityActivity::class.java))
                                }
                            } else {
                                null
                            },
                    ),
                )
                if (!hasExportedPath) {
                    tips.add("• " + getString(R.string.doctor_tip_su_bridge_a16))
                }
            }
        }

        // 9. Background Limits (Android 9+ — isBackgroundRestricted() available since API 28)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val am = getSystemService(android.app.ActivityManager::class.java)
            val isRestricted = am?.isBackgroundRestricted == true
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_background),
                    if (isRestricted) getString(R.string.doctor_status_optimized) else getString(R.string.doctor_status_ok),
                    !isRestricted,
                    onFix =
                        if (isRestricted) {
                            { SettingsHelper.requestIgnoreBatteryOptimizations(this, batteryOptimizationListener) }
                        } else {
                            null
                        },
                ),
            )
            if (isRestricted) tips.add("• " + getString(R.string.doctor_tip_background_restricted))
        }

        // 10. Phantom Process Killer (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checks.add(
                DoctorCheck(
                    getString(R.string.doctor_check_phantom_process),
                    getString(R.string.doctor_status_manual_check),
                    true,
                    onFix = {
                        serviceScope.launch {
                            try {
                                // Try to disable it via Shizuku if running
                                if (ShizukuStateMachine.isRunning()) {
                                    // serviceScope defaults to Dispatchers.Main - newProcess()/waitFor()
                                    // is a blocking IPC round-trip, so it must run off Main or it ANRs
                                    // (SHIZUKUPLUS-7H/7P).
                                    withContext(Dispatchers.IO) {
                                        // Shizuku.newProcess() is a Java platform type - it can return null
                                        // at runtime (e.g. binder died mid-call) despite Kotlin not flagging
                                        // it as nullable, which NPEs on p.waitFor() (Sentry SHIZUKUPLUS-8K/8M).
                                        val p =
                                            Shizuku.newProcess(arrayOf("device_config", "put", "activity_manager", "max_phantom_processes", "2147483647"), null, null)
                                                ?: throw IllegalStateException("Shizuku returned a null remote process for newProcess()")
                                        try {
                                            p.waitFor()
                                        } finally {
                                            try {
                                                p.destroy()
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                    withContext(Dispatchers.Main) { Toast.makeText(this@ServiceDoctorActivity, R.string.service_doctor_fix_phantom_attempted, Toast.LENGTH_SHORT).show() }
                                } else {
                                    withContext(Dispatchers.Main) { Toast.makeText(this@ServiceDoctorActivity, R.string.service_doctor_fix_requires_service, Toast.LENGTH_SHORT).show() }
                                }
                            } catch (e: Exception) {
                                val isNullProcess = e is IllegalStateException && e.message?.contains("null remote process") == true
                                Timber.w(e, "Phantom process fix failed")
                                withContext(Dispatchers.Main) {
                                    val msg =
                                        if (isNullProcess && EnvironmentUtils.isSamsung()) {
                                            getString(R.string.service_doctor_fix_blocked_samsung)
                                        } else if (isNullProcess) {
                                            getString(R.string.service_doctor_fix_blocked_generic)
                                        } else {
                                            getString(R.string.service_doctor_fix_failed, e.message)
                                        }
                                    Toast.makeText(this@ServiceDoctorActivity, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                ),
            )
            tips.add("• " + getString(R.string.doctor_tip_phantom_process))
        }

        // 11. Samsung Auto Restart & Sleeping Apps
        if (EnvironmentUtils.isSamsung()) {
            tips.add("• " + getString(R.string.doctor_tip_samsung_optimization))
            tips.add("• " + getString(R.string.doctor_tip_samsung_sleeping))

            val board = Build.HARDWARE.lowercase()
            if (board.contains("exynos")) {
                tips.add("• " + getString(R.string.doctor_tip_s22_ultra_exynos))
            } else if (board.contains("qcom") || board.contains("snapdragon")) {
                tips.add("• " + getString(R.string.doctor_tip_s22_ultra_snapdragon))
            }
        }

        checkListAdapter.submitList(checks)

        val issueCount = checks.count { !it.ok }
        if (issueCount == 0) {
            binding.summaryTitle.text = getString(R.string.doctor_summary_healthy_title)
            binding.summarySubtitle.text = getString(R.string.doctor_summary_healthy_subtitle)
            binding.summaryIcon.setImageResource(R.drawable.ic_server_ok_24)
            binding.summaryIcon.imageTintList =
                android.content.res.ColorStateList
                    .valueOf(themeColor(com.google.android.material.R.attr.colorTertiary))
        } else {
            binding.summaryTitle.text = getString(R.string.doctor_summary_issues_title, issueCount)
            binding.summarySubtitle.text = getString(R.string.doctor_summary_issues_subtitle)
            binding.summaryIcon.setImageResource(R.drawable.ic_server_error_24)
            binding.summaryIcon.imageTintList =
                android.content.res.ColorStateList
                    .valueOf(themeColor(android.R.attr.colorError))
        }

        if (tips.isEmpty()) {
            tipsTextView.text = getString(R.string.doctor_system_well_configured)
        } else {
            val content = tips.joinToString("\n\n")
            tipsTextView.text = content
        }
    }

    private data class DoctorCheck(
        val title: String,
        val status: String,
        val ok: Boolean,
        val onFix: (() -> Unit)? = null,
        val topicId: String? = null,
    )

    private inner class CheckListAdapter : RecyclerView.Adapter<CheckViewHolder>() {
        private var items = emptyList<DoctorCheck>()

        fun submitList(newItems: List<DoctorCheck>) {
            val diff =
                DiffUtil.calculateDiff(
                    object : DiffUtil.Callback() {
                        override fun getOldListSize() = items.size

                        override fun getNewListSize() = newItems.size

                        override fun areItemsTheSame(
                            o: Int,
                            n: Int,
                        ) = items[o].title == newItems[n].title

                        override fun areContentsTheSame(
                            o: Int,
                            n: Int,
                        ) = items[o] == newItems[n]
                    },
                )
            items = newItems
            diff.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): CheckViewHolder = CheckViewHolder(ItemDoctorCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(
            holder: CheckViewHolder,
            position: Int,
        ) {
            val check = items[position]
            val context = holder.itemView.context
            holder.binding.title.text = check.title
            holder.binding.status.text = check.status
            holder.binding.icon.setImageResource(if (check.ok) R.drawable.ic_server_ok_24 else R.drawable.ic_server_error_24)
            val colorAttr = if (check.ok) com.google.android.material.R.attr.colorTertiary else android.R.attr.colorError
            holder.binding.icon.imageTintList =
                android.content.res.ColorStateList
                    .valueOf(context.themeColor(colorAttr))

            androidx.appcompat.widget.TooltipCompat.setTooltipText(
                holder.itemView,
                "${check.title}: ${check.status}",
            )

            val topic =
                check.topicId ?: when {
                    check.title.contains("battery", ignoreCase = true) || check.title.contains("sleeping", ignoreCase = true) || check.title.contains("autostart", ignoreCase = true) || check.title.contains("blocker", ignoreCase = true) -> af.shizuku.manager.knowledgebase.KnowledgebaseRegistry.TOPIC_BATTERY_OEM
                    check.title.contains("adb", ignoreCase = true) || check.title.contains("network", ignoreCase = true) -> af.shizuku.manager.knowledgebase.KnowledgebaseRegistry.TOPIC_WIRELESS_ADB
                    check.title.contains("su", ignoreCase = true) || check.title.contains("root", ignoreCase = true) || check.title.contains("wallet", ignoreCase = true) -> af.shizuku.manager.knowledgebase.KnowledgebaseRegistry.TOPIC_ROOT_COMPAT
                    else -> af.shizuku.manager.knowledgebase.KnowledgebaseRegistry.TOPIC_BATTERY_OEM
                }
            holder.itemView.setOnClickListener {
                af.shizuku.manager.utils.HapticUtils
                    .tap(it)
                af.shizuku.manager.knowledgebase.KnowledgebaseHelper
                    .show(context, topic)
            }

            if (check.onFix != null) {
                holder.binding.btnFix.visibility = View.VISIBLE
                holder.binding.btnFix.setOnClickListener { check.onFix.invoke() }
            } else {
                holder.binding.btnFix.visibility = View.GONE
            }
        }

        override fun getItemCount() = items.size
    }

    private class CheckViewHolder(
        val binding: ItemDoctorCheckBinding,
    ) : RecyclerView.ViewHolder(binding.root)
}
