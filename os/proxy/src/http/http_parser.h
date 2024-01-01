#ifndef _HTTP_PARSER_H_INCLUDED_
#define _HTTP_PARSER_H_INCLUDED_


#include "http.h"
#include "core/str.h"


typedef enum HttpParseCode {
    HTTP_MORE_HEADERS = 10,
    HTTP_INVALID_METHOD,
    HTTP_INVALID_REQUEST,
    HTTP_INVALID_RESPONSE,
    HTTP_INVALID_STATUS,
    HTTP_INVALID_VERSION,
    HTTP_INVALID_HEADER,
    HTTP_INVALID_09_METHOD
} HttpParseCode;


typedef enum ParserState { 
    sw_res_start = 0,
    sw_res_H,
    sw_res_HT,
    sw_res_HTT,
    sw_res_HTTP,
    sw_res_first_major_digit,
    sw_res_major_digit,
    sw_res_first_minor_digit,
    sw_res_minor_digit,
    sw_res_status_code,
    sw_res_space_after_status_code,
    sw_res_status_text,
    sw_res_almost_done,

    sw_req_start,
    sw_req_method,
    sw_req_schema,
    sw_req_schema_h,
    sw_req_schema_ht,
    sw_req_schema_htt,
    sw_req_schema_http,
    sw_req_schema_slash,
    sw_req_schema_slash_slash,
    sw_req_host_start,
    sw_req_host,
    sw_req_ip_literal,
    sw_req_port,
    sw_req_after_slash_in_uri,
    sw_req_09,
    sw_req_http_H,
    sw_req_http_HT,
    sw_req_http_HTT,
    sw_req_http_HTTP,
    sw_req_first_major_digit,
    sw_req_major_digit,
    sw_req_first_minor_digit,
    sw_req_minor_digit,
    sw_req_space_after_digit,
    sw_req_almost_done,

    sw_header_start,
    sw_header_name,
    sw_header_space_before_value,
    sw_header_value,
    sw_header_space_after_value,
    sw_header_almost_done,
    sw_all_headers_almost_done
} ParserState;


/**
 * Finite state machine based http parser.
*/
typedef struct HttpParser {
    ParserState state;

    bool is_request_parser;
    HttpRequest *req; // if parse request
    HttpResponse *res; // if parse response

    unsigned short http_major;
    unsigned short http_minor;

    int status;

    unsigned char *req_start;
    unsigned char *req_end;
    unsigned char *host_start;
    unsigned char *host_end;
    int port;

    HttpHeader header;
    unsigned char *header_name_start;
    unsigned char *header_name_end;
    unsigned char *header_val_start;
    unsigned char *header_val_end;
} HttpParser;


/**
 * Initializes request parser.
*/
void http_request_parser_init(HttpParser *p, HttpRequest *req);


/**
 * Initializes response parser.
*/
void http_response_parser_init(HttpParser *p, HttpResponse *res);


/**
 * Parses request line.
 * @returns OK if request line is parsed successfully.
 * @returns AGAIN if request line is not parsed completely.
 * @returns ERROR if parser is not request parser.
 * @returns HTTP_INVALID_REQUEST if request line is not valid.
 * @returns HTTP_INVALID_METHOD if request method is not valid.
 * @returns HTTP_INVALID_VERSION if http version is not valid.
 * @returns HTTP_INVALID_09_METHOD if http0.9 request method is not valid.
*/
int http_parse_request_line(HttpParser *parser);


/**
 * Parses request headers.
 * @returns OK if all headers are parsed successfully.
 * @returns AGAIN if header line is not parsed completely.
 * @returns HTTP_MORE_HEADERS if there are more headers to parse.
 * @returns HTTP_INVALID_HEADER if header line is not valid.
*/
int http_parse_header_line(HttpParser *parser);


/**
 * Parses status line.
 * @returns OK if status line is parsed successfully.
 * @returns AGAIN if status line is not parsed completely.
 * @returns ERROR if parser is not response parser.
 * @returns HTTP_INVALID_RESPONSE if status line is not valid.
 * @returns HTTP_INVALID_VERSION if http version is not valid.
*/
int http_parse_status_line(HttpParser *parser);


#endif // _HTTP_PARSER_H_INCLUDED_
