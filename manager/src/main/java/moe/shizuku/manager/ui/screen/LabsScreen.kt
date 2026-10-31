package moe.shizuku.manager.ui.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.manage.ForceDark
import moe.shizuku.manager.manage.Hiding
import moe.shizuku.manager.manage.PackageTools
import moe.shizuku.manager.settings.DeveloperFeatures
import moe.shizuku.manager.ui.Detail
import moe.shizuku.manager.ui.component.FeaturePlateIcon
import moe.shizuku.manager.ui.component.ScrollToTopButton
import moe.shizuku.manager.ui.component.SegmentedCard
import moe.shizuku.manager.ui.component.SegmentedListItem
import moe.shizuku.manager.ui.televisionReturnFocus

/** The dot that says a feature named on a row is doing something. */
private val ActiveDotSize = 10.dp

/**
 * Deliberately not a theme role.
 *
 * The palette is seeded from the wallpaper, so a role here would come out teal on a teal phone,
 * amber on an amber one - and the one thing this dot has to do is mean the same thing on every
 * device. The home screen's Restart button is a fixed blue for the same reason.
 */
private val ActiveGreen = Color(0xFF34C759)

private enum class LabCategory(@StringRes val titleRes: Int) {
    APP_MANAGEMENT(R.string.labs_category_app_management),
    SYSTEM_CONTROLS(R.string.labs_category_system_controls),
    UTILITIES(R.string.labs_category_utilities),
    COMPANION_APPS(R.string.labs_category_companion_apps)
}

