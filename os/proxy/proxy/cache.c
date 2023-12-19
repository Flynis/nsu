#include "cache.h"


#include <assert.h>
#include <stdlib.h>


#include "core/log.h"
#include "core/status.h"


Cache* cache_create(size_t capacity) {
    assert(capacity > 0);

    Cache *cache = malloc(sizeof(cache));
    if(cache == NULL) {
        return NULL;
    }

    size_t map_cap = (capacity * 4) / 3; // * 4/3 to avoid hash collisions
    cache->map = hashmap_create(map_cap, false);
    if(cache->map == NULL) {
        goto fail_nodes_create;
    } 
    cache->lru = queue_create();
    if(cache->lru == NULL) {
        goto fail_queue_create;
    }
    cache->qnode_pool = malloc(capacity * sizeof(QueueNode));
    if(cache->qnode_pool == NULL) {
        goto fail_qnode_pool_create;
    }

    // successfully allocate all resources
    cache->capacity = capacity;
    cache->size = 0;
    cache->empty_node_index = 0;
    return cache;

fail_qnode_pool_create:
    queue_destroy(cache->lru);
fail_queue_create:
    hashmap_destroy(cache->map);
fail_nodes_create:
    free(cache);
    return NULL;
}


void* cache_peek(Cache *cache, String key) {
    assert(cache != NULL);

    QueueNode *node = hashmap_get(&cache->map, key);
    if(node == NULL) {
        // cache miss
        return NULL;
    }

    // move node to head of queue
    queue_remove(cache->lru, node);
    queue_push(cache->lru, node);

    return node->value;
}


static void element_destroy(CacheElement *el) {
    free(el->key.data);
    free(el);
}


static CacheElement* element_create(String key, void *value) {
    CacheElement *el = malloc(sizeof(el));
    if(el == NULL) {
        return NULL;
    }

    el->key = string_dup(key);
    if(string_equals(el->key, EMPTY_STRING)) {
        free(el);
        return NULL;
    }

    // successfully allocate all the memory
    el->value = value;
    return el;
}


int cache_push(Cache *cache, String key, void *val) {
    assert(cache != NULL);
    assert(val != NULL);

    CacheElement *el = element_create(key, val);
    if(el == NULL) {
        LOG_ERR("Failed to create cache element");
        return ERROR;
    }

    QueueNode *pool = cache->qnode_pool;
    if(cache->size < cache->capacity) {
        // cache is not full
        QueueNode *empty_node = &pool[cache->empty_node_index];
        cache->empty_node_index += 1;

        empty_node->value = el;
        // insert new node in map
        int ret = hashmap_put(cache->map, el->key, empty_node);
        if(ret != OK) {
            LOG_ERR("hashmap_put() failed");
        }
        cache->size += 1;
        return OK;
    } 

    // cache is full need to replace some node
    // get least recently used node
    QueueNode *node = queue_pop(cache->lru);

    // replace data
    CacheElement *replaced = node->value;
    node->value = el;

    // replace in map and queue
    hashmap_remove(cache->map, replaced->key);
    int ret = hashmap_put(cache->map, el->key, node);
    if(ret != OK) {
        LOG_ERR("hashmap_put() failed");
    }
    queue_push(cache->lru, node);

    element_destroy(replaced);
    return OK;
}


void cache_destroy(Cache *cache) {
    assert(cache != NULL);

    // destroy allocated elements
    QueueNode *cur = cache->lru->head;
    while(cur != NULL) {
        element_destroy(cur->value);
        cur = cur->next;
    }

    free(cache->qnode_pool);
    queue_destroy(cache->lru);
    hashmap_destroy(cache->map);
    free(cache);
}

