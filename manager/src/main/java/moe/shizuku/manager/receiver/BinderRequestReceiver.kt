package moe.shizuku.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.shell.ShellBinderRequestHandler
import moe.shizuku.manager.utils.BootTimeline
import moe.shizuku.manager.utils.Diag
import moe.shizuku.manager.utils.ShizukuStateMachine

class BinderRequestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "rikka.shizuku.intent.action.REQUEST_BINDER") return

        // Who is waiting for the binder, and how long after boot, is the other end of the timeline:
        // the start's own lines say when the server came up, this says when somebody needed it.
        Diag.info(
            AppConstants.TAG,
            "Binder requested by ${requester(intent)}, server ${ShizukuStateMachine.get()}" +
                BootTimeline.suffix()
        )

        ShellBinderRequestHandler.handleRequest(context, intent)

        // Auto wake-up: an app asked for the binder while the server is down.
        // Start it in the background, but only when the user enabled start on
        // boot and did not deliberately stop Shizuku.
        if (!ShizukuStateMachine.isRunning() &&
            !ShizukuSettings.getManuallyStopped() &&
            ShizukuSettings.getStartOnBoot(context)
        ) {
            ShizukuReceiverStarter.start(context, reason = "binder-request")
        } else if (!ShizukuStateMachine.isRunning()) {
            // Nothing happens here otherwise, which is why a background start can look like
            // the request went nowhere: say which setting stopped it. A deliberate Stop is
            // the usual one, and it stays in force until a start is asked for from the app.
            Diag.info(
                AppConstants.TAG,
                "Background start not attempted: " +
                    (if (ShizukuSettings.getManuallyStopped()) "the service was stopped deliberately"
                    else "start on boot is off") +
                    BootTimeline.suffix()
            )
        }
    }

    /**
     * The package the request names, when it names one. The shell loader sends it from the shell,
     * which has no package, and an app that opts in to sharing its identity is the only other
     * source, so "unknown sender" is the common answer rather than an error.
     */
    private fun requester(intent: Intent): String =
        intent.`package`
            ?: (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) sentFromPackage else null)
            ?: "an unknown sender"
}
