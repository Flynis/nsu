#ifndef _BUFFER_H_INCLUDED_
#define _BUFFER_H_INCLUDED_


#include <stddef.h>
#include <sys/types.h>


/**
 * Byte buffer. start <= pos <= last <= end.
*/
typedef struct Buffer{
    unsigned char *start;
    unsigned char *end;
    unsigned char *pos; // for reading
    unsigned char *last;
} Buffer;


typedef struct Chain {
    Buffer *buf;
    struct Chain *next;
} Chain;


/**
 * Creates new buffer.
 * @returns new buffer on success, NULL otherwise.
*/
Buffer* buffer_create(size_t capacity);


/**
 * Receives data from sock and store it in buf.
 * Stores data from last to end of buf
 * @returns number of bytes read.
 * @returns FULL if buffer is full, i.e. end - last == 0.
 * @returns IO on I/O error.
 * @returns END_OF_STREAM on sock stream shutdown.
*/
ssize_t buffer_recv(int sock, Buffer *buf);


/**
 * Sends data to sock from buf.
 * Sends data between start and last of buf, and resets last and pos.
 * @returns number of bytes sent.
 * @returns IO on I/O error.
 * @returns CONN_RESET if connection reset by peer.
*/
ssize_t buffer_send(int sock, Buffer *buf);


/**
 * Sends data to sock from buf.
 * Sends data between start and last of buf, and resets last and pos.
 * @returns number of bytes sent.
 * @returns IO on I/O error.
 * @returns CONN_RESET if connection reset by peer.
*/
ssize_t buffer_send_range(int sock, Buffer *buf, size_t pos, size_t len);


/**
 * Clears the buffer.
*/
void buffer_clear(Buffer *buf);


/**
 * Destroys buffer.
*/
void buffer_destroy(Buffer *buffer);


#endif // _BUFFER_H_INCLUDED_
