#include "http_util.h"


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>


#include "core/status.h"


char const* http_status_tostring(HttpStatus status) {
    switch(status) {
    case 200:
        return "OK";
    case 201:
        return "Created";
    case 202:
        return "Accepted";
    case 204:
        return "No Content";

    case 301:
        return "Moved Permanently";
    case 302:
        return "Found";
    case 304:
        return "Not Modified";

    case 400:
        return "Bad Request";
    case 401:
        return "Unauthorized";
    case 403:
        return "Forbidden";
    case 404:
        return "Not Found";

    case 500:
        return "Internal Server Error";
    case 501:
        return "Not Implemented";
    case 502:
        return "Bad Gateway";
    case 503:
        return "Service Unavailable";

    default: abort();
    }
}


char const* http_parse_code_tostring(HttpParseCode parse_code) {
    switch(parse_code) { 
    case HTTP_INVALID_METHOD: 
        return "Client sent invalid method";    
    case HTTP_INVALID_REQUEST:
        return "Client sent invalid request";
    case HTTP_INVALID_VERSION:
        return "Client sent invalid version";
    case HTTP_INVALID_HEADER:
        return "Client sent invalid header";
    case HTTP_INVALID_09_METHOD: 
        return "Client sent invalid method in HTTP/0.9 request";
    
    case HTTP_INVALID_RESPONSE:
        return "Gateway sent invalid response";
    case HTTP_INVALID_STATUS:
        return "Gateway sent invalid status";
    
    default: abort();
    }
}


int send_error_response(int sock, HttpStatus http_status) {
    char msg[256];
    char body[128];

    int status = (int) http_status;

    // Build the HTTP response body
    sprintf(body, "<html><title>Proxy Error</title>" \
                  "<body bgcolor=""ffffff"">\r\n" \
                  "<h1>%d: %s</h1></body></html>\r\n", \
                  status, http_status_tostring(http_status));

    // Print the HTTP response
    sprintf(msg, "HTTP/1.0 %d\r\n" \
                 "Content-type: text/html\r\n" \
                 "Content-length: %d\r\n\r\n%s", \
                 status, (int)strlen(body), body);

    ssize_t n = sock_send(sock, msg, strlen(msg));
    if(n == IO) {
        return ERROR;
    }
    return OK;
}
