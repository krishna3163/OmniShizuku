#include <jni.h>
#include "porsh_terminal.h"
#include "porsh_host.h"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;

    if (porsh_host_registerNatives(env) != JNI_OK
        || porsh_terminal_registerNatives(env) != JNI_OK) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
