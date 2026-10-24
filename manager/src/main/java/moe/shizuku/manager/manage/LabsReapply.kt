package moe.shizuku.manager.manage

import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuApplication
import moe.shizuku.manager.utils.Diag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Putting back whatever the platform has lost.
 *
 * A firewall block is `cmd connectivity` state, which belongs to the framework: a full reboot takes
 * it, and so does a soft one, where only the framework restarts and the kernel keeps running. The
 * list of what was blocked is this app's and survives either, so afterwards every blocked app is
 * online again with only the list still saying otherwise - issue #75 from the reporting side.
 *
 * Run when a server comes up rather than at boot, because the commands need the shell and there is
 * none until Shizuku is running. Nothing decides in advance whether this is needed: the platform is
 * asked what it still has, so a reboot, a soft reboot and a state somebody else changed mid-session
 * are all one case. An earlier version of this compared the device's uptime against the last time
 * it had run, which a soft reboot does not change - the check was there and the state was gone.
 *
 * Only the lists that are this app's are here. Autostart is an app op, which the platform keeps
 * across a reboot and reports back, so there is nothing to put back; and the hiding lists are
 * applied while a listed app is in front by design, not kept on for the device.
 */
object LabsReapply {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Looks at the firewall list and puts back what is missing, logging what it found.
     *
     * Silent when there is nothing blocked, and a line either way when there is - an automation
     * author reading the log should be able to tell "nothing was lost" from "nothing ran".
     */
    fun putBack() {
        val context = ShizukuApplication.appContext
        if (PackageTools.readFirewallBlocked(context).isEmpty()) return

        scope.launch {
            val restored = runCatching { PackageTools.reapplyNetworkBlocked(context) }
                .onFailure { Diag.warn(AppConstants.TAG, "Could not put the firewall list back", it) }
                .getOrDefault(0)
            Diag.info(AppConstants.TAG, "Firewall: $restored of the blocked apps had to be put back")
        }
    }
}
