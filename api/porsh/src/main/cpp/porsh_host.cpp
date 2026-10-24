#include <jni.h>
#include <unistd.h>
#include <termios.h>
#include <fcntl.h>
#include <cstdlib>
#include <wait.h>
#include <android/log.h>
#include <pthread.h>
#include "logging.h"
#include "pts.h"

static int setWindowSize(int ptmx, jlong size) {
    static_assert(sizeof(jlong) == sizeof(winsize));
    winsize w = *((winsize *) &size);

    LOGD("setWindowSize %d %d %d %d", w.ws_row, w.ws_col, w.ws_xpixel, w.ws_ypixel);

    if (ioctl(ptmx, TIOCSWINSZ, &w) == -1) {
        PLOGE("ioctl TIOCGWINSZ");
        return -1;
    }
    return 0;
}

// libcore/ojluni/src/main/native/UNIXProcess_md.c

static void *xmalloc(JNIEnv *env, size_t size) {
    void *p = malloc(size);
    if (p == nullptr)
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), nullptr);
    else
        memset(p, 0, size);
    return p;
}

#define NEW(type, n) ((type *) xmalloc(env, (n) * sizeof(type)))

static const char *getBytes(JNIEnv *env, jbyteArray arr) {
    return arr == nullptr ? nullptr : (const char *) env->GetByteArrayElements(arr, nullptr);
}

static void releaseBytes(JNIEnv *env, jbyteArray arr, const char *parr) {
    if (parr != nullptr)
        env->ReleaseByteArrayElements(arr, (jbyte *) parr, JNI_ABORT);
}

static void initVectorFromBlock(const char **vector, const char *block, int count) {
    int i;
    const char *p;
    for (i = 0, p = block; i < count; i++) {
        /* Invariant: p always points to the start of a C string. */
        vector[i] = p;
        while (*(p++));
    }
    vector[count] = nullptr;
}

// Once the child has called setsid() it leads its own process group, and -pid reaches everything
// it started. Before that it has started nothing, and only pid reaches it. pid is signalled only
// while the child is unreaped: after that its number may belong to someone else.
static void kill_child(pid_t pid) {
    siginfo_t info{};
    bool unreaped = waitid(P_PID, pid, &info, WEXITED | WNOHANG | WNOWAIT) == 0;
    kill(-pid, SIGKILL);
    if (unreaped) kill(pid, SIGKILL);
}

