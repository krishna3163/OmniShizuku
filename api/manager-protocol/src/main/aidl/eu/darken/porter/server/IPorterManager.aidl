package eu.darken.porter.server;

import eu.darken.porter.server.IPorterRemoteProcess;

// The operations only the Porter manager may perform. Not part of the published protocol: the
// server hands this binder to the manager alone, and gates every method on the manager identity
// again, so holding the binder is never what authorizes a call.
interface IPorterManager {

    IPorterRemoteProcess newProcess(in String[] cmd, in String[] env, String dir) = 1;

    void exit() = 2;

    void attachUserService(in IBinder binder, String token) = 3;

    oneway void dispatchPermissionConfirmationResult(int uid, int pid, int requestCode, boolean allowed, boolean onetime) = 4;

    int getFlagsForUid(int uid, int mask) = 5;

    void updateFlagsForUid(int uid, int mask, int value) = 6;
}
