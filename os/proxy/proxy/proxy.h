#ifndef _PROXY_H_INCLUDED_
#define _PROXY_H_INCLUDED_


#include <pthread.h>
#include <stdbool.h>
#include <sys/socket.h>


#include "cache.h"
#include "core/queue.h"


typedef struct Proxy {
    BlockingCache *cache;
    Queue *connections;
    bool running;
    pthread_attr_t handler_attr;
} Proxy;


/**
 * Initializes the proxy.
 * @returns new proxy, or NULL on error.
*/
Proxy* proxy_create(size_t cache_size);


/**
 * Starts to listen to incoming connections and serve its.
 * @returns ERRC_FAILED on some error.
*/
int proxy_listen(Proxy *proxy, struct sockaddr const *sockaddr, socklen_t socklen);


/**
 * Destroys the proxy.
*/
void proxy_destroy(Proxy *proxy);


#endif // _PROXY_H_INCLUDED_
