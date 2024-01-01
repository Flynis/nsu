#include "cache.h"

#include <errno.h>
#include <stdio.h>
#include <string.h>

#include "util.h"


static void destroy(cache_t *cache, size_t size) {
    cnode_t *nodes = cache->nodes;
    for(size_t i = 0; i < size; i++) {
        pthread_rwlock_destroy(&nodes[i].lock);
    }
    free(nodes);

    pthread_mutex_destroy(&cache->replace_lock);
    pthread_rwlock_destroy(&cache->map_lock);
    str_hashmap_destroy(&cache->hashmap);
}


int cache_init(cache_t *cache, size_t capacity) {
    int err = pthread_mutex_init(&cache->replace_lock, NULL);
    if(err) {
        return err;
    }

    err = pthread_rwlock_init(&cache->map_lock, NULL);
    if(err) {
        pthread_mutex_destroy(&cache->replace_lock);
        return err;
    }

    // increase map capacity to maintain load factor at 0.75
    err = str_hashmap_init(&cache->hashmap, (capacity * 4) / 3, strhash);
    if(err) {
        pthread_mutex_destroy(&cache->replace_lock);
        pthread_rwlock_destroy(&cache->map_lock);
        return err;
    }

    cnode_t *nodes = malloc(sizeof(cnode_t) * capacity);
    if(!nodes) {
        destroy(cache, 0);
        return ENOMEM;
    }

    cache->capacity = capacity;
    cache->size = 0;
    cache->head = 0;
    for(size_t i = 0; i < capacity; i++) {
        err = pthread_rwlock_init(&nodes[i].lock, NULL);
        if(err) {
            if(i > 0) {
                destroy(cache, i - 1);
            }
            return err;
        }
    }

    return 0;
}


void* cache_get(cache_t *cache, char *key, void *dest) {
    pthread_rwlock_rdlock(&cache->map_lock);
    cnode_t *node = str_hashmap_get(&cache->hashmap, key);
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


int cache_push(cache_t *cache, char *key, void *data, size_t data_size) {
    cdata_t *new_data = data_create(key, data, data_size);
    if(!new_data) {
        perror("Cache data object create failed");
        return ENOMEM;
    }

    pthread_mutex_lock(&cache->replace_lock);
    cnode_t *nodes = cache->nodes;
    if(cache->size < cache->capacity) {
        // cache is not full
        cnode_t *free_node = &nodes[cache->size];
        cache->size++;
        pthread_rwlock_wrlock(&free_node->lock);

        // we acquire needed node so another thread may continue replace
        pthread_mutex_unlock(&cache->replace_lock);
        
        free_node->data = new_data;
        free_node->referenceBit = 0;

        // insert new node in map
        pthread_rwlock_wrlock(&cache->map_lock);
        str_hashmap_put(&cache->hashmap, new_data->key, free_node);
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
        str_hashmap_remove(&cache->hashmap, replaced->key);
        str_hashmap_put(&cache->hashmap, new_data->key, node);
        pthread_rwlock_unlock(&cache->map_lock);

        node->data = new_data;
        pthread_rwlock_unlock(&node->lock);
    }

    return 0;
}


void cache_destroy(cache_t *cache) {
    destroy(cache, cache->capacity);
}

