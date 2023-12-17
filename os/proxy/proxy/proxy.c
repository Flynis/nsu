#include "proxy.h"


#include <errno.h>
#include <netinet/in.h>
#include <string.h>


#include "core/buffer.h"
#include "core/connection.h"
#include "core/errcode.h"
#include "core/log.h"
#include "core/socket.h"
#include "http/http_parser.h"
#include "http/http_process.h"


/**
 * Argument for connection handler function.
*/
typedef struct ConnectionHandlerArgs {
    Connection connection;
    BlockingCache *cache;
} ConnectionHandlerArgs;


Proxy* proxy_create(size_t cache_size) {
    if(cache_size == 0) {
        return NULL;
    }

    Proxy *proxy = malloc(sizeof(proxy));
    if(proxy == NULL) {
        return NULL;    
    }

    proxy->cache = cache_create(cache_size);
    if(proxy->cache == NULL) {
        log_error("Cache create failed");
        goto fail_cache_create;
    }

	int ret;
    pthread_attr_t *attr = &proxy->handler_attr;
    ret = pthread_attr_init(attr);
    if (ret != 0) {
        log_error_code(ret, "pthread_attr_init() failed");
        goto fail_handler_attr_init;
    }
    ret = pthread_attr_setdetachstate(attr, PTHREAD_CREATE_DETACHED);
    if (ret != 0) {
        log_error_code(ret, "pthread_attr_setdetachstate() failed");
        goto fail_handler_attr_set;
    }

    // successfully allocate all resources 
    proxy->running = false;
    return proxy;

fail_handler_attr_set:
    pthread_attr_destroy(&proxy->handler_attr);
fail_handler_attr_init:
    cache_destroy(proxy->cache);
fail_cache_create:
    free(proxy);
    return NULL;
}


static void terminate_connection(ConnectionHandlerArgs *args) {
    close(args->connection.sockfd);
    free(args);
}


static void* connection_handler(void *args) {
    ConnectionHandlerArgs *arg = (ConnectionHandlerArgs*)args;
    Connection *conn = &arg->connection;
    BlockingCache *cache = arg->cache;

    HttpRequest *req = http_request_create(conn);
    if(req == NULL) {
        terminate_connection(arg);
        return NULL;
    }

    HttpState state = HTTP_PROCESS_STATE;
    while (state == HTTP_PROCESS_STATE) {
        HttpParser parser;
        http_request_parser_init(&parser, req);

        state = http_process_request_line(&parser);
        if(state == HTTP_TERMINATE_STATE) {
            break;
        }

        state = http_process_headers(&parser);
        if(state == HTTP_TERMINATE_STATE) {
            break;
        }
    }
    
    http_request_destroy(req);
    terminate_connection(arg);
    return NULL;
}


/**
 * Starts a new connection handler thread.
 * @returns ERRC_OK on success, ERRC_FAILED otherwise.
*/
static int start_connection_handler(Proxy *proxy, Connection const *client) {
    ConnectionHandlerArgs *args = malloc(sizeof(args));
    if(args == NULL) {
        return ERRC_FAILED;
    }
    args->cache = proxy->cache;
    args->connection = *client;

    int ret;
    pthread_t tid;
    ret = pthread_create(&tid, &proxy->handler_attr, connection_handler, args);
    if(ret != 0) {
        log_error_code(ret, "Connection handler thread create failed");
        free(args);
        return ERRC_FAILED;
    }

    return ERRC_OK;    
}

int proxy_listen(Proxy *proxy, struct sockaddr_in const *sockaddr) {
    if(proxy == NULL || sockaddr == NULL) {
        return ERRC_FAILED;
    }

    int listen_sock = open_listening_socket(sockaddr);
    if(listen_sock < 0) {
        log_error("Open server socket failed");
        return ERRC_FAILED;
    }

    proxy->running = true;
    while(proxy->running) {
        struct sockaddr_in clientaddr;
        socklen_t clientlen = sizeof(clientaddr);

        int sockfd = accept(listen_sock, (struct sockaddr*)&clientaddr, &clientlen);
        if(sockfd < 0) {
            log_error_code(errno, "Accept failed");
            goto err;
        }

        Connection conn = { 
            .sockfd = sockfd,
            .sockaddr = clientaddr,
        };  
        
        int ret = start_connection_handler(proxy, &conn);
        if(ret != ERRC_OK) {
            log_error("Start client connection handler failed");
            close(sockfd);
            // continue listening
        }
    }

err:
    close(listen_sock);
    return ERRC_FAILED;
}


void proxy_destroy(Proxy *proxy) {
    if(proxy == NULL) return;
    pthread_attr_destroy(&proxy->handler_attr);
    cache_destroy(proxy->cache);
    free(proxy);
}
