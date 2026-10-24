package roro.stellar.manager.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.widget.Toast
import roro.stellar.manager.R
import roro.stellar.manager.compat.BuildUtils.atLeast26
import roro.stellar.manager.ui.features.terminal.CommandItem

object CommandShortcutManager {
    fun requestPin(context: Context, command: CommandItem) {
        if (!atLeast26) {
            Toast.makeText(context, R.string.shortcut_not_supported, Toast.LENGTH_SHORT).show()
            return
        }
        val manager = context.getSystemService(ShortcutManager::class.java)
        if (!manager.isRequestPinShortcutSupported) {
            Toast.makeText(context, R.string.shortcut_not_supported, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(context, CommandShortcutActivity::class.java)
            .setAction(CommandShortcutActivity.ACTION_EXECUTE_COMMAND)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
            .putExtra(CommandShortcutActivity.EXTRA_COMMAND_ID, command.id)
        val shortcut = ShortcutInfo.Builder(context, "command:${command.id}")
            .setShortLabel(command.title)
            .setLongLabel(command.title)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_stellar))
            .setIntent(intent)
            .build()
        manager.requestPinShortcut(shortcut, null)
    }
}
