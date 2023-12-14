#include "hashmap.h"


#include <assert.h>
#include <errno.h>


int hashmap_init(Hashmap *hashmap, size_t capacity, Hashfunc hashfunc) {
    assert(hashmap != NULL);
    assert(hashfunc != NULL);

    Bucket *buckets = calloc(capacity, sizeof(Bucket));
    if(buckets == NULL) {
        return errno;
    }

    hashmap->capacity = capacity;
    hashmap->size = 0;
    hashmap->buckets = buckets;
    hashmap->hashfunc = hashfunc;

    return 0;
}


static Bucket* find_bucket(Hashmap *hashmap, String key) {
    Bucket *buckets = hashmap->buckets;
    unsigned long hash = hashmap->hashfunc(key);
    size_t i = hash % hashmap->capacity;

    if(buckets[i].key != NULL) {
        // hash miss
        // looking for bucket with specified key
        size_t attempts = 0;
        while(hash != buckets[i].hash && strcmp(key, buckets[i].key) != 0) {
            attempts++;
            if(attempts == hashmap->capacity) {
                return NULL;
            }
            i = (i + 1) % hashmap->capacity;
        }
    }
    
    return &buckets[i];
}


int hashmap_put(Hashmap *hashmap, String key, void *value) {
    if(hashmap->size == hashmap->capacity) {
        return -1;
    }

    Bucket *buckets = hashmap->buckets;
    unsigned long hash = hashmap->hashfunc(key);
    size_t i = hash % hashmap->capacity;

    if(buckets[i].key != NULL) {
        // hash miss
        // looking for free bucket
        while(hash != buckets[i].hash && strcmp(key, buckets[i].key) != 0) {
            i = (i + 1) % hashmap->capacity;
        }
    }
    
    buckets[i].key = key;
    buckets[i].value = value;
    buckets[i].hash = hash;
    return 0;
}


void* hashmap_get(Hashmap *hashmap, String key) {
    Bucket *bucket = find_bucket(hashmap, key);
    return (bucket->key == NULL) ? NULL : bucket->value;    
}


void hashmap_remove(Hashmap *hashmap, String key) {
    Bucket *bucket = find_bucket(hashmap, key);
    bucket->key = NULL;
}


void hashmap_destroy(Hashmap *hashmap) {
    free(hashmap->buckets);
}
