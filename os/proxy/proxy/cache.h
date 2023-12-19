#ifndef _CACHE_H_INCLUDED_
#define _CACHE_H_INCLUDED_


#include <stddef.h>


#include "core/hashmap.h"
#include "core/queue.h"
#include "core/str.h"


typedef struct CacheElement {
    String key;
	void *value;
} CacheElement;


/**
 * LRU replacement policy based cache. Not synchronized. 
*/
typedef struct Cache {
    size_t capacity;
    size_t size;
    size_t val_size;
    
    Hashmap *map;
    Queue *lru;

    QueueNode *qnode_pool;
    size_t empty_node_index;
} Cache;


/**
 * Creates cache with the specified capacity.
 * @returns new cache on success, or NULL otherwise. 
*/
Cache* cache_create(size_t capacity, size_t val_size);


/**
 * Gets value with specified key.
 * @returns value, or NULL if there was a cache miss.
*/
void* cache_peek(Cache *cache, String key);


/**
 * Puts value with specified key.
 * @returns OK on success, ERROR otherwise.
*/
int cache_push(Cache *cache, String key, void *val);


/**
 * Destroys cache
*/
void cache_destroy(Cache *cache);


#endif // _CACHE_H_INCLUDED_
