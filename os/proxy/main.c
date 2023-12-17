#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>


#include "core/errcode.h"
#include "core/inet_limits.h"
#include "core/log.h"
#include "proxy/proxy.h"


#define CACHE_SIZE 1024
#define DEFAULT_PORT 3128


int main(int argc, char const **argv) {
    int port = DEFAULT_PORT;

    // read port from arguments
	if(argc == 2) {
        int p = atoi(argv[1]);
        if(p <= 0 || p > PORT_MAX) {
            printf("usage: %s <port>\n", argv[0]);
		    return EXIT_FAILURE;
        }
        // correct input port
        port = p;
	}

    Proxy *proxy = proxy_create(CACHE_SIZE);
    if(proxy == NULL) {
		log_error("Proxy create failed");
        return EXIT_FAILURE;
    }

    struct sockaddr_in addr;
    socklen_t addrlen = sizeof(addr);
    addr.sin_family = AF_INET; 
    addr.sin_addr.s_addr = htonl(INADDR_ANY); 
    addr.sin_port = htons((unsigned short)port); 
    
    int status = EXIT_SUCCESS;
    int err = proxy_listen(proxy, &addr);
    if(err == ERRC_FAILED) {
		log_error("Proxy listen failed");
        status = EXIT_FAILURE;
    }

    proxy_destroy(proxy);
    return status;
}
