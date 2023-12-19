#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>


#include "core/inet_limits.h"
#include "core/status.h"
#include "proxy/proxy.h"


#define CACHE_SIZE 1024
#define DEFAULT_PORT 3128


int main(int argc, char const **argv) {
    int port = DEFAULT_PORT;

    // read port from arguments
	if(argc == 2) {
        int p = atoi(argv[1]);
        if(p <= 0 || p > PORT_MAX) {
            printf("Proxy usage: %s <port>\n", argv[0]);
		    return EXIT_FAILURE;
        }
        // correct input port
        port = p;
	}

    // proxy address
    struct sockaddr_in addr;
    addr.sin_family = AF_INET; 
    addr.sin_addr.s_addr = htonl(INADDR_ANY); 
    addr.sin_port = htons((unsigned short)port); 

    Proxy *proxy = proxy_create(CACHE_SIZE, &addr);
    if(proxy == NULL) {
		puts("Failed to start proxy server");
        return EXIT_FAILURE;
    }
    
    int status = EXIT_SUCCESS;
    int ret = proxy_listen(proxy);
    if(ret == ERROR) {
		puts("Fatal proxy server error");
        status = EXIT_FAILURE;
    }

    proxy_destroy(proxy);
    return status;
}
