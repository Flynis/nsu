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
 * Closes socket
*/
void close_socket(int sock);


#endif // _SOCKET_H_INCLUDED_
