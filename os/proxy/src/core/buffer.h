#ifndef _BUFFER_H_INCLUDED_
#define _BUFFER_H_INCLUDED_


#include <stddef.h>


/**
 * Byte buffer. start <= pos <= last <= end.
*/
typedef struct Buffer{
    unsigned char *start;
    unsigned char *end;
    unsigned char *pos;
    unsigned char *last;
} Buffer;


typedef struct Chain {
    Buffer buf;
    struct Chain *next;
} Chain;


/**
 * Creates buffers chain with specified first buffer size.
 * @returns new chain on success, NULL otherwise.
*/
Chain* chain_create(size_t first_buf_size);


/**
 * Allocates new buffer and adds it to the chain.
 * @returns buffer on success, NULL otherwise.
*/
Buffer* chain_alloc_next_buf(Chain *chain, size_t buf_size);


/**
 * Allocates new chain and copy specified chain into it.
 * @returns copy of chain or NULL on failure.
*/
Chain* chain_clone(Chain *chain);


/**
 * Destroys the chain.
*/
void chain_destroy(Chain *chain);


#endif // _BUFFER_H_INCLUDED_
