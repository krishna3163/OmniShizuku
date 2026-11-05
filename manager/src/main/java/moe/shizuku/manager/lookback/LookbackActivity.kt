package moe.shizuku.manager.lookback

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import com.google.android.material.button.MaterialButton
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.utils.runShellCommand
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import android.util.Log

class LookbackActivity : AppActivity() {

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { installApk(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lookback)
        findViewById<MaterialButton>(R.id.startBtn).setOnClickListener {
            pickFile.launch("application/vnd.android.package-archive")
        }
    }

    private fun installApk(uri: Uri) {
        Thread {
            val inputStream = contentResolver.openInputStream(uri) ?: return@Thread
            val cacheFile = File(Environment.getExternalStorageDirectory(), "/Android/data/${packageName}/installcache.apk")
            copyFile(inputStream, cacheFile) { success ->
                if (success) {
                    val packageToInstall = packageManager.getPackageArchiveInfo(cacheFile.absolutePath, 0)?.packageName ?: ""
                    val command = "cat ${cacheFile.absolutePath} | pm install -S ${cacheFile.length()} -r -d"
                    runOnUiThread {
                        Toast.makeText(this@LookbackActivity, "Installing $packageToInstall.", Toast.LENGTH_SHORT).show()
                    }
                    val output = runShellCommand(command)
                    runOnUiThread {
                        if (output != null && output.contains("Success", true)) {
                            Toast.makeText(
                                applicationContext,
                                "Installed Successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Failure: $output",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        cacheFile.delete()
                    }
                }
            }
        }.start()
    }

    private fun copyFile(inputStream: InputStream, outputFile: File, callback: (Boolean) -> Unit) {
        try {
            if (!outputFile.exists()) {
                outputFile.parentFile?.mkdirs()
                outputFile.createNewFile()
            }
            val outputStream = FileOutputStream(outputFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            callback(true)
        } catch (e: IOException) {
            Log.e("LookbackActivity", Log.getStackTraceString(e))
            callback(false)
        }
    }
}
