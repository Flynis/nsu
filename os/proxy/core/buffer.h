#ifndef _BUFFER_H_INCLUDED_
#define _BUFFER_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


#include "connection.h"


/**
 * Byte buffer.
*/
typedef struct Buffer{
    unsigned char *start;
    unsigned char *end;
    unsigned char *pos;
    unsigned char *last;
} Buffer;


/**
 * Creates a new buffer with the given capacity.
 * @returns new buffer on success, NULL otherwise.
*/
Buffer* buffer_create(size_t capacity);


size_t buffer_remaining(Buffer *buffer);


ssize_t buffer_recv(Connection *c, Buffer *buf);


/**
 * Destroys the buffer.
*/
void buffer_destroy(Buffer *buffer);


#endif // _BUFFER_H_INCLUDED_
