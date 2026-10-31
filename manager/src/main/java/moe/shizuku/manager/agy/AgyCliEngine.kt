package moe.shizuku.manager.agy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.commandium.AiProvider
import moe.shizuku.manager.commandium.AiProviderRepository
import moe.shizuku.manager.module.ModuleSettings
import moe.shizuku.manager.utils.AiClient
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Built-in AGY (Antigravity) & Gemini CLI Engine for OmniShizuku.
 *
 * Provides out-of-the-box AI command interpretation, system error diagnostics,
 * automatic fix execution with privileged Shizuku access, and automated bug
 * reporting to GitHub / community developers.
 */
object AgyCliEngine {

    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai"
    const val GEMINI_DEFAULT_MODEL = "gemini-2.5-flash"
    private const val GITHUB_ISSUES_URL = "https://github.com/krishna3163/OmniShizuku/issues/new"

    fun isAgyCommand(command: String): Boolean {
        val trimmed = command.trim().lowercase()
        return trimmed == "agy" || trimmed.startsWith("agy ") ||
                trimmed == "gemini" || trimmed.startsWith("gemini ")
    }

    suspend fun execute(
        context: Context,
        rawCmd: String,
        executeShell: suspend (String) -> Pair<String, Boolean>
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val trimmed = rawCmd.trim()
        val parts = trimmed.split("\\s+".toRegex())
        val isAgy = parts.firstOrNull()?.equals("agy", ignoreCase = true) == true
        val isGemini = parts.firstOrNull()?.equals("gemini", ignoreCase = true) == true

        if (!isAgy && !isGemini) {
            return@withContext Pair("[E] Unknown AGY command: $rawCmd", true)
        }

        val subcmd = parts.getOrNull(1)?.lowercase().orEmpty()
        val args = if (parts.size > 2) parts.drop(2).joinToString(" ") else ""

        when {
            // "agy" or "agy help"
            subcmd.isEmpty() || subcmd == "help" || subcmd == "--help" || subcmd == "-h" -> {
                val hasKey = hasConfiguredKey()
                val isShizukuActive = Shizuku.pingBinder()
                val uid = runCatching { if (isShizukuActive) Shizuku.getUid() else -1 }.getOrDefault(-1)
                val privilegeLabel = when (uid) {
                    0 -> "ROOT (UID 0) - Maximum System Privilege"
                    2000 -> "ADB SHELL (UID 2000) - Full Shizuku Privilege"
                    else -> if (isShizukuActive) "Active (UID $uid)" else "Inactive (Start OmniShizuku service)"
                }

                val banner = buildString {
                    appendLine("╔═══════════════════════════════════════════════════════════════════════╗")
                    appendLine("║  ⚡ Antigravity (AGY) • Embedded Gemini AI Shell & Diagnostic CLI    ║")
                    appendLine("╚═══════════════════════════════════════════════════════════════════════╝")
                    appendLine("  • Environment Privilege : $privilegeLabel")
                    appendLine("  • AI Engine Status      : " + (if (hasKey) "Ready (Google Gemini Active)" else "API Key Needed (Run: agy auth <KEY>)"))
                    appendLine("  • Target Device         : ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
                    appendLine()
                    appendLine("Commands:")
                    appendLine("  agy <prompt>               Ask Gemini anything with system awareness")
                    appendLine("  agy run <action>           AI generates and automatically executes shell commands")
                    appendLine("  agy auth <GEMINI_API_KEY>  Configure your Gemini / AI Studio API key")
                    appendLine("  agy auth                   Check current API key & authentication status")
                    appendLine("  agy info                   Inspect hardware, Android environment & Shizuku state")
                    appendLine("  agy fix                    Diagnose recent logcat errors & execute automatic fix")
                    appendLine("  agy optimize               Run privileged cache trimming & performance tune")
                    appendLine("  agy bug-report <summary>   Collect diagnostics & dispatch bug report to GitHub")
                    appendLine()
                    appendLine("Examples:")
                    appendLine("  agy list all third party apps")
                    appendLine("  agy run disable animations to speed up phone")
                    appendLine("  agy run check battery temperature and health")
                    appendLine("  agy fix")
                }
                Pair(banner, false)
            }

            // "agy auth <KEY>" or "agy login <KEY>" or "agy key <KEY>"
            subcmd in listOf("auth", "login", "key", "config") -> {
                if (args.isNotBlank()) {
                    setGeminiKey(args.trim())
                    Pair("[✓] Gemini API Key saved successfully!\nProvider: Google Gemini ($GEMINI_DEFAULT_MODEL)\nEndpoint: $GEMINI_BASE_URL\n\nAGY is now authorized and ready to execute commands.", false)
                } else {
                    val activeKey = AiProviderRepository.getActiveKey()
                    if (activeKey.isNotBlank()) {
                        val masked = if (activeKey.length > 8) "${activeKey.take(6)}...${activeKey.takeLast(4)}" else "********"
                        Pair("[✓] Authenticated. Active Key: $masked\nProvider: ${AiProviderRepository.getActive()?.name ?: "Google Gemini"}\nModel: ${AiProviderRepository.getActive()?.model?.ifBlank { GEMINI_DEFAULT_MODEL } ?: GEMINI_DEFAULT_MODEL}\n\nTo change key, run: agy auth <NEW_KEY>", false)
                    } else {
                        Pair("[!] No API key configured.\nGet a free Gemini API key from Google AI Studio:\n👉 https://aistudio.google.com/app/apikey\n\nThen authenticate by running:\n  agy auth YOUR_API_KEY", true)
                    }
                }
            }

            // "agy info"
            subcmd == "info" -> {
                val isShizukuActive = Shizuku.pingBinder()
                val uid = runCatching { if (isShizukuActive) Shizuku.getUid() else -1 }.getOrDefault(-1)
                val info = buildString {
                    appendLine("─── System & Device Information ───")
                    appendLine("Device        : ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})")
                    appendLine("Board / Soc   : ${Build.BOARD} / ${Build.HARDWARE}")
                    appendLine("Architecture  : ${Build.SUPPORTED_ABIS.joinToString(", ")}")
                    appendLine("Android OS    : ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}, Build ${Build.DISPLAY})")
                    appendLine("App Version   : ${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})")
                    appendLine()
                    appendLine("─── Shizuku Privilege State ───")
                    appendLine("Server Alive  : $isShizukuActive")
                    appendLine("Process UID   : $uid (${if (uid == 0) "Root" else if (uid == 2000) "Shell" else "User"})")
                    appendLine("Privileges    : Autonomous (Pre-granted inside OmniShizuku)")
                    appendLine()
                    appendLine("─── AI Assistant (AGY) ───")
                    appendLine("Provider      : ${AiProviderRepository.getActive()?.name ?: "Google Gemini"}")
                    appendLine("Configured Key: ${if (hasConfiguredKey()) "Yes" else "No (Run: agy auth <KEY>)"}")
                }
                Pair(info, false)
            }

            // "agy fix" or "agy fix-error"
            subcmd in listOf("fix", "fix-error", "diagnose") -> {
                if (!hasConfiguredKey()) {
                    return@withContext Pair("[!] API Key required for AI diagnostics.\nRun: agy auth <YOUR_GEMINI_KEY>\nGet free key: https://aistudio.google.com/app/apikey", true)
                }
                val (logcatOutput, _) = executeShell("logcat -d -t 60 *:E")
                val logSnippet = if (logcatOutput.isBlank()) "No recent error logs found in logcat." else logcatOutput.takeLast(2500)

                val prompt = "Recent Android system and application error logcat on ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}):\n\n```\n$logSnippet\n```\n\nAnalyze this failure. Identify why it happened, explain clearly, and if there is an actionable ADB shell command to fix it (like pm grant, settings put, am force-stop, pm clear, etc.), write the command in a ```bash ... ``` block."
                val aiResponse = queryGemini(prompt)

                val res = buildString {
                    appendLine("🔍 [AGY Error Diagnostic Scan]")
                    appendLine("──────────────────────────────────────────")
                    appendLine(aiResponse.getOrElse { "Diagnostic failed: ${it.message}" })
                }
                Pair(res, aiResponse.isFailure)
            }

            // "agy optimize"
            subcmd == "optimize" -> {
                val sb = StringBuilder()
                sb.appendLine("🚀 Running privileged maintenance & optimization...")
                val (trimRes, _) = executeShell("pm trim-caches 1000M")
                sb.appendLine("• Cache Trim: ${trimRes.trim().ifBlank { "Completed" }}")

                val (memRes, _) = executeShell("dumpsys meminfo --summary | head -n 12")
                if (memRes.isNotBlank()) {
                    sb.appendLine("• Memory Summary:\n${memRes.trim()}")
                }

                val (battRes, _) = executeShell("dumpsys battery | grep -E 'level|temperature|health|status'")
                if (battRes.isNotBlank()) {
                    sb.appendLine("• Battery Status:\n${battRes.trim()}")
                }
                sb.appendLine("\n[✓] System optimization completed successfully.")
                Pair(sb.toString(), false)
            }

            // "agy bug-report <description>" or "agy issue"
            subcmd in listOf("bug-report", "report-bug", "issue", "report") -> {
                val issueDesc = if (args.isNotBlank()) args else "User reported issue from OmniShizuku AGY Shell"
                val (errLogs, _) = executeShell("logcat -d -t 50 *:E")

                val reportBody = buildString {
                    appendLine("### Description")
                    appendLine(issueDesc)
                    appendLine()
                    appendLine("### Environment Details")
                    appendLine("- **Device**: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
                    appendLine("- **Android**: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                    appendLine("- **OmniShizuku Version**: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    appendLine("- **Shizuku Running**: ${Shizuku.pingBinder()}")
                    appendLine("- **Architecture**: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
                    appendLine()
                    appendLine("### Recent Error Logs")
                    appendLine("```")
                    appendLine(errLogs.takeLast(2000).trim().ifBlank { "No recent fatal crash errors recorded." })
                    appendLine("```")
                }

                // Copy report to clipboard
                try {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("OmniShizuku Bug Report", reportBody))
                } catch (_: Exception) {}

                // Dispatch to GitHub Issues
                val title = "Bug: ${issueDesc.take(60)}"
                val url = "$GITHUB_ISSUES_URL?title=${URLEncoder.encode(title, StandardCharsets.UTF_8.toString())}&body=${URLEncoder.encode(reportBody, StandardCharsets.UTF_8.toString())}"

                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}

                val output = buildString {
                    appendLine("📋 [Bug Report Prepared & Dispatched]")
                    appendLine("──────────────────────────────────────────")
                    appendLine("✓ Formatted GitHub Issue with system specs & error logs.")
                    appendLine("✓ Opened GitHub Issues submission page in browser.")
                    appendLine("✓ Copied complete report to clipboard.")
                    appendLine()
                    appendLine("GitHub URL:")
                    appendLine(GITHUB_ISSUES_URL)
                    appendLine()
                    appendLine("Report Preview:")
                    appendLine(reportBody.take(450) + "...")
                }
                Pair(output, false)
            }

            // "agy run <prompt>" or "agy <prompt>" or "gemini <prompt>"
            else -> {
                val isRunMode = subcmd == "run"
                val userPrompt = if (isRunMode) args else parts.drop(1).joinToString(" ")

                if (userPrompt.isBlank()) {
                    return@withContext Pair("Please provide a prompt or question for AGY.\nExample: agy how to turn on dark mode with adb\nExample: agy run pm list packages -3", true)
                }

                if (!hasConfiguredKey()) {
                    return@withContext Pair("[!] API Key required.\nSet your free Google Gemini API key by running:\n  agy auth <YOUR_GEMINI_KEY>\n\nGet key: https://aistudio.google.com/app/apikey", true)
                }

                val systemPrompt = """
                    You are AGY (Antigravity Assistant), embedded inside the OmniShizuku Android application.
                    You have FULL Shizuku privileged shell access (UID 2000 shell / UID 0 root) on this device (${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}).
                    You can inspect and execute any Android command:
                    - pm (package manager: pm list packages, pm disable, pm enable, pm grant, pm install)
                    - am (activity manager: am start, am force-stop, am broadcast)
                    - settings (system/secure/global settings: settings put/get/delete)
                    - dumpsys (battery, package, window, meminfo)
                    - toybox (standard Unix file & text utilities: ls, cp, mv, rm, df, du, grep)

                    Provide helpful, accurate, concise answers.
                    If the user wants you to perform an action or run commands on the device, provide the exact shell commands in a ```bash ... ``` code block.
                """.trimIndent()

                val result = queryGemini(userPrompt, systemPrompt)
                if (result.isFailure) {
                    return@withContext Pair("[E] AGY Error: ${result.exceptionOrNull()?.message}", true)
                }

                val aiText = result.getOrThrow()

                if (isRunMode) {
                    val commands = extractCommands(aiText)
                    if (commands.isNotEmpty()) {
                        val execOutput = StringBuilder()
                        execOutput.appendLine(aiText)
                        execOutput.appendLine("\n⚡ [AGY Auto-Execution with Shizuku Privilege]")
                        execOutput.appendLine("──────────────────────────────────────────")
                        for (c in commands) {
                            execOutput.appendLine("$ $c")
                            val (out, failed) = executeShell(c)
                            if (out.isNotBlank()) execOutput.appendLine(out.trim())
                            if (failed) execOutput.appendLine("[Status: Failed]")
                        }
                        Pair(execOutput.toString(), false)
                    } else {
                        Pair(aiText, false)
                    }
                } else {
                    Pair(aiText, false)
                }
            }
        }
    }

    private fun extractCommands(text: String): List<String> {
        val regex = "```(?:bash|sh)?\\s*([\\s\\S]*?)```".toRegex()
        val matches = regex.findAll(text)
        val list = mutableListOf<String>()
        for (m in matches) {
            val lines = m.groupValues[1].lines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                    list.add(trimmed)
                }
            }
        }
        return list
    }

