#include "socket.h"


#include <assert.h>
#include <errno.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <unistd.h>


#include "core/errcode.h"
#include "core/log.h"


#define MAX_PENDING_CONNECTIONS 256 


int open_listening_socket(struct sockaddr const *sockaddr, socklen_t socklen) {
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
    err = bind(fd, sockaddr, socklen);
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


int open_and_connect_socket(struct sockaddr const *sockaddr, socklen_t socklen) {
    assert(sockaddr != NULL);

    int fd = socket(AF_INET, SOCK_STREAM, 0);
    if(fd < 0) {
        log_error_code(errno, "Failed to open socket");
        return ERRC_FAILED;
    }

    int err = connect(fd, sockaddr, socklen);
    if(err < 0) {
        log_error_code(errno, "Failed to socket connect");
        close(fd);
        return ERRC_FAILED;
    }

    return fd;
}
