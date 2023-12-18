#ifndef _SOCKET_H_INCLUDED_
#define _SOCKET_H_INCLUDED_


#include <netinet/in.h>


/**
 * Opens socket for listening incoming connections.
 * @returns socket on success, IO on I/O error.
*/
int open_listening_socket(struct sockaddr_in const *sockaddr);


/**
 * Accepts a socket from pending connections for the listening socket.
 * @returns socket on success, IO on I/O error.
*/
int accept_socket(int listen_sock);


/**
 * Opens and connects socket to remote host.
 * @returns socket on success.
 * @returns IO on I/O error. 
 * @returns UNKNOWN_HOST if failed to resolve host.
*/
int open_and_connect_socket(char const *host, int port);


/**
 * Receives data from sock and store it in buf.
 * @returns number of bytes read.
 * @returns IO on I/O error.
*/
ssize_t sock_recv(int sock, unsigned char *buf, size_t size);


/**
 * Sends data to sock from buf.
 * @returns number of bytes sent.
 * @returns IO on I/O error.
*/
ssize_t sock_send(int sock, unsigned char const *buf, size_t size);


/**
 * Closes socket
*/
void close_socket(int sock);


#endif // _SOCKET_H_INCLUDED_
