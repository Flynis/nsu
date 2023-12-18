#include "buffer.h"


#include <assert.h>
#include <stdlib.h>
#include <sys/types.h>


#include "status.h"


Buffer* buffer_create(size_t capacity) {
    assert(capacity > 0);

    Buffer *buf = malloc(sizeof(Buffer));
    if(buf == NULL) {
        return NULL;
    }

    buf->start = malloc(capacity);
    if(buf->start == NULL) {
        free(buf);
        return NULL;
    }

    buf->end = buf->start + capacity;
    buf->pos = buf->start;
    buf->last = buf->start;

    return buf;
}


size_t buffer_remaining(Buffer *buffer) {
    assert(buffer != NULL);
    return buffer->last - buffer->pos;
}


ssize_t buffer_recv(int sock, Buffer *buf) {
    assert(sock >= 0);
    assert(buf != NULL);

    size_t remainig_size = buf->end - buf->last;
    if(remainig_size == 0) {
        return FULL;
    }

    ssize_t n = sock_recv(sock, buf->last, remainig_size);
    if(n > 0) {
        buf->last += n;
    }
    return n;
}


void buffer_destroy(Buffer *buffer) {
    assert(buffer != NULL);
    free(buffer->start);
    free(buffer);
}
