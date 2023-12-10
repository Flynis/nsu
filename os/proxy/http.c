#include "http.h"


#include "util.h"


/* Tokens as defined by rfc 1945.
 *        token       = 1*<any CHAR except CTLs or tspecials>
 *     tspecials      = "(" | ")" | "<" | ">" | "@"
 *                    | "," | ";" | ":" | "\" | <">
 *                    | "/" | "[" | "]" | "?" | "="
 *                    | "{" | "}" | SP  | HT
 */
static const char tokens[256] = {
/*   0 nul    1 soh    2 stx    3 etx    4 eot    5 enq    6 ack    7 bel  */
        0,       0,       0,       0,       0,       0,       0,       0,
/*   8 bs     9 ht    10 nl    11 vt    12 np    13 cr    14 so    15 si   */
        0,       0,       0,       0,       0,       0,       0,       0,
/*  16 dle   17 dc1   18 dc2   19 dc3   20 dc4   21 nak   22 syn   23 etb */
        0,       0,       0,       0,       0,       0,       0,       0,
/*  24 can   25 em    26 sub   27 esc   28 fs    29 gs    30 rs    31 us  */
        0,       0,       0,       0,       0,       0,       0,       0,
/*  32 sp    33  !    34  "    35  #    36  $    37  %    38  &    39  '  */
       ' ',     '!',      0,      '#',     '$',     '%',     '&',    '\'',
/*  40  (    41  )    42  *    43  +    44  ,    45  -    46  .    47  /  */
        0,       0,      '*',     '+',      0,      '-',     '.',      0,
/*  48  0    49  1    50  2    51  3    52  4    53  5    54  6    55  7  */
       '0',     '1',     '2',     '3',     '4',     '5',     '6',     '7',
/*  56  8    57  9    58  :    59  ;    60  <    61  =    62  >    63  ?  */
       '8',     '9',      0,       0,       0,       0,       0,       0,
/*  64  @    65  A    66  B    67  C    68  D    69  E    70  F    71  G  */
        0,      'a',     'b',     'c',     'd',     'e',     'f',     'g',
/*  72  H    73  I    74  J    75  K    76  L    77  M    78  N    79  O  */
       'h',     'i',     'j',     'k',     'l',     'm',     'n',     'o',
/*  80  P    81  Q    82  R    83  S    84  T    85  U    86  V    87  W  */
       'p',     'q',     'r',     's',     't',     'u',     'v',     'w',
/*  88  X    89  Y    90  Z    91  [    92  \    93  ]    94  ^    95  _  */
       'x',     'y',     'z',      0,       0,       0,      '^',     '_',
/*  96  `    97  a    98  b    99  c   100  d   101  e   102  f   103  g  */
       '`',     'a',     'b',     'c',     'd',     'e',     'f',     'g',
/* 104  h   105  i   106  j   107  k   108  l   109  m   110  n   111  o  */
       'h',     'i',     'j',     'k',     'l',     'm',     'n',     'o',
/* 112  p   113  q   114  r   115  s   116  t   117  u   118  v   119  w  */
       'p',     'q',     'r',     's',     't',     'u',     'v',     'w',
/* 120  x   121  y   122  z   123  {   124  |   125  }   126  ~   127 del */
       'x',     'y',     'z',      0,      '|',      0,      '~',      0 };


// char classes
#define LOWER(c) (unsigned char)(c | 0x20)
#define NOT_TOKEN(c) (!tokens[(unsigned char) c])
#define IS_TOKEN(c) (tokens[(unsigned char) c])
#define NOT_ALPHA(c) (LOWER(c) < 'a' || LOWER(c) > 'z')
#define IS_ALPHA(c) (LOWER(c) >= 'a' && LOWER(c) <= 'z')
#define NOT_DIGIT(c) ((c) < '0' || (c) > '9')
#define IS_DIGIT(c) ((c) >= '0' && (c) <= '9')


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
    sw_all_headers_almost_done,
    sw_header_invalid
};


int http_request_parser_init(http_parser_t *parser, http_request_t *request) {
    parser->state = sw_req_method;

    parser->is_request_parser = true;
    parser->request = request;
    parser->response = NULL;

    parser->http_major = 0;
    parser->http_minor = 0;
    
    parser->method_len = 0;
    parser->url_len = 0;
    parser->host_len = 0;
    parser->headers_len = 0;

    return 0;
}


int http_response_parser_init(http_parser_t *parser, http_response_t *response) {
    parser->state = sw_res_start;
    
    parser->is_request_parser = false;
    parser->request = NULL;
    parser->response = response;

    parser->http_major = 0;
    parser->http_minor = 0;

    parser->status_text_len = 0; 
    
    return 0;
}


