#include "storage.h"


#include <errno.h>
#include <stdio.h>
#include <string.h>


static void print_error(int code, char *msg) {
	fprintf(stderr, "%s: %s\n", msg, strerror(code));
}


static int node_init(node_t *node, char *value, node_t *next) {
	int err = pthread_mutex_init(&node->lock, NULL);
	if(err) {
		print_error(err, "Node lock initialize failed");
		return err;
	}

	node->value = value;
	node->next = next;
	
	return 0;
}


static void fill_value(char *value) {
	int len = rand() % VAL_SIZE;

	for(int j = 0; j < len; j++) {
		value[j] = 'a' + rand() % 26;
	}

	value[len] = '\0';
}


static void destroy_nodes(node_t *head) {
	node_t *cur = head;
	while (cur) {
		pthread_mutex_destroy(&cur->lock);
	}
}


static int fill_nodes(node_t **head_out, size_t size, node_t *node_storage, char *val_storage, node_t *tail) {
	srand(time(NULL));

	node_t* head = &node_storage[0]; 
	
	for(int i = 0; i < size; i++) {
		node_t *cur = &node_storage[i];

		char *value = NULL;
		if(val_storage != NULL) {
			value = val_storage + i * VAL_SIZE;
			fill_value(value);
		}

		node_t *next = node_storage + i + 1;

		int err = node_init(cur, value, next);
		if(err) {
			if(i > 0) {
				destroy_nodes(head);
			}
			return -1;
		}
	}

	node_storage[size - 1].next = tail;

	if(head_out) {
		*head_out = head;
	}
	return 0;
}


int storage_init(storage_t *s, size_t size) {
	int err = pthread_mutex_init(&s->head_lock, NULL);
	if(err) {
		print_error(err, "Storage head lock initialize failed");
		return err;
	}

	s->size = size;
	s->ghost_count = GHOST_COUNT;
	s->head = NULL;
	s->val_storage = NULL;

	s->node_storage = malloc(sizeof(node_t) * s->size);
	if (!s->node_storage) {
		perror("Memory allocate for node storage failed");
		storage_destroy(s);
		return errno;
	}

	s->val_storage = malloc(sizeof(char) * VAL_SIZE * s->size);
	if (!s->val_storage) {
		perror("Memory allocate for val storage failed\n");
		storage_destroy(s);
		return errno;
	}

	// fill ordinary nodes
	err = fill_nodes(&s->head, s->size, s->node_storage, s->val_storage, NULL);
	if(err) {
		storage_destroy(s);
		return -1;
	}
	// fill ghosts and join its with other nodes
    err = fill_nodes(NULL, s->ghost_count, s->ghost_storage, NULL, s->head);
	if(err) {
		storage_destroy(s);
		return -1;
	}

	return 0;
}


void storage_destroy(storage_t *s) {
	destroy_nodes(s->head);

	pthread_mutex_destroy(&s->head_lock);

	free(s->node_storage);
	free(s->val_storage);
}

/**
 * Gets ghost list with the specified size.
*/
static node_t* get_ghosts(storage_t *s, size_t count) {
	return &s->ghost_storage[s->ghost_count - count];
}


int iterator_init(iterator_t *iter, storage_t *s, iterator_mode_t mode) {
	iter->s = s;
	iter->mode = mode;

	switch(mode) {
		case READ2:
			iter->prev = NULL;
			iter->cur = get_ghosts(s, 2);
			iter->next = iter->cur->next;
			pthread_mutex_lock(&iter->cur->lock);
			pthread_mutex_lock(&iter->next->lock);
			break;

		case SWAP:
			iter->prev = get_ghosts(s, 3);
			iter->cur = iter->prev->next;
			iter->next = iter->cur->next;
			pthread_mutex_lock(&iter->prev->lock);
			pthread_mutex_lock(&iter->cur->lock);
			pthread_mutex_lock(&iter->next->lock);
			break;

		default: return -1;
	}

	return 0;
}


bool iterator_has_next(iterator_t *iter) {
	return iter->next->next != NULL;
}


char* iterator_next(iterator_t *iter) {
	node_t *cur = iter->cur;
	node_t *next = iter->next;

	// move
	switch(iter->mode) {
		case READ2:
			pthread_mutex_unlock(&iter->cur->lock);
			iter->cur = next;
			iter->next = next->next;
			pthread_mutex_lock(&iter->next->lock);
			break;
	
		case SWAP:
			pthread_mutex_unlock(&iter->prev->lock);
			iter->prev = cur;
			iter->cur = next;
			iter->next = next->next;
			pthread_mutex_lock(&iter->next->lock);
			break;

		default:
			return NULL;
	}

	return iter->next->value;
}


void iterator_swap(iterator_t *iter) {
	//printf("%p %p %p %p %p\n", iter->prev, iter->cur, iter->prev->next, iter->next, iter->prev->next->next);
	storage_t *s = iter->s;
	node_t *prev = iter->prev;
	node_t *cur = iter->cur;
	node_t *next = iter->next;

	node_t *tmp = next->next;

	bool swap_head = (cur == s->head);
	if(swap_head) {
		// need update storage head
		pthread_mutex_lock(&s->head_lock);
	}

	prev->next = next;
	cur->next = tmp;
	next->next = cur;

	iter->cur = next;
	iter->next = cur;

	if(swap_head) {
		s->head = next;
		pthread_mutex_unlock(&s->head_lock);
	}
	//printf("%p %p %p %p %p\n", iter->prev, iter->cur, iter->prev->next, iter->next, iter->prev->next->next);
}


void iterator_destroy(iterator_t *iter) {
	if(iter->prev) {
		pthread_mutex_unlock(&iter->prev->lock);
	}
	pthread_mutex_unlock(&iter->cur->lock);
	pthread_mutex_unlock(&iter->next->lock);
}
