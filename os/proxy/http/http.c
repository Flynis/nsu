#include "http.h"


#include <assert.h>
#include <stdlib.h>


#include "core/socket.h"


#define RAW_HEAD_BUF_SIZE 4096
#define INVALID_SOCKET -1


HttpRequest* http_request_create(void) {
    HttpRequest *req = malloc(sizeof(req));
    if(req == NULL) {
        return NULL;
    }

    req->raw_head = buffer_create(RAW_HEAD_BUF_SIZE);
    if(req->raw_head == NULL) {
        free(req);
        return NULL;
    }
    req->sock = INVALID_SOCKET;

    req->request_line = EMPTY_STRING;

    req->version = HTTP_NOT_SUPPORTED_VERSION;

    req->method = HTTP_UNKNOWN;
    req->method_name = EMPTY_STRING;

    req->host = EMPTY_STRING;
    req->port = 80;

    req->headers = EMPTY_STRING;

    req->content_length = 0;
    req->has_body = false;

    return req;
}


void http_request_destroy(HttpRequest *req) {
    assert(req != NULL);
    buffer_destroy(req->raw_head);
    if(req->sock != INVALID_SOCKET) {
        close_socket(req->sock);
    }
    free(req);
}


HttpResponse* http_response_create(void) {
    HttpResponse *res = malloc(sizeof(res));
    if(res == NULL) {
        return NULL;
    }

    res->raw_head = buffer_create(RAW_HEAD_BUF_SIZE);
    if(res->raw_head == NULL) {
        free(res);
        return NULL;
    }
    res->sock = INVALID_SOCKET;

    res->status_line = EMPTY_STRING;

    res->version = HTTP_NOT_SUPPORTED_VERSION;
    res->status_code = HTTP_OK;

    res->headers = EMPTY_STRING;

    res->content_length = 0;
    res->has_body = false;
    res->body = NULL;

    return res;
}


void http_response_destroy(HttpResponse *res) {
    assert(res != NULL);
    buffer_destroy(res->raw_head);
    if(res->sock != INVALID_SOCKET) {
        close_socket(res->sock);
    }
    free(res->body);
    free(res);
}
