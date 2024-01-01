#ifndef _STORAGE_H_INCLUDED_
#define _STORAGE_H_INCLUDED_


#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif


#include <pthread.h>
#include <stdbool.h>
#include <stdlib.h>


#define VAL_SIZE 100
#define GHOST_COUNT 3


typedef struct node_s {
	char *value;
	struct node_s *next;
	pthread_mutex_t lock;
} node_t;


typedef struct storage_s {
	pthread_mutex_t head_lock;
	node_t *head;
	node_t ghost_storage[GHOST_COUNT];
	size_t size;
	size_t ghost_count;
	node_t *node_storage;
	char *val_storage;
} storage_t;


typedef enum iterator_mode_e {
	READ2,
	SWAP
} iterator_mode_t;


typedef struct iterator_s {
	iterator_mode_t mode;
	storage_t *s;
	node_t *prev;
	node_t *cur;
	node_t *next;
} iterator_t;


/**
 * Initiates and fills storage with the specified size.
*/
int storage_init(storage_t *s, size_t size);

/**
 * Destroys storage.
*/
void storage_destroy(storage_t *s);

/**
 * Initiates iterator over elements of the storage with the specified mode.
 * A destroyed iterator can be reinitialized.
*/
int iterator_init(iterator_t *iter, storage_t *s, iterator_mode_t mode);

/**
 * Returns true if the iteration has more elements.
*/
bool iterator_has_next(iterator_t *iter);

/**
 * Returns the next element of the iteration.
*/
char* iterator_next(iterator_t *iter);

/**
 * Swaps the current element with the previous element of the iteration.
*/
void iterator_swap(iterator_t *iter);

/**
 * Destroys iterator.
*/
void iterator_destroy(iterator_t *iter);


#endif
