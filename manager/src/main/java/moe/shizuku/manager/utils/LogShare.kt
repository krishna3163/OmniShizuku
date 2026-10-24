package moe.shizuku.manager.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.AppConstants
import java.io.File

/**
 * Where the whole log is put before it is handed over.
 *
 * The cache, because the file provider already hands that directory out. The other place a log
 * could be written is the app's own directory on shared storage, where the screen's Save puts one,
 * and everything else kept there - the app's modules among it - would be reachable through the
 * same provider if that path were exposed to it. A file that exists to be handed to another app
 * once also has no business outliving the report it was made for.
 */
private const val SHARED_LOG = "shizuku-log.txt"

/**
 * [file] handed to whatever the user picks from the share sheet, which is what a log is for.
 *
 * Returns whether a chooser was opened. A uri the provider will not mint and no app that will take
 * one both throw rather than answer, and both are said out loud to the log rather than swallowed:
 * every caller here is a button, and a button that appears to do nothing is worse than one that
 * reports it could not.
 */
fun shareFile(context: Context, file: File): Boolean =
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
        true
    }.onFailure {
        Diag.warn(AppConstants.TAG, "could not share a log", it)
    }.getOrDefault(false)

/**
 * The whole app log, written and handed over, and whether it could be.
 *
 * The same file a bug report attaches, reachable from where the log is read as well: somebody
 * looking at the lines that matter should not have to open a report dialog to send them on. The
 * write happens here rather than in the caller because it is the part that must not be on the main
 * thread - the window holds a day of lines, and every caller is a tap.
 */
suspend fun shareAppLog(context: Context): Boolean {
    val file = withContext(Dispatchers.IO) {
        File(context.cacheDir, SHARED_LOG).takeIf { Diag.write(it) }
    } ?: return false

    return shareFile(context, file)
}
