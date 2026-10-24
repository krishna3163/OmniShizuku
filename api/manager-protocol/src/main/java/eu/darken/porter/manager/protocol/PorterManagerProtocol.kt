package eu.darken.porter.manager.protocol

/** What the manager and the server agree on beyond the generated `IPorterManager` stub. */
object PorterManagerProtocol {

    /** Must equal the descriptor of the generated `IPorterManager.Stub`. */
    const val DESCRIPTOR: String = "eu.darken.porter.server.IPorterManager"

    // permission flags, as getFlagsForUid and updateFlagsForUid speak them
    const val FLAG_ALLOWED: Int = 1 shl 1
    const val FLAG_DENIED: Int = 1 shl 2
    const val MASK_PERMISSION: Int = FLAG_ALLOWED or FLAG_DENIED
}
