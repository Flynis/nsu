#ifndef _BUFFER_H_INCLUDED_
#define _BUFFER_H_INCLUDED_


#include <stddef.h>


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


/**
 * @returns number of remaining unread bytes from the buffer.
*/
size_t buffer_remaining(Buffer *buffer);


/**
 * Fills buffer from socket.
 * @returns number of recv bytes from socket.
 * @returns FULL if buffer is full.
 * @returns IO if I/O error occurs.
*/
ssize_t buffer_recv(int sock, Buffer *buf);


/**
 * Destroys the buffer.
*/
void buffer_destroy(Buffer *buffer);


#endif // _BUFFER_H_INCLUDED_
