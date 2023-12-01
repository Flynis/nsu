#include "str_hashmap.h"

#include <errno.h>
#include <string.h>

typedef struct _bucket_struct {
    char *key;
    void *value;
} bucket_t;

typedef struct _str_hashmap_struct {
	bucket_t *buckets;
	size_t capacity;
    size_t size;
    str_hashfunc_t hashfunc;
} str_hashmap_t;


int str_hashmap_init(str_hashmap_t *hashmap, size_t capacity, str_hashfunc_t hashfunc) {
    bucket_t *buckets = calloc(capacity, sizeof(bucket_t));
    if(!buckets) {
        return errno;
    }

    hashmap->capacity = capacity;
    hashmap->size = 0;
    hashmap->buckets = buckets;
    hashmap->hashfunc = hashfunc;

    return 0;
}


static bucket_t* find_bucket(str_hashmap_t *hashmap, char *key) {
    bucket_t *buckets = hashmap->buckets;
    size_t index = hashmap->hashfunc(key) % hashmap->capacity;

    if(buckets[index].key != NULL) {
        // hash miss
        // looking for bucket with specified key
        size_t attempts = 0;
        while(strcmp(key, buckets[index].key) != 0) {
            attempts++;
            if(attempts == hashmap->capacity) {
                return NULL;
            }
            index = (index + 1) % hashmap->capacity;
        }
    }
    
    return &buckets[index];
}


int str_hashmap_put(str_hashmap_t *hashmap, char *key, void *value) {
    if(hashmap->size == hashmap->capacity) {
        return -1;
    }

    bucket_t *buckets = hashmap->buckets;
    size_t index = hashmap->hashfunc(key) % hashmap->capacity;

    if(buckets[index].key != NULL) {
        // hash miss
        // looking for free bucket
        while(strcmp(key, buckets[index].key) != 0) {
            index = (index + 1) % hashmap->capacity;
        }
    }
    
    buckets[index].key = key;
    buckets[index].value = value;
    return 0;
}


void* str_hashmap_get(str_hashmap_t *hashmap, char *key) {
    bucket_t *bucket = find_bucket(hashmap, key);
    return (bucket->key == NULL) ? NULL : bucket->value;    
}


void str_hashmap_remove(str_hashmap_t *hashmap, char *key) {
    bucket_t *bucket = find_bucket(hashmap, key);
    bucket->key = NULL;
}


void str_hashmap_destroy(str_hashmap_t *hashmap) {
    free(hashmap->buckets);
}