/**
 * The things that are gone to rather than lived in.
 *
 * Every one of these was a tab of its own before, which is most of the bar's width spent on
 * screens that are largely empty when you land on them: you open the app-ops list to look one
 * app up, and you open the shell to run something and leave. A tab is for a place the app keeps
 * you in, so this is a list of the others instead, and the next one to earn a place here costs a
 * line rather than a fifth of the bar.
 *
 * It used to be a grid of tiles. A grid has to be told how many columns it is, and a count that
 * is not a multiple of that number leaves a partly empty last row - three columns and thirteen
 * features did exactly that. A list has no such arithmetic: it takes any number of rows, a long
 * name like "Hide wireless debugging" reads on one line, and it is the shape every other list in
 * the app already has.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabsScreen(bottomPadding: Dp, onOpenDetail: (Detail) -> Unit) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }

    // What each feature is doing, so the list can be read without opening eight screens to find
    // out. It is the only place every one of them is in sight at once, and a hiding mode that is
    // on with apps listed looks exactly like one that is off from here.
    //
    // Re-read whenever the tab is come back to, which is when a change made in one of the lists
    // is about to be looked at.
    var refresh by remember { mutableIntStateOf(0) }
    var active by remember { mutableStateOf<Set<Detail>>(emptySet()) }
    var essentialsHubUnlocked by remember {
        mutableStateOf(DeveloperFeatures.isEssentialsHubUnlocked(context))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refresh++
        essentialsHubUnlocked = DeveloperFeatures.isEssentialsHubUnlocked(context)
    }
    LaunchedEffect(refresh) {
        active = withContext(Dispatchers.IO) { activeFeatures(context) }
    }
    // The full Labs catalogue. Descriptive terms are indexed alongside the visible names so a
    // search can find common alternate names without adding duplicate entries to the list.
    val entries = remember {
        listOf(
            LabEntry(
                R.string.tab_manage, Icons.Outlined.AdminPanelSettings, Detail.APP_OPS,
                LabCategory.APP_MANAGEMENT, "app permissions app ops"
            ),
            LabEntry(
                R.string.tab_essentials, Icons.Outlined.AutoAwesome, Detail.ESSENTIALS,
                LabCategory.UTILITIES, "essentials hub"
            ),
            LabEntry(
                R.string.tab_shell, Icons.Outlined.Terminal, Detail.SHELL,
                LabCategory.UTILITIES, "command line shell"
            ),
            LabEntry(
                AppToggleFeature.FIREWALL.titleRes,
                AppToggleFeature.FIREWALL.icon,
                Detail.FIREWALL,
                LabCategory.APP_MANAGEMENT,
                "network block internet deny"
            ),
            LabEntry(
                AppToggleFeature.AUTOSTART.titleRes,
                AppToggleFeature.AUTOSTART.icon,
                Detail.AUTOSTART,
                LabCategory.APP_MANAGEMENT,
                "auto start startup launch at boot"
            ),
            // The six hiding modes are one row rather than six: they are the same list with a
            // different setting behind it, and six rows of the same shape was most of the list.
            LabEntry(
                R.string.tab_hiding, Icons.Outlined.VisibilityOff, Detail.HIDING,
                LabCategory.APP_MANAGEMENT, "hide apps packages"
            ),
            LabEntry(
                R.string.tab_log, Icons.AutoMirrored.Outlined.ReceiptLong, Detail.LOG,
                LabCategory.UTILITIES, "logs diagnostics"
            ),
            LabEntry(
                R.string.tab_device_info, Icons.Outlined.PhoneAndroid, Detail.DEVICE_INFO,
                LabCategory.UTILITIES, "device details phone hardware"
            ),
            LabEntry(
                R.string.tab_force_dark, Icons.Outlined.DarkMode, Detail.FORCE_DARK,
                LabCategory.APP_MANAGEMENT, "dark mode applications"
            ),
            LabEntry(
                R.string.tab_activities, Icons.Outlined.Widgets, Detail.ACTIVITIES,
                LabCategory.UTILITIES, "launch activities"
            ),
            LabEntry(
                R.string.tab_power, Icons.Outlined.PowerSettingsNew, Detail.POWER,
                LabCategory.SYSTEM_CONTROLS, "power menu reboot"
            ),
            LabEntry(
                R.string.tab_display, Icons.Outlined.AspectRatio, Detail.DISPLAY,
                LabCategory.SYSTEM_CONTROLS, "screen resolution density"
            ),
            LabEntry(
                R.string.tab_wifi_passwords, Icons.Outlined.Wifi, Detail.WIFI_PASSWORDS,
                LabCategory.SYSTEM_CONTROLS, "wireless network credentials"
            ),
            LabEntry(
                R.string.tab_files, Icons.Outlined.FolderOpen, Detail.FILES,
                LabCategory.UTILITIES, "file manager browse"
            ),
            LabEntry(
                R.string.tab_install, Icons.Outlined.Android, Detail.INSTALL,
                LabCategory.UTILITIES, "install apk package"
            ),
            LabEntry(
                R.string.tab_debloater, Icons.Outlined.Delete, Detail.DEBLOATER,
                LabCategory.APP_MANAGEMENT, "debloat remove uninstall apps"
            ),
            LabEntry(
                R.string.tab_freezer, Icons.Outlined.AcUnit, Detail.FREEZER,
                LabCategory.APP_MANAGEMENT, "freeze suspend applications"
            ),
            LabEntry(
                R.string.tab_systemui_tuner, Icons.Outlined.Settings, Detail.SYSTEMUI_TUNER,
                LabCategory.SYSTEM_CONTROLS, "system ui settings status bar"
            ),
            LabEntry(
                R.string.tab_enforce_doze, Icons.Outlined.DarkMode, Detail.ENFORCE_DOZE,
                LabCategory.SYSTEM_CONTROLS, "doze battery idle"
            ),
            LabEntry(
                R.string.tab_private_dns, Icons.Outlined.Wifi, Detail.PRIVATE_DNS,
                LabCategory.SYSTEM_CONTROLS, "dns domain name server"
            ),
            LabEntry(
                R.string.tab_audio_master, Icons.Outlined.VolumeUp, Detail.AUDIO_MASTER,
                LabCategory.SYSTEM_CONTROLS, "volume sound audio"
            ),
            LabEntry(
                R.string.tab_carrier_tether, Icons.Outlined.WifiTethering, Detail.TETHER,
                LabCategory.SYSTEM_CONTROLS, "hotspot tethering"
            ),
            LabEntry(
                "Mixed Audio", Icons.Outlined.GraphicEq, Detail.MIXED_AUDIO,
                LabCategory.SYSTEM_CONTROLS, "mixed audio appops focus"
            ),
            LabEntry(
                "SoundMaster", Icons.Outlined.Tune, Detail.SOUND_MASTER,
                LabCategory.SYSTEM_CONTROLS, "per app volume soundmaster"
            ),
            LabEntry(
                R.string.tab_screen_security, Icons.Outlined.Security, Detail.SECURITY,
                LabCategory.SYSTEM_CONTROLS, "screen capture protection secure"
            ),
            LabEntry(
                R.string.tab_autodnd, Icons.Outlined.DoNotDisturbOn, Detail.AUTODND,
                LabCategory.SYSTEM_CONTROLS, "do not disturb notifications"
            ),
            LabEntry(
                R.string.tab_optimizer, Icons.Outlined.Speed, Detail.OPTIMIZER,
                LabCategory.APP_MANAGEMENT, "app optimization performance"
            ),
            LabEntry(
                R.string.tools_terminal, Icons.Outlined.Terminal, Detail.TERMINAL,
                LabCategory.UTILITIES, "terminal command prompt"
            ),
            LabEntry(
                R.string.intents_title, Icons.Outlined.Bolt, Detail.INTENTS,
                LabCategory.UTILITIES, "intent activity broadcast"
            ),
            LabEntry(
                R.string.tools_stealth, Icons.Outlined.VisibilityOff, Detail.STEALTH,
                LabCategory.UTILITIES, "hide shizuku root detection"
            ),
            LabEntry(
                R.string.settings_permissions, Icons.Outlined.VerifiedUser, Detail.PERMISSIONS,
                LabCategory.UTILITIES, "permission access grant"
            ),
            LabEntry(
                R.string.lab_more_apps, Icons.Outlined.ShoppingBag, Detail.MORE_APPS,
                LabCategory.UTILITIES, "more applications discover"
            ),
            LabEntry(
                R.string.lab_companion_call_recorder,
                Icons.Outlined.Call,
                category = LabCategory.COMPANION_APPS,
                searchTerms = "phone calls recording recorder",
                packageName = "com.kitsumed.shizucallrecorder",
                releaseUrl = "https://github.com/kitsumed/ShizuCallRecorder/releases/latest"
            ),
            LabEntry(
                R.string.lab_companion_quick_tiles,
                Icons.Outlined.Widgets,
                category = LabCategory.COMPANION_APPS,
                searchTerms = "quick settings tiles",
                packageName = "com.rbn.qtsettings",
                releaseUrl = "https://github.com/RBN-Apps/Quick-Tile-Settings/releases/latest"
            ),
            LabEntry(
                R.string.lab_companion_shizuwall,
                Icons.Outlined.Security,
                category = LabCategory.COMPANION_APPS,
                searchTerms = "wallpaper security",
                packageName = "com.arslan.shizuwall",
                releaseUrl = "https://github.com/AhmetCanArslan/ShizuWall/releases/latest"
            ),
            LabEntry(
                "Theme Patcher", Icons.Outlined.ColorLens, Detail.THEME_PATCHER,
                LabCategory.SYSTEM_CONTROLS, "theme patcher customization"
            ),
            LabEntry(
                "WallYou Wallpapers", Icons.Outlined.Wallpaper, Detail.WALLYOU,
                LabCategory.UTILITIES, "wallpaper wall you"
            ),
        )
    }

    val visibleEntries = entries.filterNot {
        it.detail == Detail.ESSENTIALS && !essentialsHubUnlocked
    }
    val displayEntries = visibleEntries.map { entry ->
        LabDisplayEntry(
            entry = entry,
            label = entry.labelString ?: if (entry.labelRes != 0) stringResource(entry.labelRes) else "",
            categoryLabel = stringResource(entry.category.titleRes)
        )
    }
    val searchItems = remember(displayEntries) {
        displayEntries.map { row ->
            LabsSearchItem(
                value = row,
                name = row.label,
                description = row.entry.searchTerms,
                aliases = row.entry.searchTerms,
                category = row.categoryLabel
            )
        }
    }
    val filteredEntries = filterLabsSearchItems(searchItems, query).map { it.value }
    val groupedEntries = filteredEntries
        .groupBy { it.entry.category }
        .toSortedMap(compareBy { it.ordinal })

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.tab_labs)) },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.labs_search_hint)) },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.labs_clear_search)
                        )
                    }
                }
            } else {
                null
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val listState = rememberLazyListState()
            LaunchedEffect(query) {
                listState.scrollToItem(0)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                if (groupedEntries.isEmpty()) {
                    item(key = "empty-search") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.labs_search_empty_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.labs_search_empty_message),
                                modifier = Modifier.padding(top = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { query = "" },
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(stringResource(R.string.labs_clear_search))
                            }
                        }
                    }
                } else {
                    groupedEntries.forEach { (category, rows) ->
                        item(key = "category-${category.name}") {
                            Text(
                                text = rows.first().categoryLabel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, top = 8.dp, bottom = 1.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(
                            items = rows,
                            key = { row ->
                                "${row.entry.labelRes}:${row.entry.packageName.orEmpty()}"
                            }
                        ) { row ->
                            val entry = row.entry
                            SegmentedCard {
                                SegmentedListItem(
                                    modifier = entry.detail?.let {
                                        Modifier.televisionReturnFocus(it)
                                    } ?: Modifier,
                                    centerSlots = true,
                                    leadingContent = {
                                        Box {
                                            FeaturePlateIcon(entry.icon)
                                            if (entry.detail?.let { it in active } == true) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .offset(x = 2.dp, y = 2.dp)
                                                        .size(ActiveDotSize)
                                                        .background(ActiveGreen, CircleShape)
                                                )
                                            }
                                        }
                                    },
                                    headlineContent = { Text(row.label) },
                                    trailingContent = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        if (entry.packageName != null && entry.releaseUrl != null) {
                                            openCompanionTool(
                                                context,
                                                entry.packageName,
                                                entry.releaseUrl
                                            )
                                        } else {
                                            entry.detail?.let(onOpenDetail)
                                        }
                                    }
                                )
                            }
                        }
                    }
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

/**
 * The Lab features that are doing something right now.
 *
 * A list counts only when it is switched on *and* has apps on it. The switch arms a hiding mode
 * and the list is what it acts on, so either without the other is a setting rather than a state -
 * and a dot that lit up for an empty list would say a phone was hiding something it was not.
 *
 * Every answer here but one is this app's own record, which is a preference read. Autostart is the
 * exception: its list belongs to the platform, and the platform is asked one command for all of
 * it. That, and the size of the hiding lists, is why this is worked out off the main thread once
 * when the tab is shown rather than inside each row.
 *
 * Two features are left out on purpose, and not because they were forgotten. App ops is a browser
 * rather than a mode: nothing about it is on or off, and the one change it can make that is
 * recorded at all - blocking an app's network - writes the firewall's list, which already has a
 * dot of its own. Activities, device info, the log, the shell and More Apps are places to look
 * rather than things that run, so there is no state of theirs to report either.
 */
