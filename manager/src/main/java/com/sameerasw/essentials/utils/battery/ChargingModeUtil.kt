/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Battery Utilities
 * File: ChargingModeUtil.kt
 * Description: Reads and writes the Off/Adaptive/Limited charging mode secure settings.
 */

package com.sameerasw.essentials.utils.battery

import android.content.Context
import android.provider.Settings
import androidx.annotation.Keep
import com.sameerasw.essentials.utils.ShellUtils

@Keep
enum class ChargingMode { OFF, ADAPTIVE, LIMITED }

object ChargingModeUtil {
    private const val ADAPTIVE_CHARGING_SETTING = "adaptive_charging_enabled"
    private const val CHARGE_OPTIMIZATION_MODE = "charge_optimization_mode"

    fun hasPermission(context: Context): Boolean = ShellUtils.isAvailable(context) && ShellUtils.hasPermission(context)

    private fun getSecureInt(
        context: Context,
        key: String,
        def: Int,
    ): Int =
        try {
            Settings.Secure.getInt(context.contentResolver, key)
        } catch (_: SecurityException) {
            ShellUtils
                .runCommandWithOutput(context, "settings get secure $key", notifyOnError = false)
                ?.toIntOrNull() ?: def
        } catch (_: Exception) {
            def
        }

    private fun putSecureInt(
        context: Context,
        key: String,
        value: Int,
    ) {
        try {
            Settings.Secure.putInt(context.contentResolver, key, value)
        } catch (_: Exception) {
            ShellUtils.runCommand(context, "settings put secure $key $value", notifyOnError = false)
        }
    }

    fun getMode(context: Context): ChargingMode {
        val isAdaptive = getSecureInt(context, ADAPTIVE_CHARGING_SETTING, 0) == 1
        val isLimited = getSecureInt(context, CHARGE_OPTIMIZATION_MODE, 0) == 1
        return when {
            isLimited -> ChargingMode.LIMITED
            isAdaptive -> ChargingMode.ADAPTIVE
            else -> ChargingMode.OFF
        }
    }

    fun setMode(
        context: Context,
        mode: ChargingMode,
    ) {
        when (mode) {
            ChargingMode.OFF -> {
                putSecureInt(context, CHARGE_OPTIMIZATION_MODE, 0)
                putSecureInt(context, ADAPTIVE_CHARGING_SETTING, 0)
            }

            ChargingMode.ADAPTIVE -> {
                putSecureInt(context, CHARGE_OPTIMIZATION_MODE, 0)
                putSecureInt(context, ADAPTIVE_CHARGING_SETTING, 1)
            }

            ChargingMode.LIMITED -> {
                putSecureInt(context, CHARGE_OPTIMIZATION_MODE, 1)
                putSecureInt(context, ADAPTIVE_CHARGING_SETTING, 0)
            }
        }
    }
}
