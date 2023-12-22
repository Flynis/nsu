#ifndef _QUEUE_H_INCLUDED_
#define _QUEUE_H_INCLUDED_


#include <stddef.h>


typedef struct QueueNode {
    void *value;
    struct QueueNode *prev;
    struct QueueNode *next;
} QueueNode;


/**
 * Doubly-linked list based queue. Not synchronized.
*/
typedef struct Queue {
    size_t size;
    QueueNode *head;
    QueueNode *tail;
} Queue;


/**
 * Creates empty queue.
 * @returns new queue on success, NULL otherwise.
*/
Queue* queue_create(void);


/**
 * Inserts node into tail of the queue.
*/
void queue_push(Queue *queue, QueueNode *node);


/**
 * Removes node from the queue.
 * The node must be in the queue.
*/
void queue_remove(Queue *queue, QueueNode *node);


/**
 * Destroy the queue.
*/
void queue_destroy(Queue *queue);


#endif // _QUEUE_H_INCLUDED_
