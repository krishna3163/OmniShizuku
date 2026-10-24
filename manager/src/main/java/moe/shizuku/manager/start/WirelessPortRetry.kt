package moe.shizuku.manager.start

import java.io.IOException
import java.net.SocketException
import javax.net.ssl.SSLException
import moe.shizuku.manager.adb.AdbPairingRequiredException

/** How a connection to adbd failed, as far as choosing a new port is concerned. */
enum class ConnectFailureKind {
    /** Nothing listens, or what listens closed the connection: the signature of a stale port. */
    REFUSED_OR_CLOSED,

    /** The handshake failed in a way that is not the device refusing our key. */
    TLS,

    /** The device rejected our key. Another port cannot change that. */
    PAIRING,

    /** Anything else: not a reason to go looking for another port. */
    OTHER
}

/**
 * Whether a TLS failure's text is the device refusing our key, as opposed to the handshake failing
 * for some other reason.
 *
 * Both reach the start as [AdbPairingRequiredException] (see AdbStarter's connect), because any
 * [SSLException] is reported as pairing. That is right for the person and wrong for a port: a
 * stale wireless port that something else now holds fails the handshake as well, and it is not a
 * device waiting to be paired.
 */
internal fun isPairingRejection(message: String?): Boolean =
    message != null &&
        (message.contains("CERTIFICATE_UNKNOWN", ignoreCase = true) ||
            message.contains("SSLV3_ALERT", ignoreCase = true))

internal fun classifyConnectFailure(e: Throwable): ConnectFailureKind = when {
    e is AdbPairingRequiredException ->
        if (isPairingRejection(e.message) || isPairingRejection(e.cause?.message)) {
            ConnectFailureKind.PAIRING
        } else {
            ConnectFailureKind.TLS
        }
    e is SSLException -> ConnectFailureKind.TLS
    // EOFException, ConnectException and a timed out connect are all IOExceptions or
    // SocketExceptions; what they share is that no handshake completed.
    e is SocketException || e is IOException -> ConnectFailureKind.REFUSED_OR_CLOSED
    else -> ConnectFailureKind.OTHER
}

/** What to do about a wireless port before connecting to it, or after failing to. */
enum class PortRetryDecision {
    /** Use the port as it is. */
    USE_AS_IS,

    /** Ask for a fresh wireless port, other than the ones that failed. */
    SEARCH_AGAIN,

    /** Stop asking: report the failure the connection gave. */
    GIVE_UP
}

/** How many fresh ports one start may ask for after the first one failed. */
const val MAX_FRESH_PORT_SEARCHES = 3

/**
 * Before a connection: a wireless port that nothing answers on is not worth a connect.
 *
 * The connect itself retries five times over ten seconds, which is a long time to spend on a
 * socket that refused on the first. A classic port is never searched for - there is no other
 * port to find - and once the searches are used up the port is left to say what is wrong.
 */
internal fun decideBeforeConnect(
    wireless: Boolean,
    listening: Boolean,
    searchesUsed: Int,
    maxSearches: Int = MAX_FRESH_PORT_SEARCHES
): PortRetryDecision = when {
    !wireless || listening -> PortRetryDecision.USE_AS_IS
    searchesUsed < maxSearches -> PortRetryDecision.SEARCH_AGAIN
    else -> PortRetryDecision.USE_AS_IS
}

/**
 * After a connection failed: whether another wireless port is worth asking for.
 *
 * Never once the starter has been sent ([commandSent]): that failure is about a server that may be
 * coming up, and a second start over it is what kills it. Never for a rejected key, which every
 * port would reject alike.
 */
internal fun decideAfterConnectFailure(
    wireless: Boolean,
    commandSent: Boolean,
    failure: ConnectFailureKind,
    searchesUsed: Int,
    maxSearches: Int = MAX_FRESH_PORT_SEARCHES
): PortRetryDecision = when {
    !wireless || commandSent -> PortRetryDecision.GIVE_UP
    failure == ConnectFailureKind.PAIRING || failure == ConnectFailureKind.OTHER ->
        PortRetryDecision.GIVE_UP
    searchesUsed >= maxSearches -> PortRetryDecision.GIVE_UP
    else -> PortRetryDecision.SEARCH_AGAIN
}
