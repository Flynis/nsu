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

    req->raw = chain_create(HEAD_BUF_SIZE);
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
    assert(req != NULL);
    chain_destroy(req->raw);
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

    res->raw = chain_create(HEAD_BUF_SIZE);
    if(res->raw == NULL) {
        free(res);
        return NULL;
    }
    res->sock = INVALID_SOCKET;

    res->version = HTTP_NOT_SUPPORTED_VERSION;
    res->status_code = HTTP_OK;

    res->content_length = 0;
    res->is_content_len_set = false;

    return res;
}


HttpResponse* http_response_clone(HttpResponse* res) {
    assert(res != NULL);

    HttpResponse *result = malloc(sizeof(res));
    if(result == NULL) {
        return NULL;
    }

    result->raw = chain_clone(res->raw);
    if(result->raw == NULL) {
        free(result);
        return NULL;
    }

    result->sock = INVALID_SOCKET;

    result->version = res->version;
    result->status_code = res->status_code;

    result->content_length = res->content_length;
    result->is_content_len_set = res->is_content_len_set;

    return result;
}


void http_response_destroy(HttpResponse *res) {
    assert(res != NULL);
    chain_destroy(res->raw);
    if(res->sock != INVALID_SOCKET) {
        close_socket(res->sock);
    }
    free(res);
}
