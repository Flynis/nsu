#include "proxy.h"


#include <errno.h>
#include <netinet/in.h>
#include <pthread.h>
#include <stdlib.h>


#include "core/log.h"
#include "core/socket.h"
#include "core/status.h"
#include "http/http.h"
#include "http/http_process.h"


/**
 * Argument for connection handler function.
*/
typedef struct HandlerArgs {
    HttpRequest *req;
    Cache *cache;
} HandlerArgs;


static HandlerArgs* create_handler_args(Cache *cache) {
    HandlerArgs *args = malloc(sizeof(HandlerArgs));
    if(args == NULL) {
        return NULL;
    }

    args->req = http_request_create();
    if(args->req == NULL) {
        free(args);
        return NULL;
    }

    args->cache = cache;
    return args;
}


static void destroy_handler_args(HandlerArgs *args) {
    if(args == NULL) return;
    http_request_destroy(args->req);
    free(args);
}


Proxy* proxy_create(size_t cache_size, struct sockaddr_in const *sockaddr) {
    if(cache_size == 0 || sockaddr == NULL) {
        return NULL;
    }

    Proxy *proxy = malloc(sizeof(Proxy));
    if(proxy == NULL) {
        return NULL;    
    }

    proxy->cache = cache_create(cache_size);
    if(proxy->cache == NULL) {
        LOG_ERR("Proxy cache create failed\n");
        goto fail_cache_manager_create;
    }

	proxy->listen_sock = open_listening_socket(sockaddr);
    if(proxy->listen_sock == IO) {
        LOG_ERR("Proxy listening socket open failed\n");
        goto fail_open_socket;
    }

    // successfully allocate all resources 
    return proxy;

fail_open_socket:
    cache_destroy(proxy->cache);
fail_cache_manager_create:
    free(proxy);
    return NULL;
}


static void* connection_handler(void *data) {
    pthread_detach(pthread_self());
    
    HandlerArgs *args = (HandlerArgs*)data;
    HttpRequest *req = args->req;
    Cache *cache = args->cache;

    HttpState state = HTTP_READ_REQUEST_HEAD;
    bool not_closed_request = true;
    while(not_closed_request) {
        switch(state)
        {
        case HTTP_READ_REQUEST_HEAD:
            state = http_read_request_head(req);
            break;

        case HTTP_PROCESS_REQUEST:
            if(req->method == HTTP_GET || req->method == HTTP_HEAD) {
                state = cache_process_request(cache, req);                
            } else {
                state = http_process_request(req);
            }
            break;

        case HTTP_UNCACHEABLE_REQUEST:
            state = http_process_request(req);
            break;

        case HTTP_TERMINATE_REQUEST:
            http_terminate_request(req);
            state = HTTP_CLOSE_REQUEST;
            break;
        
        case HTTP_CLOSE_REQUEST:
            destroy_handler_args(args);
            not_closed_request = false;
            break;

        default: abort();
        }
    }
    return NULL;
}


int start_connection_handler(Proxy *proxy, int sock) {
    HandlerArgs *args = create_handler_args(proxy->cache);
    if(args == NULL) {
        LOG_ERR("Failed to create handler args\n");
        return ERROR;
    }

    args->req->sock = sock;

    pthread_t tid;
    int ret = pthread_create(&tid, NULL, connection_handler, args);
    if(ret != 0) {
        LOG_ERRNO(ret, "pthread_create() failed");
        destroy_handler_args(args);
        return ERROR;
    }

    return OK;
}


int proxy_listen(Proxy *proxy) {
    if(proxy == NULL) {
        return ERROR;
    }

    // listen for new connections
    while(1) {
        int sock = accept_socket(proxy->listen_sock);
        if(sock == IO) {
            return ERROR;
        }

        LOG_DEBUG("Starting connection handler for %d\n", sock);
        int ret = start_connection_handler(proxy, sock);
        if(ret != OK) {
            LOG_ERR("Failed to start client connection handler\n");
            close_socket(sock);
            // continue listening
        }
    }

    return OK;
}


void proxy_destroy(Proxy *proxy) {
    if(proxy == NULL) return;
    cache_destroy(proxy->cache);
    close_socket(proxy->listen_sock);
    free(proxy);
}
