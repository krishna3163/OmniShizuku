package moe.shizuku.manager.wallyou

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.youapps.wallpaper_apis.Wallpaper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

enum class WallpaperTargetMode(val title: String) {
    HOME("Home Screen"),
    LOCK("Lock Screen"),
    BOTH("Both Screens")
}

object WallpaperManagerHelper {
    private const val TAG = "WallYou:Helper"

    /**
     * Downloads an image from the network and decodes it as an uncompressed Software Bitmap.
     */
    suspend fun fetchBitmap(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                return@withContext bitmap
            }
        } catch (e: Exception) {
            Log.w(TAG, "Coil decode failed, attempting standard stream fallback", e)
        }

        // Direct stream fallback
        runCatching {
            URL(url).openStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    /**
     * Applies the bitmap as wallpaper using Android's WallpaperManager.
     */
    suspend fun applyWallpaper(
        context: Context,
        bitmap: Bitmap,
        target: WallpaperTargetMode
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val wm = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when (target) {
                    WallpaperTargetMode.HOME -> {
                        wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    }
                    WallpaperTargetMode.LOCK -> {
                        wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                    WallpaperTargetMode.BOTH -> {
                        wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    }
                }
            } else {
                wm.setBitmap(bitmap)
            }
            Unit
        }
    }

    /**
     * Saves the wallpaper image into device's standard Pictures/Wallpapers directory.
     */
    suspend fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        suggestedName: String? = null
    ): Result<Uri?> = withContext(Dispatchers.IO) {
        runCatching {
            val title = suggestedName?.replace(Regex("[^a-zA-Z0-9_]"), "_")?.take(40)
                ?: "WallYou_${System.currentTimeMillis()}"
            val fileName = "$title.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/WallYou")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Failed to create MediaStore entry")

                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
                uri
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WallYou")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }
                Uri.fromFile(file)
            }
        }
    }
}

/**
 * Lightweight Persistent Favorites Manager for WallYou
 */
object WallYouFavoritesManager {
    private const val PREFS_NAME = "wallyou_favorites"
    private const val KEY_FAVORITES = "fav_wallpapers_json"

    fun getFavorites(context: Context): List<Wallpaper> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return runCatching {
            val list = mutableListOf<Wallpaper>()
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Wallpaper(
                        imgSrc = obj.optString("imgSrc"),
                        title = obj.optString("title").ifEmpty { null },
                        url = obj.optString("url").ifEmpty { null },
                        author = obj.optString("author").ifEmpty { null },
                        category = obj.optString("category").ifEmpty { null },
                        resolution = obj.optString("resolution").ifEmpty { null },
                        thumb = obj.optString("thumb").ifEmpty { null },
                        description = obj.optString("description").ifEmpty { null }
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    fun isFavorite(context: Context, imgSrc: String): Boolean {
        return getFavorites(context).any { it.imgSrc == imgSrc }
    }

    fun toggleFavorite(context: Context, wallpaper: Wallpaper): Boolean {
        val current = getFavorites(context).toMutableList()
        val exists = current.any { it.imgSrc == wallpaper.imgSrc }
        val newLikedState: Boolean

        if (exists) {
            current.removeAll { it.imgSrc == wallpaper.imgSrc }
            newLikedState = false
        } else {
            current.add(0, wallpaper)
            newLikedState = true
        }

        val arr = JSONArray()
        current.forEach { w ->
            val obj = JSONObject().apply {
                put("imgSrc", w.imgSrc)
                put("title", w.title.orEmpty())
                put("url", w.url.orEmpty())
                put("author", w.author.orEmpty())
                put("category", w.category.orEmpty())
                put("resolution", w.resolution.orEmpty())
                put("thumb", w.thumb.orEmpty())
                put("description", w.description.orEmpty())
            }
            arr.put(obj)
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FAVORITES, arr.toString())
            .apply()

        return newLikedState
    }
}
