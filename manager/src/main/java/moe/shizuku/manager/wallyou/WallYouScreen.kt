package moe.shizuku.manager.wallyou

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.youapps.wallpaper_apis.RetrofitHelper
import net.youapps.wallpaper_apis.bi.BingApi
import net.youapps.wallpaper_apis.ow.OwallsApi
import net.youapps.wallpaper_apis.pb.PixabayApi
import net.youapps.wallpaper_apis.ps.PicsumApi
import net.youapps.wallpaper_apis.re.RedditApi
import net.youapps.wallpaper_apis.wh.WallhavenApi
import net.youapps.wallpaper_apis.wi.WikipediaPotdApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallYouScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedEngine by remember { mutableStateOf("Wallhaven") }
    var wallpapers by remember { mutableStateOf<List<net.youapps.wallpaper_apis.Wallpaper>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(1) }
    
    val engines = listOf("Wallhaven", "Pixabay", "Reddit", "Picsum", "Bing", "Owalls", "Wikimedia")
    
    fun loadWallpapers() {
        scope.launch {
            loading = true
            try {
                val api = when (selectedEngine) {
                    "Wallhaven" -> WallhavenApi()
                    "Pixabay" -> PixabayApi()
                    "Reddit" -> RedditApi()
                    "Picsum" -> PicsumApi()
                    "Bing" -> BingApi()
                    "Owalls" -> OwallsApi()
                    "Wikimedia" -> WikipediaPotdApi()
                    else -> WallhavenApi()
                }
                val results = withContext(Dispatchers.IO) {
                    api.getWallpapers(page)
                }
                wallpapers = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }
    
    LaunchedEffect(selectedEngine, page) {
        loadWallpapers()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WallYou Wallpapers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = engines.indexOf(selectedEngine)) {
                engines.forEachIndexed { index, engine ->
                    Tab(
                        selected = selectedEngine == engine,
                        onClick = { selectedEngine = engine; page = 1 },
                        text = { Text(engine) }
                    )
                }
            }
            
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(wallpapers) { wallpaper ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.6f)
                                .clickable {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                // Basic implementation - wallpaper setting
                                                val wm = WallpaperManager.getInstance(context)
                                                // Would need to download and set
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = wallpaper.preview,
                                contentDescription = wallpaper.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
