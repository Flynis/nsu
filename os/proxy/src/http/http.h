#ifndef _HTTP_H_INCLUDED_
#define _HTTP_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


#include "core/buffer.h"
#include "core/str.h"


typedef enum HttpVersion {
    HTTP_9 = 9,
    HTTP_10 = 10,
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


typedef enum HttpState {
    HTTP_READ_REQUEST_HEAD,
    HTTP_PROCESS_REQUEST,
    HTTP_UNCACHEABLE_REQUEST,
    HTTP_TERMINATE_REQUEST,
    HTTP_CLOSE_REQUEST
} HttpState;


typedef struct HttpRequest {
    Buffer *raw; // raw request string
    int sock; // connection with client
    HttpState state;
    HttpStatus status;

    String request_line;
    HttpVersion version;
    HttpMethod method;
    String host;
    unsigned int port;

    size_t content_length;
    bool is_content_len_set;
} HttpRequest;


typedef struct HttpResponse {
    Buffer *raw; // raw response string
    int sock; // connection with upstream
   
    HttpVersion version;
    unsigned int status;

    size_t content_length;
    bool is_content_len_set;
} HttpResponse;


typedef struct HttpHeader {
    String name;
    String val;
} HttpHeader;


/**
 * Creates http request.
 * @return new http request or NULL if error occurred.
*/
HttpRequest* http_request_create(void);


/**
 * Destroys http request and releases all resources.
*/
void http_request_destroy(HttpRequest *req);


/**
 * Creates http response.
 * @return new http response or NULL if error occurred.
*/
HttpResponse* http_response_create(void);


/**
 * Destroys http response and releases all resources.
*/
void http_response_destroy(HttpResponse *res);


#endif // _HTTP_H_INCLUDED_
