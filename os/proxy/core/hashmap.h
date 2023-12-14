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
    void const *value;
    bool is_empty;
} HashElement;


/**
 * Hashmap with string keys. Not synchronized.
*/
typedef struct {
	HashElement *elements;
    Hashfunc hashfunc;
    bool resizable;
    double load_factor; // ignored if resizable is false
	size_t capacity;
    size_t nelements;
} Hashmap;


/**
 * Initiates hashmap with the specified capacity and hash function.
 * @returns ERRC_OK on success, ERRC_FAILED otherwise. 
*/
int hashmap_init(Hashmap *map, size_t capacity, bool resizable);


/**
 * Adds an element to the hashmap.
 * @returns ERRC_OK on success, or ERRC_FAILED if rehash failed.
*/
int hashmap_put(Hashmap *map, String key, void const *value);


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