static jintArray PorshHost_startHost(
        JNIEnv *env, jclass clazz,
        jbyteArray argBlock, jint argc,
        jbyteArray envBlock, jint envc,
        jbyteArray dirBlock,
        jbyte tty,
        jint stdin_read, jint stdout_write, jint stderr_write) {

    bool in_tty = tty & ATTY_IN;
    bool out_tty = tty & ATTY_OUT;
    bool err_tty = tty & ATTY_ERR;

    int ptmx = -1;
    if (tty) {
        ptmx = open_ptmx();
        if (ptmx == -1) {
            env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), "Unable to open ptmx");
            return nullptr;
        }
        LOGD("ptmx %d", ptmx);
    }

    int stdin_pipe[2]{-1}, stdout_pipe[2]{-1}, stderr_pipe[2]{-1};

    LOGD("istty stdin %d stdout %d stderr %d", (tty & ATTY_IN) ? 1 : 0, (tty & ATTY_OUT) ? 1 : 0, (tty & ATTY_ERR) ? 1 : 0);

    if (!in_tty) {
        pipe2(stdin_pipe, 0);
    }
    if (!out_tty) {
        pipe2(stdout_pipe, 0);
    }
    if (!err_tty) {
        pipe2(stderr_pipe, 0);
    }

    const char *pargBlock = getBytes(env, argBlock);
    const char **argv = NEW(const char *, argc + 2);
    argv[0] = "/system/bin/sh";
    initVectorFromBlock(argv + 1, pargBlock, argc);

    for (int i = 0; i < argc + 2; ++i) {
        LOGD("arg%d=%s", i, argv[i]);
    }

    const char **envv = nullptr;
    const char *penvBlock = nullptr;
    if (envc > 0) {
        penvBlock = getBytes(env, envBlock);
        envv = NEW(const char *, envc + 1);

        initVectorFromBlock(envv, penvBlock, envc);
    }

    const char *pdir = nullptr;
    if (dirBlock) {
        pdir = getBytes(env, dirBlock);
    }

    auto pid = fork();
    if (pid == -1) {
        releaseBytes(env, argBlock, pargBlock);
        releaseBytes(env, envBlock, penvBlock);
        releaseBytes(env, dirBlock, pdir);

        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), "Unable to fork");
        return nullptr;
    }

    if (pid > 0) {
        releaseBytes(env, argBlock, pargBlock);
        releaseBytes(env, envBlock, penvBlock);
        releaseBytes(env, dirBlock, pdir);

        auto called = std::make_shared<std::atomic_bool>(false);
        // Only a relay that failed, not one that reached the end of its input, ends the child
        // (PorshHostTtyTest.ttyChild_keepsItsExitCode).
        auto func = [pid, called](bool failed) {
            if (!failed || called->exchange(true)) {
                return;
            }

            LOGW("client dead, kill forked process");
            kill_child(pid);
        };

        // The ends this server keeps for the session's lifetime, close-on-exec from here on so the
        // shells of later sessions do not inherit them. Set after the fork: the shell's own copies
        // stay as the child sets them up.
        if (!in_tty) fcntl(stdin_pipe[1], F_SETFD, FD_CLOEXEC);
        if (!out_tty) fcntl(stdout_pipe[0], F_SETFD, FD_CLOEXEC);
        if (!err_tty) fcntl(stderr_pipe[0], F_SETFD, FD_CLOEXEC);

        // Each transfer thread closes a copy of its own, so none can close the master under
        // another or under PorshHost, which closes the original once the shell has exited. A shell
        // left without a relay would block on its pty, so it is killed instead.
        auto pty = [ptmx, pid]() {
            int fd = fcntl(ptmx, F_DUPFD_CLOEXEC, 0);
            if (fd == -1) {
                PLOGE("dup ptmx");
                kill_child(pid);
            }
            return fd;
        };

        if (in_tty) {
            int fd = pty();
            if (fd != -1) {
                transfer_async(stdin_read, fd/*, func*/);
            } else {
                close(stdin_read);
            }
        } else {
            transfer_async(stdin_read, stdin_pipe[1]/*, func*/);
            close(stdin_pipe[0]);
        }

        if (out_tty) {
            int fd = pty();
            if (fd != -1) {
                transfer_async(fd, stdout_write, func, true, true, true);
            } else {
                close(stdout_write);
            }
        } else {
            transfer_async(stdout_pipe[0], stdout_write, func, true, true, true);
            close(stdout_pipe[1]);

            if (in_tty || err_tty) {
                // Nothing else reads this pty, yet stderr or /dev/tty writes land on it, so it is
                // drained or the child blocks once its buffer fills (PorshHostTtyTest).
                int null_fd = open("/dev/null", O_WRONLY | O_CLOEXEC);
                if (null_fd == -1) {
                    PLOGE("open /dev/null");
                    kill_child(pid);
                } else {
                    int fd = pty();
                    if (fd != -1) {
                        transfer_async(fd, null_fd, [pid, called](bool failed) {
                            if (failed && !called->exchange(true)) kill_child(pid);
                        });
                    } else {
                        close(null_fd);
                    }
                }
            }
        }

        if (!err_tty) {
            transfer_async(stderr_pipe[0], stderr_write/*, func*/);
            close(stderr_pipe[1]);
        }

        auto result = env->NewIntArray(2);
        env->SetIntArrayRegion(result, 0, 1, &pid);
        env->SetIntArrayRegion(result, 1, 1, &ptmx);
        return result;
    } else {
        if (setsid() < 0) {
            PLOGE("setsid");
            exit(1);
        }

        if (pdir) {
            LOGD("attempt to chdir %s", pdir);

            if (access(pdir, X_OK) == 0) {
                if (chdir(pdir) == -1) {
                    PLOGE("chdir %s", pdir);
                } else {
                    LOGD("chdir %s", pdir);
                }
            } else {
                PLOGE("access %s", pdir);
            }
        }

        int pts = -1;
        if (tty) {
            char pts_slave[PATH_MAX]{0};
            if (ptsname_r(ptmx, pts_slave, PATH_MAX - 1) == -1) {
                PLOGE("ptsname_r");
                exit(1);
            }

            if ((pts = open(pts_slave, O_RDWR)) == -1) {
                PLOGE("open %s", pts_slave);
            }
            LOGD("pts %d", pts);
        } else {
            LOGD("no need pts");
        }

        if (in_tty) {
            dup2(pts, STDIN_FILENO);
            LOGD("pts -> in");
        } else {
            dup2(stdin_pipe[0], STDIN_FILENO);
            close(stdin_pipe[1]);
            LOGD("pipe -> in");
        }

        if (out_tty) {
            dup2(pts, STDOUT_FILENO);
            LOGD("pts -> out");
        } else {
            dup2(stdout_pipe[1], STDOUT_FILENO);
            close(stdout_pipe[0]);
            LOGD("pipe -> out");
        }

        if (err_tty) {
            dup2(pts, STDERR_FILENO);
            LOGD("pts -> err");
        } else {
            dup2(stderr_pipe[1], STDERR_FILENO);
            close(stderr_pipe[0]);
            LOGD("pipe -> err");
        }

        LOGD("istty stdin %d stdout %d stderr %d", isatty(STDIN_FILENO), isatty(STDOUT_FILENO), isatty(STDERR_FILENO));

        if (pts != -1) {
            close(pts);
        }

        if (envv) {
            if (execvpe("/system/bin/sh", (char *const *) argv, (char *const *) envv) == -1) {
                PLOGE("execv");
                exit(1);
            }
        } else {
            if (execvp("/system/bin/sh", (char *const *) argv) == -1) {
                PLOGE("execv");
                exit(1);
            }
        }
        exit(0);
    }
}