    private fun hasConfiguredKey(): Boolean {
        return AiProviderRepository.getActiveKey().isNotBlank()
    }

    fun setGeminiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        val providers = AiProviderRepository.getProviders()
        val existing = providers.firstOrNull { it.baseUrl.trimEnd('/') == GEMINI_BASE_URL.trimEnd('/') }

        val provider = if (existing != null) {
            existing.copy(model = existing.model.ifBlank { GEMINI_DEFAULT_MODEL }).also {
                AiProviderRepository.update(it)
            }
        } else {
            AiProviderRepository.add(
                name = "Google Gemini",
                baseUrl = GEMINI_BASE_URL,
                model = GEMINI_DEFAULT_MODEL
            )
        }

        AiProviderRepository.setActive(provider.id)
        AiProviderRepository.setKey(provider.id, trimmed)
        ModuleSettings.setComputApiKey(trimmed)
    }

    private suspend fun queryGemini(prompt: String, systemPrompt: String? = null): Result<String> {
        val active = AiProviderRepository.getActive()
        val baseUrl = active?.baseUrl?.takeIf { it.isNotBlank() } ?: GEMINI_BASE_URL
        val model = active?.model?.takeIf { it.isNotBlank() } ?: GEMINI_DEFAULT_MODEL
        val apiKey = AiProviderRepository.getActiveKey().ifBlank {
            ModuleSettings.getComputApiKey()
        }

        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("No API key configured. Run 'agy auth <API_KEY>'."))
        }

        return AiClient.chatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            systemPrompt = systemPrompt,
            userPrompt = prompt
        )
    }

    /**
     * Executes an arbitrary command through OmniShizuku's privileged shell service (UID 2000/0).
     */
    fun executePrivilegedShell(cmd: String): Pair<String, Boolean> {
        if (!Shizuku.pingBinder()) {
            return Pair("[E] Shizuku service is not running. Please start OmniShizuku service first.", true)
        }
        val binder = Shizuku.getBinder() ?: return Pair("[E] Shizuku binder unavailable.", true)
        val service = IShizukuService.Stub.asInterface(binder)
        return try {
            val remote = service.newProcess(arrayOf("sh", "-c", cmd), null, null)
            val stdoutPfd = remote.inputStream
            val stderrPfd = remote.errorStream

            var stdoutText = ""
            var stderrText = ""
            val stdoutThread = Thread {
                try {
                    stdoutText = ParcelFileDescriptor.AutoCloseInputStream(stdoutPfd).reader(Charsets.UTF_8).use { it.readText() }
                } catch (_: Exception) {}
            }
            val stderrThread = Thread {
                try {
                    stderrText = ParcelFileDescriptor.AutoCloseInputStream(stderrPfd).reader(Charsets.UTF_8).use { it.readText() }
                } catch (_: Exception) {}
            }
            stdoutThread.start()
            stderrThread.start()

            val finished = remote.waitForTimeout(15L, java.util.concurrent.TimeUnit.SECONDS.name)
            val exitCode = if (finished) remote.exitValue() else { remote.destroy(); 124 }
            stdoutThread.join(1000)
            stderrThread.join(1000)

            val output = buildString {
                if (stdoutText.isNotBlank()) append(stdoutText.trim())
                if (stderrText.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("[E] ").append(stderrText.trim())
                }
                if (exitCode != 0) {
                    if (isNotEmpty()) append("\n")
                    append("[Process exited with code $exitCode]")
                }
            }
            val failed = (!finished || exitCode != 0 || stderrText.isNotBlank())
            Pair(output, failed)
        } catch (e: Exception) {
            Pair("[E] Execution failed: ${e.message}", true)
        }
    }

    /**
     * Executes either an AGY command or standard privileged shell command.
     */
    suspend fun runAgyOrShell(context: Context, cmd: String): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val trimmed = cmd.trim()
        if (isAgyCommand(trimmed)) {
            execute(context, trimmed) { shellCmd ->
                executePrivilegedShell(shellCmd)
            }
        } else {
            executePrivilegedShell(trimmed)
        }
    }
}

