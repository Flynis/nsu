#ifndef _CACHE_H_INCLUDED_
#define _CACHE_H_INCLUDED_

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <pthread.h>
#include <stdlib.h>

#include "containers/str_hashmap.h"

/**
 * CLOCK replacement policy based cache. 
 * This implementation is synchronized but not resizable.
*/

typedef struct {
    char *key;
	void *value;
    size_t val_size;
} cdata_t;

typedef struct {
	cdata_t *data;
    int referenceBit;
	pthread_rwlock_t lock;
} cnode_t;

typedef struct {
    size_t capacity;
    size_t size;
    size_t head;
    cnode_t *nodes;
    pthread_mutex_t replace_lock;
    pthread_rwlock_t map_lock;
	str_hashmap_t hashmap;
} cache_t;

/**
 * Initiates cache with the specified capacity.
 * @returns zero on success, or an error number. 
*/
int cache_init(cache_t *cache, size_t capacity);

/**
 * Writes data with the specified key to the dest buffer.
 * @returns dest on success, or null if there was a cache miss.
*/
void* cache_get(cache_t *cache, char *key, void *dest);

/**
 * Puts the specified value with the specified size and key in cache.
 * @returns zero on success, or an error number.
*/
int cache_push(cache_t *cache, char *key, void *data, size_t data_size);

/**
 * Destroys cache
*/
void cache_destroy(cache_t *cache);

#endif
