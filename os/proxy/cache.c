#include "cache.h"


#include <assert.h>
#include <stdbool.h>


#define PENDING_REQUESTS_INITIAL_SIZE 512


BlockingCache* cache_create(size_t capacity) {
    assert(capacity > 0);

    BlockingCache *cache = malloc(sizeof(cache));
    if(cache == NULL) {
        return NULL;
    }

    int err;
    err = pthread_mutex_init(&cache->lock, NULL);
    if(err) {
        goto fail_lock_init;
    }
    err = pthread_cond_init(&cache->cond, NULL);
    if(err) {
        goto fail_cond_init;        
    }

    size_t pending_req_size = PENDING_REQUESTS_INITIAL_SIZE;
    cache->pending_requests = hashmap_create(pending_req_size, true);
    if(cache->pending_requests == NULL) {
        goto fail_pending_req_create;
    }
    size_t nodes_size = (capacity * 4) / 3; // * 4/3 to avoid hash collisions
    cache->map = hashmap_create(nodes_size, false);
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
    hashmap_destroy(cache->pending_requests);
fail_pending_req_create:
    pthread_cond_destroy(&cache->cond);
fail_cond_init:
    pthread_mutex_destroy(&cache->lock);
fail_lock_init:
    free(cache);
    return NULL;
}


void* cache_peek(BlockingCache *cache, String key, void *dest) {
    assert(cache != NULL);
    assert(dest != NULL);
    pthread_rwlock_rdlock(&cache->map_lock);
    cnode_t *node = hashmap_get(&cache->hashmap, key);
    pthread_rwlock_unlock(&cache->map_lock);

    if(node == NULL) {
        // cache miss
        return NULL;
    }
    
    // cache hit
    void *ret = NULL;
    cdata_t *data = node->data;
    pthread_rwlock_rdlock(&node->lock);
    // make sure that another thread has not replaced the data
    if(strcmp(key, data->key) == 0) {
        memcpy(dest, data->value, data->val_size);
        node->referenceBit = 1;
        ret = dest;
    }
    pthread_rwlock_unlock(&node->lock);

    return ret;
}


static void data_destroy(cdata_t *data) {
    free(data->key);
    free(data->value);
    free(data);
}


static cdata_t* data_create(char *key, void *value, size_t val_size) {
    cdata_t *data = malloc(sizeof(data));
    if(!data) {
        return NULL;
    }

    data->value = NULL;
    data->key = malloc(sizeof(char) * strlen(key));
    if(!data->key) {
        data_destroy(data);
        return NULL;
    }

    data->value = malloc(sizeof(char) * val_size);
    if(!data->value) {
        data_destroy(data);
        return NULL;
    }

    strcpy(data->key, key);
    memcpy(data->value, value, val_size);
    data->val_size = val_size;

    return data;
}


int cache_push(BlockingCache *cache, char *key, void *data, size_t data_size) {
    cdata_t *new_data = data_create(key, data, data_size);
    if(!new_data) {
        perror("Cache data object create failed");
        return ENOMEM;
    }

    pthread_mutex_lock(&cache->replace_lock);
    cnode_t *nodes = cache->map;
    if(cache->nnodes < cache->capacity) {
        // cache is not full
        cnode_t *free_node = &nodes[cache->nnodes];
        cache->nnodes++;
        pthread_rwlock_wrlock(&free_node->lock);

        // we acquire needed node so another thread may continue replace
        pthread_mutex_unlock(&cache->replace_lock);
        
        free_node->data = new_data;
        free_node->referenceBit = 0;

        // insert new node in map
        pthread_rwlock_wrlock(&cache->map_lock);
        hashmap_put(&cache->hashmap, new_data->key, free_node);
        pthread_rwlock_unlock(&cache->map_lock);

        pthread_rwlock_unlock(&free_node->lock);
    } else {
        // looking for node to replace
        int found = 0;
        cnode_t *node = NULL;
        do {
            node = &nodes[cache->head];
            // moving along ring buffer
            cache->head = (cache->head + 1) % cache->capacity;
            int busy = pthread_rwlock_trywrlock(&node->lock);
            if(!busy) {
                if(node->referenceBit == 0) {
                    found = 1;
                } else {
                    node->referenceBit = 0;
                    pthread_rwlock_unlock(&node->lock);
                }
            }
        } while (found);

        // we acquire needed node so another thread may continue replace
        pthread_mutex_unlock(&cache->replace_lock);

        // replace data in map
        cdata_t *replaced = node->data;
        pthread_rwlock_wrlock(&cache->map_lock);
        hashmap_remove(&cache->hashmap, replaced->key);
        hashmap_put(&cache->hashmap, new_data->key, node);
        pthread_rwlock_unlock(&cache->map_lock);

        node->data = new_data;
        pthread_rwlock_unlock(&node->lock);
    }

    return 0;
}


void cache_destroy(BlockingCache *cache) {
    assert(cache != NULL);

    free(cache->qnode_pool);
    queue_destroy(cache->lru);
    hashmap_destroy(cache->map);
    hashmap_destroy(cache->pending_requests);
    pthread_cond_destroy(&cache->cond);
    pthread_mutex_destroy(&cache->lock);
    free(cache);
}

