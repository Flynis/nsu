#ifndef _SOCKET_H_INCLUDED_
#define _SOCKET_H_INCLUDED_


#include <sys/socket.h>


/**
 * Opens socket listening incoming connections.
 * @returns socket descriptor on success, ERRC_FAILED otherwise.
*/
int open_listening_socket(struct sockaddr const *sockaddr, socklen_t socklen);


/**
 * Opens and connects socket to remote host.
 * @returns socket descriptor on success, ERRC_FAILED otherwise.
*/
int open_and_connect_socket(struct sockaddr const *sockaddr, socklen_t socklen);


#endif // _SOCKET_H_INCLUDED_
