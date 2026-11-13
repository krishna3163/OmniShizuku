package moe.shizuku.manager.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.graphics.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.ShizukuStateMachine
import moe.shizuku.manager.utils.runShellCommand
import kotlin.math.roundToInt

data class DisplayInformation(
    val name: String,
    val physicalWidth: Int,
    val physicalHeight: Int,
    val logicalWidth: Int,
    val logicalHeight: Int,
    val logicalDensity: Int,
    val physicalDensity: Int,
    val currentRefreshRate: Float,
    val currentModeId: Int,
    val modes: List<DisplayModeInfo>,
    val state: Int,
    val rotation: Int,
    val hdrTypes: List<Int>,
    val wideColorGamut: Boolean,
    val snapshot: DisplaySnapshot
)

object DisplayRepository {

    suspend fun read(context: Context): DisplayInformation? = withContext(Dispatchers.IO) {
        val display = context.getSystemService(DisplayManager::class.java)
            .getDisplay(Display.DEFAULT_DISPLAY) ?: return@withContext null
        val shell = readShellSnapshot()
        val metrics = android.util.DisplayMetrics()
        display.getRealMetrics(metrics)
        val logicalSize = Point()
        display.getSize(logicalSize)
        val modes = display.supportedModes.map { mode ->
            DisplayModeInfo(
                id = mode.modeId,
                width = mode.physicalWidth,
                height = mode.physicalHeight,
                refreshRate = mode.refreshRate,
                supportedRefreshRates = display.supportedModes
                    .filter { it.physicalWidth == mode.physicalWidth && it.physicalHeight == mode.physicalHeight }
                    .map { it.refreshRate }
                    .distinct()
                    .sorted()
            )
        }.distinctBy { Triple(it.width, it.height, it.refreshRate) }
        val hdr = display.hdrCapabilities
        DisplayInformation(
            name = display.name,
            physicalWidth = metrics.widthPixels,
            physicalHeight = metrics.heightPixels,
            logicalWidth = logicalSize.x,
            logicalHeight = logicalSize.y,
            logicalDensity = metrics.densityDpi,
            physicalDensity = ((metrics.xdpi + metrics.ydpi) / 2f).roundToInt(),
            currentRefreshRate = display.refreshRate,
            currentModeId = display.mode.modeId,
            modes = modes,
            state = display.state,
            rotation = display.rotation,
            hdrTypes = hdr?.supportedHdrTypes?.toList().orEmpty(),
            wideColorGamut = display.isWideColorGamut,
            snapshot = shell.copy(
                modeId = display.mode.modeId,
                currentRefreshRate = display.refreshRate
            )
        )
    }

    suspend fun readShellSnapshot(): DisplaySnapshot = withContext(Dispatchers.IO) {
        val size = parseValue(runShellCommand("wm size"), "Override size:", "Physical size:")
        val density = parseValue(runShellCommand("wm density"), "Override density:", "Physical density:")
            ?.toIntOrNull()
        DisplaySnapshot(
            size = size,
            density = density,
            minRefreshRate = setting("min_refresh_rate"),
            peakRefreshRate = setting("peak_refresh_rate")
        )
    }

    suspend fun apply(context: Context, change: DisplayChange): Boolean = withContext(Dispatchers.IO) {
        if (!ShizukuStateMachine.isRunning()) return@withContext false
        val commands = buildList {
            change.size?.let { add("wm size $it") }
            change.density?.let { add("wm density $it") }
            change.minRefreshRate?.let { add("settings put system min_refresh_rate $it") }
            change.peakRefreshRate?.let { add("settings put system peak_refresh_rate $it") }
        }
        if (commands.isEmpty()) return@withContext false
        commands.all { command ->
            val result = runShellCommand("$command; echo EXIT:\$?") ?: return@all false
            result.lineSequence().any { it.trim() == "EXIT:0" }
        } && verify(change)
    }

    suspend fun reset(context: Context, resetSize: Boolean, resetDensity: Boolean, resetRefresh: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            if (!ShizukuStateMachine.isRunning()) return@withContext false
            val commands = buildList {
                if (resetSize) add("wm size reset")
                if (resetDensity) add("wm density reset")
                if (resetRefresh) {
                    add("settings delete system min_refresh_rate")
                    add("settings delete system peak_refresh_rate")
                }
            }
            commands.isNotEmpty() && commands.all { command ->
                val result = runShellCommand("$command; echo EXIT:\$?") ?: return@all false
                result.lineSequence().any { it.trim() == "EXIT:0" }
            }
        }

    suspend fun restore(context: Context, snapshot: DisplaySnapshot): Boolean =
        withContext(Dispatchers.IO) {
            if (!ShizukuStateMachine.isRunning()) return@withContext false
            val commands = buildList {
                if (snapshot.size != null) add("wm size ${snapshot.size}") else add("wm size reset")
                if (snapshot.density != null) add("wm density ${snapshot.density}") else add("wm density reset")
                if (snapshot.minRefreshRate != null) {
                    add("settings put system min_refresh_rate ${snapshot.minRefreshRate}")
                } else {
                    add("settings delete system min_refresh_rate")
                }
                if (snapshot.peakRefreshRate != null) {
                    add("settings put system peak_refresh_rate ${snapshot.peakRefreshRate}")
                } else {
                    add("settings delete system peak_refresh_rate")
                }
            }
            commands.all { command ->
                val result = runShellCommand("$command; echo EXIT:\$?") ?: return@all false
                result.lineSequence().any { it.trim() == "EXIT:0" }
            } && readShellSnapshot().let { current ->
                current.size == snapshot.size &&
                    current.density == snapshot.density &&
                    current.minRefreshRate == snapshot.minRefreshRate &&
                    current.peakRefreshRate == snapshot.peakRefreshRate
            }
        }

    private suspend fun verify(change: DisplayChange): Boolean {
        val current = readShellSnapshot()
        val sizeMatches = change.size == null || current.size == change.size
        val densityMatches = change.density == null || current.density == change.density
        val minMatches = change.minRefreshRate == null || current.minRefreshRate == change.minRefreshRate
        val peakMatches = change.peakRefreshRate == null || current.peakRefreshRate == change.peakRefreshRate
        return sizeMatches && densityMatches && minMatches && peakMatches
    }

    private fun setting(key: String): String? =
        runShellCommand("settings get system $key")?.trim()
            ?.takeUnless { it.isBlank() || it == "null" }

    private fun parseValue(output: String?, override: String, physical: String): String? {
        val text = output ?: return null
        val overrideValue = Regex("""${Regex.escape(override)}\s*(\d+x\d+|\d+)""")
            .find(text)?.groupValues?.get(1)
        val physicalValue = Regex("""${Regex.escape(physical)}\s*(\d+x\d+|\d+)""")
            .find(text)?.groupValues?.get(1)
        return overrideValue ?: physicalValue
    }
}
