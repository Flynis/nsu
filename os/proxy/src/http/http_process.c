#include "http_process.h"


#include <assert.h>
#include <stdbool.h>
#include <string.h>
#include <sys/types.h>


#include "core/inet_limits.h"
#include "core/socket.h"
#include "core/status.h"
#include "core/str.h"
#include "http.h"
#include "http_util.h"


#define IO_BUFFER_SIZE 8192
#define RESPONSE_MAX_SIZE 1024 * 1024 // 1 mb


typedef int (*RecvCallback)(HttpParser *parser);


void http_terminate_request(HttpRequest *req) {
    char response[256];
    print_error_response(response, req->status);
    sock_send(req->sock, (unsigned char*)response, strlen(response));
}


static int parse_on_recv(int sock, Buffer *buf, HttpParser *parser, RecvCallback callback) {
    while(1) {
        size_t remaining = buf->last - buf->pos;
        if(remaining == 0) {
            // fill buffer from connection
            size_t free_space = buf->end - buf->last;
            if(free_space == 0) {
                return FULL;
            }

            ssize_t nread = sock_recv(sock, buf->last, free_space);
            if(nread < 0) {
                return IO;
            }
            buf->last += nread;
        }

        int status = callback(parser);
        if(status == OK) {
            return OK;
        }

        if(status != AGAIN) {
            // some parse error occurred
            return status;
        }
    }
    return ERROR; // unreachable
}


static int process_request_line(HttpParser *parser) {
    HttpRequest *req = parser->req;
    Buffer *head = &req->raw->buf;

    int status = parse_on_recv(req->sock, head, parser, http_parse_request_line);
    if(status == IO || status == FULL) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return ERROR;
    }
    if(status != OK) {
        // some parse error occurred
        req->status = HTTP_BAD_REQUEST;
        return ERROR;
    }

    // analyse request line
    if(req->version == HTTP_NOT_SUPPORTED_VERSION) {
        req->status = HTTP_NOT_IMPLEMENTED;
        return ERROR;
    }
    size_t hostlen = req->host.len;
    if(hostlen == 0 || hostlen >= DOMAIN_NAME_STR_LEN) {
        req->status = HTTP_BAD_REQUEST;
        return ERROR;
    }

    return OK;
}


static int process_request_headers(HttpParser *parser) {
    HttpRequest *req = parser->req;
    Buffer *head = &req->raw->buf;

    while(1) {
        int status = parse_on_recv(req->sock, head, parser, http_parse_header_line);
        if(status == OK) {
            break; // successfully parsed all headers
        }
        if(status == IO || status == FULL) {
            req->status = HTTP_INTERNAL_SERVER_ERROR;
            return ERROR;
        }
        if(status == HTTP_MORE_HEADERS) {
            // analyse parsed header
            HttpHeader *header = &parser->header;
            if(string_equal_chararray(header->name, "Content-Length")) {
                long content_len;
                status = string_to_long(header->val, &content_len);
                if(status != OK) {
                    // parse content length failed
                    req->status = HTTP_BAD_REQUEST;
                    return ERROR;
                }
                if(content_len > 0) {
                    req->content_length = content_len;
                    req->is_content_len_set = true;
                }
            }
            // continue parsing headers
            continue;
        }
        if(status != OK) {
            // some parse error occurred
            req->status = HTTP_BAD_REQUEST;
            return ERROR;
        }
    }

    // analyse headers
    if(req->method == HTTP_GET || req->method == HTTP_HEAD) {
        size_t remaining = head->last - head->pos;
        if(req->is_content_len_set || remaining != 0) {
            req->status = HTTP_BAD_REQUEST;
            return ERROR;
        }
    }
    if(req->method == HTTP_POST && !req->is_content_len_set) {
        req->status = HTTP_BAD_REQUEST;
        return ERROR;
    }
    return OK;
}


HttpState http_read_request_head(HttpRequest *req) {
    assert(req != NULL);

    HttpParser parser;
    http_request_parser_init(&parser, req);

    int status = process_request_line(&parser);
    if(status != OK) {
        http_terminate_request(req);
        return HTTP_TERMINATE_REQUEST;
    }
    status = process_request_headers(&parser);
    if(status != OK) {
        http_terminate_request(req);
        return HTTP_TERMINATE_REQUEST;
    }

    return HTTP_PROCESS_REQUEST;
}