int http_parse_request_line(http_parser_t *parser, buffer_t *buf) {
    http_request_t *req = parser->request;
    unsigned int state = parser->state;

    char *p;
    for(p = buf->pos; p < buf->last; p++) {
        char ch = *p;

        switch(state) {

        // HTTP methods: GET, HEAD, POST
        case sw_req_method:
            if (ch == ' ') {
                req->method_name[parser->method_len] = '\0';
                req->method = HTTP_UNKNOWN;

                // determine method
                switch(parser->method_len) {
                case 3:
                    if(streq(req->method_name, "GET")) {
                        req->method = HTTP_GET;
                    }
                    break;
                case 4:
                    if(req->method_name[1] == 'O') {
                        if(streq(req->method_name, "POST")) {
                            req->method = HTTP_POST;
                        }
                    } else {
                        if(streq(req->method_name, "HEAD")) {
                            req->method = HTTP_HEAD;
                        }
                    }
                    break;
                }

                state = sw_req_schema;
                break;
            }

            if(NOT_TOKEN(ch)) {
                return HTTP_INVALID_METHOD;
            }

            req->method_name[parser->method_len] = ch;
            parser->method_len++;
            if(parser->method_len >= HTTP_METHOD_MAX_LEN) {
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
        
        // legal host domain name or IP address [rfc 1123, rfc 952] 
        case sw_req_host_start:
            if(IS_DIGIT(ch) || IS_ALPHA(ch)) {
                req->port = 0;
                req->host[parser->host_len] = ch;
                parser->host_len++;
                req->url[parser->url_len] = ch;
                parser->url_len++;

                if(IS_DIGIT(ch)) {
                    req->is_host_domain_name = false;
                } else {
                    req->is_host_domain_name = true;
                }
                state = sw_req_host;
            } else {
                return HTTP_INVALID_REQUEST;
            }
            break;
        case sw_req_host:
            if(req->is_host_domain_name && (IS_ALPHA(ch) || IS_DIGIT(ch) || ch == '.' || ch == '-') 
                || !req->is_host_domain_name && (IS_DIGIT(ch) || ch == '.')) {
                
                req->host[parser->host_len] = ch;
                parser->host_len++;
                if(parser->host_len >= HTTP_HOST_MAX_LEN) {
                    return HTTP_INVALID_REQUEST;
                }
                req->url[parser->url_len] = ch;
                parser->url_len++;
                break;
            } 
            switch(ch) {
            case ':':
                state = sw_req_port;
                break;
            case '/':
                req->url[parser->url_len] = ch;
                parser->url_len++;
                state = sw_req_after_slash_in_uri;
                break;
            case ' ':
                req->url[parser->url_len] = '\0';
                state = sw_req_09;
                break;
            default:
                return HTTP_INVALID_REQUEST;
            }
            // end host name
            req->host[parser->host_len] = '\0';
            break;
        case sw_req_port:
            if(IS_DIGIT(ch)) {
                req->port = req->port * 10 + (ch - '0');
                if(req->port > 65535) {
                    return HTTP_INVALID_REQUEST;
                }
                break;
            }
            switch(ch) {
            case '/':
                req->url[parser->url_len] = ch;
                parser->url_len++;
                state = sw_req_after_slash_in_uri;
                break;
            case ' ':
                req->url[parser->url_len] = '\0';
                state = sw_req_09;
                break;
            default:
                return HTTP_INVALID_REQUEST;
            }
            break;
            
        // just copy uri
        case sw_req_after_slash_in_uri:
            switch(ch) {
            case ' ':
                req->url[parser->url_len] = '\0';
                state = sw_req_09;
                break;
            case '\r':
                req->url[parser->url_len] = '\0';
                parser->http_minor = 9;
                state = sw_req_almost_done;
                break;
            case '\n':
                req->url[parser->url_len] = '\0';
                parser->http_minor = 9;
                goto done;
            }

            req->url[parser->url_len] = ch;
            parser->url_len++;
            if(parser->url_len >= HTTP_URL_MAX_LEN) {
                return HTTP_INVALID_REQUEST;
            }
            break;

        // space after uri
        case sw_req_09:
            switch(ch) {
            case ' ':
                break; // ignore spaces
            case '\r':
                parser->http_minor = 9;
                state = sw_req_almost_done;
                break;
            case '\n':
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
            if(NOT_DIGIT(ch)) {
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
            if(NOT_DIGIT(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_major = parser->http_major * 10 + (ch - '0');
            if(parser->http_major > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // minor HTTP version
        case sw_req_first_minor_digit:
            if(NOT_DIGIT(ch)) {
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
            if(NOT_DIGIT(ch)) {
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
        }
    }

    buf->pos = p;
    parser->state = state;
    return HTTP_AGAIN;

done:
    buf->pos = p + 1;
    req->version = parser->http_major * 1000 + parser->http_minor;
    parser->state = sw_header_start;

    if(req->version == 9 && req->method != HTTP_GET) {
        return HTTP_INVALID_09_METHOD;
    }

    return 0;
}


static unsigned int append_headers(char *headers, char ch, unsigned int state, http_parser_t *parser) {
    *headers = ch;
    headers++;
    parser->headers_len++;
    if(parser->headers_len >= HTTP_HEADERS_MAX_LEN) {
        return sw_header_invalid;
    } else {
        return state;
    }
}


int http_parse_header_line(http_parser_t *parser, buffer_t *buf) {
    http_request_t *req = parser->request;
    char *headers;
    if(parser->is_request_parser) {
        headers = parser->request->headers;
    } else {
        headers = parser->response->headers;
    }
    headers += parser->headers_len;
    unsigned int state = parser->state;

    char *p;
    for (p = buf->pos; p < buf->last; p++) {
        char ch = *p;

        switch(state) {

        // first char or end of headers section
        case sw_header_start:
            parser->header_name_start = headers;

            if(IS_TOKEN(ch)) {
                *headers = ch;
                headers++;
                parser->headers_len++;
                state = sw_header_name;
                break;
            }

            switch(ch) {
            case '\r':
                parser->header_name_end = headers;
                state = sw_all_headers_almost_done;
                break;
            case '\n':
                parser->header_name_end = headers;
                goto all_headers_done;
            default:
                return HTTP_INVALID_HEADER;
            }
            break;

        // header name 
        case sw_header_name:
            if(IS_TOKEN(ch)) {
                // copy name in headers
                state = append_headers(headers, ch, state, parser);
                break;
            }
            if(ch == ':') {
                parser->header_name_end = headers;
                state = sw_header_space_before_value;
                state = append_headers(headers, ch, state, parser);
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
                parser->header_val_start = headers;
                parser->header_val_end = headers;
                state = sw_header_almost_done;
                break;
            case '\n':
                parser->header_val_start = headers;
                parser->header_val_end = headers;
                goto done;
            case '\0':
                return HTTP_INVALID_HEADER;
            default:
                parser->header_val_start = headers;
                state = sw_header_value;
                state = append_headers(headers, ch, state, parser);
                break;
            }
            break;

        // header value 
        case sw_header_value:
            switch(ch) {
            case ' ':
                parser->header_val_end = headers;
                state = sw_header_space_after_value;
                state = append_headers(headers, ch, state, parser);
                break;
            case '\r':
                parser->header_val_end = headers;
                state = sw_header_almost_done;
                break;
            case '\n':
                parser->header_val_end = headers;
                goto done;
            case '\0':
                return HTTP_INVALID_HEADER;
            }
            // copy value in headers
            state = append_headers(headers, ch, state, parser);
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
                state = append_headers(headers, ch, state, parser);
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
        }
    }

    buf->pos = p;
    parser->state = state;
    return HTTP_AGAIN;

done:
    buf->pos = p + 1;
    parser->state = sw_header_start;
    return HTTP_MORE_HEADERS;

all_headers_done:
    buf->pos = p + 1;
    parser->state = (parser->is_request_parser) ? sw_req_method : sw_res_start;
    return 0;
}


int http_parse_status_line(http_parser_t *parser, buffer_t *buf) {
    http_response_t *res = parser->response;
    unsigned int state = sw_res_start;

    char *p;
    for(p = buf->pos; p < buf->last; p++) {
        char ch = *p;

        switch(state) {

        // "HTTP/"
        case sw_res_start:
            if(ch == 'H') {
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
            if(NOT_DIGIT(ch)) {
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
            if(NOT_DIGIT(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_major = parser->http_major * 10 + (ch - '0');
            if(parser->http_major > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // minor HTTP version
        case sw_res_first_minor_digit:
            if(NOT_DIGIT(ch)) {
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
            if(NOT_DIGIT(ch)) {
                return HTTP_INVALID_VERSION;
            }
            parser->http_minor = parser->http_minor * 10 + (ch - '0');
            if(parser->http_minor > 99) {
                return HTTP_INVALID_VERSION;
            }
            break;

        // HTTP status code
        case sw_res_status_code:
            if(ch == ' ' && res->status_code == 0) {
                break;
            }
            if(NOT_DIGIT(ch)) {
                return HTTP_INVALID_STATUS;
            }
            res->status_code = res->status_code * 10 + (ch - '0');
            if(res->status_code >= 100) {
                state = sw_res_space_after_status_code;
            } else if(res->status_code >= 1000) {
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
            parser->status_text_len++;
            if(parser->status_text_len >= HTTP_STATUS_MAX_LEN) {
                return HTTP_INVALID_STATUS;
            }
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
    return HTTP_AGAIN;

done:
    buf->pos = p + 1;
    res->version = parser->http_major * 1000 + parser->http_minor;
    parser->state = sw_header_start;

    return 0;  
}
