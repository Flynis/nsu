#include "socket.h"


#include <assert.h>
#include <errno.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>


#include "errcode.h"
#include "inet_limits.h"
#include "log.h"


#define MAX_PENDING_CONNECTIONS 256 


int open_listening_socket(struct sockaddr_in const *sockaddr) {
    assert(sockaddr != NULL);

    int fd = socket(AF_INET, SOCK_STREAM, 0);
    if(fd < 0) {
        log_error_code(errno, "Failed to open socket");
        return ERRC_FAILED;
    }
    
    int err;

    // Eliminates "Address already in use" error from bind.
    int opval = 1;
    err = setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &opval , sizeof(opval));
    if(err) {
        log_error_code(errno, "setsockopt(SO_REUSEADDR) failed");
        goto fail_configure;
    }
    err = bind(fd, sockaddr, sizeof(sockaddr));
    if(err) {
        log_error_code(errno, "Bind server socket failed");
        goto fail_configure;
    }
    err = listen(fd, MAX_PENDING_CONNECTIONS);
    if(err) {
        log_error_code(errno, "Listen conf server socket failed");
        goto fail_configure;
    }

    // successfully configured socket
    return fd;

fail_configure:
    close(fd);
    return ERRC_FAILED;
}


int open_and_connect_socket(char const *host, int port, struct sockaddr_in *out_addr) {
    assert(host != NULL);
    assert(port > 0 && port <= PORT_MAX);
    assert(out_addr != NULL);

    int fd = socket(AF_INET, SOCK_STREAM, 0);
    if(fd < 0) {
        log_error_code(errno, "Failed to open socket");
        return ERRC_FAILED;
    }

    char port_str[PORT_STR_LEN];
    sprintf(port_str, "%d", port);

    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hitns.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;


    // get a list of host addresses
    struct addrinfo *addr_list;
    int err = getaddrinfo(host, port_str, &hints, &addr_list);
    if(err) {
        fprintf(stderr, "Hostname resolve failed: %s", gai_strerror(err));
        close_socket(fd);
        return ERRC_FAILED;
    }
  
    // try to connect to any address
    struct addrinfo *cur = addr_list;
    while(cur) {
        err = connect(fd, cur->ai_addr, cur->ai_addrlen);
        if(err == 0) {
            // successful connect 
            memcpy(out_addr, cur->ai_addr, cur->ai_addrlen);
            break; 
        }
        cur = cur->ai_next;
    }

    freeaddrinfo(addr_list);
    if(cur == NULL) { 
        // all connects failed
        close_socket(fd);
        return ERRC_FAILED;
    } 
    
    return fd;    
}


void close_socket(int sock) {
    int err = close(sock);
    if(err) {
        log_error_code(errno, "Failed to close socket");
    }
}
