#include "connection.h"


#include <assert.h>
#include <errno.h>


#include "errcode.h"
#include "log.h"


ssize_t conn_recv(Connection *connection, unsigned char *buf, size_t size) {
    assert(connection != NULL);
    assert(buf != NULL);
    assert(size > 0);

    int flags = 0;
    ssize_t n = recv(connection->sockfd, buf, size, flags);
    if(n < 0) {
        log_error_code(errno, "socket recv() failed");
        return ERRC_FAILED;
    }
    return n;
}


ssize_t conn_send(Connection *connection, unsigned char const *buf, size_t size) {
    assert(connection != NULL);
    assert(buf != NULL);
    assert(size > 0);

    int flags = 0;
    ssize_t n = send(connection->sockfd, buf, size, flags);
    if(n < 0) {
        log_error_code(errno, "socket send() failed");
        return ERRC_FAILED;
    }
    return n;
}
