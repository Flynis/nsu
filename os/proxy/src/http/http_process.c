#include "http_process.h"


#include <assert.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>


#include "core/inet_limits.h"
#include "core/socket.h"
#include "core/status.h"
#include "core/str.h"
#include "http.h"
#include "http_util.h"


#define RESPONSE_MAX_SIZE 1024 * 1024 // 1 mb


void http_terminate_request(HttpRequest *req) {
    char response[256];
    print_error_response(response, req->status);
    sock_send(req->sock, (unsigned char*)response, strlen(response));
}


static int process_request_line(HttpParser *parser) {
    HttpRequest *req = parser->req;
    int sock = req->sock;
    Buffer *buf = req->raw;

    while(1) {
        size_t remaining = buf->last - buf->pos;
        if(remaining == 0) {
            // fill buffer from connection
            ssize_t n = buffer_recv(sock, buf);
            if(n < 0) {
                return n;
            }
        }

        int status = http_parse_request_line(parser);
        if(status == OK) {
            // request line parsed successfully 

            // analyse request line
            if(req->version == HTTP_NOT_SUPPORTED_VERSION) {
                req->status = HTTP_NOT_IMPLEMENTED;
                return ERROR;
            }
            size_t hostlen = req->host.len;
            if(hostlen == 0 || hostlen >= DOMAIN_NAME_STR_LEN) {
                return ERROR;
            }
            
            // successful analysis
            return OK;
        }

        if(status != AGAIN) {
            // some parse error occurred
            return ERROR;
        }
    }
    return ERROR; // unreachable
}


static int process_headers(HttpParser *parser) {
    int sock;
    Buffer *buf;
    HttpRequest *req = parser->req;
    HttpResponse *res = parser->res;
    if(parser->is_request_parser) {    
        sock = req->sock;
        buf = req->raw;
    } else {
        sock = res->sock;
        buf = res->raw;
    }

    while(1) {
        size_t remaining = buf->last - buf->pos;
        if(remaining == 0) {
            // fill buffer from connection
            ssize_t n = buffer_recv(sock, buf);
            if(n < 0) {
                return n;
            }
        }

        int status = http_parse_header_line(parser);
        if(status == OK) {
            // all headers parsed successfully
            return OK;
        }

        if(status == HTTP_MORE_HEADERS) {
            // header parsed successfully
            HttpHeader *header = &parser->header;
            if(string_equal_chararray(header->name, "Content-Length")) {
                long content_len;
                status = string_to_long(header->val, &content_len);
                if(status != OK) {
                    // parse content length failed
                    return ERROR;
                }
                if(content_len > 0) {
                    if(parser->is_request_parser) {
                        req->content_length = content_len;
                        req->is_content_len_set = true;
                    } else {
                        res->content_length = content_len;
                        res->is_content_len_set = true;
                    }
                }
            }
            // parse next header line
            continue;
        }

        if(status != AGAIN) {
            // some parse error occurred
            return ERROR;
        }
    }
    return ERROR; // unreachable
}


static int process_status_line(HttpParser *parser) {
    HttpResponse *res = parser->res;
    int sock = res->sock;
    Buffer *buf = res->raw;

    while(1) {
        size_t remaining = buf->last - buf->pos;
        if(remaining == 0) {
            // fill buffer from connection
            ssize_t n = buffer_recv(sock, buf);
            if(n < 0) {
                return n;
            }
        }

        int status = http_parse_status_line(parser);
        if(status == OK) {
            // status line parsed successfully
            return OK;
        }

        if(status != AGAIN) {
            // some parse error occurred
            return ERROR;
        }
    }
    return ERROR; // unreachable
}


static HttpState handle_req_read_err(HttpRequest *req, int status) {
    switch (status)
    {
    case ERROR:
        if(req->status == HTTP_OK) {
            req->status = HTTP_BAD_REQUEST;
        }
        return HTTP_TERMINATE_REQUEST;            
    case IO:
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return HTTP_TERMINATE_REQUEST;            
    case END_OF_STREAM:
        return HTTP_CLOSE_REQUEST;
    default: abort();
    }
}


