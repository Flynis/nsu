#include "buffer.h"


#include <assert.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>


#include "log.h"
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


ssize_t buffer_recv(int sock, Buffer *buf) {
    assert(sock >= 0);
    assert(buf != NULL);

    size_t free_space = buf->end - buf->last;
    if(free_space == 0) {
        return FULL;
    }

    ssize_t n = recv(sock, buf->last, free_space, 0);
    if(n < 0) {
        return IO;
        LOG_ERRNO(errno, "socket recv() failed");
    }

    // update buf length
    buf->last += n;
    
    return (n == 0) ? END_OF_STREAM : n;
}


ssize_t buffer_send(int sock, Buffer *buf) {
    assert(sock >= 0);
    assert(buf != NULL);

    size_t payload_size = buf->last - buf->start;
    size_t remaining = payload_size;
    while(remaining > 0) {
        ssize_t n = send(sock, buf->start, remaining, 0);
        if(n < 0) {
            LOG_ERRNO(errno, "socket send() failed");
            return (errno == ECONNRESET) ? CONN_RESET : IO;
        }
        remaining -= n;
    }

    // clear buffer
    buf->pos = buf->start;
    buf->last = buf->start;

    return payload_size;
}


void buffer_destroy(Buffer *buffer) {
    assert(buffer != NULL);
    free(buffer->start);
    free(buffer);
}
