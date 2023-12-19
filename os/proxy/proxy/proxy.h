#ifndef _PROXY_H_INCLUDED_
#define _PROXY_H_INCLUDED_


#include <netinet/in.h>


#include "cache.h"


typedef struct Proxy {
    Cache *cache;
    int listen_sock;
} Proxy;


/**
 * Initializes the proxy.
 * @returns new proxy, or NULL on error.
*/
Proxy* proxy_create(size_t cache_size, struct sockaddr_in const *sockaddr);


/**
 * Starts to listen to incoming connections and serve its.
 * @returns ERROR on error.
*/
int proxy_listen(Proxy *proxy);


/**
 * Destroys the proxy.
*/
void proxy_destroy(Proxy *proxy);


#endif // _PROXY_H_INCLUDED_