static int read_response_head(HttpResponse *res, HttpRequest *req) {
    HttpParser parser;
    Buffer *head = &res->raw->buf;
    http_response_parser_init(&parser, res);

    int status = parse_on_recv(res->sock, head, &parser, http_parse_status_line);
    if(status == IO || status == FULL) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return ERROR;
    }
    if(status != OK) {
        // some parse error occurred
        req->status = HTTP_BAD_GATEWAY;
        return ERROR;
    }

    while(1) {
        status = parse_on_recv(res->sock, head, &parser, http_parse_header_line);
        if(status == OK) {
            break; // successfully parsed all headers
        }
        if(status == IO || status == FULL) {
            req->status = HTTP_INTERNAL_SERVER_ERROR;
            return ERROR;
        }
        if(status == HTTP_MORE_HEADERS) {
            // analyse parsed header
            HttpHeader *header = &parser.header;
            if(string_equal_chararray(header->name, "Content-Length")) {
                long content_len;
                status = string_to_long(header->val, &content_len);
                if(status != OK) {
                    // parse content length failed
                    req->status = HTTP_BAD_GATEWAY;
                    return ERROR;
                }
                if(content_len > 0) {
                    res->content_length = content_len;
                    res->is_content_len_set = true;
                }
            }
            // continue parsing headers
            continue;
        }
        if(status != OK) {
            // some parse error occurred
            req->status = HTTP_BAD_REQUEST;
            return ERROR;
        }
    }
    return OK;
}


static int connect_to_upstream(HttpRequest *req, HttpResponse *res) {
    // make null-terminated string
    char host[DOMAIN_NAME_STR_LEN];
    memcpy(host, req->host.data, req->host.len);
    host[req->host.len] = '\0';

    res->sock = open_and_connect_socket(host, req->port);
    if(res->sock == IO) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return ERROR;
    }
    if(res->sock == UNKNOWN_HOST) {
        req->status = HTTP_NOT_FOUND;
        return ERROR;
    }

    return OK;
}


static int transfer_body(int from, int to, size_t content_len, Buffer *head, bool wait_eof) {
    unsigned char body[IO_BUFFER_SIZE];
    ssize_t remainig = head->last - head->pos;
    memcpy(body, head->pos, remainig);
    size_t len = content_len;
    while(1) {
        if(remainig != 0) {
            ssize_t n = sock_send(to, body, remainig);
            if(n == IO) {
                return IO;
            }
            len -= n;
        }
        if(!wait_eof && len == 0) {
            // successfully sent body
            break;
        }
        // recv another body part
        remainig = sock_recv(from, body, IO_BUFFER_SIZE);
        if(remainig > 0) {
            continue;
        }
        if(remainig == END_OF_STREAM && wait_eof) {
            break;
        } else {
            return remainig;
        }
    }
    return OK;
}


static int send_request(HttpRequest *req, int upstream) {
    int sock = req->sock;
    Buffer *head = &req->raw->buf;

    // send request header
    ssize_t n = sock_send(upstream, head->start, head->pos - head->start);
    if(n == IO) {
        return IO;
    }

    // transfer request body
    if(req->is_content_len_set) {
        return transfer_body(sock, upstream, req->content_length, head, false);
    }
    return OK;
}


static int transfer_response(HttpRequest *req, HttpResponse *res) {
    Buffer *head = &res->raw->buf;
    if(req->version == HTTP_9) {
        return transfer_body(res->sock, req->sock, res->content_length, head, true);
    }

    int status = read_response_head(res, req);
    if(status != OK) {
        return status;
    }
    
    // send response header
    ssize_t n = sock_send(req->sock, head->start, head->pos - head->start);
    if(n == IO) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return ERROR;
    }

    if(res->is_content_len_set) {
        status = transfer_body(res->sock, req->sock, res->content_length, head, false);
    } else {
        // wait EOF
        status = transfer_body(res->sock, req->sock, res->content_length, head, true);
    }
    return status;
}


HttpState http_process_request(HttpRequest *req) {
    assert(req != NULL);

    HttpResponse *res = http_response_create();
    if(res == NULL) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return HTTP_TERMINATE_REQUEST;
    }

    HttpStatus status = connect_to_upstream(req, res);
    if(status != OK) {
        goto fail;
    }
    status = send_request(req, res->sock);
    if(status != OK) {
        goto fail;
    }
    status = transfer_response(req, res);
    if(status != OK) {
        goto fail;
    }

    http_response_destroy(res);
    return HTTP_CLOSE_REQUEST;

fail:
    if(req->status == HTTP_OK) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
    }
    http_response_destroy(res);
    return HTTP_TERMINATE_REQUEST;
}


