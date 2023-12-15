#include "buffer.h"


Buffer* buffer_create(size_t capacity) {
    Buffer *buf = malloc(sizeof(Buffer));
    if(buf == NULL) {
        return NULL;
    }

    buf->start = malloc(capacity);
    if(buf->start == NULL) {
        free(buf);
        return NULL;
    }

    buf->capacity = capacity;
    buf->pos = buf->start;
    buf->limit = buf->start;

    return buf;
}


void buffer_destroy(Buffer *buffer) {
    free(buffer->start);
    free(buffer);
}
