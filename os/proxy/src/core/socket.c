#include "socket.h"


#include <arpa/inet.h>
#include <assert.h>
#include <errno.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>


#include "inet_limits.h"
#include "log.h"
#include "status.h"


#define MAX_PENDING_CONNECTIONS 256 


static void print_addr(char const *msg, int sock, struct sockaddr_in const *addr) {
#ifndef NDEBUG
    char addrstr[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, addr, addrstr, sizeof(addrstr));
    LOG_DEBUG("%s %d[%s]\n", msg, sock, addrstr);
#endif
}


int open_listening_socket(struct sockaddr_in const *sockaddr) {
    assert(sockaddr != NULL);

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock < 0) {
        LOG_ERRNO(errno, "Failed to open socket");
        return IO;
    }
    
    int err;
    int opval = 1;
    // Eliminates "Address already in use" error from bind.
    err = setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &opval , sizeof(opval));
    if(err) {
        LOG_ERRNO(errno, "setsockopt(SO_REUSEADDR) failed");
        goto fail_configure;
    }
    err = bind(sock, (struct sockaddr const*)sockaddr, sizeof(sockaddr));
    if(err) {
        LOG_ERRNO(errno, "Failed to bind listening socket");
        goto fail_configure;
    }
    err = listen(sock, MAX_PENDING_CONNECTIONS);
    if(err) {
        LOG_ERRNO(errno, "listen() failed");
        goto fail_configure;
    }

    // successfully configured socket
    print_addr("Open listening socket", sock, sockaddr);
    return sock;

fail_configure:
    close_socket(sock);
    return IO;
}


int accept_socket(int listen_sock) {
    assert(listen_sock >= 0);

    struct sockaddr_in clientaddr;
    socklen_t clientlen = sizeof(clientaddr);

    int sock = accept(listen_sock, (struct sockaddr*)&clientaddr, &clientlen);
    if(sock < 0) {
        LOG_ERRNO(errno, "Failed to accept socket");
        return ERROR;
    }
    print_addr("Accept socket", sock, &clientaddr);

    return sock;
}


int open_and_connect_socket(char const *host, int port) {
    assert(host != NULL);
    assert(port > 0 && port <= PORT_MAX);

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock < 0) {
        LOG_ERRNO(errno, "Failed to open socket");
        return IO;
    }

    char port_str[PORT_STR_LEN];
    sprintf(port_str, "%d", port);

    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;

    // get a list of host addresses
    struct addrinfo *addr_list;
    int err = getaddrinfo(host, port_str, &hints, &addr_list);
    if(err) {
        LOG_ERR("Hostname resolve failed: %s", gai_strerror(err));
        close_socket(sock);
        return IO;
    }
  
    // try to connect to any address
    struct addrinfo *cur = addr_list;
    while(cur) {
        err = connect(sock, cur->ai_addr, cur->ai_addrlen);
        if(err == 0) {
            // successful connect 
            print_addr("Connect socket", sock, cur->ai_addr);
            break; 
        }
        cur = cur->ai_next;
    }

    freeaddrinfo(addr_list);
    if(cur == NULL) { 
        // all connects failed
        close_socket(sock);
        return IO;
    } 
    
    return sock;    
}


ssize_t sock_recv(int sock, unsigned char *buf, size_t size) {
    assert(sock >= 0);
    assert(buf != NULL);
    assert(size > 0);

    ssize_t n = recv(sock, buf, size, 0);
    if(n < 0) {
        LOG_ERRNO(errno, "socket recv() failed");
        return IO;
    }
    return n;
}


ssize_t sock_send(int sock, unsigned char const *buf, size_t size) {
    assert(sock >= 0);
    assert(buf != NULL);
    assert(size > 0);

    ssize_t n = send(sock, buf, size, 0);
    if(n < 0) {
        LOG_ERRNO(errno, "socket send() failed");
        return IO;
    }
    if(n == 0) {
        return END_OF_STREAM;
    }
    return n;
}


void close_socket(int sock) {
    int err = close(sock);
    if(err) {
        LOG_ERRNO(errno, "Failed to close socket");
    }
    LOG_DEBUG("Close socket %d\n", sock);
}
