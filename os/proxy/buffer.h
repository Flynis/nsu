#ifndef _BUFFER_H_INCLUDED_
#define _BUFFER_H_INCLUDED_


#include <stdlib.h>
#include <unistd.h>


typedef struct {
    char *pos;
    char *last;
    char *start;
    size_t size;
} buffer_t;


buffer_t* buffer_create(size_t size);


ssize_t buffer_read(buffer_t *buffer, int fd);


void buffer_destroy(buffer_t *buffer);


#endif
