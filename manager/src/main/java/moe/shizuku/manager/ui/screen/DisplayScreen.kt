package moe.shizuku.manager.ui.screen

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import moe.shizuku.manager.display.DisplayChange
import moe.shizuku.manager.display.DisplayConfigurationValidator
import moe.shizuku.manager.display.DisplayInformation
import moe.shizuku.manager.display.DisplayProfile
import moe.shizuku.manager.display.DisplayProfileManager
import moe.shizuku.manager.display.DisplayConfigurationStore
import moe.shizuku.manager.display.DisplayRollbackState
import moe.shizuku.manager.display.DisplayRepository
import moe.shizuku.manager.display.DisplayRollbackManager
import moe.shizuku.manager.display.PendingDisplayChange
import moe.shizuku.manager.R
import moe.shizuku.manager.ui.component.ScrollToTopButton
import moe.shizuku.manager.ui.component.SegmentedCard
import moe.shizuku.manager.ui.theme.OmniTokens
import moe.shizuku.manager.utils.ShizukuStateMachine

/** The value a row shows when the platform has no override for it. */
private const val DEFAULT = "default"

/**
 * What the shell says the screen is, split into the value in force and whether it is an override.
 *
 * `wm size` and `wm density` print the physical value and, when one was set, an override beside
 * it. The override is what the screen is actually using, and its presence is also the only way
 * to know whether a reset has anything to undo.
 */
private data class DisplayValue(val current: String?)

