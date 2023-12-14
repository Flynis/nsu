#ifndef _STR_HASHMAP_H_INCLUDED_
#define _STR_HASHMAP_H_INCLUDED_


#include <stddef.h>


#include "str.h"


/**
 * Hash function for strings.
*/
typedef unsigned long (*Hashfunc)(char *string);


/**
 * Hashmap bucket.
*/
typedef struct Bucket {
    String                   key;
    void                    *value;
    unsigned long            hash;
} Bucket;


/**
 * Hashmap with string keys. This implementation doesn't copy keys and values 
 * if its didn't store in somewhere else any operation may produce segfault. 
 * This implementation is not synchronized and not resizable.
*/
typedef struct {
	Bucket                  *buckets;
	size_t                   capacity;
    size_t                   size;
    Hashfunc                 hashfunc;
} Hashmap;


/**
 * Initiates hashmap with the specified capacity and hash function.
 * @returns zero on success, or an error number. 
*/
int hashmap_init(Hashmap *hashmap, size_t capacity, Hashfunc hashfunc);


/**
 * Associates the specified value with the specified key in map.
 * @returns zero on success, or -1 if map is filled to capacity.
*/
int hashmap_put(Hashmap *hashmap, String key, void *value);


/**
 * Returns the value to which the specified key is mapped, or NULL if map contains no mapping for the key. 
 * @returns the value to which the specified key is mapped, or NULL if map contains no mapping for the key
*/
void* hashmap_get(Hashmap *hashmap, String key);


/**
 * Removes the mapping for the specified key from map if present.
*/
void hashmap_remove(Hashmap *hashmap, String key);


/**
 * Destroys hashmap
*/
void hashmap_destroy(Hashmap *hashmap);


#endif
