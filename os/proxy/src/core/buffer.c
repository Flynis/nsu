#include "buffer.h"


#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>


#include "status.h"


Chain* chain_create(size_t first_buf_size) {
    assert(first_buf_size > 0);

    Chain *chain = malloc(sizeof(chain));
    if(chain == NULL) {
        return NULL;
    }

    Buffer *buf = &chain->buf;
    buf->start = malloc(first_buf_size);
    if(buf->start == NULL) {
        free(chain);
        return NULL;
    }

    buf->end = buf->start + first_buf_size;
    buf->pos = buf->start;
    buf->last = buf->start;
    chain->next = NULL;

    return chain;
}

Buffer* chain_alloc_next_buf(Chain *chain, size_t buf_size) {
    assert(chain != NULL);
    assert(buf_size > 0);

    Chain *new_chain = chain_create(buf_size);
    if(new_chain == NULL) {
        return NULL;
    }

    // lookup tail of chain
    Chain *cur = chain;
    while(cur->next != NULL) {
        cur = cur->next;
    }

    cur->next = new_chain;
    return &new_chain->buf;
}


static void buffer_copy(Buffer *dest, Buffer *src) {
    size_t payload_size = src->last - src->start;
    memcpy(dest->start, src->start, payload_size);
    size_t read_bytes = src->pos - src->start;
    dest->pos = dest->start + read_bytes;
    dest->last = dest->start + payload_size;
}


Chain* chain_clone(Chain *chain) {
    assert(chain != NULL);

    // clone head
    size_t size = chain->buf.end - chain->buf.start;
    Chain *new_chain = chain_create(size);
    if(new_chain == NULL) {
        return NULL;
    }
    buffer_copy(&new_chain->buf, &chain->buf);

    Chain *clone = new_chain;
    Chain *cur = chain->next;
    while(cur != NULL) {
        size_t new_buf_size = cur->buf.end - cur->buf.start;
        Buffer *new_buf = chain_alloc_next_buf(clone, new_buf_size);
        if(new_buf == NULL) {
            chain_destroy(new_chain);
            return NULL;
        }
        buffer_copy(new_buf, &cur->buf);
        cur = cur->next;
        clone = clone->next;
    }
    return new_chain;
}


void chain_destroy(Chain *chain) {
    assert(chain != NULL);
    Chain *cur = chain;
    while(cur != NULL) {
        Chain *tmp = cur;
        cur = cur->next;
        free(tmp->buf.start);
        free(tmp);
    }
}
