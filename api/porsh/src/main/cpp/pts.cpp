#include <jni.h>
#include <unistd.h>
#include <termios.h>
#include <fcntl.h>
#include <cstdlib>
#include <android/log.h>
#include <pthread.h>
#include <poll.h>
#include <sys/sendfile.h>
#include <functional>
#include "logging.h"

int make_tty_raw(int fd, termios &old_termios) {
    struct termios termios{};

    if (tcgetattr(fd, &termios) < 0) {
        PLOGE("tcgetattr");
        return -1;
    }

    old_termios = termios;

    cfmakeraw(&termios);

    if (tcsetattr(fd, TCSANOW, &termios) < 0) {
        PLOGE("tcsetattr");
        return -1;
    }
    return 0;
}

int restore_fd(int fd, const termios &old_termios) {
    if (tcsetattr(fd, TCSANOW, &old_termios) < 0) {
        PLOGE("tcsetattr");
        return -1;
    }
    return 0;
}

static int write_full(int fd, const void *buf, size_t count) {
    while (count > 0) {
        ssize_t size = write(fd, buf, count < SSIZE_MAX ? count : SSIZE_MAX);
        if (size <= 0) {
            if (errno == EINTR)
                continue;
            else
                return -1;
        }
        buf = (const void *) ((uintptr_t) buf + size);
        count -= size;
    }
    return 0;
}

struct transfer_thread_data {
    int in;
    int out;
    bool close_in;
    bool close_out;
    bool watch_out;
    std::function<void(bool)> function;
};

// function is told whether the relay failed rather than reaching the end of its input. With
// watch_out, out losing its reader counts as a failure even while in has nothing to send.
void transfer(int in, int out, bool close_in, bool close_out, bool watch_out,
              const std::function<void(bool)> &function) {
    char buf[8192];
    int len;
    bool failed = false;
    while (true) {
        if (watch_out) {
            pollfd fds[] = {{in, POLLIN, 0}, {out, 0, 0}};
            if (TEMP_FAILURE_RETRY(poll(fds, 2, -1)) == -1) {
                failed = true;
                break;
            }
            if (fds[1].revents & (POLLERR | POLLHUP | POLLNVAL)) {
                failed = true;
                break;
            }
        }
        if ((len = TEMP_FAILURE_RETRY(read(in, buf, 8192))) <= 0) {
            break;
        }
        if (write_full(out, buf, len) == -1) {
            //PLOGE("write");
            failed = true;
            break;
        }
    }
    //PLOGE("read");

    if (close_in) close(in);
    if (close_out) close(out);
    if (function) function(failed);
}

static void *transfer_thread(void *_data) {
    auto data = (transfer_thread_data *) _data;
    transfer(data->in, data->out, data->close_in, data->close_out, data->watch_out, data->function);
    delete data;
    return nullptr;
}

void transfer_async(int in, int out, const std::function<void(bool)> &function, bool close_in, bool close_out,
                    bool watch_out) {
    pthread_t pthread;
    auto *data = new transfer_thread_data{in, out, close_in, close_out, watch_out, function};
    int err = pthread_create(&pthread, nullptr, transfer_thread, data);
    if (err != 0) {
        errno = err;
        PLOGE("pthread_create");
        delete data;
        if (close_in) close(in);
        if (close_out) close(out);
        if (function) function(true);
        return;
    }
    pthread_detach(pthread);
}

int open_ptmx() {
    int fd = open("/dev/ptmx", O_RDWR | O_CLOEXEC);
    if (fd == -1) {
        return -1;
    }

    if (grantpt(fd) == -1) {
        PLOGE("grantpt");
        close(fd);
        return -1;
    }

    if (unlockpt(fd) == -1) {
        PLOGE("unlockpt");
        close(fd);
        return -1;
    }

    return fd;
}
