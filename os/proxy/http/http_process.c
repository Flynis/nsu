#include "http_process.h"


#include <assert.h>
#include <string.h>


#include "core/errcode.h"
#include "core/socket.h"
#include "core/str.h"
#include "http.h"
#include "http_util.h"


#define IO_BUFFER_SIZE 8192


static ssize_t fill_buffer(Connection *c, Buffer *buf) {
    ssize_t n = buffer_remaining(buf);
    if(n > 0) {
        return n;
    }
    return buffer_recv(c, buf);
}


HttpState http_process_request_line(HttpParser *parser) {
    assert(parser != NULL);
    assert(parser->is_request_parser);

    HttpRequest *req = parser->request;
    Connection *c = req->conn;
    Buffer *buf = req->header_in;

    while(1) {
        // fill buffer from connection
        ssize_t n = fill_buffer(c, buf);
        if(n == ERRC_FAILED) {
            send_error_response(c, HTTP_INTERNAL_SERVER_ERROR);
            return HTTP_TERMINATE_STATE;
        }

        int status = http_parse_request_line(parser);
        if(status == ERRC_OK) {
            // request line parsed successfully 

            // analyse request line
            HttpRequest *req = parser->request;
            if(req->version == HTTP_NOT_SUPPORTED_VERSION) {
                send_error_response(c, HTTP_NOT_IMPLEMENTED);
                return HTTP_TERMINATE_STATE;
            }
            // TODO check host
            
            // successful analysis
            return HTTP_PROCESS_STATE;
        }

        if(status != ERRC_AGAIN) {
            // some error occurred
            send_error_response(c, HTTP_BAD_REQUEST);
            return HTTP_TERMINATE_STATE;
        }
    }

    return HTTP_TERMINATE_STATE;
}


HttpState http_process_status_line(HttpParser *parser) {
    assert(parser != NULL);
    assert(!parser->is_request_parser);
   
    HttpResponse *res = parser->response;
    Connection *c = res->conn;
    Buffer *buf = res->header_in;

    while(1) {
        // fill buffer from connection
        status = connection_recv(c, buf);
        if(status != ERRC_OK) {
            return status;
        }

        status = http_parse_status_line(parser);
        if(status != ERRC_AGAIN) {
            // status line parsed successfully or some error occurred
            return status;
        }
    }

    return ERRC_FAILED;
}


static HttpState analyse_req_headers(HttpRequest *req) {
    Connection *c = req->conn;
    Buffer *buf = req->header_in;

    if(req->method == HTTP_GET || req->method == HTTP_HEAD) {
        if(req->has_body || buffer_remaining(buf) != 0) {
            send_error_response(c, HTTP_BAD_REQUEST);
            return HTTP_TERMINATE_STATE;
        }
    }
    if(req->method == HTTP_POST && !req->has_body) {
        send_error_response(c, HTTP_BAD_REQUEST);
        return HTTP_TERMINATE_STATE;
    }
    
    return HTTP_PROCESS_STATE;
}


HttpState http_process_headers(HttpParser *parser) {
    assert(parser != NULL);

    Connection *c;
    Buffer *buf;
    if(parser->is_request_parser) {
        HttpRequest *req = parser->request;
        c = req->conn;
        buf = req->header_in;
    } else {
        HttpResponse *res = parser->response;
        c = res->conn;
        buf = res->header_in;
    }

    while(1) {
        // fill buffer from connection
        ssize_t n = fill_buffer(c, buf);
        if(n == ERRC_FAILED) {
            send_error_response(c, HTTP_INTERNAL_SERVER_ERROR);
            return HTTP_TERMINATE_STATE;
        }

        HttpHeader header;
        int status = http_parse_header_line(parser, &header);
        if(status == ERRC_OK) {
            // all headers parsed successfully

            // analyse headers
            HttpState state = HTTP_PROCESS_STATE;
            if(parser->is_request_parser) {
                state = analyse_req_headers(parser->request);
            }
            return state;
        }

        if(status == HTTP_MORE_HEADERS) {
            // header parsed successfully
            if(string_equal_chararray(header.name, "Content-Length")) {
                long ret;
                status = string_to_long(header.value, &ret);
                if(status != ERRC_OK) {
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

        if(status != ERRC_AGAIN) {
            // some error occurred
            send_error_response(c, HTTP_BAD_REQUEST);
            return HTTP_TERMINATE_STATE;
        }
    }

    return HTTP_TERMINATE_STATE;
}


HttpState http_send_request(HttpRequest *req) {
    assert(req != NULL);

    Connection *c = req->conn;
    Buffer *header = req->header_in;

    Connection dest;
    dest.sockfd = open_and_connect_socket(req->host.data, req->port, &dest.sockaddr);
    if(dest.sockfd == ERRC_FAILED) {
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
