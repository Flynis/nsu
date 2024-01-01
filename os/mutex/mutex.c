#include "mutex.h"

#include <sys/syscall.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <limits.h>
#include <linux/futex.h>


static int futex(int *uaddr, int futex_op, int val, const struct timespec *timeout, int *uaddr2, int val3) {
    return syscall(SYS_futex, uaddr, futex_op, val, timeout, uaddr2, val3);
}


void mutex_init(mutex_t *m) {
    atomic_init(m, 0);
}


int mutex_lock(mutex_t *m) {
    while (1) {
        int zero = 0;
        if(atomic_compare_exchange_strong(m, &zero, 1)) {
            break;
        }
        int err = futex((int*)m, FUTEX_WAIT, 1, NULL, NULL, 0);
        if(err == -1 && errno != EAGAIN) {
            return err;
        }
    }
    return 0;
}


int mutex_unlock(mutex_t *m) {
    atomic_store(m, 0);
    int err = futex((int*)m, FUTEX_WAKE, INT_MAX, NULL, NULL, 0);
    if(err == -1) {
        return err;
    }
    return 0;
}

