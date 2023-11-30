#ifndef _STR_HASHMAP_H_
#define _STR_HASHMAP_H_

#include <stdlib.h>

typedef struct _str_hashmap_struct str_hashmap_t;

typedef unsigned long (*str_hashfunc_t)(char *s);

int str_hashmap_init(str_hashmap_t *hashmap, size_t capacity, str_hashfunc_t hashfunc);

int str_hashmap_put(str_hashmap_t *hashmap, char *key, void *value);

void* str_hashmap_get(str_hashmap_t *hashmap, char *key);

void str_hashmap_remove(str_hashmap_t *hashmap, char *key);

void str_hashmap_destroy(str_hashmap_t *hashmap);

#endif
