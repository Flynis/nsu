#ifndef _STR_HASHMAP_H_INCLUDED_
#define _STR_HASHMAP_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


#include "str.h"


/**
 * Hash function for strings.
*/
typedef unsigned int (*Hashfunc)(String string);


/**
 * Key-value mapping.
*/
typedef struct HashElement {
    String key;
    void *value;
    bool is_empty;
} HashElement;


/**
 * Hashmap with string keys. Not resizable, not synchronized.
*/
typedef struct {
	HashElement *elements;
    Hashfunc hashfunc;
    int max_chain_length; // max linear probing chain length
	size_t capacity;
    size_t nelements;
} Hashmap;


/**
 * Creates hashmap with the specified capacity.
 * @returns new hashmap on success, NULL otherwise. 
*/
Hashmap* hashmap_create(size_t capacity);


/**
 * Adds an element to the hashmap.
 * @returns OK on success, 
 * @returns FULL if hashmap is full.
*/
int hashmap_put(Hashmap *map, String key, void *value);


/**
 * Gets an element from the hashmap. 
 * @returns the element if key is present in the hashmap, or NULL otherwise.
*/
void* hashmap_get(Hashmap *map, String key);


/**
 * Removes an element by key from the hashmap if the key is present
*/
void hashmap_remove(Hashmap *map, String key);


/**
 * Destroys hashmap
*/
void hashmap_destroy(Hashmap *map);


#endif // _STR_HASHMAP_H_INCLUDED_
