package moe.shizuku.manager.settings

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.BugReportDialogBinding
import moe.shizuku.manager.ktx.asLink
import moe.shizuku.manager.ktx.applyTemplateArgs
import moe.shizuku.manager.utils.CustomTabsHelper
import moe.shizuku.manager.utils.shareAppLog
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.worker.AdbStartWorker

class BugReportDialog : DialogFragment() {

    private lateinit var binding: BugReportDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = BugReportDialogBinding.inflate(layoutInflater)

        // Releases and issues are this fork's; the wiki belongs to the fork this is based on,
        // because it documents the behaviour that came from there.
        val updateLink = getString(R.string.bug_report_dialog_link_update)
            .asLink("https://github.com/rushiranpise/Shizuku-Next/releases/latest")

        val wikiLink = getString(R.string.bug_report_dialog_link_wiki)
            .asLink("https://github.com/thedjchi/Shizuku/wiki#troubleshooting")

        val issuesLink = getString(R.string.bug_report_dialog_link_issues)
            .asLink("https://github.com/rushiranpise/Shizuku-Next/issues")

        binding.apply {
            updateText.applyTemplateArgs(updateLink)
            wikiText.applyTemplateArgs(wikiLink)
            issuesText.applyTemplateArgs(issuesLink)
            methodText.applyTemplateArgs("GitHub")
            shareLog.setOnClickListener { shareLog() }
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.settings_report_bug)
            .setView(binding.root)
            // The chooser, not the blank composer: the repository's own templates ask for the
            // things a report needs, in the order they are needed, and a form is easier to fill
            // than an empty box. What the app used to do - put a block of machine text and a
            // sentence at the top of a new issue - only produced reports that read like a
            // template and still said nothing about what went wrong, so nothing is pre-filled
            // any more, not even a title.
            .setPositiveButton("GitHub") { _, _ ->
                CustomTabsHelper.launchUrlOrCopy(context, ISSUES_NEW)
            }
            .setNeutralButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    /**
     * The whole log, handed to whatever the reporter picks so they can attach it.
     *
     * [shareAppLog] does the writing and the sharing, and the same call is behind the Log screen's
     * own Share - one implementation, so what a report attaches and what the screen sends cannot
     * be two different things. What this adds is only what to say when it cannot: a button that
     * appears to do nothing is worse than one that reports it could not.
     */
    private fun shareLog() {
        lifecycleScope.launch {
            val context = context ?: return@launch
            if (!shareAppLog(context)) {
                Toast.makeText(context, R.string.log_share_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ShizukuReceiverStarter.NOTIFICATION_ID)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (activity is BugReportDialogActivity) activity?.finish()
    }

    private companion object {
        /** The repository's issue templates, which is where a report should start. */
        const val ISSUES_NEW = "https://github.com/rushiranpise/Shizuku-Next/issues/new/choose"
    }
}
