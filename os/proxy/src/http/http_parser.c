#include "http_parser.h"


#include <assert.h>
#include <stdlib.h>
#include <string.h>


#include "core/inet_limits.h"
#include "core/status.h"
#include "http_alphabet.h"


void http_request_parser_init(HttpParser *p, HttpRequest *req) {
    assert(p != NULL);
    assert(req != NULL);

    p->state = sw_req_start;

    p->is_request_parser = true;
    p->req = req;
    p->res = NULL;

    p->http_major = 1;
    p->http_minor = 0;

    p->req_start = NULL;
    p->req_end = NULL;
    p->host_start = NULL;
    p->host_end = NULL;
    p->port = 0;
}


void http_response_parser_init(HttpParser *p, HttpResponse *res) {
    assert(p != NULL);
    assert(res != NULL);

    p->state = sw_res_start;
    
    p->is_request_parser = false;
    p->req = NULL;
    p->res = res;

    p->http_major = 1;
    p->http_minor = 0;

    p->req_start = NULL;
    p->req_end = NULL;
    p->status = 0;
}


int http_parse_request_line(HttpParser *parser) {
    assert(parser != NULL);
    assert(parser->is_request_parser);

    HttpRequest *req = parser->req;
    Buffer *buf = &req->raw->buf;
    unsigned int state = parser->state;

    unsigned char *p; // we need to update buf->pos after parsing
    for(p = buf->pos; p < buf->last; p += 1) {
        char ch = *p;

        switch(state) {
        // HTTP methods: GET, HEAD, POST section 5.1.1
        case sw_req_start:
            parser->req_start = p;
            if(!is_http_token(ch)) {
                return HTTP_INVALID_METHOD;
            }
            state = sw_req_method;
            break;

        case sw_req_method:
            if(ch == ' ') {
                unsigned char *method_start = parser->req_start;
                size_t len = p - method_start;
                // determine method
                switch(len) {
                case 3:
                    if(strncmp((char*)method_start, "GET", len) == 0) {
                        req->method = HTTP_GET;
                    }
                    break;
                case 4:
                    if(method_start[1] == 'O') {
                        if(strncmp((char*)method_start, "POST", len) == 0) {
                            req->method = HTTP_POST;
                        }
                    } else {
                        if(strncmp((char*)method_start, "HEAD", len) == 0) {
                            req->method = HTTP_HEAD;
                        }
                    }
                    break;
                }

                state = sw_req_schema;
                break;
            }

            if(!is_http_token(ch)) {
                return HTTP_INVALID_METHOD;
            }
            break;            

        // schema: "http://" or "/" if abs_path
        case sw_req_schema:
            switch(ch) {
            case ' ':
                break; // ignore spaces
            case 'h':
                state = sw_req_schema_h;
                break;
            case '/':
                state = sw_req_after_slash_in_uri;
                break;
            default: 
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_schema_h:
            if(ch == 't') {
                state = sw_req_schema_ht;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_schema_ht:
            if(ch == 't') {
                state = sw_req_schema_htt;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_schema_htt:
            if(ch == 'p') {
                state = sw_req_schema_http;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_schema_http:
            if(ch == ':') {
                state = sw_req_schema_slash;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_schema_slash:
            if(ch == '/') {
                state = sw_req_schema_slash_slash;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_schema_slash_slash:
            if(ch == '/') {
                state = sw_req_host_start;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        
        // legal host domain name or IP address [rfc 1123 section 2.1, rfc 952] 
        case sw_req_host_start:
            if(is_http_digit(ch) || is_http_alpha(ch)) {
                parser->host_start = p;

                if(is_http_digit(ch)) {
                    state = sw_req_ip_literal;
                } else {
                    state = sw_req_host;
                }
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_host:
            if(is_http_alpha(ch) || is_http_digit(ch) || ch == '.' || ch == '-') {
                break;
            } 
            parser->host_end = p;
            switch(ch) {
            case ':':
                state = sw_req_port;
                break;
            case '/':
                state = sw_req_after_slash_in_uri;
                break;
            case ' ':
                state = sw_req_09;
                break;
            default:
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_ip_literal:
            if(is_http_digit(ch) || ch == '.') {
                break;
            }
            parser->host_end = p;
            switch(ch) {
            case ':':
                state = sw_req_port;
                break;
            case '/':
                state = sw_req_after_slash_in_uri;
                break;
            case ' ':
                state = sw_req_09;
                break;
            default:
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_port:
            if(is_http_digit(ch)) {
                req->port = req->port * 10 + (ch - '0');
                if(req->port >= PORT_MAX) {
                    return HTTP_INVALID_REQUEST;
                }
                break;
            }
            switch(ch) {
            case '/':
                state = sw_req_after_slash_in_uri;
                break;
            case ' ':
                state = sw_req_09;
                break;
            default:
                return HTTP_INVALID_REQUEST;
            }
            break;
            
        // just skip url
        case sw_req_after_slash_in_uri:
            switch(ch) {
            case ' ':
                state = sw_req_09;
                break;
            case '\r':
                parser->http_major = 0;
                parser->http_minor = 9;
                state = sw_req_almost_done;
                break;
            case '\n':
                parser->http_major = 0;
                parser->http_minor = 9;
                goto done;
            }
            // skip char
            break;

        // space after uri
        case sw_req_09:
            switch(ch) {
            case ' ':
                break; // ignore spaces
            case '\r':
                parser->http_major = 0;
                parser->http_minor = 9;
                state = sw_req_almost_done;
                break;
            case '\n':
                parser->http_major = 0;
                parser->http_minor = 9;
                goto done;
            case 'H':
                state = sw_req_http_H;
                break;
            default:
                return HTTP_INVALID_REQUEST;
            }
            break;

        // "HTTP/"
        case sw_req_http_H:
            if(ch == 'T') {
                state = sw_req_http_HT;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_http_HT:
            if(ch == 'T') {
                state = sw_req_http_HTT;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_http_HTT:
            if(ch == 'P') {
                state = sw_req_http_HTTP;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_http_HTTP:
            if(ch == '/') {
                state = sw_req_first_major_digit;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;

        // major HTTP version 
        case sw_req_first_major_digit:
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_major = ch - '0';
            state = sw_req_major_digit;
            break;

        // major HTTP version or dot
        case sw_req_major_digit:
            if(ch == '.') {
                state = sw_req_first_minor_digit;
                break;
            }
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_major = parser->http_major * 10 + (ch - '0');
            if(parser->http_major > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // minor HTTP version
        case sw_req_first_minor_digit:
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_minor = ch - '0';
            state = sw_req_minor_digit;
            break;

        // minor HTTP version or end of request line
        case sw_req_minor_digit:
            switch(ch) {
            case '\r':
                state = sw_req_almost_done;
                break;
            case '\n':
                goto done;
            }
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_minor = parser->http_minor * 10 + (ch - '0');
            if(parser->http_minor > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // end of request line 
        case sw_req_almost_done:
            if(ch == '\n') {
                goto done;
            } else {
                return HTTP_INVALID_REQUEST;
            }

        default: abort();
        }
    }

    buf->pos = p;
    parser->state = state;
    return AGAIN;

done:
    buf->pos = p + 1;
    parser->req_end = p + 1;

    // HTTP 0.9
    if(parser->http_major == 0 && parser->http_minor == 9) {
        req->version = HTTP_9;
    }
    // HTTP 1.0
    if(parser->http_major == 1 && parser->http_minor == 0) {
        req->version = HTTP_10;
    }

    if(req->version == HTTP_9 && req->method != HTTP_GET) {
        return HTTP_INVALID_09_METHOD;
    }

    string_set(&req->host, parser->host_start, parser->host_end);
    string_set(&req->request_line, parser->req_start, parser->req_end);
    req->port = parser->port;

    parser->state = sw_header_start;

    return OK;
}


int http_parse_header_line(HttpParser *parser) {
    assert(parser != NULL);

    Buffer *buf;
    if(parser->is_request_parser) {
        buf = &parser->req->raw->buf;
    } else {
        buf = &parser->res->raw->buf;
    }
    parser->header_name_start = NULL;
    parser->header_name_end = NULL;
    parser->header_val_start = NULL;
    parser->header_val_end = NULL;
    parser->header.name = EMPTY_STRING;
    parser->header.val = EMPTY_STRING;
    unsigned int state = parser->state;

    unsigned char *p; // we need to update buf->pos after parsing
    for (p = buf->pos; p < buf->last; p += 1) {
        char ch = *p;

        switch(state) {
        // first char or end of headers section
        case sw_header_start:
            parser->header_name_start = p;

            if(is_http_token(ch)) {
                state = sw_header_name;
                break;
            }

            switch(ch) {
            case '\r':
                parser->header_name_end = p;
                state = sw_all_headers_almost_done;
                break;
            case '\n':
                parser->header_name_end = p;
                goto all_headers_done;
            default:
                return HTTP_INVALID_HEADER;
            }
            break;

        // header name rfc 1945 section 4.2
        case sw_header_name:
            if(is_http_token(ch)) {
                break;
            }

            if(ch == ':') {
                parser->header_name_end = p;
                state = sw_header_space_before_value;
            } else {
                return HTTP_INVALID_HEADER;
            }
            break;

        // spaces before header value
        case sw_header_space_before_value:
            switch(ch) {
            case ' ':
                break; // ignore spaces
            case '\r':
                state = sw_header_almost_done;
                break;
            case '\n':
                goto done;
            case '\0':
                return HTTP_INVALID_HEADER;
            default:
                parser->header_val_start = p;
                state = sw_header_value;
                break;
            }
            break;

        // header value 
        case sw_header_value:
            switch(ch) {
            case ' ':
                parser->header_val_end = p;
                state = sw_header_space_after_value;
                break;
            case '\r':
                parser->header_val_end = p;
                state = sw_header_almost_done;
                break;
            case '\n':
                parser->header_val_end = p;
                goto done;
            case '\0':
                return HTTP_INVALID_HEADER;
            }
            // skip value chars
            break;

        // spaces before end of header line
        case sw_header_space_after_value:
            switch(ch) {
            case ' ':
                break; // ignore spaces
            case '\r':
                state = sw_header_almost_done;
                break;
            case '\n':
                goto done;
            case '\0':
                return HTTP_INVALID_HEADER;
            default:
                // another value
                state = sw_header_value;
                break;
            }
            break;

        // end of header line 
        case sw_header_almost_done:
            switch(ch) {
            case '\n':
                goto done;
            case '\r':
                break;
            default:
                return HTTP_INVALID_HEADER;
            }
            break;

        // end of all headers
        case sw_all_headers_almost_done:
            switch (ch) {
            case '\n':
                goto all_headers_done;
            default:
                return HTTP_INVALID_HEADER;
            }

        default: abort();
        }
    }

    buf->pos = p;
    parser->state = state;
    return AGAIN;

done:
    buf->pos = p + 1;
    parser->state = sw_header_start;

    HttpHeader *header = &parser->header;
    if(parser->header_name_start != NULL) {
        string_set(&header->name, parser->header_name_start, parser->header_name_end);
    }
    if(parser->header_val_start != NULL) {
        string_set(&header->val, parser->header_val_start, parser->header_val_end);
    }

    return HTTP_MORE_HEADERS;

all_headers_done:
    buf->pos = p + 1;
    return OK;
}


int http_parse_status_line(HttpParser *parser) {
    assert(parser != NULL);
    assert(!parser->is_request_parser);

    HttpResponse *res = parser->res;
    Buffer *buf = &res->raw->buf;
    unsigned int state = sw_res_start;

    unsigned char *p; // we need to update buf->pos after parsing
    for(p = buf->pos; p < buf->last; p += 1) {
        char ch = *p;

        switch(state) {
        // "HTTP/"
        case sw_res_start:
            if(ch == 'H') {
                parser->req_start = p;
                state = sw_res_H;
            } else {
                return HTTP_INVALID_RESPONSE;
            }
            break;
        case sw_res_H:
            if(ch == 'T') {
                state = sw_res_HT;
            } else {
                return HTTP_INVALID_RESPONSE;
            }
            break;
        case sw_res_HT:
            if(ch == 'T') {
                state = sw_res_HTT;
            } else {
                return HTTP_INVALID_RESPONSE;
            }
            break;
        case sw_res_HTT:
            if(ch == 'P') {
                state = sw_res_HTTP;
            } else {
                return HTTP_INVALID_RESPONSE;
            }
            break;
        case sw_res_HTTP:
            if(ch == '/') {
                state = sw_res_first_major_digit;
            } else {
                return HTTP_INVALID_RESPONSE;
            }
            break;

        // major HTTP version
        case sw_res_first_major_digit:
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_major = ch - '0';
            state = sw_res_major_digit;
            break;

        // major HTTP version or dot
        case sw_res_major_digit:
            if(ch == '.') {
                state = sw_res_first_minor_digit;
                break;
            }
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_major = parser->http_major * 10 + (ch - '0');
            if(parser->http_major > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // minor HTTP version
        case sw_res_first_minor_digit:
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_minor = ch - '0';
            state = sw_res_minor_digit;
            break;

        // minor HTTP version or space
        case sw_res_minor_digit:
            if(ch == ' ') {
                state = sw_res_status_code;
                break;
            }
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_minor = parser->http_minor * 10 + (ch - '0');
            if(parser->http_minor > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // HTTP status code
        case sw_res_status_code:
            if(ch == ' ' && parser->status == 0) {
                break;
            }
            if(!is_http_digit(ch)) {
                return HTTP_INVALID_STATUS;
            }
            parser->status = parser->status * 10 + (ch - '0');
            if(parser->status >= 100) {
                state = sw_res_space_after_status_code;
            } else if(parser->status >= 1000) {
                return HTTP_INVALID_STATUS;
            }
            break;

        // space or end of line
        case sw_res_space_after_status_code:
            switch(ch) {
            case ' ':
                state = sw_res_status_text;
                break;
            case '\r':
                state = sw_res_almost_done;
                break;
            case '\n':
                goto done;
            default:
                return HTTP_INVALID_STATUS;
            }
            break;

        // any text until end of line
        case sw_res_status_text:
            switch(ch) {
            case '\r':
                state = sw_res_almost_done;
                break;
            case '\n':
                goto done;
            }
            // skip status text
            break;

        // end of status line
        case sw_res_almost_done:
            if(ch == '\n') {
                goto done;
            } else {
                return HTTP_INVALID_STATUS;
            }
        }
    }

    buf->pos = p;
    parser->state = state;
    return AGAIN;

done:
    buf->pos = p + 1;
    parser->req_end = p + 1;

    if(parser->http_major == 1 && parser->http_minor == 0) {
        res->version = HTTP_10;
    }

    res->status_code = parser->status;

    parser->state = sw_header_start;

    return OK;  
}
