#include "queue.h"


#include <assert.h>
#include <stdlib.h>


Queue* queue_create(void) {
    Queue *queue = malloc(sizeof(Queue));
    if(queue == NULL) {
        return NULL;
    }

    queue->size = 0;
    queue->head = NULL;
    queue->tail = NULL;

    return queue;
}


void queue_push(Queue *queue, QueueNode *node) {
    assert(queue != NULL);
    assert(node != NULL);

    node->next = NULL;
    if(queue->size == 0) {
        queue->head = node;
        node->prev = NULL;
    } else {
        queue->tail->next = node;
        node->prev = queue->tail;
    }
    queue->tail = node;
    queue->size++;
}


void queue_remove(Queue *queue, QueueNode *node) {
    assert(queue != NULL);
    assert(node != NULL);

    if(queue->size == 0) {
        return;
    }
    if(node == queue->head) {
        queue->head = node->next;
    }
    if(node == queue->tail) {
        queue->tail = node->prev;
    }
    if(node->prev != NULL) {
        node->prev->next = node->next;
    }
    if(node->next != NULL) {
        node->next->prev = node->prev;
    }

    queue->size--;
    node->next = NULL;
    node->prev = NULL;
}


void queue_destroy(Queue *queue) {
    free(queue);
}
