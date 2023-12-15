#ifndef _BUFFER_H_INCLUDED_
#define _BUFFER_H_INCLUDED_


#include <stddef.h>


/**
 * Byte buffer.
 * Pos should be between start and limit, i.e start <= pos <= limit.
*/
typedef struct Buffer{
    unsigned char *pos;
    unsigned char *limit;
    unsigned char *start;
    size_t capacity;
} Buffer;


/**
 * Creates a new buffer with the given capacity.
 * @returns new buffer on success, NULL otherwise.
*/
Buffer* buffer_create(size_t capacity);


/**
 * Destroys the buffer.
*/
void buffer_destroy(Buffer *buffer);


#endif // _BUFFER_H_INCLUDED_
