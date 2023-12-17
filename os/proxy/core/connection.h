#ifndef _CONNECTION_H_INCLUDED_
#define _CONNECTION_H_INCLUDED_


#include <netinet/in.h>
#include <stddef.h>


typedef struct Connection {
    int sockfd;
    struct sockaddr_in sockaddr;
} Connection;


ssize_t conn_recv(Connection *connection, unsigned char *buf, size_t size);


ssize_t conn_send(Connection *connection, unsigned char const *buf, size_t size);


#endif // _CONNECTION_H_INCLUDED_