static void PorshHost_setWindowSize(JNIEnv *env, jclass clazz, jint ptmx, jlong size) {
    setWindowSize(ptmx, size);
}

static jint PorshHost_waitFor(JNIEnv *env, jclass clazz, jint pid) {
    if (pid < 0)
        return -1;

    int status;
    int w;
    do {
        w = TEMP_FAILURE_RETRY(waitpid(pid, &status, 0));
        if (w == -1) {
            if (errno == ECHILD) {
                return 0;
            }
            PLOGE("waitpid");
            return -1;
        }

        if (WIFEXITED(status)) {
            LOGD("exited with %d", WEXITSTATUS(status));
            return WEXITSTATUS(status);
        } else if (WIFSIGNALED(status)) {
            LOGD("killed by signal %d", WTERMSIG(status));
            return 128 + WTERMSIG(status);
        }
    } while (!WIFEXITED(status) && !WIFSIGNALED(status));

    return -1;
}

int porsh_host_registerNatives(JNIEnv *env) {
    auto clazz = env->FindClass("eu/darken/porter/porsh/PorshHost");
    JNINativeMethod methods[] = {
            {"start",         "([BI[BI[BBIII)[I", (void *) PorshHost_startHost},
            {"setWindowSize", "(IJ)V",            (void *) PorshHost_setWindowSize},
            {"waitFor",       "(I)I",             (void *) PorshHost_waitFor},
    };
    return env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
}
