package eu.darken.porter.sdk.extras.internal;

import eu.darken.porter.sdk.extras.internal.IPorterShellProcess;

interface IPorterShellService {

    // The server sends this to stop the service.
    void destroy() = 16777114;

    // The process is destroyed when owner's process dies.
    IPorterShellProcess start(in String[] command, String dir, IBinder owner) = 1;
}
