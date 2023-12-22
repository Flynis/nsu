#include "http.h"


#include <assert.h>
#include <stdlib.h>


#include "core/socket.h"


#define HEAD_BUF_SIZE 4096
#define INVALID_SOCKET -1


HttpRequest* http_request_create(void) {
    HttpRequest *req = malloc(sizeof(req));
    if(req == NULL) {
        return NULL;
    }

    req->raw = buffer_create(HEAD_BUF_SIZE);
    if(req->raw == NULL) {
        free(req);
        return NULL;
    }

    req->sock = INVALID_SOCKET;
    req->state = HTTP_READ_REQUEST_HEAD;
    req->status = HTTP_OK;

    req->request_line = EMPTY_STRING;
    req->version = HTTP_NOT_SUPPORTED_VERSION;
    req->method = HTTP_UNKNOWN;
    req->host = EMPTY_STRING;
    req->port = 80;

    req->content_length = 0;
    req->is_content_len_set = false;

    return req;
}


void http_request_destroy(HttpRequest *req) {
    if(req == NULL) return;
    buffer_destroy(req->raw);
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

    res->raw = buffer_create(HEAD_BUF_SIZE);
    if(res->raw == NULL) {
        free(res);
        return NULL;
    }
    res->sock = INVALID_SOCKET;

    res->version = HTTP_NOT_SUPPORTED_VERSION;
    res->status = HTTP_OK;

    res->content_length = 0;
    res->is_content_len_set = false;

    return res;
}


void http_response_destroy(HttpResponse *res) {
    if(res == NULL) return;
    buffer_destroy(res->raw);
    if(res->sock != INVALID_SOCKET) {
        close_socket(res->sock);
    }
    free(res);
}