static int read_response_body_eof(HttpResponse *res) {
    Chain *head = res->raw;
    size_t res_size = head->buf.last - head->buf.start; 
    Chain *cur = head;
    while(res_size < RESPONSE_MAX_SIZE) {
        Buffer *buf = &cur->buf;
        size_t free_space = buf->end - buf->last;
        if(free_space == 0) {
            Buffer *ret = chain_alloc_next_buf(cur, IO_BUFFER_SIZE);
            if(ret == NULL) {
                return ERROR;
            }
            cur = cur->next;
            continue;
        }

        ssize_t nread = sock_recv(res->sock, buf->last, free_space);
        if(nread > 0) {
            buf->last += nread;
            res_size += nread;
            continue;
        }
        if(nread == END_OF_STREAM) {
            return OK;
        } else {
            return ERROR;
        }
    }
    // too large response
    return ERROR;
}


static int transfer_response_chain_eof(int from, int to, Chain *chain) {
    Chain *cur = chain;
    while(cur != NULL) {
        Buffer *buf = &cur->buf;
        size_t len = buf->last - buf->start;
        ssize_t n = sock_send(to, buf->start, len);
        if(n == IO) {
            return ERROR;
        }
        cur = cur->next;
    }
    
    Buffer *buf = &chain->buf;
    size_t len = buf->end - buf->start;
    while(1) {
        ssize_t nread = sock_recv(from, buf->start, len);
        if(nread < 0) {
            if(nread == END_OF_STREAM) {
                return OK;
            } else {
                return ERROR;
            }
        }
        ssize_t nsend = sock_send(to, buf->start, nread);
        if(nsend == IO) {
            return IO;
        }        
    }
    return ERROR; // unreachable
}


static int read_response_content(HttpResponse *res) {
    size_t content_len = res->content_length;
    Buffer *buf = &res->raw->buf;
    size_t size = buf->end - buf->last;
    if(size > content_len) {
        size = content_len;
    }
    while(size > 0) {
        ssize_t nread = sock_recv(res->sock, buf->last, size);
        if(nread < 0) {
            return ERROR;
        }
        buf->last += nread;
        size -= nread;
        content_len -= nread;        
    }
    if(content_len == 0) {
        return OK;
    }
    buf = chain_alloc_next_buf(res->raw, content_len);
    if(buf == NULL) {
        return ERROR;
    }
    while(content_len > 0) {
        ssize_t nread = sock_recv(res->sock, buf->last, content_len);
        if(nread < 0) {
            return ERROR;
        }
        buf->last += nread;
        content_len -= nread;        
    }
    return OK;
}


static int read_response(HttpRequest *req, HttpResponse *res) {
    int status;
    if(req->version == HTTP_9) {
        status = read_response_body_eof(res);
        if(status == OK) {
            return OK;
        } else {
            return transfer_response_chain_eof(res->sock, req->sock, res->raw);
        }
    }

    status = read_response_head(res, req);
    if(status != OK) {
        return status;
    }

    if(res->is_content_len_set) {
        status = read_response_content(res);
    } else {
        status = read_response_body_eof(res);
        if(status == OK) {
            return OK;
        } else {
            return transfer_response_chain_eof(res->sock, req->sock, res->raw);
        }
    }
    return status;
}


HttpState http_load_response(HttpRequest *req, HttpResponse **out_res) {
    assert(req != NULL);
    assert(out_res != NULL);

    HttpResponse *res = http_response_create();
    if(res == NULL) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return HTTP_TERMINATE_REQUEST;
    }

    HttpStatus status = connect_to_upstream(req, res);
    if(status != OK) {
        goto fail;
    }
    status = send_request(req, res->sock);
    if(status != OK) {
        goto fail;
    }
    status = read_response(req, res);
    if(status != OK) {
        goto fail;
    }

    res->recv_time = time(NULL);
    *out_res = res;
    return HTTP_PROCESS_REQUEST;

fail:
    if(req->status == HTTP_OK) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
    }
    http_response_destroy(res);
    return HTTP_TERMINATE_REQUEST;
}


void http_send_response(HttpRequest *req, HttpResponse *res) {
    assert(req != NULL);
    assert(res != NULL);

    Chain *cur = res->raw;
    while(cur != NULL) {
        Buffer *buf = &cur->buf;
        size_t len = buf->last - buf->start;
        ssize_t n = sock_send(req->sock, buf->start, len);
        if(n == IO) {
            return;
        }
        cur = cur->next;
    }
}
