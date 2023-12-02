#include "http.h"


// char classes
#define LOWER(c) (unsigned char)(c | 0x20)
#define IS_ALPHA(c) (LOWER(c) >= 'a' && LOWER(c) <= 'z')
#define NOT_NUM(c) ((c) < '0' || (c) > '9')
#define IS_NUM(c) ((c) >= '0' && (c) <= '9')
#define IS_ALPHANUM(c) (IS_ALPHA(c) || IS_NUM(c))
#define IS_HEX(c) (IS_NUM(c) || (LOWER(c) >= 'a' && LOWER(c) <= 'f'))
#define IS_MARK(c) ((c) == '-' || (c) == '_' || (c) == '.' || (c) == '!' || \
  (c) == '~' || (c) == '*' || (c) == '\'' || (c) == '(' || (c) == ')')


#define HTTP_PARSER_BUFFER_SIZE 8192 


int http_parser_init(http_parser_t *parser, int socket) {
    buffer_t *buf = buffer_create(HTTP_PARSER_BUFFER_SIZE);
    if(!buf) {
        return HTTP_SYSTEM_ERROR;
    }

    parser->buf = buf;
    parser->socket = socket;
    parser->request = NULL;
    parser->response = NULL;

    parser->http_major = 0;
    parser->http_minor = 0;

    parser->status_code = 0;
    parser->status_text_len = 0; 
    
    return 0;
}


enum state { 
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
    sw_req_spaces_before_url,
    sw_req_schema,
    sw_req_schema_slash,
    sw_req_schema_slash_slash,
    sw_req_server_start,
    sw_req_server,
    sw_req_server_with_at,
    sw_req_path,
    sw_req_query_string_start,
    sw_req_query_string,
    sw_req_fragment_start,
    sw_req_fragment,
    sw_req_http_start,
    sw_req_http_H,
    sw_req_http_HT,
    sw_req_http_HTT,
    sw_req_http_HTTP,
    sw_req_http_I,
    sw_req_http_IC,
    sw_req_http_major,
    sw_req_http_dot,
    sw_req_http_minor,
    sw_req_http_end,
    sw_req_line_almost_done,

    sw_header_field_start,
    sw_header_field,
    sw_header_value_discard_ws,
    sw_header_value_discard_ws_almost_done,
    sw_header_value_discard_lws,
    sw_header_value_start,
    sw_header_value,
    sw_header_value_lws,
    sw_header_almost_done,
    sw_chunk_size_start,
    sw_chunk_size,
    sw_chunk_parameters,
    sw_chunk_size_almost_done,
    sw_headers_almost_done,
    sw_headers_done,
    sw_chunk_data,
    sw_chunk_data_almost_done,
    sw_chunk_data_done,
    sw_body_identity,
    sw_body_identity_eof, 
    sw_message_done
};


int http_parse_request_line(http_parser_t *parser) {

}


int http_parse_headers(http_parser_t *parser) {

}


int http_parse_body(http_parser_t *parser) {

}


int http_parse_status_line(http_parser_t *parser) {
    http_response_t *res = malloc(sizeof(http_response_t));
    if(res == NULL) {
        return HTTP_SYSTEM_ERROR;
    }

    parser->response = res;
    unsigned int state = sw_res_start;
    buffer_t *buf = parser->buf;

    while(1) {
        ssize_t nread = buffer_read(buf, parser->socket);
        if(nread == 0) {
            return HTTP_PARSE_INVALID_EOF_STATE;
        } else if(nread < 0) {
            return HTTP_SYSTEM_ERROR;
        }

        for(char *p = buf->pos; p < buf->last; p++) {
            char ch = *p;

            switch(state) {

            // "HTTP/"
            case sw_res_start:
                if(ch == 'H') {
                    state = sw_res_H;
                } else {
                    return HTTP_PARSE_INVALID_RESPONSE;
                }
                break;
            case sw_res_H:
                if(ch == 'T') {
                    state = sw_res_HT;
                } else {
                    return HTTP_PARSE_INVALID_RESPONSE;
                }
                break;
            case sw_res_HT:
                if(ch == 'T') {
                    state = sw_res_HTT;
                } else {
                    return HTTP_PARSE_INVALID_RESPONSE;
                }
                break;
            case sw_res_HTT:
                if(ch == 'P') {
                    state = sw_res_HTTP;
                } else {
                    return HTTP_PARSE_INVALID_RESPONSE;
                }
                break;
            case sw_res_HTTP:
                if(ch == '/') {
                    state = sw_res_first_major_digit;
                } else {
                    return HTTP_PARSE_INVALID_RESPONSE;
                }
                break;

            // major HTTP version
            case sw_res_first_major_digit:
                if(NOT_NUM(ch)) {
                    return HTTP_PARSE_INVALID_VERSION;
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
                if(NOT_NUM(ch)) {
                    return HTTP_PARSE_INVALID_VERSION;
                }
                parser->http_major = parser->http_major * 10 + (ch - '0');
                break;

            // minor HTTP version
            case sw_res_first_minor_digit:
                if(NOT_NUM(ch)) {
                    return HTTP_PARSE_INVALID_VERSION;
                }
                parser->http_minor = ch - '0';
                state = sw_res_minor_digit;
                break;

            // minor HTTP version or the end of the status line
            case sw_res_minor_digit:
                if(ch == ' ') {
                    state = sw_res_status_code;
                    break;
                }
                if(NOT_NUM(ch)) {
                    return HTTP_PARSE_INVALID_VERSION;
                }
                parser->http_minor = parser->http_minor * 10 + (ch - '0');
                break;

            // HTTP status code
            case sw_res_status_code:
                if(ch == ' ') {
                    break;
                }
                if(NOT_NUM(ch)) {
                    return HTTP_PARSE_INVALID_STATUS;
                }
                parser->status_code = parser->status_code * 10 + (ch - '0');
                if(parser->status_code >= 100) {
                    state = sw_res_space_after_status_code;
                } else if(parser->status_code >= 1000) {
                    return HTTP_PARSE_INVALID_STATUS;
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
                    return HTTP_PARSE_INVALID_STATUS;
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
                parser->status_text_len++;
                if(parser->status_text_len >= HTTP_STATUS_MAX_SIZE) {
                    return HTTP_PARSE_INVALID_STATUS;
                }
                break;

            // end of status line
            case sw_res_almost_done:
                switch(ch) {
                case '\n':
                    goto done;
                default:
                    return HTTP_PARSE_INVALID_STATUS;
                }
            }
        }

        done:
        res->version = parser->http_major * 100 + parser->http_minor;
        res->status_code = parser->status_code;

        return 0;
    }    
}
