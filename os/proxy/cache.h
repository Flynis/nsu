#ifndef _CACHE_H_INCLUDED_
#define _CACHE_H_INCLUDED_


#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif


#include <pthread.h>
#include <stddef.h>
#include <time.h>


#include "core/hashmap.h"
#include "core/queue.h"
#include "core/str.h"
#include "http/http.h"


typedef struct CacheElement {
    String key;
	void *value;
    size_t val_size;
    struct timespec insert_time; // for tracking ttl
} CacheElement;


/**
 * LRU replacement policy based cache. 
*/
typedef struct BlockingCache {
    size_t capacity;
    size_t size;

    pthread_mutex_t lock;
    pthread_cond_t cond;

	Hashmap *pending_requests;
    Hashmap *map;

    Queue *lru;
    QueueNode *qnode_pool;
    size_t empty_node_index;
} BlockingCache;


/**
 * Creates cache with the specified capacity.
 * @returns new cache on success, or NULL otherwise. 
*/
BlockingCache* cache_create(size_t capacity);


/**
 * Writes data with the specified key to the dest buffer.
 * @returns dest on success, or NULL if there was a cache miss.
*/
void* cache_peek(BlockingCache *cache, String key, void *dest);


/**
 * Destroys cache
*/
void cache_destroy(BlockingCache *cache);


#endif // _CACHE_H_INCLUDED_
