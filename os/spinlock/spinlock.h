#ifndef _SPINLOCK_H_INCLUDED_
#define _SPINLOCK_H_INCLUDED_


#include <stdatomic.h>


/**
 * 0 - unlocked
 * 1 - locked
*/
typedef atomic_int spinlock_t;


void spin_init(spinlock_t *s);


void spin_lock(spinlock_t *s);


void spin_unlock(spinlock_t *s);


#endif
