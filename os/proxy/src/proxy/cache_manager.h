#ifndef _CACHE_MANAGER_H_INCLUDED_
#define _CACHE_MANAGER_H_INCLUDED_


#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif


#include <pthread.h>
#include <stddef.h>


#include "cache.h"
#include "http/http.h"


typedef struct CacheManager {
    Cache *cache;
    pthread_mutex_t lock;
    pthread_cond_t cond;
} CacheManager;


CacheManager* cache_manager_create(size_t cache_capacity);


HttpState process_cacheable_request(CacheManager *m, HttpRequest* req);


void cache_manager_destroy(CacheManager *m);


#endif // _CACHE_MANAGER_H_INCLUDED_
