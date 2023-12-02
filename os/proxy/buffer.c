#include "buffer.h"


buffer_t* buffer_create(size_t size) {
    buffer_t *buf = malloc(sizeof(buffer_t));
    if(!buf) {
        return NULL;
    }

    buf->start = malloc(sizeof(char) * size);
    if(!buf->start) {
        free(buf);
        return NULL;
    }

    buf->size = size;
    buf->pos = buf->start;
    buf->last = buf->start;

    return buf;
}


ssize_t buffer_read(buffer_t *buffer, int fd) {
    ssize_t n = buffer->pos - buffer->last;
    if(n > 0) {
        return n;
    }

    ssize_t nread = read(fd, buffer->start, buffer->size);
    if(nread > 0) {
        buffer->pos = buffer->start;
        buffer->last = buffer->pos + nread;
    }

    return nread;
}


void buffer_destroy(buffer_t *buffer) {
    free(buffer->start);
    free(buffer);
}
