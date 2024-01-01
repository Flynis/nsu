#ifndef _MUTEX_H_INCLUDED_
#define _MUTEX_H_INCLUDED_


#include <stdatomic.h>


typedef atomic_int mutex_t;


void mutex_init(mutex_t *m);


int mutex_lock(mutex_t *m);


int mutex_unlock(mutex_t *m);


#endif
