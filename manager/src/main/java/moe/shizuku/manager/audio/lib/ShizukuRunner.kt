package moe.shizuku.manager.audio.lib

import moe.shizuku.manager.utils.runShellCommand

/**
 * @author legendsayantan
 */
class ShizukuRunner {
    interface CommandResultListener {
        /*
         * Runs after the command executes, at least partially. Does not run with 'done' if the command throws an error.
         * output: The output of the command
         * done: If the command has finished executing
         */
        fun onCommandResult(output: String, done: Boolean) {}

        /*
         * Runs if the command throws an error.
         * error: The error message
         */
        fun onCommandError(error: String) {}
    }

    companion object {
        fun command(command: String, listener: CommandResultListener, lineBundle: Int = 50) {
            Thread {
                try {
                    val output = runShellCommand(command)
                    if (output != null) {
                        listener.onCommandResult(output, true)
                    } else {
                        listener.onCommandError("No Shizuku or command failed")
                    }
                } catch (e: Exception) {
                    listener.onCommandError(e.message ?: "No Shizuku")
                }
            }.start()
        }
    }
}
