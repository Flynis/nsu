#include "proxy.h"


#include <errno.h>
#include <netinet/in.h>
#include <string.h>


#include "core/errcode.h"
#include "core/log.h"
#include "socket.h"


/**
 * Argument for connection handler function.
*/
typedef struct Connection {
    int sockfd;
    struct sockaddr_in sockaddr;
    socklen_t socklen;
    Proxy *proxy;
    QueueNode node; // for fast remove
} Connection;


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
    proxy->connections = queue_create();
    if(proxy->connections == NULL) {
        log_error("Connections list create failed");
        goto fail_connections_create;
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
        goto fail_handler_attr_init;
    }

    // successfully allocate all resources 
    proxy->running = false;
    return proxy;

fail_handler_attr_init:
    vector_destroy(proxy->connections);
fail_connections_create:
    cache_destroy(proxy->cache);
fail_cache_create:
    free(proxy);
    return NULL;
}


static void* connection_handler(void *args) {
    Connection *conn = (Connection*)args;

}


/**
 * Starts a new connection handler thread.
 * @returns ERRC_OK on success, ERRC_FAILED otherwise.
*/
static int start_connection_handler(Connection const *client) {
    Connection *connection = malloc(sizeof(connection));
    if(connection == NULL) {
        return ERRC_FAILED;
    }

    // dup connection
    memcpy(connection, client, sizeof(Connection));

    // initialize node for queue
    QueueNode *node = &connection->node;
    node->value = connection;

    Proxy *proxy = connection->proxy;
    int ret;
    pthread_t tid;
    ret = pthread_create(&tid, &proxy->handler_attr, connection_handler, connection);
    if(ret != 0) {
        log_error_code(ret, "Connection handler thread create failed");
        free(connection);
        return ERRC_FAILED;
    }

    // insert node into queue to free connection later
    queue_push(&proxy->connections, node);
    return ERRC_OK;    
}


int proxy_listen(Proxy *proxy, struct sockaddr const *sockaddr, socklen_t socklen) {
    int listen_sock = open_listening_socket(sockaddr, socklen);
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
            .socklen = clientlen,
            .proxy = proxy
        };  
        
        int ret = start_connection_handler(&conn);
        if(ret != ERRC_OK) {
            log_error("Start client connection handler failed");
            close(sockfd);
            if(proxy->connections->size == 0) {
                goto err;
            }
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
    queue_destroy(proxy->connections);
    free(proxy);
}
