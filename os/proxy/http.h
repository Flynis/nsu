#ifndef _HTTP_H_INCLUDED_
#define _HTTP_H_INCLUDED_


#include <stdbool.h>
#include <stdlib.h>


#include "buffer.h"


#define HTTP_METHOD_MAX_LEN            16
#define HTTP_HOST_MAX_LEN              512
#define HTTP_URL_MAX_LEN               1024 // must be > HTTP_HOST_MAX_LEN
#define HTTP_HEADERS_MAX_LEN           2048
#define HTTP_STATUS_MAX_LEN            256


#define HTTP_VERSION_9                 9
#define HTTP_VERSION_10                1000


#define HTTP_UNKNOWN                   1
#define HTTP_GET                       2
#define HTTP_HEAD                      3
#define HTTP_POST                      4


// return codes
#define HTTP_AGAIN                     10
#define HTTP_MORE_HEADERS              11
#define HTTP_INVALID_METHOD            12
#define HTTP_INVALID_REQUEST           13
#define HTTP_INVALID_RESPONSE          14
#define HTTP_INVALID_STATUS            15
#define HTTP_INVALID_VERSION           16
#define HTTP_INVALID_HEADER            17
#define HTTP_INVALID_09_METHOD         18

#define HTTP_NO_CONTENT_LEN ((size_t) -1) // 0xFFFFFFFF


typedef struct {
    unsigned int version;

    unsigned int method;
    char method_name[HTTP_METHOD_MAX_LEN];
    
    char url[HTTP_URL_MAX_LEN];
    char host[HTTP_HOST_MAX_LEN];
    bool is_host_domain_name;
    unsigned int port;

    char headers[HTTP_HEADERS_MAX_LEN];
    size_t headers_size;

    size_t content_length; // bytes in body or HTTP_NO_CONTENT_LEN
    void *body;
} http_request_t;


typedef struct {
    unsigned int version;
    unsigned int status_code;

    char headers[HTTP_HEADERS_MAX_LEN];
    size_t headers_size;

    size_t content_length; // bytes in body or HTTP_NO_CONTENT_LEN
    void *body;
} http_response_t;


typedef struct {
    unsigned int state;

    bool is_request_parser;
    http_request_t *request; // if parse request
    http_response_t *response; // if parse response

    unsigned short http_major;
    unsigned short http_minor;

    size_t method_len;
    size_t host_len;
    size_t url_len;
    size_t headers_len;
    size_t status_text_len;

    char *header_name_start;
    char *header_name_end;
    char *header_val_start;
    char *header_val_end;
} http_parser_t;


int http_request_parser_init(http_parser_t *parser, http_request_t *request);


int http_response_parser_init(http_parser_t *parser, http_response_t *response);


int http_parse_request_line(http_parser_t *parser, buffer_t *buf);


int http_parse_header_line(http_parser_t *parser, buffer_t *buf);


int http_parse_status_line(http_parser_t *parser, buffer_t *buf);


#endif