private fun activeFeatures(context: Context): Set<Detail> = buildSet {
    AppToggleFeature.entries.forEach { feature ->
        // Every hiding signal answers for the grouped row rather than for a detail of its own:
        // whichever of the six is doing something lights the one dot, and the set is what keeps
        // six answers from becoming six entries.
        val detail = if (feature.signal != null) {
            Detail.HIDING
        } else {
            Detail.of(feature) ?: return@forEach
        }

        val count = when {
            // The mode has to be on for the list to be doing anything, which is the whole
            // difference between the two hiding lists that look alike from here.
            feature.signal != null ->
                if (Hiding.isSignalEnabled(feature.signal)) Hiding.appsFor(feature.signal).size
                else 0

            feature == AppToggleFeature.AUTOSTART ->
                PackageTools.readOpDeniedInstalled(feature.op).size
            else -> PackageTools.readFirewallBlocked(context).size
        }

        if (count > 0) add(detail)
    }

    // The other switch in the list, and the same question asked of it: on, and with apps to act
    // on.
    if (ForceDark.hasWorkToDo()) add(Detail.FORCE_DARK)
}

/** One row of the list: what it is called, what is on it, and where it goes. */
private data class LabEntry(
    @StringRes val labelRes: Int = 0,
    val icon: ImageVector,
    val detail: Detail? = null,
    val category: LabCategory,
    val searchTerms: String,
    val packageName: String? = null,
    val releaseUrl: String? = null,
    val labelString: String? = null
) {
    constructor(
        labelString: String,
        icon: ImageVector,
        detail: Detail? = null,
        category: LabCategory,
        searchTerms: String,
        packageName: String? = null,
        releaseUrl: String? = null
    ) : this(
        labelRes = 0,
        icon = icon,
        detail = detail,
        category = category,
        searchTerms = searchTerms,
        packageName = packageName,
        releaseUrl = releaseUrl,
        labelString = labelString
    )
}

private data class LabDisplayEntry(
    val entry: LabEntry,
    val label: String,
    val categoryLabel: String
)

private fun openCompanionTool(context: Context, packageName: String, releaseUrl: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.lab_companion_open_failed, Toast.LENGTH_LONG).show()
    }
}
