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


int buffer_resize(Buffer *buf, size_t capacity) {
    assert(buf != NULL);
    assert(capacity > 0);

    unsigned char *new_start = realloc(buf->start, capacity);
    if(new_start == NULL) {
        return ERROR;
    }

    unsigned char *old_start = buf->start;
    buf->start = new_start;
    buf->end = new_start + capacity;
    if(buf->last - old_start > capacity) {
        buf->last = buf->end;
    }
    if(buf->pos - old_start > capacity) {
        buf->pos = buf->end;
    }
    return OK;
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


ssize_t buffer_send_all(int sock, Buffer *buf) {
    assert(sock >= 0);
    assert(buf != NULL);

    size_t payload_size = buf->last - buf->start;
    size_t remaining = payload_size;
    unsigned char *pos = buf->start;
    while(remaining > 0) {
        ssize_t n = send(sock, pos, remaining, 0);
        if(n < 0) {
            LOG_ERRNO(errno, "socket send() failed");
            return (errno == ECONNRESET) ? CONN_RESET : IO;
        }
        remaining -= n;
        pos += n;
    }
    return payload_size;
}


ssize_t buffer_send_range(int sock, Buffer *buf, size_t pos, size_t len) {
    assert(sock >= 0);
    assert(buf != NULL);
    assert(pos >= 0);
    assert(len > 0);

    size_t remaining = len;
    unsigned char *start = buf->start + pos;
    while(remaining > 0) {
        ssize_t n = send(sock, start, remaining, 0);
        if(n < 0) {
            LOG_ERRNO(errno, "socket send() failed");
            return (errno == ECONNRESET) ? CONN_RESET : IO;
        }
        remaining -= n;
        start += n;
    }

    return len;
}


void buffer_clear(Buffer *buf) {
    assert(buf != NULL);
    buf->pos = buf->start;
    buf->last = buf->start;
}


void buffer_destroy(Buffer *buffer) {
    if(buffer == NULL) return;
    free(buffer->start);
    free(buffer);
}