/**
 * Resolution, density and refresh rate, through the shell.
 *
 * All three are settings the platform owns, changed with the same commands a computer would use:
 * `wm size`, `wm density`, and the two refresh-rate keys. Every change is read back before the
 * screen says it worked, because the point of an override is that the platform can refuse it.
 *
 * A wrong resolution or density can leave a phone whose screen is hard to use, which is why the
 * reset is on every row rather than behind a menu, and why values are bounded before they are
 * sent: a typo of one digit is the difference between 420 dpi and 42 dpi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(bottomPadding: Dp, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var running by remember { mutableStateOf(ShizukuStateMachine.isRunning()) }
    var size by remember { mutableStateOf<DisplayValue?>(null) }
    var density by remember { mutableStateOf<DisplayValue?>(null) }
    var minHz by remember { mutableStateOf<String?>(null) }
    var maxHz by remember { mutableStateOf<String?>(null) }
    var modes by remember { mutableStateOf<List<Int>>(emptyList()) }
    var displayInfo by remember { mutableStateOf<DisplayInformation?>(null) }
    var profiles by remember { mutableStateOf<List<DisplayProfile>>(emptyList()) }
    var profileName by remember { mutableStateOf("") }
    var pendingChange by remember { mutableStateOf<PendingDisplayChange?>(null) }
    var remainingSeconds by remember { mutableStateOf(0L) }

    var sizeField by remember { mutableStateOf("") }
    var densityField by remember { mutableStateOf("") }
    var minField by remember { mutableStateOf("") }
    var maxField by remember { mutableStateOf("") }

    fun refresh() {
        scope.launch {
            val info = withContext(Dispatchers.IO) {
                if (ShizukuStateMachine.update() != ShizukuStateMachine.State.RUNNING) {
                    return@withContext null
                }
                DisplayRepository.read(context)
            }
            running = info != null
            displayInfo = info
            pendingChange = withContext(Dispatchers.IO) {
                DisplayConfigurationStore(context).pending()
                    ?.takeIf { it.state == DisplayRollbackState.AWAITING_CONFIRMATION }
            }
            if (info != null) {
                size = DisplayValue(info.snapshot.size)
                density = DisplayValue(info.snapshot.density?.toString())
                minHz = info.snapshot.minRefreshRate
                maxHz = info.snapshot.peakRefreshRate
                modes = info.modes
                    .flatMap { it.supportedRefreshRates }
                    .map { it.roundToInt() }
                    .filter { it in 1..240 }
                    .distinct()
                    .sorted()
                // Fields start on what is in force, so an apply changes one number rather
                // than asking for all of it again.
                if (sizeField.isBlank()) sizeField = info.snapshot.size.orEmpty()
                if (densityField.isBlank()) densityField = info.snapshot.density?.toString().orEmpty()
                if (minField.isBlank()) minField = info.snapshot.minRefreshRate.orEmpty()
                if (maxField.isBlank()) maxField = info.snapshot.peakRefreshRate.orEmpty()
                profiles = DisplayProfileManager.merge(
                    DisplayConfigurationStore(context).profiles(),
                    info.snapshot
                )
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh() }

    /** Applies a typed change through the display repository's provider and recovery boundary. */
    fun applyChange(@StringRes what: Int, change: DisplayChange) {
        if (!ShizukuStateMachine.isRunning()) {
            Toast.makeText(context, R.string.display_needs_shizuku, Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            val result = DisplayRollbackManager.beginTemporary(
                context = context,
                change = change,
                modes = displayInfo?.modes.orEmpty()
            )
            Toast.makeText(
                context,
                context.getString(
                    if (result.applied) R.string.display_applied else R.string.display_failed,
                    context.getString(what)
                ),
                Toast.LENGTH_SHORT
            ).show()
            pendingChange = result.pending
            refresh()
        }
    }

    fun resetChange(@StringRes what: Int, reset: suspend () -> Boolean) {
        if (!ShizukuStateMachine.isRunning()) {
            Toast.makeText(context, R.string.display_needs_shizuku, Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) { reset() }
            Toast.makeText(
                context,
                context.getString(if (ok) R.string.display_applied else R.string.display_failed, context.getString(what)),
                Toast.LENGTH_SHORT
            ).show()
            refresh()
        }
    }

    fun invalid() {
        Toast.makeText(context, R.string.display_invalid, Toast.LENGTH_SHORT).show()
    }

    fun applySize() {
        val value = sizeField.trim()
        val error = DisplayConfigurationValidator.validateSize(value, displayInfo?.modes.orEmpty())
        if (error != null) return invalid()
        applyChange(R.string.display_resolution, DisplayChange(size = value))
    }

    fun applyNumber(@StringRes what: Int, value: String, range: IntRange) {
        val number = value.trim().toIntOrNull() ?: return invalid()
        if (number !in range) return invalid()
        val change = when (what) {
            R.string.display_density -> DisplayChange(density = number)
            R.string.display_min_refresh -> DisplayChange(minRefreshRate = number.toString())
            else -> DisplayChange(peakRefreshRate = number.toString())
        }
        applyChange(what, change)
    }

    LaunchedEffect(pendingChange) {
        val pending = pendingChange ?: return@LaunchedEffect
        while (true) {
            val seconds = ((pending.deadlineMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            remainingSeconds = seconds
            if (seconds == 0L) {
                val restored = DisplayRollbackManager.reject(context)
                pendingChange = null
                Toast.makeText(
                    context,
                    context.getString(if (restored) R.string.display_restored else R.string.display_failed, context.getString(R.string.display_resolution)),
                    Toast.LENGTH_SHORT
                ).show()
                refresh()
                break
            }
            delay(1_000L)
        }
    }

    if (pendingChange != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.display_confirm_title)) },
            text = {
                Text(stringResource(R.string.display_confirm_message, remainingSeconds))
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        DisplayRollbackManager.confirm(context)
                        pendingChange = null
                        Toast.makeText(context, R.string.display_confirmed, Toast.LENGTH_SHORT).show()
                        refresh()
                    }
                }) {
                    Text(stringResource(R.string.display_keep_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        val restored = DisplayRollbackManager.reject(context)
                        pendingChange = null
                        Toast.makeText(
                            context,
                            context.getString(if (restored) R.string.display_restored else R.string.display_failed, context.getString(R.string.display_resolution)),
                            Toast.LENGTH_SHORT
                        ).show()
                        refresh()
                    }
                }) {
                    Text(stringResource(R.string.display_restore))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(stringResource(R.string.tab_display))
                    Text(
                        text = stringResource(R.string.labs_display_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(
                            if (running) R.string.display_description else R.string.display_needs_shizuku
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (running) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                item {
                    DisplayInformationCard(
                        info = displayInfo,
                        onRestoreLast = {
                            scope.launch {
                                val restored = DisplayRollbackManager.restoreLast(context)
                                Toast.makeText(
                                    context,
                                    context.getString(
                                        if (restored) R.string.display_restored else R.string.display_failed,
                                        context.getString(R.string.display_profile)
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                                refresh()
                            }
                        }
                    )
                }

                item {
                    DisplayProfilesCard(
                        profiles = profiles,
                        enabled = running,
                        name = profileName,
                        onNameChange = { profileName = it },
                        onSave = {
                            val name = profileName.trim()
                            if (name.isBlank()) {
                                invalid()
                            } else {
                                val saved = DisplayProfile(name, DisplayChange(
                                    size = sizeField.trim().takeIf(String::isNotBlank),
                                    density = densityField.trim().toIntOrNull(),
                                    minRefreshRate = minField.trim().takeIf(String::isNotBlank),
                                    peakRefreshRate = maxField.trim().takeIf(String::isNotBlank)
                                ))
                                val store = DisplayConfigurationStore(context)
                                store.saveProfiles(store.profiles().filterNot { it.name == name } + saved)
                                profileName = ""
                                displayInfo?.snapshot?.let { snapshot ->
                                    profiles = DisplayProfileManager.merge(store.profiles(), snapshot)
                                }
                                Toast.makeText(context, R.string.display_profile_saved, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onApply = { profile -> applyChange(R.string.display_profile, profile.change) }
                    )
                }

                item {
                    DisplayControl(
                        title = stringResource(R.string.display_resolution),
                        summary = stringResource(R.string.display_resolution_summary),
                        current = size?.current,
                        field = sizeField,
                        onFieldChange = { sizeField = it },
                        keyboardType = KeyboardType.Text,
                        applyEnabled = running,
                        onApply = { applySize() },
                        onReset = {
                            resetChange(R.string.display_resolution) {
                                DisplayRepository.reset(context, resetSize = true, resetDensity = false, resetRefresh = false)
                            }
                        }
                    )
                }

                item {
                    DisplayControl(
                        title = stringResource(R.string.display_density),
                        summary = stringResource(R.string.display_density_summary),
                        current = density?.current,
                        field = densityField,
                        onFieldChange = { densityField = it },
                        keyboardType = KeyboardType.Number,
                        applyEnabled = running,
                        onApply = {
                            applyNumber(
                                R.string.display_density,
                                densityField,
                                100..1000
                            )
                        },
                        onReset = {
                            resetChange(R.string.display_density) {
                                DisplayRepository.reset(context, resetSize = false, resetDensity = true, resetRefresh = false)
                            }
                        }
                    )
                }

                item {
                    RefreshControl(
                        title = stringResource(R.string.display_min_refresh),
                        summary = stringResource(R.string.display_min_refresh_summary),
                        current = minHz,
                        rates = modes,
                        field = minField,
                        onFieldChange = { minField = it },
                        enabled = running,
                        onPick = { rate ->
                            applyChange(R.string.display_min_refresh, DisplayChange(minRefreshRate = rate.toString()))
                        },
                        onApplyManual = {
                            applyNumber(
                                R.string.display_min_refresh,
                                minField,
                                24..240
                            )
                        },
                        onReset = {
                            resetChange(R.string.display_min_refresh) {
                                DisplayRepository.reset(context, false, false, true)
                            }
                        }
                    )
                }

                item {
                    RefreshControl(
                        title = stringResource(R.string.display_max_refresh),
                        summary = stringResource(R.string.display_max_refresh_summary),
                        current = maxHz,
                        rates = modes,
                        field = maxField,
                        onFieldChange = { maxField = it },
                        enabled = running,
                        onPick = { rate ->
                            applyChange(R.string.display_max_refresh, DisplayChange(peakRefreshRate = rate.toString()))
                        },
                        onApplyManual = {
                            applyNumber(
                                R.string.display_max_refresh,
                                maxField,
                                24..240
                            )
                        },
                        onReset = {
                            resetChange(R.string.display_max_refresh) {
                                DisplayRepository.reset(context, false, false, true)
                            }
                        }
                    )
                }
            }

            ScrollToTopButton(
                listState = listState,
                bottomPadding = bottomPadding,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun DisplayInformationCard(
    info: DisplayInformation?,
    onRestoreLast: () -> Unit
) {
    SegmentedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(stringResource(R.string.display_information), style = MaterialTheme.typography.titleMedium)
            if (info == null) {
                Text(
                    stringResource(R.string.display_information_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("${info.name} • ${info.physicalWidth}x${info.physicalHeight}")
                Text(stringResource(R.string.display_information_logical, info.logicalWidth, info.logicalHeight))
                Text(
                    stringResource(
                        R.string.display_information_mode,
                        info.currentModeId,
                        info.currentRefreshRate.roundToInt(),
                        info.logicalDensity
                    )
                )
                Text(
                    stringResource(
                        R.string.display_information_modes,
                        info.modes.size,
                        if (info.hdrTypes.isEmpty()) {
                            stringResource(R.string.display_information_hdr_missing)
                        } else {
                            info.hdrTypes.joinToString()
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(
                        if (info.wideColorGamut) {
                            R.string.display_wide_gamut_supported
                        } else {
                            R.string.display_wide_gamut_missing
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onRestoreLast) {
                    Text(stringResource(R.string.display_restore_last))
                }
            }
        }
    }
}

@Composable
private fun DisplayProfilesCard(
    profiles: List<DisplayProfile>,
    enabled: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onApply: (DisplayProfile) -> Unit
) {
    SegmentedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.display_profiles), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.display_profiles_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                label = { Text(stringResource(R.string.display_custom_profile_name)) }
            )
            TextButton(onClick = onSave, enabled = enabled) {
                Text(stringResource(R.string.display_save_current))
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profiles.forEach { profile ->
                    FilterChip(
                        selected = false,
                        onClick = { onApply(profile) },
                        enabled = enabled,
                        label = { Text(profile.name) }
                    )
                }
            }
        }
    }
}

/**
 * One of the two refresh-rate keys, as the rates the panel reported.
 *
 * The numbers come from the display itself, so a preset here is a mode the panel can actually
 * run; the field is the fallback for a ROM whose dump could not be read, and it is only shown
 * when there are no rates to offer.
 */
@Composable
private fun RefreshControl(
    title: String,
    summary: String,
    current: String?,
    rates: List<Int>,
    field: String,
    onFieldChange: (String) -> Unit,
    enabled: Boolean,
    onPick: (Int) -> Unit,
    onApplyManual: () -> Unit,
    onReset: () -> Unit
) {
    val currentRate = current?.toFloatOrNull()?.roundToInt()

    SegmentedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.display_current, current ?: DEFAULT),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )

            if (rates.isEmpty()) {
                Text(
                    text = stringResource(R.string.display_modes_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
OutlinedTextField(
                value = field,
                onValueChange = onFieldChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OmniTokens.brandPrimary(),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rates.forEach { rate ->
                        FilterChip(
                            selected = currentRate == rate,
                            onClick = { onPick(rate) },
                            enabled = enabled,
                            label = { Text(stringResource(R.string.display_hz, rate)) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rates.isEmpty()) {
                    Button(onClick = onApplyManual, enabled = enabled) {
                        Text(stringResource(R.string.display_apply))
                    }
                }
                TextButton(onClick = onReset, enabled = enabled) {
                    Text(stringResource(R.string.display_reset))
                }
            }
        }
    }
}

@Composable
private fun DisplayControl(
    title: String,
    summary: String,
    current: String?,
    field: String,
    onFieldChange: (String) -> Unit,
    keyboardType: KeyboardType,
    applyEnabled: Boolean,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    SegmentedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                // "default" rather than an empty line: a key nobody set is a state, and the
                // reset is what goes back to it.
                text = stringResource(R.string.display_current, current ?: DEFAULT),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            OutlinedTextField(
                value = field,
                onValueChange = onFieldChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = applyEnabled,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onApply, enabled = applyEnabled) {
                    Text(stringResource(R.string.display_apply))
                }
                TextButton(onClick = onReset, enabled = applyEnabled) {
                    Text(stringResource(R.string.display_reset))
                }
            }
        }
    }
}
