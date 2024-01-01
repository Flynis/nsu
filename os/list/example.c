#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>


#include "storage.h"


#define SWAP_THREAD_COUNT 3
#define MONITOR_ITERATION 300000


typedef struct shared_context_s {
	unsigned long long asc;
	unsigned long long desc;
	unsigned long long eq;
	atomic_ullong swap;
	storage_t *storage;
} shared_context_t;


void* thread_asc(void *arg) {
	shared_context_t *context = (shared_context_t *)arg;
	iterator_t iter;
	unsigned long long asc_count = 0;

	while (1) {
		iterator_init(&iter, context->storage, READ2);
		char* prev = iterator_next(&iter);

		while(iterator_has_next(&iter)) {
			char* cur = iterator_next(&iter);
			//printf("asc %s < %s\n", prev, cur);
			if(strlen(prev) < strlen(cur)) {
				asc_count++;
			}
			prev = cur;
		}
		
		iterator_destroy(&iter);
		context->asc++;
		if(context->asc % MONITOR_ITERATION == 0) {
			printf("asc pairs count %llu \n", asc_count);
			asc_count = 0;
		}
 		// printf("[%llu] asc count = %d\n", context->asc, asc_count);
		// sleep(5);
	}
    return NULL;
}


void* thread_desc(void *arg) {
	shared_context_t *context = (shared_context_t *)arg;
	iterator_t iter;
	unsigned long long desc_count = 0;

	while (1) {
		iterator_init(&iter, context->storage, READ2);
		char* prev = iterator_next(&iter);

		while(iterator_has_next(&iter)) {
			char* cur = iterator_next(&iter);
			//printf("desc %s > %s\n", prev, cur);
			if(strlen(prev) > strlen(cur)) {
				desc_count++;
			}
			prev = cur;
		}
		
		iterator_destroy(&iter);
		context->desc++;
		if(context->desc % MONITOR_ITERATION == 0) {
			printf("desc pairs count %llu \n", desc_count);
			desc_count = 0;
		}
		// printf("[%llu] desc count = %d\n", context->desc, desc_count);
		// sleep(5);
	}
    return NULL;
}


void* thread_eq(void *arg) {
	shared_context_t *context = (shared_context_t *)arg;
	iterator_t iter;
	unsigned long long eq_count = 0;

	while (1) {
		iterator_init(&iter, context->storage, READ2);
		char* prev = iterator_next(&iter);

		while(iterator_has_next(&iter)) {
			char* cur = iterator_next(&iter);
			//printf("eq %s == %s\n", prev, cur);
			if(strlen(prev) == strlen(cur)) {
				eq_count++;
			}
			prev = cur;
		}
		
		iterator_destroy(&iter);
		context->eq++;
		if(context->eq % MONITOR_ITERATION == 0) {
			printf("eq pairs count %llu \n", eq_count);
			eq_count = 0;
		}
		// printf("[%llu] eq count = %d\n", context->eq, eq_count);
		// sleep(5);
	}
    return NULL;
}


void* thread_swap(void *arg) {
	shared_context_t *context = (shared_context_t *)arg;
	iterator_t iter;
	struct drand48_data seed_buf;
	srand48_r(time(NULL), &seed_buf);
	long random;
	unsigned long long swap_count = 0;

	while(1) {
		iterator_init(&iter, context->storage, SWAP);
		iterator_next(&iter);

		while(iterator_has_next(&iter)) {
			iterator_next(&iter);
			lrand48_r(&seed_buf, &random);
			if(random % 2) {
				//printf("swap %s %s\n", prev, cur);
				iterator_swap(&iter);
				swap_count++;
			}
		}
		
		iterator_destroy(&iter);
		atomic_fetch_add_explicit(&context->swap, 1, memory_order_relaxed);
		if(context->swap % MONITOR_ITERATION == 0) {
			printf("swap pairs count %llu \n", swap_count);
			swap_count = 0;
		}
		// printf("[%llu] swap count = %d\n", context->swap, swap_count);
		// sleep(5);
	}

    return NULL;
}


int main() {
	shared_context_t context;
	context.asc = 0;
	context.desc = 0;
	context.eq = 0;
	context.swap = 0;
	context.storage = malloc(sizeof(storage_t));
	if(!context.storage) {
		printf("Memomy allocate for storage failed\n");
        return 1;
    }
	int err = storage_init(context.storage, 100);
	if(err) {
		printf("Storage init failed\n");
		return 1;
	}
	// node_t *node = context.storage->head;
	// while(node) {
	// 	printf("%s\n", node->value);
	// 	node = node->next;
	// }

	pthread_t tid;
	err = pthread_create(&tid, NULL, thread_asc, &context);
	if (err) {
		printf("main: pthread_create() failed: %s\n", strerror(err));
		return 2;
	}
	err = pthread_create(&tid, NULL, thread_desc, &context);
	if (err) {
		printf("main: pthread_create() failed: %s\n", strerror(err));
		return 2;
	}
	err = pthread_create(&tid, NULL, thread_eq, &context);
	if (err) {
		printf("main: pthread_create() failed: %s\n", strerror(err));
		return 2;
	}
	for(int i = 0; i < SWAP_THREAD_COUNT; i++) {
		err = pthread_create(&tid, NULL, thread_swap, &context);
		if (err) {
			printf("main: pthread_create() failed: %s\n", strerror(err));
			return 2;
		}
	}

	while (1) {
		printf("asc=%llu desc=%llu eq=%llu swap=%llu\n", context.asc, context.desc, context.eq, context.swap);
		sleep(1);
	}

	free(context.storage);
	pthread_exit(NULL);
}
