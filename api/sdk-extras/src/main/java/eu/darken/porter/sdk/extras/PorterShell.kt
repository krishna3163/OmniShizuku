@file:JvmName("PorterShell")

package eu.darken.porter.sdk.extras

import android.content.ComponentName
import android.os.Binder
import android.os.DeadObjectException
import android.os.IBinder
import android.os.RemoteException
import eu.darken.porter.sdk.PorterConnection
import eu.darken.porter.sdk.UserServiceArgs
import eu.darken.porter.sdk.extras.internal.IPorterShellService
import eu.darken.porter.sdk.extras.internal.PorterShellService
import java.util.WeakHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch

/**
 * Runs [command] at the connection's identity, shell or root, and returns once it exits.
 *
 * ```kotlin
 * val result = connection.exec("sh", "-c", "ls /data/local/tmp")
 * if (result.exitCode == 0) show(result.output)
 * ```
 *
 * The command gets no input. Its output is read as UTF-8; a command that writes binary data or
 * needs input takes [startProcess] instead. Cancelling returns at once and, while the command runs,
 * kills it and its process group with SIGKILL, so `withTimeout` bounds a command that does not end.
 * A command that is not found exits with 127, as in a shell.
 *
 * The first call starts a user service for this app, which the calls after it reuse. It needs the
 * permission granted, and a refusal throws the SDK's `PorterSecurityException`. A [dir] that does
 * not exist or a service that stops midway throws [PorterShellException], and an empty [command]
 * throws `IllegalArgumentException`.
 */
public suspend fun PorterConnection.exec(vararg command: String, dir: String? = null): PorterShellResult {
    val process = start(command, dir)
    val run = ShellCalls.scope.async(ShellCalls.blocking) {
        process.outputStream.close()
        val errors = async { process.errorStream.readUtf8() }
        val output = process.inputStream.readUtf8()
        PorterShellResult(process.waitFor(), output, errors.await())
    }
    try {
        return run.await()
    } catch (e: Throwable) {
        // Cancelled, or the output could not be read: either way nobody waits for the process now.
        run.cancel()
        ShellCalls.scope.launch(ShellCalls.blocking) { process.destroy() }
        throw e
    }
}

/**
 * Starts [command] at the connection's identity, shell or root, and returns while it runs.
 *
 * ```kotlin
 * val logcat = connection.startProcess("logcat", "-v", "brief")
 * withContext(Dispatchers.IO) {
 *     try {
 *         logcat.inputStream.bufferedReader().useLines { lines -> lines.take(100).forEach(::show) }
 *     } finally {
 *         logcat.destroy()
 *     }
 * }
 * ```
 *
 * Read its output as it comes, or the command blocks once a pipe is full. Cancelling kills the
 * command only while the start is pending; once this returns, the command is the caller's to stop
 * with [PorterShellProcess.destroy]. Needs the same user service and permission as [exec], and
 * fails the same ways.
 */
public suspend fun PorterConnection.startProcess(vararg command: String, dir: String? = null): PorterShellProcess =
    start(command, dir)

private suspend fun PorterConnection.start(command: Array<out String>, dir: String?): PorterShellProcess {
    require(command.isNotEmpty()) { "No command to run" }
    val binding = ShellCalls.binding(this)
    var service = binding.service(this)
    if (!service.asBinder().isBinderAlive) {
        binding.forget(service)
        service = binding.service(this)
    }
    return try {
        service.startProcess(command, dir)
    } catch (e: PorterShellException) {
        // Not retried: the service may have started the command before it died.
        if (e.cause is DeadObjectException) binding.forget(service)
        throw e
    }
}

private suspend fun IPorterShellService.startProcess(command: Array<out String>, dir: String?): PorterShellProcess {
    val started = AtomicReference<PorterShellProcess?>()
    val call = ShellCalls.scope.async(ShellCalls.blocking) {
        val remote = shellCall { start(arrayOf(*command), dir, ShellCalls.owner) }
        PorterShellProcess(remote).also { started.set(it) }
    }
    try {
        return call.await()
    } catch (e: CancellationException) {
        // A start already sent cannot be withdrawn, so the process it made is killed once it exists.
        call.cancel()
        call.invokeOnCompletion { started.get()?.let { ShellCalls.scope.launch(ShellCalls.blocking) { it.destroy() } } }
        throw e
    }
}

