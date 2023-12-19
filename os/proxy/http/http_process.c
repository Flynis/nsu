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


static ssize_t fill_buffer(int sock, Buffer *buf) {
    ssize_t n = buffer_remaining(buf);
    if(n > 0) {
        return n;
    }
    return buffer_recv(sock, buf);
}


static HttpStatus process_request_line(HttpParser *parser) {
    HttpRequest *req = parser->request;
    int sock = req->sock;
    Buffer *buf = req->raw_head;

    while(1) {
        // fill buffer from connection
        ssize_t n = fill_buffer(sock, buf);
        if(n == IO || n == FULL) {
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        int status = http_parse_request_line(parser);
        if(status == OK) {
            // request line parsed successfully 

            // analyse request line
            HttpRequest *req = parser->request;
            if(req->version == HTTP_NOT_SUPPORTED_VERSION) {
                return HTTP_NOT_IMPLEMENTED;
            }
            size_t hostlen = req->host.len;
            if(hostlen == 0 || hostlen >= DOMAIN_NAME_STR_LEN) {
                return HTTP_BAD_REQUEST;
            }
            
            // successful analysis
            return HTTP_OK;
        }

        if(status != AGAIN) {
            // some parse error occurred
            return HTTP_BAD_REQUEST;
        }
    }

    return HTTP_INTERNAL_SERVER_ERROR;
}


static HttpStatus analyse_req_headers(HttpRequest *req) {
    Buffer *buf = req->raw_head;

    if(req->method == HTTP_GET || req->method == HTTP_HEAD) {
        if(req->is_content_len_set || buffer_remaining(buf) != 0) {
            return HTTP_BAD_REQUEST;
        }
    }
    if(req->method == HTTP_POST && !req->is_content_len_set) {
        return HTTP_BAD_REQUEST;
    }

    return HTTP_OK;
}


static HttpStatus process_headers(HttpParser *parser) {
    int sock;
    Buffer *buf;
    if(parser->is_request_parser) {
        HttpRequest *req = parser->request;
        sock = req->sock;
        buf = req->raw_head;
    } else {
        HttpResponse *res = parser->response;
        sock = res->sock;
        buf = res->raw_head;
    }

    while(1) {
        // fill buffer from connection
        ssize_t n = fill_buffer(sock, buf);
        if(n == IO || n == FULL) {
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        HttpHeader header;
        int status = http_parse_header_line(parser, &header);
        if(status == OK) {
            // all headers parsed successfully

            // analyse headers
            if(parser->is_request_parser) {
                status = analyse_req_headers(parser->request);
            } else {
                status = HTTP_OK;
            }
            return status;
        }

        if(status == HTTP_MORE_HEADERS) {
            // header parsed successfully
            if(string_equal_chararray(header.name, "Content-Length")) {
                long ret;
                status = string_to_long(header.val, &ret);
                if(status != OK) {
                    // parse content length failed
                    return HTTP_BAD_REQUEST;
                }
                if(ret > 0) {
                    parser->request->content_length = ret;
                    parser->request->is_content_len_set = true;
                }
            }
            
            // parse next header line
            continue;
        }

        if(status != AGAIN) {
            // some error occurred
            return HTTP_BAD_REQUEST;
        }
    }

    return HTTP_INTERNAL_SERVER_ERROR;
}


HttpState http_read_request_head(HttpRequest *req) {
    assert(req != NULL);

    HttpParser parser;
    http_request_parser_init(&parser, req);

    HttpStatus status = process_request_line(&parser);
    if(status != HTTP_OK) {
        send_error_response(req->sock, status);
        return HTTP_TERMINATE_REQUEST;
    }
    status = http_process_request_headers(&parser);
    if(status != HTTP_OK) {
        send_error_response(req->sock, status);
        return HTTP_TERMINATE_REQUEST;
    }

    return HTTP_PROCESS_REQUEST;
}


static HttpStatus process_status_line(HttpParser *parser) {
    HttpResponse *res = parser->response;
    int sock = res->sock;
    Buffer *buf = res->raw_head;

    while(1) {
        // fill buffer from connection
        ssize_t n = fill_buffer(sock, buf);
        if(n == IO || n == FULL) {
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        int status = http_parse_status_line(parser);
        if(status == OK) {
            // status line parsed successfully
            return HTTP_OK;
        }

        if(status != AGAIN) {
            // some parse error occurred
            return HTTP_BAD_GATEWAY;
        }
    }

    return HTTP_INTERNAL_SERVER_ERROR;
}


static HttpStatus transfer_body(int from, int to, size_t content_len, Buffer *head, bool wait_eof) {
    unsigned char body[IO_BUFFER_SIZE];
    size_t remainig = buffer_remaining(head);
    memcpy(body, head->pos, remainig);
    size_t len = content_len;
    while(1) {
        if(remainig != 0) {
            ssize_t n = sock_send(to, body, remainig);
            if(n < 0) {
                return HTTP_INTERNAL_SERVER_ERROR;
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
        if(remainig == END_OF_STREAM) {
            break;
        } else {
            return HTTP_INTERNAL_SERVER_ERROR;
        }
    }
    return HTTP_OK;
}


static HttpStatus send_request(HttpRequest *req, int upstream) {
    int sock = req->sock;
    Buffer *head = req->raw_head;

    // send request header
    ssize_t n = sock_send(upstream, head->start, head->pos - head->start);
    if(n == IO) {
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    // transfer request body
    if(req->is_content_len_set) {
        return transfer_body(sock, upstream, req->content_length, head, false);
    }
    return HTTP_OK;
}


static HttpStatus read_response_head(HttpResponse *res) {
    HttpParser parser;
    http_response_parser_init(&parser, res);

    HttpStatus status = process_status_line(&parser);
    if(status != HTTP_OK) {
        return status;
    }
    return http_process_request_headers(&parser);
}


static HttpStatus connect_to_upstream(HttpRequest *req, HttpResponse *res) {
    // make null-terminated string
    char host[DOMAIN_NAME_STR_LEN];
    memcpy(host, req->host.data, req->host.len);
    host[req->host.len] = '\0';

    res->sock = open_and_connect_socket(host, req->port);
    if(res->sock == IO) {
        return HTTP_INTERNAL_SERVER_ERROR;
    }
    if(res->sock == UNKNOWN_HOST) {
        return HTTP_NOT_FOUND;
    }

    return HTTP_OK;
}


static HttpStatus transfer_response(HttpRequest *req, HttpResponse *res) {
    if(req->version == HTTP_9) {
        return transfer_body(res->sock, req->sock, res->content_length, res->raw_head, true);
    }

    HttpStatus status = read_response_head(res);
    if(status != HTTP_OK) {
        return status;
    }
    
    Buffer *head = req->raw_head;
    // send response header
    ssize_t n = sock_send(req->sock, head->start, head->pos - head->start);
    if(n == IO) {
        return HTTP_INTERNAL_SERVER_ERROR;
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
        send_error_response(req, HTTP_INTERNAL_SERVER_ERROR);
        return HTTP_TERMINATE_REQUEST;
    }

    HttpStatus status = connect_to_upstream(req, res);
    if(status != HTTP_OK) {
        goto fail;
    }
    status = send_request(req, res->sock);
    if(status != HTTP_OK) {
        goto fail;
    }
    status = transfer_response(req, res);
    if(status != HTTP_OK) {
        goto fail;
    }

    http_response_destroy(res);
    return HTTP_PROCESS_REQUEST;

fail:
    send_error_response(req->sock, status);
    http_response_destroy(res);
    return HTTP_TERMINATE_REQUEST;
}
