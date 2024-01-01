#include "hashmap.h"


#include <assert.h>
#include <stdlib.h>


#include "status.h"


#define DEFAULT_CHAIN_LENGTH 8


Hashmap* hashmap_create(size_t capacity) {
    assert(capacity > 0);

    Hashmap *map = malloc(sizeof(Hashmap));
    if(map == NULL) {
        return NULL;
    }

    HashElement *elements = malloc(capacity * sizeof(HashElement));
    if(elements == NULL) {
        free(map);
        return NULL;
    }

    map->capacity = capacity;
    map->nelements = 0;
    map->elements = elements;
    map->hashfunc = string_hash;
    map->max_chain_length = DEFAULT_CHAIN_LENGTH;
    for (size_t i = 0; i < capacity; i++) {
        elements[i].is_empty = true;
        elements[i].value = NULL;
    }

    return map;
}


/**
 * Finds an element by key or place to insert in the hashmap 
 * if the key is present.
 * @returns the element if key is present in the map, or NULL otherwise.
*/
static HashElement* find_element(Hashmap *map, String key) {
    HashElement *elements = map->elements;

    // find index
    size_t cur = map->hashfunc(key) % map->capacity;

    HashElement *not_empty_el = NULL;
    // we should check all elements in the chain 
    // because we have remove operation
    for(int i = 0; i < map->max_chain_length; i += 1) {
        HashElement *element = &elements[cur];

        // save element for insert
        if(not_empty_el == NULL && element->is_empty) {
            not_empty_el = element; 
        }

        if(!element->is_empty && string_equals(element->key, key)) {
            return element;
        }

        cur = (cur + 1) % map->capacity;
    }
    
    // returns element for insert or NULL if map is full
    return not_empty_el;
}


int hashmap_put(Hashmap *map, String key, void *value) {
    assert(map != NULL);
    assert(value != NULL);

    // find a place to put our value
    HashElement *el = find_element(map, key);
    if(el == NULL) {
        return FULL;
    }
    
    el->key = key;
    el->value = value;
    el->is_empty = false;

    map->nelements += 1;
    
    return OK;
}


void* hashmap_get(Hashmap *map, String key) {
    assert(map != NULL);

    // find value location
    size_t cur = map->hashfunc(key) % map->capacity;

    // linear probing if necessary
    for(int i = 0; i < map->max_chain_length; i += 1) {
        HashElement *element = &map->elements[cur];
        if(!element->is_empty && string_equals(element->key, key)) {
            return element->value;
        }

        cur = (cur + 1) % map->capacity;
    }

    // not found
    return NULL;
}


void hashmap_remove(Hashmap *map, String key) {
    assert(map != NULL);

    // find value location
    size_t cur = map->hashfunc(key) % map->capacity;

    // linear probing if necessary
    for(int i = 0; i < map->max_chain_length; i += 1) {
        HashElement *element = &map->elements[cur];
        if(!element->is_empty && string_equals(element->key, key)) {
            // blank out the fields
            element->is_empty = true;
            element->value = NULL;
            
            map->nelements -= 1;

            return;
        }

        cur = (cur + 1) % map->capacity;
    }
}


void hashmap_destroy(Hashmap *map) {
    if(map == NULL) return;
    free(map->elements);
    free(map);
}
