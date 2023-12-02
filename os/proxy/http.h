#ifndef _HTTP_H_INCLUDED_
#define _HTTP_H_INCLUDED_


#include <stdlib.h>


#include "buffer.h"


#define HTTP_METHOD_MAX_SIZE 16
#define HTTP_URL_MAX_SIZE 1024
#define HTTP_HOST_MAX_SIZE 512
#define HTTP_HEADERS_MAX_SIZE 2048
#define HTTP_STATUS_MAX_SIZE 256


#define HTTP_VERSION_9                 9
#define HTTP_VERSION_10                100


#define HTTP_UNKNOWN                   1
#define HTTP_GET                       2
#define HTTP_HEAD                      3
#define HTTP_POST                      4


// error codes
#define HTTP_SYSTEM_ERROR              9
#define HTTP_PARSE_INVALID_EOF_STATE   10
#define HTTP_PARSE_INVALID_METHOD      11
#define HTTP_PARSE_INVALID_REQUEST     12
#define HTTP_PARSE_INVALID_URL         13
#define HTTP_PARSE_INVALID_RESPONSE    14
#define HTTP_PARSE_INVALID_STATUS      15
#define HTTP_PARSE_INVALID_VERSION     16
#define HTTP_PARSE_INVALID_09_METHOD   17
#define HTTP_PARSE_INVALID_HEADER      18


#define HTTP_NO_CONTENT ((size_t) -1) // 0xFFFFFFFF


typedef struct {
    unsigned int version;

    unsigned int method;
    char method_name[HTTP_METHOD_MAX_SIZE];
    
    char url[HTTP_URL_MAX_SIZE];
    char host[HTTP_HOST_MAX_SIZE];
    unsigned int port;

    char headers[HTTP_HEADERS_MAX_SIZE];
    size_t headers_size;

    size_t content_length; // bytes int body or HTTP_NO_CONTENT
    void *body;
} http_request_t;


typedef struct {
    unsigned int version;
    unsigned int status_code;
    char status_message[HTTP_STATUS_MAX_SIZE];

    char headers[HTTP_HEADERS_MAX_SIZE];
    size_t headers_size;

    size_t content_length; // bytes int body or HTTP_NO_CONTENT
    void *body;
} http_response_t;


typedef struct {
    int socket;
    buffer_t *buf;

    http_request_t *request; // if parse request
    http_response_t *response; // if parse response

    unsigned short http_major;
    unsigned short http_minor;

    unsigned int status_code;
    size_t status_text_len;
} http_parser_t;


int http_parser_init(http_parser_t *parser, int socket);


int http_parse_request_line(http_parser_t *parser);


int http_parse_headers(http_parser_t *parser);


int http_parse_body(http_parser_t *parser);


int http_parse_status_line(http_parser_t *parser);


void http_parser_destroy(http_parser_t *parser);


#endif
