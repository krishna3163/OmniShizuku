package moe.shizuku.manager.wallyou

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.youapps.wallpaper_apis.Wallpaper
import net.youapps.wallpaper_apis.bi.BingApi
import net.youapps.wallpaper_apis.na.NasaPotdApi
import net.youapps.wallpaper_apis.ow.OwallsApi
import net.youapps.wallpaper_apis.pb.PixabayApi
import net.youapps.wallpaper_apis.ps.PicsumApi
import net.youapps.wallpaper_apis.px.GooglePixelApi
import net.youapps.wallpaper_apis.re.RedditApi
import net.youapps.wallpaper_apis.wh.WallhavenApi
import moe.shizuku.manager.ui.Detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallYouScreen(onBack: () -> Unit, onOpenDetail: (Detail) -> Unit = {}) {
import net.youapps.wallpaper_apis.wi.WikipediaPotdApi

data class WallpaperEngineOption(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val supportsSearch: Boolean = false
)

private val ENGINES = listOf(
    WallpaperEngineOption("Wallhaven", "Ultra HD & Anime", Icons.Outlined.AutoAwesome, supportsSearch = true),
    WallpaperEngineOption("Bing", "Daily Highlights", Icons.Outlined.Today, supportsSearch = false),
    WallpaperEngineOption("Pixabay", "4K Curated Photos", Icons.Outlined.Camera, supportsSearch = true),
    WallpaperEngineOption("Picsum", "Minimalist Art", Icons.Outlined.Palette, supportsSearch = false),
    WallpaperEngineOption("Reddit", "Community Choice", Icons.Outlined.Forum, supportsSearch = false),
    WallpaperEngineOption("Google Pixel", "Stock Pixel Walls", Icons.Outlined.Smartphone, supportsSearch = false),
    WallpaperEngineOption("NASA", "Astronomy of the Day", Icons.Outlined.NightsStay, supportsSearch = false),
    WallpaperEngineOption("OWalls", "Minimalist Vector", Icons.Outlined.Brush, supportsSearch = false),
    WallpaperEngineOption("Wikipedia", "Cultural Landmarks", Icons.Outlined.Public, supportsSearch = false)
)

private val POPULAR_TAGS = listOf(
    "Trending", "Nature", "Space", "Minimal", "Cyberpunk", "Dark", "Anime", "Architecture", "Cars"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallYouScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Explore, 1: Favorites
    var selectedEngine by remember { mutableStateOf(ENGINES[0]) }
    var searchQuery by remember { mutableStateOf("") }
    var activeTag by remember { mutableStateOf("Trending") }

    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var favorites by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(1) }
    
    val engines = listOf("Wallhaven", "Pixabay", "Reddit", "Picsum", "Bing", "Owalls")
    
    fun loadWallpapers() {
    var page by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var activeWallpaperDetail by remember { mutableStateOf<Wallpaper?>(null) }

    fun refreshFavorites() {
        favorites = WallYouFavoritesManager.getFavorites(context)
    }

    LaunchedEffect(Unit) {
        refreshFavorites()
    }

    fun loadWallpapers(reset: Boolean = false) {
        scope.launch {
            if (reset) {
                page = 1
                wallpapers = emptyList()
            }
            loading = true
            errorMessage = null
            try {
                val api = when (selectedEngine) {
                    "Wallhaven" -> WallhavenApi()
                    "Pixabay" -> PixabayApi()
                    "Reddit" -> RedditApi()
                    "Picsum" -> PicsumApi()
                    "Bing" -> BingApi()
                    "Owalls" -> OwallsApi()
                    else -> WallhavenApi()
                }
                val currentPage = if (reset) 1 else page
                val results = withContext(Dispatchers.IO) {
                    val api = when (selectedEngine.name) {
                        "Wallhaven" -> WallhavenApi().apply {
                            if (activeTag != "Trending" || searchQuery.isNotBlank()) {
                                selectedTags = listOf(searchQuery.ifBlank { activeTag })
                            }
                        }
                        "Bing" -> BingApi()
                        "Pixabay" -> PixabayApi().apply {
                            if (searchQuery.isNotBlank()) {
                                selectedTags = listOf(searchQuery)
                            } else if (activeTag != "Trending") {
                                selectedTags = listOf(activeTag)
                            }
                        }
                        "Picsum" -> PicsumApi()
                        "Reddit" -> RedditApi()
                        "Google Pixel" -> GooglePixelApi()
                        "NASA" -> NasaPotdApi()
                        "OWalls" -> OwallsApi()
                        "Wikipedia" -> WikipediaPotdApi()
                        else -> WallhavenApi()
                    }
                    api.getWallpapers(currentPage)
                }

                if (results.isEmpty() && currentPage == 1) {
                    errorMessage = "No wallpapers returned from ${selectedEngine.name}"
                } else {
                    wallpapers = if (reset) results else (wallpapers + results).distinctBy { it.imgSrc }
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load wallpapers"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(selectedEngine, activeTag) {
        loadWallpapers(reset = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WallYou",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Labs",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenDetail(Detail.WALLYOU_SETTINGS) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                }
                    IconButton(onClick = { loadWallpapers(reset = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Navigation Tabs (Explore vs Favorites)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Explore", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        refreshFavorites()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Favorites (${favorites.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Engine Selector Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(ENGINES) { engine ->
                        val isSelected = selectedEngine.name == engine.name
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    selectedEngine = engine
                                    searchQuery = ""
                                }
                            },
                            label = { Text(engine.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = engine.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Search & Category Tags (if engine supports search or for categories)
                if (selectedEngine.supportsSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search ${selectedEngine.name} wallpapers...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        loadWallpapers(reset = true)
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                } else {
                                    IconButton(onClick = { loadWallpapers(reset = true) }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    }
                                }
                            }
                        )
                    }

                    // Popular tags row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(POPULAR_TAGS) { tag ->
                            val isTagActive = activeTag == tag && searchQuery.isBlank()
                            SuggestionChip(
                                onClick = {
                                    activeTag = tag
                                    searchQuery = ""
                                    loadWallpapers(reset = true)
                                },
                                label = { Text(tag, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isTagActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            )
                        }
                    }
                }

                // Main Content
                Box(modifier = Modifier.fillMaxSize()) {
                    if (loading && wallpapers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Loading wallpapers from ${selectedEngine.name}...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (errorMessage != null && wallpapers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Failed to load wallpapers",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = errorMessage.orEmpty(),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(onClick = { loadWallpapers(reset = true) }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    } else {
                        WallpaperGridList(
                            wallpapers = wallpapers,
                            onWallpaperClick = { activeWallpaperDetail = it },
                            canLoadMore = selectedEngine.name in listOf("Wallhaven", "Pixabay"),
                            isLoadingMore = loading,
                            onLoadMore = {
                                page += 1
                                loadWallpapers(reset = false)
                            }
                        )
                    }
                }
            } else {
                // Favorites Tab
                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No favorites yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the heart icon on any wallpaper in Explore to save it here for quick access.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    WallpaperGridList(
                        wallpapers = favorites,
                        onWallpaperClick = { activeWallpaperDetail = it },
                        canLoadMore = false,
                        isLoadingMore = false,
                        onLoadMore = {}
                    )
                }
            }
        }
    }

    // Fullscreen Wallpaper Detail Viewer & Actions Sheet
    activeWallpaperDetail?.let { wallpaper ->
        WallpaperDetailDialog(
            wallpaper = wallpaper,
            onDismiss = {
                activeWallpaperDetail = null
                refreshFavorites()
            }
        )
    }
}

@Composable
private fun WallpaperGridList(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(wallpapers, key = { it.imgSrc }) { wallpaper ->
            WallpaperCard(wallpaper = wallpaper, onClick = { onWallpaperClick(wallpaper) })
        }

        if (canLoadMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    } else {
                        OutlinedButton(
                            onClick = onLoadMore,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load More Wallpapers")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.preview)
                    .crossfade(true)
                    .build(),
                contentDescription = wallpaper.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Metadata info badge
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                if (!wallpaper.resolution.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = wallpaper.resolution.orEmpty(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!wallpaper.title.isNullOrBlank()) {
                    Text(
                        text = wallpaper.title.orEmpty(),
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Rich Fullscreen Wallpaper Viewer with Live Effects & Shizuku/System Application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallpaperDetailDialog(
    wallpaper: Wallpaper,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isFavorite by remember { mutableStateOf(WallYouFavoritesManager.isFavorite(context, wallpaper.imgSrc)) }
    var rawBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }

    // Filter controls
    var showFilterSheet by remember { mutableStateOf(false) }
    var blurRadius by remember { mutableFloatStateOf(0f) }
    var grayscale by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(1.0f) }

    var filteredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showApplyChoiceDialog by remember { mutableStateOf(false) }

    // Preload bitmap in background
    LaunchedEffect(wallpaper.imgSrc) {
        withContext(Dispatchers.IO) {
            val bmp = WallpaperManagerHelper.fetchBitmap(context, wallpaper.imgSrc)
            rawBitmap = bmp
            filteredBitmap = bmp
        }
    }

    // Re-apply filters whenever sliders change
    LaunchedEffect(rawBitmap, blurRadius, grayscale, brightness) {
        val base = rawBitmap ?: return@LaunchedEffect
        filteredBitmap = withContext(Dispatchers.Default) {
            BitmapFilters.applyFilters(
                src = base,
                blurRadius = blurRadius.toInt(),
                grayscale = grayscale,
                brightness = brightness
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Ambient blurred backdrop
            AsyncImage(
                model = wallpaper.preview,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Wallpaper image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 50.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (filteredBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = filteredBitmap!!.asImageBitmap(),
                        contentDescription = wallpaper.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(wallpaper.imgSrc)
                            .crossfade(true)
                            .build(),
                        contentDescription = wallpaper.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Favorite Button
                    IconButton(
                        onClick = {
                            isFavorite = WallYouFavoritesManager.toggleFavorite(context, wallpaper)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else Color.White
                        )
                    }

                    // Effects / Filter button
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filters",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Actions Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!wallpaper.title.isNullOrBlank() || !wallpaper.author.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wallpaper.title ?: "Wallpaper",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "By ${wallpaper.author ?: "Unknown"} • ${wallpaper.resolution ?: "HD"}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Apply Wallpaper Button
                        Button(
                            onClick = { showApplyChoiceDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isApplying
                        ) {
                            if (isApplying) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Wallpaper, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply Wallpaper", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Download Button
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    isDownloading = true
                                    val bmp = filteredBitmap ?: rawBitmap ?: WallpaperManagerHelper.fetchBitmap(context, wallpaper.imgSrc)
                                    if (bmp != null) {
                                        val res = WallpaperManagerHelper.saveToGallery(context, bmp, wallpaper.title)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Saved to Pictures/WallYou!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Could not download image", Toast.LENGTH_SHORT).show()
                                    }
                                    isDownloading = false
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = "Download")
                            }
                        }
                    }
                }
            }
        }

        // Apply Target Selector Dialog
        if (showApplyChoiceDialog) {
            AlertDialog(
                onDismissRequest = { showApplyChoiceDialog = false },
                title = { Text("Set Wallpaper", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select where to apply this wallpaper:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        WallpaperTargetMode.entries.forEach { target ->
                            FilledTonalButton(
                                onClick = {
                                    showApplyChoiceDialog = false
                                    scope.launch {
                                        isApplying = true
                                        val bmp = filteredBitmap ?: rawBitmap ?: WallpaperManagerHelper.fetchBitmap(context, wallpaper.imgSrc)
                                        if (bmp != null) {
                                            val res = WallpaperManagerHelper.applyWallpaper(context, bmp, target)
                                            if (res.isSuccess) {
                                                Toast.makeText(context, "Applied to ${target.title} successfully!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to download wallpaper bitmap", Toast.LENGTH_SHORT).show()
                                        }
                                        isApplying = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(target.title)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showApplyChoiceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Effects / Filter BottomSheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Image Effects & Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Blur slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AsyncImage(
                                model = wallpaper.preview,
                                contentDescription = wallpaper.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Text("Blur Effect", style = MaterialTheme.typography.bodyMedium)
                            Text("${blurRadius.toInt()} px", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = blurRadius,
                            onValueChange = { blurRadius = it },
                            valueRange = 0f..25f
                        )
                    }

                    // Brightness slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Brightness", style = MaterialTheme.typography.bodyMedium)
                            Text(String.format(java.util.Locale.getDefault(), "%.1fx", brightness), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            valueRange = 0.3f..1.8f
                        )
                    }

                    // Grayscale switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monochrome / Grayscale", fontWeight = FontWeight.Medium)
                            Text("Convert wallpaper to black & white", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = grayscale,
                            onCheckedChange = { grayscale = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                blurRadius = 0f
                                grayscale = false
                                brightness = 1.0f
                            }
                        ) {
                            Text("Reset")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { showFilterSheet = false }) {
                            Text("Done")
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
