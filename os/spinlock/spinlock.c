#include "spinlock.h"


void spin_init(spinlock_t *s) {
    atomic_init(s, 0);
}


void spin_lock(spinlock_t *s) {
    while (1) {
        int zero = 0;
        if(atomic_compare_exchange_weak(s, &zero, 1)) {
            break;
        }
    }
}


void spin_unlock(spinlock_t *s) {
    atomic_store(s, 0);
}
