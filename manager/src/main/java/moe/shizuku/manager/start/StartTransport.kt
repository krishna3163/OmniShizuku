package moe.shizuku.manager.start

/**
 * Which transport a start will use, as a decision that can be reasoned about without a
 * device.
 *
 * Two of them carry a start: the wireless (TLS) port, which is found over mDNS and so
 * needs a network interface, and the classic ADB port, which is a number that is already
 * known and needs nothing at all. TCP mode is what keeps the classic port open, so once
 * it is on and the port is there a start does not need Wi-Fi which is the point of the
 * mode, and what makes a restart after a reboot work on a device with no network.
 *
 * The two questions here have to be answered together, and for a while they were not: a
 * start that was told it needed no network still went looking for a wireless port over
 * mDNS - which needs exactly the network that was just declared unnecessary - and reached
 * the port it already had only after that search timed out, two minutes later for the
 * forced-wireless method. On a phone whose wireless debugging TCP mode itself had switched
 * away, the search could not succeed at all.
 */
object StartTransport {

    /**
     * Whether a start has to wait for a network before it can reach the TLS port.
     *
     * [forceWireless] is the experimental setting that asks for wireless debugging
     * without a network at all: waiting for one would gate the exact start it exists to
     * make possible, which is why it overrides everything else here.
     */
    fun wifiRequired(tcpPort: Int, tcpMode: Boolean, forceWireless: Boolean = false): Boolean =
        !forceWireless && (tcpPort <= 0 || !tcpMode)

    /**
     * Whether a start should go over the classic ADB port rather than look for a wireless one.
     *
     * The USB method is that port, and a platform without wireless debugging has no other; what is
     * added here is the port TCP mode is keeping open, which is the one that mode exists to provide.
     * [wifiRequired] has already promised that a start in this state needs no network, and this is
     * the answer that makes the promise hold.
     *
     * On Android 17 a port is not merely the faster answer, it is the only one: the platform now
     * refuses to run adbd's wireless server without a wireless network at all - "Not connected to
     * any wireless network. Not enabling adbwifi.", measured with a local-only hotspot up and a
     * wireless network absent - so searching for a wireless port there is asking for a port that
     * cannot exist, and the search is what a networkless start spends its whole window on.
     *
     * [portListening] is the answer only the caller can give, because it is about the phone rather
     * than about the request: `service.adb.tcp.port` outlives adbd, so a number in it is not a port
     * that is there. A phone whose debugging toggles were turned off when Shizuku stopped still
     * reads the port the daemon was using, and taking that number sent a wireless start to a socket
     * nothing was listening on - while skipping the mDNS search that is the only thing which turns
     * wireless debugging back on, which is what the start after that stop has to do. Issue #71:
     * a start there failed with the toggles still off, and needed the toggle switched on by hand.
     */
    fun classicPortInUse(
        usbMethod: Boolean,
        tlsSupported: Boolean,
        tcpPort: Int,
        tcpMode: Boolean,
        portListening: Boolean
    ): Boolean = usbMethod || !tlsSupported ||
        (tcpMode && classicPortFallback(tcpPort) != null && portListening)

    /**
     * The port to use when discovery over mDNS has found nothing, or null when there is
     * nothing to fall back to.
     */
    fun classicPortFallback(tcpPort: Int): Int? = tcpPort.takeIf { it > 0 }

    /**
     * The ports a start should try, in order, before it searches for one over mDNS.
     *
     * Three answers to the same question, and the order is what they are worth: what the platform
     * says the classic port is, what the last start actually reached adbd on - a wireless port is
     * random per boot, so this is the one that makes a restart instant - and the port this app
     * would open itself. [configured] and [appTcpPort] are often the same number and the last one
     * is often absent, which is why the list is cleaned here rather than at each caller: a port
     * that is not a port, or one already asked about, is not a candidate.
     *
     * Nothing here is trusted on its own - the caller probes each in turn - so the cost of a stale
     * entry is one refused connection, and the cost of this list being empty is the search.
     */
    fun portCandidates(configured: Int, lastStart: Int, appTcpPort: Int): List<Int> =
        listOf(configured, lastStart, appTcpPort).filter { it > 0 }.distinct()

    /**
     * Where a candidate port came from, for the log: a port that fails is only useful to read about
     * when the line also says which of the guesses it was.
     */
    enum class PortSource(val label: String) {
        CLASSIC_PROPERTY("classic port property"),
        TLS_PROPERTY("service.adb.tls.port"),
        REMEMBERED("remembered from the last start"),
        APP_SETTING("TCP port setting"),
        MDNS("mDNS")
    }

    /** One port worth asking, and the guess that named it. */
    data class PortCandidate(val port: Int, val source: PortSource)

    /** What [rememberedPortVerdict] makes of the port the last start recorded. */
    enum class RememberedPort {
        /** Worth asking. */
        USABLE,

        /** Nothing was recorded. */
        NONE,

        /** A wireless port, recorded in an earlier boot than this one. */
        OTHER_BOOT,

        /** A wireless port, and either boot is unknown, so it cannot be told to be this one's. */
        UNKNOWN_BOOT
    }

    /**
     * Whether the port the last start reached adbd on can still be the port adbd listens on.
     *
     * A classic port is a number the app or the user chose, and it comes back with TCP mode on every
     * boot, so it stays a candidate. A wireless (TLS) port is drawn at random whenever adbd brings
     * its wireless server up, and that happens at every boot: one recorded in an earlier boot is
     * stale by definition, and the log of the phone that reported this showed exactly that, a first
     * attempt on port 36371 that the new boot had never opened. It is judged by the boot it was
     * recorded in rather than by asking it, because a stale port can be taken by anything else.
     *
     * [lastBoot] and [currentBoot] are the platform's boot counts, -1 where unknown. A wireless port
     * with no recorded boot (one saved by a version before this existed) is not trusted either.
     */
    fun rememberedPortVerdict(
        lastPort: Int,
        lastWasClassic: Boolean,
        lastBoot: Int,
        currentBoot: Int,
        appTcpPort: Int
    ): RememberedPort = when {
        lastPort <= 0 -> RememberedPort.NONE
        lastWasClassic || lastPort == appTcpPort -> RememberedPort.USABLE
        lastBoot <= 0 || currentBoot <= 0 -> RememberedPort.UNKNOWN_BOOT
        lastBoot != currentBoot -> RememberedPort.OTHER_BOOT
        else -> RememberedPort.USABLE
    }

    /**
     * The ports a start asks before it searches, in order, each with the guess that named it.
     *
     * Same order and the same cleaning as [portCandidates]; what is new is the TLS port the platform
     * names itself ([tlsProperty], current by construction: it is cleared with the daemon) and that
     * [remembered] is passed in already judged by [rememberedPortVerdict] (null when it was not
     * usable). A port that two guesses agree on is asked once, under the first.
     */
    fun candidatePorts(
        configured: Int,
        tlsProperty: Int,
        remembered: Int?,
        appTcpPort: Int
    ): List<PortCandidate> = listOf(
        PortCandidate(configured, PortSource.CLASSIC_PROPERTY),
        PortCandidate(tlsProperty, PortSource.TLS_PROPERTY),
        PortCandidate(remembered ?: -1, PortSource.REMEMBERED),
        PortCandidate(appTcpPort, PortSource.APP_SETTING)
    ).filter { it.port > 0 }.distinctBy { it.port }
}
