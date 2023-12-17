#ifndef _HTTP_H_INCLUDED_
#define _HTTP_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


#include "core/buffer.h"
#include "core/connection.h"
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


typedef enum HttpStatusCode {
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
} HttpStatusCode;


typedef struct HttpRequest {
    Buffer *header_in; // contains request line and headers
    Connection *conn; // connection with client

    String request_line;

    HttpVersion version;

    HttpMethod method;
    String method_name;
    
    String url;
    String host;
    unsigned int port;

    String headers;

    size_t content_length;
    bool has_body;
    void *body;
} HttpRequest;


typedef struct HttpResponse {
    Buffer *header_in; // contains status line and headers
    Connection *conn; // upstream
   
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


HttpRequest* http_request_create(Connection *conn);


void http_request_destroy(HttpRequest *req);


HttpResponse* http_response_create(Connection *conn);


void http_response_destroy(HttpResponse *res);


#endif // _HTTP_H_INCLUDED_
