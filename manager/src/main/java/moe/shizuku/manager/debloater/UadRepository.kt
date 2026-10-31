package moe.shizuku.manager.debloater

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object UadRepository {
    private const val TAG = "UadRepository"
    private const val REMOTE_UAD_URL =
        "https://raw.githubusercontent.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation/main/resources/assets/uad_lists.json"
    private const val UPDATED_FILE_NAME = "uad_lists_updated.json"

    private var cachedDatabase: Map<String, UadEntry>? = null

    suspend fun getDatabase(context: Context): Map<String, UadEntry> = withContext(Dispatchers.IO) {
        cachedDatabase?.let { return@withContext it }

        val db = loadDatabase(context)
        cachedDatabase = db
        db
    }

    private fun loadDatabase(context: Context): Map<String, UadEntry> {
        val updatedFile = File(context.filesDir, UPDATED_FILE_NAME)
        val inputStream: InputStream = if (updatedFile.exists() && updatedFile.length() > 1024) {
            try {
                updatedFile.inputStream()
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading updated UAD file, falling back to assets", e)
                context.assets.open("uad_lists.json")
            }
        } else {
            try {
                context.assets.open("uad_lists.json")
            } catch (e: Exception) {
                Log.e(TAG, "Failed opening uad_lists.json from assets", e)
                return emptyMap()
            }
        }

        return try {
            val jsonText = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonText)
            val result = HashMap<String, UadEntry>(root.length())
            val keys = root.keys()

            while (keys.hasNext()) {
                val pkg = keys.next()
                val obj = root.optJSONObject(pkg) ?: continue
                val list = obj.optString("list", "General")
                val description = obj.optString("description", "")
                val removalStr = obj.optString("removal", "")
                val removal = UadRemoval.fromString(removalStr)

                val deps = mutableListOf<String>()
                val depsArr = obj.optJSONArray("dependencies")
                if (depsArr != null) {
                    for (i in 0 until depsArr.length()) {
                        deps.add(depsArr.optString(i))
                    }
                }

                val needed = mutableListOf<String>()
                val neededArr = obj.optJSONArray("neededBy")
                if (neededArr != null) {
                    for (i in 0 until neededArr.length()) {
                        needed.add(neededArr.optString(i))
                    }
                }

                result[pkg] = UadEntry(
                    packageName = pkg,
                    list = list,
                    description = description,
                    removal = removal,
                    dependencies = deps,
                    neededBy = needed
                )
            }
            Log.i(TAG, "Loaded ${result.size} UAD entries")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing UAD database", e)
            emptyMap()
        }
    }

    suspend fun updateFromRemote(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(REMOTE_UAD_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.requestMethod = "GET"

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }

            val targetFile = File(context.filesDir, UPDATED_FILE_NAME)
            val tempFile = File(context.filesDir, "$UPDATED_FILE_NAME.tmp")

            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.exists() && tempFile.length() > 1024) {
                tempFile.renameTo(targetFile)
                cachedDatabase = null
                val newDb = getDatabase(context)
                newDb.size
            } else {
                tempFile.delete()
                throw IllegalStateException("Downloaded file is empty or invalid")
            }
        }
    }
}
