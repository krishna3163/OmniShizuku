package moe.shizuku.manager.manage

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ui.screen.AppToggleFeature

data class LabsToggleAppItem(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean
)

data class LabsToggleUiState(
    val isLoading: Boolean = true,
    val isGateEnabled: Boolean = true,
    val installedApps: List<LabsToggleAppItem> = emptyList(),
    val blockedPackages: Set<String> = emptySet(),
    val filteredApps: List<LabsToggleAppItem> = emptyList(),
    val query: String = "",
    val showSystemApps: Boolean = false,
    val onlyBlockedFilter: Boolean = false
)

class LabsToggleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LabsToggleUiState())
    val uiState: StateFlow<LabsToggleUiState> = _uiState.asStateFlow()

    fun load(context: Context, feature: AppToggleFeature) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val isGate = withContext(Dispatchers.IO) {
                if (feature.signal != null) Hiding.isSignalEnabled(feature.signal) else true
            }
            val installed = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                InstalledPackages.all(context, 0).map { pi ->
                    val isSys = (pi.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
                    val lbl = pi.applicationInfo?.loadLabel(pm)?.toString() ?: pi.packageName
                    LabsToggleAppItem(
                        packageName = pi.packageName,
                        label = lbl,
                        isSystemApp = isSys
                    )
                }
            }
            val blocked = withContext(Dispatchers.IO) {
                when {
                    feature.signal != null -> Hiding.appsFor(feature.signal)
                    feature == AppToggleFeature.AUTOSTART -> PackageTools.readOpDeniedInstalled("RUN_IN_BACKGROUND")
                    else -> PackageTools.readFirewallBlocked(context)
                }
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isGateEnabled = isGate,
                installedApps = installed,
                blockedPackages = blocked
            )
            applyFilters()
        }
    }

    fun setQuery(q: String) {
        _uiState.value = _uiState.value.copy(query = q)
        applyFilters()
    }

    fun setShowSystemApps(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSystemApps = show)
        applyFilters()
    }

    fun setOnlyBlockedFilter(onlyBlocked: Boolean) {
        _uiState.value = _uiState.value.copy(onlyBlockedFilter = onlyBlocked)
        applyFilters()
    }

    fun toggleAppBlocked(context: Context, feature: AppToggleFeature, pkg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentlyBlocked = _uiState.value.blockedPackages.contains(pkg)
            val newBlockedState = !currentlyBlocked
            val success = when {
                feature.signal != null -> Hiding.setOnList(feature.signal, pkg, newBlockedState)
                feature == AppToggleFeature.AUTOSTART -> PackageTools.setOpBlocked(context, pkg, "RUN_IN_BACKGROUND", newBlockedState)
                else -> PackageTools.setNetworkBlocked(context, pkg, newBlockedState)
            }
            if (success) {
                val updatedSet = if (newBlockedState) {
                    _uiState.value.blockedPackages + pkg
                } else {
                    _uiState.value.blockedPackages - pkg
                }
                _uiState.value = _uiState.value.copy(blockedPackages = updatedSet)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = filterAndSortApps(
            apps = state.installedApps,
            blockedPackages = state.blockedPackages,
            query = state.query,
            showSystemApps = state.showSystemApps,
            onlyBlocked = state.onlyBlockedFilter
        )
        _uiState.value = state.copy(filteredApps = filtered)
    }

    companion object {
        fun filterAndSortApps(
            apps: List<LabsToggleAppItem>,
            blockedPackages: Set<String>,
            query: String,
            showSystemApps: Boolean,
            onlyBlocked: Boolean
        ): List<LabsToggleAppItem> {
            val normalizedQuery = query.trim().lowercase()
            return apps
                .filter { app ->
                    if (!showSystemApps && app.isSystemApp) false
                    else true
                }
                .filter { app ->
                    if (onlyBlocked && !blockedPackages.contains(app.packageName)) false
                    else true
                }
                .filter { app ->
                    if (normalizedQuery.isEmpty()) true
                    else app.label.lowercase().contains(normalizedQuery) ||
                            app.packageName.lowercase().contains(normalizedQuery)
                }
                .sortedWith(
                    compareByDescending<LabsToggleAppItem> { blockedPackages.contains(it.packageName) }
                        .thenBy { it.label.lowercase() }
                )
        }
    }
}
