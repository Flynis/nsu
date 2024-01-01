#ifndef _SOCKET_H_INCLUDED_
#define _SOCKET_H_INCLUDED_

#include <sys/socket.h>

#define MAX_PENDING_CONNECTIONS 256 

typedef struct sockaddr sockaddr_t;

/**
 * Opens socket descriptor for listening incoming connections.
 * @returns socket descriptor on success, -1 on failure. Errno is set to indicate the error.
*/
int server_socket_open(int port);

/**
 * Accepts an incoming connection.
 * @returns socket descriptor on success, -1 on failure. Errno is set to indicate the error.
*/
int server_socket_accept(int socket, sockaddr_t *addr, socklen_t *addrlen);

/**
 * Connects to remote host.
 * Opens socket descriptor, resolves hostname and connects to remote host.
 * @returns socket descriptor on success, -1 on system error and sets errno to indicate the error.
 * @returns -2 on resolve hostname failed.
 * @returns -3 on connect to remote host failed.
*/
int socket_connect(char *hostname, int port);

/**
 * Closes socket descriptor
*/
void socket_close(int socket);

#endif
