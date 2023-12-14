#include "hashmap.h"


#include <assert.h>


#include "errcode.h"


#define MAX_CHAIN_LENGTH 8
#define DEFAULT_LOAD_FACTOR 0.75


static void init_elements(HashElement *elements, size_t n) {
    for (size_t i = 0; i < n; i++) {
        elements[i].is_empty = true;
        elements[i].value = NULL;
    }
}


int hashmap_init(Hashmap *map, size_t capacity, bool resizable) {
    assert(map != NULL);
    assert(capacity > 0);

    HashElement *elements = malloc(capacity * sizeof(HashElement));
    if(elements == NULL) {
        return ERRC_FAILED;
    }

    map->capacity = capacity;
    map->nelements = 0;
    map->elements = elements;
    map->hashfunc = string_hash;
    map->load_factor = DEFAULT_LOAD_FACTOR;
    map->resizable = resizable;
    init_elements(map->elements, map->capacity);

    return ERRC_OK;
}


/**
 * Finds an element by key in the map if the key is present.
 * @returns the element if key is present in the map, or NULL otherwise.
*/
static HashElement* find_element(Hashmap *map, String key) {
    HashElement *elements = map->elements;

    // find index
    size_t cur = map->hashfunc(key) % map->capacity;

    for(int i = 0; i < MAX_CHAIN_LENGTH; i += 1) {
        HashElement *element = &elements[cur];
        if(element->is_empty) {
            return element;
        }

        if(!element->is_empty && string_equals(element->key, key)) {
            return element;
        }

        cur = (cur + 1) % map->capacity;
    }
    
    return NULL;
}


/**
 * Doubles the capacity of the map, and rehashes all the elements.
 * @returns ERRC_OK on success, or ERRC_FAILED otherwise.
*/
static int rehash(Hashmap *map) {
    assert(map->resizable);

    HashElement *new_elements = malloc(2 * map->capacity * sizeof(HashElement));
    if(new_elements == NULL) {
        return ERRC_FAILED;
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
        assert(ret == ERRC_OK);
    }

    free(old_elements);
    return ERRC_OK;
}


int hashmap_put(Hashmap *map, String key, void const *value) {
    assert(map != NULL);
    assert(value != NULL);

    if(map->resizable) {
        size_t max_load = (size_t) (map->capacity * map->load_factor);
        // check for overflow
        if(map->nelements >= max_load) {
            int ret = rehash(map);
            if(ret != ERRC_OK) {
                return ret;
            }
        }
    }

    // find a place to put our value
    HashElement *el = find_element(map, key);
    if(el == NULL) {
        // map is full
        return ERRC_FAILED;
    }
    
    el->key = key;
    el->value = value;
    el->is_empty = false;

    map->nelements += 1;
    
    return ERRC_OK;
}


void* hashmap_get(Hashmap *map, String key) {
    assert(map != NULL);

    // find value location
    size_t cur = map->hashfunc(key) % map->capacity;

    // linear probing if necessary
    for(int i = 0; i < MAX_CHAIN_LENGTH; i += 1) {
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
    for(int i = 0; i < MAX_CHAIN_LENGTH; i += 1) {
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
    free(map->elements);
}
