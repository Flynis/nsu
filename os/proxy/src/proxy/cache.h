#ifndef _CACHE_H_INCLUDED_
#define _CACHE_H_INCLUDED_


#include <pthread.h>
#include <stddef.h>
#include <stdint.h>


#include "core/buffer.h"
#include "core/hashmap.h"
#include "core/queue.h"
#include "core/str.h"
#include "http/http.h"


typedef enum LoadStatus {
    LOADING_RESPONSE_HEAD = 15,
    LOADING_RESPONSE_BODY,
    UNCACHEABLE_RESPONSE,
    SUCC_LOADED_RESPONSE,
    BAD_UPSTREAM,
    INTERNAL_ERROR,
    HOST_UNREACHEABLE
} LoadStatus;


typedef struct CacheElem {
    String key;
    Buffer *res;
    time_t recv_time;
    LoadStatus status;
    uint32_t last; // futex word
    unsigned int nreaders;
} CacheElem;


/**
 * LRU replacement policy based cache. 
*/
typedef struct Cache {
    size_t capacity;
    size_t size;
    size_t busy_elems;
    
    Hashmap *map;
    Queue *lru;
    QueueNode *qnode_pool;
    CacheElem *elem_pool;

    pthread_mutex_t lock;
    pthread_cond_t evict_cond;
} Cache;


/**
 * Creates cache with the specified capacity.
 * @returns new cache on success, or NULL otherwise. 
*/
Cache* cache_create(size_t capacity);


HttpState cache_process_request(Cache *cache, HttpRequest* req);


/**
 * Destroys cache
*/
void cache_destroy(Cache *cache);


#endif // _CACHE_H_INCLUDED_
