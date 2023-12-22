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


static int read_response_head(HttpResponse *res) {
    HttpParser parser;
    http_response_parser_init(&parser, res);

    int status = process_status_line(&parser);
    if(status != OK) {
        return status;
    }
    return process_headers(&parser);
}


static int connect_to_upstream(HttpRequest *req, HttpResponse *res) {
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
    ssize_t n = buffer_send(to, buf);
    if(n < 0) {
        return n;
    }
    size_t len = cont_len - read_body_size;
    while(len > 0) {
        n = buffer_recv(from, buf);
        if(n < 0) {
            return n;
        }
        n = buffer_send(to, buf);
        if(n < 0) {
            return n;
        }
        len -= n;
    }
    return OK;
}


static int transfer_until_eos(int from, int to, Buffer* buf) {
    size_t read_body_size = buf->last - buf->pos;
    ssize_t n = buffer_send(to, buf);
    if(n < 0) {
        return n;
    }
    bool eos = false;
    while(!eos) {
        n = buffer_recv(from, buf);
        if(n < 0) {
            if(n == END_OF_STREAM) {
                eos = true;
            } else {
                return n;
            }
        }
        n = buffer_send(to, buf);
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

    int status = read_response_head(res);
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


HttpState http_process_request(HttpRequest *req) {
    assert(req != NULL);

    HttpResponse *res = http_response_create();
    if(res == NULL) {
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        return HTTP_TERMINATE_REQUEST;
    }

    int status = connect_to_upstream(req, res);
    if(status != OK) {
        http_response_destroy(res);
        return handle_req_process_err(req, status, true);
    }
    status = send_request(req, res->sock);
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
