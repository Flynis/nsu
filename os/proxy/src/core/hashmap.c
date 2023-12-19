#include "hashmap.h"


#include <assert.h>
#include <stdlib.h>


#include "status.h"


#define DEFAULT_CHAIN_LENGTH 8
#define DEFAULT_LOAD_FACTOR 0.75


static void init_elements(HashElement *elements, size_t n) {
    for (size_t i = 0; i < n; i++) {
        elements[i].is_empty = true;
        elements[i].value = NULL;
    }
}


Hashmap* hashmap_create(size_t capacity, bool resizable) {
    assert(capacity > 0);

    Hashmap *map = malloc(sizeof(map));
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
    map->load_factor = DEFAULT_LOAD_FACTOR;
    map->max_chain_length = DEFAULT_CHAIN_LENGTH;
    map->resizable = resizable;
    init_elements(map->elements, map->capacity);

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


/**
 * Doubles the capacity of the map, and rehashes all the elements.
 * @returns OK on success, or ERROR otherwise.
*/
static int rehash(Hashmap *map) {
    assert(map->resizable);

    HashElement *new_elements = 
                        malloc(2 * map->capacity * sizeof(HashElement));
    if(new_elements == NULL) {
        return ERROR;
    }

    HashElement *old_elements = map->elements;
    size_t old_capacity = map->capacity;

    // update map array and capacity
    map->capacity *= 2;
    map->elements = new_elements;
    init_elements(map->elements, map->capacity);

    // rehash all elements
    for(size_t i = 0; i < old_capacity; i += 1) {
        if(old_elements[i].is_empty) {
            continue;
        }

        int ret = hashmap_put(map, old_elements[i].key, old_elements[i].value);
        // TODO log error
        assert(ret == OK);
    }

    free(old_elements);
    return OK;
}


int hashmap_put(Hashmap *map, String key, void *value) {
    assert(map != NULL);
    assert(value != NULL);

    if(map->resizable) {
        size_t max_load = (size_t) (map->capacity * map->load_factor);
        // check for overflow
        if(map->nelements >= max_load) {
            int ret = rehash(map);
            if(ret != OK) {
                return ret;
            }
        }
    }

    // find a place to put our value
    HashElement *el = find_element(map, key);
    if(el == NULL) {
        if(!map->resizable) {
            // map is full
            return FULL;
        } else {
            // increase capacity
            // TODO log this
            do {
                int ret = rehash(map);
                if(ret != OK) {
                    return ret;
                }
                el = find_element(map, key);
            } while(el == NULL);
        }
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
    assert(map != NULL);
    free(map->elements);
    free(map);
}
