#include "str_hashmap.h"

#include <errno.h>
#include <string.h>

typedef struct _entry_struct {
    char *key;
    void *value;
} entry_t;

typedef struct _str_hashmap_struct {
	entry_t *entries;
	size_t capacity;
    str_hashfunc_t hashfunc;
} str_hashmap_t;


int str_hashmap_init(str_hashmap_t *hashmap, size_t capacity, str_hashfunc_t hashfunc) {
    entry_t *entries = calloc(capacity, sizeof(entry_t));
    if(!entries) {
        return errno;
    }

    hashmap->capacity = capacity;
    hashmap->entries = entries;
    hashmap->hashfunc = hashfunc;

    return 0;
}


static entry_t* find_entry(str_hashmap_t *hashmap, char *key) {
    entry_t *entries = hashmap->entries;

    size_t index = hashmap->hashfunc(key) % hashmap->capacity;
    if(entries[index].key == NULL) {
        return &entries[index];
    }

    size_t attempts = 0;
    while(strcmp(key, entries[index].key) != 0) {
        attempts++;
        if(attempts == hashmap->capacity) {
            return NULL;
        }
        index = (index + 1) % hashmap->capacity;
    }
    
    return &entries[index];
}


int str_hashmap_put(str_hashmap_t *hashmap, char *key, void *value) {
    entry_t *entries = hashmap->entries;

    size_t index = hashmap->hashfunc(key) % hashmap->capacity;
    if(entries[index].key == NULL) {
        entries[index].key = key;
        entries[index].value = value;
        return 0;
    }

    size_t attempts = 0;
    while(strcmp(key, entries[index].key) != 0) {
        attempts++;
        if(attempts == hashmap->capacity) {
            return -1;
        }
        index = (index + 1) % hashmap->capacity;
    }
    
    entries[index].key = key;
    entries[index].value = value;
    return 0;
}


void* str_hashmap_get(str_hashmap_t *hashmap, char *key) {
    entry_t *entry = find_entry(hashmap, key);
    return (entry->key == NULL) ? NULL : entry->value;    
}


void str_hashmap_remove(str_hashmap_t *hashmap, char *key) {
    entry_t *entry = find_entry(hashmap, key);
    entry->key = NULL;
}


void str_hashmap_destroy(str_hashmap_t *hashmap) {
    free(hashmap->entries);
}
