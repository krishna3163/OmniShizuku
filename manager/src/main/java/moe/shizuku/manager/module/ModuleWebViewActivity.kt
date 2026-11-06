package moe.shizuku.manager.module

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.viewinterop.AndroidView
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.ui.compose.ShizukuExpressiveTheme
import moe.shizuku.manager.ui.compose.ShizukuScaffold
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine

private data class PendingCommand(
    val request: ModuleCommandRequest,
    val continuation: kotlinx.coroutines.CancellableContinuation<Boolean>
)

class ModuleWebViewActivity : AppActivity() {

    private val pendingCommands = androidx.compose.runtime.mutableStateListOf<PendingCommand>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moduleId = intent.getStringExtra(EXTRA_MODULE_ID)
            ?: intent.data?.lastPathSegment.orEmpty()
        val module = AdbModuleManager.readModule(AdbModuleManager.modulesRoot(this).resolve(moduleId))
        val index = module?.webRoot?.resolve("index.html")
        if (module == null || index?.isFile != true) {
            Toast.makeText(this, R.string.modules_webui_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            val trusted = ModuleSettings.isModuleTrusted(module.id)
            val webNetworkAllowed = ModuleSettings.canUseWebNetwork(module)
            val exposeBridge = module.enabled &&
                ModuleSettings.canExposeWebBridge(module) &&
                (module.declaresShellBridge || trusted) &&
                (!webNetworkAllowed || trusted)

            ShizukuExpressiveTheme {
                ShizukuScaffold(
                    title = module.name,
                    onNavigateUp = { finish() }
                ) { padding ->
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false
                                settings.allowFileAccessFromFileURLs = false
                                settings.allowUniversalAccessFromFileURLs = false
                                settings.blockNetworkLoads = false
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                                webViewClient = LocalModuleWebViewClient(module, webNetworkAllowed)
                                if (exposeBridge) {
                                    addJavascriptInterface(
                                        ModuleJsBridge(
                                            module,
                                            webView = this,
                                            commandReviewer = { request ->
                                                runBlocking {
                                                    confirmCommand(request)
                                                }
                                            }
                                        ),
                                        "Shizuku"
                                    )
                                }
                                loadUrl(LocalModuleWebViewClient.moduleUrl())
                            }
                        },
                        modifier = androidx.compose.ui.Modifier
                            .padding(padding)
                    )
                }

                val currentPending = pendingCommands.firstOrNull()
                currentPending?.let { pending ->
                    ReCommandDialog(
                        request = pending.request,
                        onDismiss = {
                            if (pending.continuation.isActive) {
                                pending.continuation.resume(false)
                            }
                            pendingCommands.remove(pending)
                        },
                        onReject = {
                            if (pending.continuation.isActive) {
                                pending.continuation.resume(false)
                            }
                            pendingCommands.remove(pending)
                        },
                        onApprove = {
                            if (pending.continuation.isActive) {
                                pending.continuation.resume(true)
                            }
                            pendingCommands.remove(pending)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingCommands.forEach { pending ->
            if (pending.continuation.isActive) {
                pending.continuation.resume(false)
            }
        }
        pendingCommands.clear()
    }

    private suspend fun confirmCommand(request: ModuleCommandRequest): Boolean = suspendCancellableCoroutine { continuation ->
        runOnUiThread {
            val item = PendingCommand(request, continuation)
            pendingCommands.add(item)
            continuation.invokeOnCancellation {
                runOnUiThread {
                    pendingCommands.remove(item)
                }
            }
        }
    }

    private class LocalModuleWebViewClient(
        private val module: AdbModule,
        private val webNetworkAllowed: Boolean
    ) : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            val uri = request.url
            if (uri.scheme?.lowercase() == MODULE_SCHEME && uri.host == MODULE_HOST) {
                val file = resolveModuleFile(uri.path.orEmpty())
                    ?: return WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", null, null)
                return try {
                    WebResourceResponse(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                            ?: "application/octet-stream",
                        null,
                        file.inputStream()
                    )
                } catch (_: Exception) {
                    WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", null, null)
                }
            }
            if (!webNetworkAllowed && (uri.scheme == "http" || uri.scheme == "https")) {
                return WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden", null, null)
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            val scheme = uri.scheme?.lowercase()
            if (scheme == MODULE_SCHEME && uri.host == MODULE_HOST) {
                return false
            }
            if (scheme == "http" || scheme == "https") {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    view.context.startActivity(intent)
                } catch (e: Exception) {
                    // Ignore
                }
                return true
            }
            return true
        }

        private fun resolveModuleFile(path: String): File? {
            val root = module.webRoot ?: return null
            return runCatching {
                val rootPath = root.canonicalPath
                val target = File(root, java.net.URLDecoder.decode(path.removePrefix("/"), "UTF-8"))
                val targetPath = target.canonicalPath
                if (targetPath == rootPath || targetPath.startsWith(rootPath + File.separator)) {
                    target.takeIf { it.isFile }
                } else {
                    null
                }
            }.getOrNull()
        }

        companion object {
            private const val MODULE_SCHEME = "https"
            private const val MODULE_HOST = "module.local"

            fun moduleUrl(): String =
                "$MODULE_SCHEME://$MODULE_HOST/index.html"
        }
    }

    companion object {
        const val EXTRA_MODULE_ID = "module_id"
    }
}
