#ifndef _STR_HASHMAP_H_
#define _STR_HASHMAP_H_

#include <stdlib.h>

/**
 * Hashmap with string keys. This implementation doesn't copy keys and values 
 * if its didn't store in somewhere else any operation may produce segfault. 
 * This implementation is not synchronized and not resizable.
*/

typedef unsigned long (*str_hashfunc_t)(char *s);

typedef struct {
    char *key;
    void *value;
    unsigned long hashcode;
} bucket_t;

typedef struct {
	bucket_t *buckets;
	size_t capacity;
    size_t size;
    str_hashfunc_t hashfunc;
} str_hashmap_t;

/**
 * Initiates hashmap with the specified capacity and hash function.
 * @returns zero on success, or an error number. 
*/
int str_hashmap_init(str_hashmap_t *hashmap, size_t capacity, str_hashfunc_t hashfunc);

/**
 * Associates the specified value with the specified key in map.
 * @returns zero on success, or -1 if map is filled to capacity.
*/
int str_hashmap_put(str_hashmap_t *hashmap, char *key, void *value);

/**
 * Returns the value to which the specified key is mapped, or null if map contains no mapping for the key. 
 * @returns the value to which the specified key is mapped, or null if map contains no mapping for the key
*/
void* str_hashmap_get(str_hashmap_t *hashmap, char *key);

/**
 * Removes the mapping for the specified key from map if present.
*/
void str_hashmap_remove(str_hashmap_t *hashmap, char *key);

/**
 * Destroys hashmap
*/
void str_hashmap_destroy(str_hashmap_t *hashmap);

#endif
