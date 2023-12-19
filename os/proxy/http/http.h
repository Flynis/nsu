#ifndef _HTTP_H_INCLUDED_
#define _HTTP_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>
#include <time.h>


#include "core/buffer.h"
#include "core/str.h"


typedef enum HttpVersion {
    HTTP_9,
    HTTP_10,
    HTTP_NOT_SUPPORTED_VERSION,
} HttpVersion;


typedef enum HttpMethod {
    HTTP_GET,
    HTTP_HEAD,
    HTTP_POST,
    HTTP_UNKNOWN
} HttpMethod;


typedef enum HttpStatus {
    HTTP_OK = 200,
    HTTP_CREATED = 201,
    HTTP_ACCEPTED = 202,
    HTTP_NO_CONTENT = 204,
    
    HTTP_MOVED_PERMANENTLY = 301,
    HTTP_MOVED_TEMPORARILY = 302,
    HTTP_NOT_MODIFIED = 304,

    HTTP_BAD_REQUEST = 400,
    HTTP_UNAUTHORIZED = 401,
    HTTP_FORBIDDEN = 403,
    HTTP_NOT_FOUND = 404,

    HTTP_INTERNAL_SERVER_ERROR = 500,
    HTTP_NOT_IMPLEMENTED = 501,
    HTTP_BAD_GATEWAY = 502,
    HTTP_SERVICE_UNAVAILABLE = 503
} HttpStatus;


typedef struct HttpRequest {
    Buffer *raw_head; // contains request line and headers
    int sock; // connection with client

    String request_line;

    HttpVersion version;

    HttpMethod method;
    String method_name;
    
    String host;
    unsigned int port;

    String headers;

    size_t content_length;
    bool is_content_len_set;
} HttpRequest;


typedef struct HttpResponse {
    Buffer *raw_head; // contains status line and headers
    int sock; // connection with upstream
   
    String status_line;

    HttpVersion version;
    unsigned int status_code;

    String headers;

    size_t content_length;
    bool is_content_len_set;
    void *body;

    time_t insert_time; // for tracking ttl
} HttpResponse;


typedef struct HttpHeader {
    String name;
    String val;
} HttpHeader;


HttpRequest* http_request_create(void);


void http_request_destroy(HttpRequest *req);


HttpResponse* http_response_create(void);


void http_response_destroy(HttpResponse *res);


#endif // _HTTP_H_INCLUDED_
