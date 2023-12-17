#ifndef _SOCKET_H_INCLUDED_
#define _SOCKET_H_INCLUDED_


#include <netinet/in.h>


/**
 * Opens socket listening incoming connections.
 * @returns socket descriptor on success, ERRC_FAILED otherwise.
*/
int open_listening_socket(struct sockaddr_in const *sockaddr);


/**
 * Opens and connects socket to remote host.
 * @returns socket descriptor on success, ERRC_FAILED otherwise.
*/
int open_and_connect_socket(char const *host, int port, struct sockaddr_in *out_addr);


/**
 * Closes socket
*/
void close_socket(int sock);


#endif // _SOCKET_H_INCLUDED_
