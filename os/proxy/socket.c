#include "socket.h"

#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

#define PORT_STR_LEN 6

int server_socket_open(int port) {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock < 0) {
        return -1;
    }
 
    // Eliminates "Address already in use" error from bind.
    int option_val = 1;
    int err = setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (void *)&option_val , sizeof(int));
    if(err) {
        return -1;
    }

    struct sockaddr_in server_addr;
    server_addr.sin_family = AF_INET; 
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY); 
    server_addr.sin_port = htons((unsigned short)port); 

    err = bind(sock, (sockaddr_t*)&server_addr, sizeof(server_addr));
    if(err) {
        return -1;
    }

    err = listen(sock, MAX_PENDING_CONNECTIONS);
    if(err) {
        return -1;
    }

    return sock;
}


int server_socket_accept(int socket, sockaddr_t *addr, socklen_t *addrlen) {
    return accept(socket, addr, addrlen);
}

int socket_connect(char *hostname, int port) {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock < 0) {
        return -1;
    }

    char port_str[PORT_STR_LEN];
    sprintf(port_str, "%d", port);

    // get a list of host addresses
    struct addrinfo *addr_list;
    int err = getaddrinfo(hostname, port_str, NULL, &addr_list);
    if(err) {
        fprintf(stderr, "Hostname resolve failed: %s", gai_strerror(err));
        return -2;
    }
  
    // try to connect to any address
    struct addrinfo *cur = addr_list;
    while(cur) {
        if (cur->ai_family == AF_INET) {
            err = connect(sock, cur->ai_addr, cur->ai_addrlen);
            if(!err) {
                break; // successful connect 
            }
        }
        cur = cur->ai_next;
    }

    freeaddrinfo(addr_list);
    if(cur == NULL) { 
        // all connects failed
        close(sock);
        return -3;
    } else {
        return sock;
    }
}

void socket_close(int socket) {
    int err = close(socket);
    if(err) {
        perror("Socket close failed");
    }
}