HttpState http_read_request_head(HttpRequest *req) {
    assert(req != NULL);

    HttpParser parser;
    http_request_parser_init(&parser, req);

    int status = process_request_line(&parser);
    if(status != OK) {
        return handle_req_read_err(req, status);
    }
    status = process_headers(&parser);
    if(status != OK) {
        return handle_req_read_err(req, status);;
    }

    // analyse headers
    if(req->method == HTTP_GET || req->method == HTTP_HEAD) {
        Buffer *head = req->raw;
        size_t remaining = head->last - head->pos;
        if(req->is_content_len_set || remaining != 0) {
            req->status = HTTP_BAD_REQUEST;
            return HTTP_TERMINATE_REQUEST;
        }
    }
    if(req->method == HTTP_POST && !req->is_content_len_set) {
        req->status = HTTP_BAD_REQUEST;
        return HTTP_TERMINATE_REQUEST;
    }
    // successfully read request head
    return HTTP_PROCESS_REQUEST;
}


int http_read_response_head(HttpResponse *res) {
    HttpParser parser;
    http_response_parser_init(&parser, res);

    int status = process_status_line(&parser);
    if(status != OK) {
        return status;
    }
    if(res->version != HTTP_10) {
        return ERROR;
    }
    return process_headers(&parser);
}


int http_connect_to_upstream(HttpRequest *req, HttpResponse *res) {
    // make null-terminated string
    char host[DOMAIN_NAME_STR_LEN];
    memcpy(host, req->host.data, req->host.len);
    host[req->host.len] = '\0';

    res->sock = open_and_connect_socket(host, req->port);
    if(res->sock < 0) {
        return res->sock;
    }
    return OK;
}


static int transfer_content_len(int from, int to, Buffer *buf, size_t cont_len) {
    size_t read_body_size = buf->last - buf->pos;
    ssize_t n = buffer_send_all(to, buf);
    if(n < 0) {
        return n;
    }
    size_t len = cont_len - read_body_size;
    while(len > 0) {
        buffer_clear(buf);
        n = buffer_recv(from, buf);
        if(n < 0) {
            return n;
        }
        n = buffer_send_all(to, buf);
        if(n < 0) {
            return n;
        }
        len -= n;
    }
    return OK;
}


static int transfer_until_eos(int from, int to, Buffer* buf) {
    size_t read_body_size = buf->last - buf->pos;
    ssize_t n = buffer_send_all(to, buf);
    if(n < 0) {
        return n;
    }
    bool eos = false;
    while(!eos) {
        buffer_clear(buf);
        n = buffer_recv(from, buf);
        if(n < 0) {
            if(n == END_OF_STREAM) {
                eos = true;
            } else {
                return n;
            }
        }
        n = buffer_send_all(to, buf);
        if(n < 0) {
            return n;
        }
    }
    return OK;
}


static int transfer_response(HttpRequest *req, HttpResponse *res) {
    Buffer *buf = res->raw;
    if(req->version == HTTP_9) {
        return transfer_until_eos(res->sock, req->sock, buf);
    }

    int status = http_read_response_head(res);
    if(status != OK) {
        return status;
    }

    if(req->method == HTTP_HEAD) {
        return OK;
    }

    if(res->is_content_len_set) {
        return transfer_content_len(res->sock, req->sock, buf, res->content_length);
    } else {
        return transfer_until_eos(res->sock, req->sock, buf);
    }
}


static HttpState handle_req_process_err(HttpRequest *req, int status, bool upstream) {
    switch (status)
    {
    case ERROR:
        req->status = HTTP_BAD_GATEWAY;
        return HTTP_TERMINATE_REQUEST;            
    case IO:
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return HTTP_TERMINATE_REQUEST;            
    case END_OF_STREAM:
        return HTTP_CLOSE_REQUEST;
    case CONN_RESET:
        if(upstream) {
            req->status = HTTP_BAD_GATEWAY;
            return HTTP_TERMINATE_REQUEST;
        } else {
            return HTTP_CLOSE_REQUEST;
        }
    case UNKNOWN_HOST:
        req->status = HTTP_NOT_FOUND;
        return HTTP_TERMINATE_REQUEST;
    default: abort();
    }
}


int http_send_request(HttpRequest *req, int dest) {
    return transfer_content_len(req->sock, dest, req->raw, req->content_length);
}


HttpState http_process_request(HttpRequest *req) {
    assert(req != NULL);

    HttpResponse *res = http_response_create();
    if(res == NULL) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return HTTP_TERMINATE_REQUEST;
    }

    int status = http_connect_to_upstream(req, res);
    if(status != OK) {
        http_response_destroy(res);
        return handle_req_process_err(req, status, true);
    }
    status = http_send_request(req, res->sock);
    if(status != OK) {
        http_response_destroy(res);
        return handle_req_process_err(req, status, true);
    }
    status = transfer_response(req, res);
    if(status != OK) {
        http_response_destroy(res);
        return handle_req_process_err(req, status, false);
    }

    http_response_destroy(res);
    return HTTP_CLOSE_REQUEST;
}
