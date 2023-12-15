#ifndef _HTTP_H_INCLUDED_
#define _HTTP_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


#include "core/str.h"


typedef enum HttpVersion {
    HTTP_9,
    HTTP_10
} HttpVersion;


typedef enum HttpMethod {
    HTTP_GET,
    HTTP_HEAD,
    HTTP_POST,
    HTTP_UNKNOWN
} HttpMethod;


enum {
    HTTP_MORE_HEADERS = 10,
    HTTP_INVALID_METHOD,
    HTTP_INVALID_REQUEST,
    HTTP_INVALID_RESPONSE,
    HTTP_INVALID_STATUS,
    HTTP_INVALID_VERSION,
    HTTP_INVALID_HEADER,
    HTTP_INVALID_09_METHOD
};


typedef struct HttpRequest {
    String request_line;

    HttpVersion version;

    HttpMethod method;
    String method_name;
    
    String url;
    String host;
    bool is_ip_literal;
    unsigned int port;

    String headers;

    size_t content_length;
    bool has_body;
    void *body;
} HttpRequest;


typedef struct HttpResponse {
    String status_line;

    HttpVersion version;
    unsigned int status_code;

    String headers;

    size_t content_length;
    bool has_body;
    void *body;
} HttpResponse;


typedef struct HttpHeader {
    String name;
    String value;
} HttpHeader;


#endif // _HTTP_H_INCLUDED_