private fun java.io.InputStream.readUtf8(): String = try {
    bufferedReader(Charsets.UTF_8).use { it.readText() }
} catch (e: java.io.IOException) {
    throw PorterShellException("Reading the command's output failed", e)
}

/** What the SDK needs for every shell call, shared by all connections of this process. */
internal object ShellCalls {

    /**
     * One thread per stream being read and per call waiting, never queued: a pipe nobody reads
     * fills up and stops the process that writes it, so a read waiting for a free thread can wait
     * for a process that in turn waits for it.
     */
    val blocking = Executors.newCachedThreadPool { task ->
        Thread(task, "porter-shell").apply { isDaemon = true }
    }.asCoroutineDispatcher()

    val scope = CoroutineScope(SupervisorJob())

    /** Links every process this app starts to the app's own lifetime. */
    val owner: IBinder = Binder()

    private val bindings = WeakHashMap<PorterConnection, ShellBinding>()

    fun binding(connection: PorterConnection): ShellBinding = synchronized(bindings) {
        bindings.getOrPut(connection) { ShellBinding(args(connection.packageName)) }
    }

    /** Runs a blocking shell call so that cancelling the caller returns at once. */
    suspend fun <T> detached(block: () -> T): T {
        val call = scope.async(blocking) { block() }
        try {
            return call.await()
        } catch (e: CancellationException) {
            call.cancel()
            throw e
        }
    }

    fun args(packageName: String): UserServiceArgs = UserServiceArgs(
        componentName = ComponentName(packageName, PorterShellService::class.java.name),
        processNameSuffix = "porter_shell",
        tag = "eu.darken.porter.sdk.extras.shell",
        // Raised whenever the service changes, so a newer app replaces a running older one.
        version = 2,
    )
}

/**
 * The shell service of one connection, bound on first use and held until the service or the
 * connection dies. One binding for every call, because a Shizuku server older than 13.4 keeps each
 * binding it was given until the app's process dies.
 */
internal class ShellBinding(private val args: UserServiceArgs) {

    private var bound: CompletableDeferred<IBinder>? = null

    /** What [bound] completed with, once it has. */
    private var boundService: IBinder? = null

    // Takes the connection rather than holding it: this is the value of a weak map keyed by it.
    suspend fun service(connection: PorterConnection): IPorterShellService {
        val pending = synchronized(this) { bound ?: CompletableDeferred<IBinder>().also { bound = it; bind(connection, it) } }
        return IPorterShellService.Stub.asInterface(pending.await())
    }

    /** Drops [service] so the next call binds again, unless a newer binding already replaced it. */
    fun forget(service: IPorterShellService) {
        synchronized(this) {
            if (boundService === service.asBinder()) {
                bound = null
                boundService = null
            }
        }
    }

    private fun bind(connection: PorterConnection, result: CompletableDeferred<IBinder>) {
        // On the SDK's own threads: callers blocking every Dispatchers.Default worker on this bind
        // would otherwise leave nothing to deliver it.
        ShellCalls.scope.launch(ShellCalls.blocking) {
            try {
                connection.userService(args).collect { service ->
                    // Only the first binder is what callers were handed.
                    synchronized(this@ShellBinding) { if (result.complete(service) && bound === result) boundService = service }
                }
                result.completeExceptionally(PorterShellException("The shell service stopped or could not be bound", null))
            } catch (e: Throwable) {
                result.completeExceptionally(e)
            } finally {
                synchronized(this@ShellBinding) {
                    if (bound === result) {
                        bound = null
                        boundService = null
                    }
                }
            }
        }
    }
}

/** Calls the shell service, whose failures a caller sees as [PorterShellException]. */
internal inline fun <T> shellCall(block: () -> T): T = try {
    block()
} catch (e: RemoteException) {
    throw PorterShellException("The shell service did not answer", e)
} catch (e: IllegalStateException) {
    throw PorterShellException(e.message ?: "The shell service refused the call", e)
}
