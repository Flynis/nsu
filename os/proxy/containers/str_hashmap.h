#ifndef _STR_HASHMAP_H_
#define _STR_HASHMAP_H_

#include <stdlib.h>

/**
 * Hashmap with string keys. This implementation doesn't copy keys and values 
 * if its didn't store in somewhere else then any operation may produce EFAULT. 
 * This implementation is not synchronized and not resizable.
*/

typedef struct _str_hashmap_struct str_hashmap_t;

typedef unsigned long (*str_hashfunc_t)(char *s);

/**
 * Initiates hashmap with the specified capacity and hash function.
 * @returns zero if successful, or an error number. 
*/
int str_hashmap_init(str_hashmap_t *hashmap, size_t capacity, str_hashfunc_t hashfunc);

/**
 * Associates the specified value with the specified key in map.
 * @returns zero if successful, or -1 if map is filled to capacity.
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
