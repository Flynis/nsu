#include "http_process.h"


#include <assert.h>
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


HttpState http_process_request_line(HttpParser *parser) {
    assert(parser != NULL);
    assert(parser->is_request_parser);

    HttpRequest *req = parser->request;
    int sock = req->sock;
    Buffer *buf = req->raw_head;

    while(1) {
        // fill buffer from connection
        ssize_t n = fill_buffer(sock, buf);
        if(n == IO || n == FULL) {
            send_error_response(sock, HTTP_INTERNAL_SERVER_ERROR);
            return HTTP_TERMINATE_REQUEST;
        }

        int status = http_parse_request_line(parser);
        if(status == OK) {
            // request line parsed successfully 

            // analyse request line
            HttpRequest *req = parser->request;
            if(req->version == HTTP_NOT_SUPPORTED_VERSION) {
                send_error_response(sock, HTTP_NOT_IMPLEMENTED);
                return HTTP_TERMINATE_REQUEST;
            }
            size_t hostlen = req->host.len;
            if(hostlen == 0 || hostlen >= DOMAIN_NAME_STR_LEN) {
                send_error_response(sock, HTTP_BAD_REQUEST);
                return HTTP_TERMINATE_REQUEST;
            }
            
            // successful analysis
            return HTTP_READ_REQUEST_HEAD;
        }

        if(status != AGAIN) {
            // some parse error occurred
            send_error_response(sock, HTTP_BAD_REQUEST);
            return HTTP_TERMINATE_REQUEST;
        }
    }

    return HTTP_TERMINATE_REQUEST;
}


HttpState http_process_status_line(HttpParser *parser) {
    assert(parser != NULL);
    assert(!parser->is_request_parser);
   
    HttpResponse *res = parser->response;
    Connection *c = res->conn;
    Buffer *buf = res->raw_head;

    while(1) {
        // fill buffer from connection
        status = connection_recv(c, buf);
        if(status != OK) {
            return status;
        }

        status = http_parse_status_line(parser);
        if(status != AGAIN) {
            // status line parsed successfully or some error occurred
            return status;
        }
    }

    return ERROR;
}


static HttpState analyse_req_headers(HttpRequest *req) {
    int sock = req->sock;
    Buffer *buf = req->raw_head;

    if(req->method == HTTP_GET || req->method == HTTP_HEAD) {
        if(req->has_body || buffer_remaining(buf) != 0) {
            send_error_response(sock, HTTP_BAD_REQUEST);
            return HTTP_TERMINATE_REQUEST;
        }
    }
    if(req->method == HTTP_POST && !req->has_body) {
        send_error_response(sock, HTTP_BAD_REQUEST);
        return HTTP_TERMINATE_REQUEST;
    }
    
    return HTTP_PROCESS;
}


HttpState http_process_headers(HttpParser *parser) {
    assert(parser != NULL);

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
            send_error_response(sock, HTTP_INTERNAL_SERVER_ERROR);
            return HTTP_TERMINATE_REQUEST;
        }

        HttpHeader header;
        int status = http_parse_header_line(parser, &header);
        if(status == OK) {
            // all headers parsed successfully

            // analyse headers
            HttpState state = HTTP_PROCESS;
            if(parser->is_request_parser) {
                state = analyse_req_headers(parser->request);
            }
            return state;
        }

        if(status == HTTP_MORE_HEADERS) {
            // header parsed successfully
            if(string_equal_chararray(header.name, "Content-Length")) {
                long ret;
                status = string_to_long(header.val, &ret);
                if(status != OK) {
                    return HTTP_INVALID_HEADER;
                }
                if(ret != 0) {
                    parser->request->content_length = ret;
                    parser->request->has_body = true;
                }
            }
            
            // parse next header line
            continue;
        }

        if(status != AGAIN) {
            // some error occurred
            send_error_response(sock, HTTP_BAD_REQUEST);
            return HTTP_TERMINATE_REQUEST;
        }
    }

    return HTTP_TERMINATE_REQUEST;
}


HttpState http_send_request(HttpRequest *req) {
    assert(req != NULL);

    Connection *c = req->conn;
    Buffer *header = req->raw_head;

    Connection dest;
    dest.sockfd = open_and_connect_socket(req->host.data, req->port, &dest.sockaddr);
    if(dest.sockfd == ERROR) {
        send_error_response(c, HTTP_INTERNAL_SERVER_ERROR);
        return HTTP_TERMINATE_STATE;
    }

    // send request header
    ssize_t n = conn_send(dest.sockfd, header->start, header->pos - header->start);
    if(n < 0) {
        goto fail;
    }

    // transfer request body
    if(req->has_body) {
        unsigned char body[IO_BUFFER_SIZE];
        size_t remainig = buffer_remaining(header);
        memcpy(body, header->pos, remainig);
        size_t len = req->content_length;
        while(1) {
            if(remainig != 0) {
                n = conn_send(dest.sockfd, body, remainig);
                if(n < 0) {
                    goto fail;
                }
                len -= n;
            }
            if(len == 0) {
                // successfully sent body
                break;
            }
            // recv another body part
            remainig = conn_recv(c, body, IO_BUFFER_SIZE);
            if(remainig < 0) {
                goto fail;
            }
        }
    }

    return HTTP_PROCESS_STATE;

fail:
    close_socket(dest.sockfd);
    send_error_response(c, HTTP_INTERNAL_SERVER_ERROR);
    return HTTP_TERMINATE_STATE;
}


HttpState http_read_request_head(HttpRequest *req) {
    HttpParser parser;
        http_request_parser_init(&parser, req);

        state = http_process_request_line(&parser);
        if(state == HTTP_TERMINATE_STATE) {
            break;
        }

        state = http_process_headers(&parser);
        if(state == HTTP_TERMINATE_STATE) {
            break;
        }
}


HttpState http_process_request(HttpRequest *req) {

}
