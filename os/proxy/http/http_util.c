#include "http_util.h"


#include <stdio.h>
#include <stdlib.h>


char const* http_status_tostring(HttpStatusCode status) {
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


int send_error_response(Connection *connection, HttpStatusCode http_status) {
    char msg[256];
    char body[128];

    int status = (int) http_status;

    // Build the HTTP response body
    sprintf(body, "<html><title>Proxy Error</title>" \
                  "<body bgcolor=""ffffff"">\r\n" \
                  "%d: %s<html>\r\n", \
                  status, http_status_tostring(http_status));

    // Print the HTTP response
    sprintf(msg, "HTTP/1.0 %d\r\n" \
                 "Content-type: text/html\r\n" \
                 "Content-length: %d\r\n\r\n%s", \
                 status, (int)strlen(body), body);
    
}
