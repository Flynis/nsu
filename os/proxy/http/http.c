#include "http.h"


#include <assert.h>
#include <stdlib.h>


#define HEADER_IN_BUF_SIZE 8192


HttpRequest* http_request_create(Connection *conn) {
    assert(conn != NULL);

    HttpRequest *req = malloc(sizeof(req));
    if(req == NULL) {
        return NULL;
    }

    req->header_in = buffer_create(HEADER_IN_BUF_SIZE);
    if(req->header_in == NULL) {
        free(req);
        return NULL;
    }
    req->conn = conn;

    req->request_line = NULL_STRING;

    req->version = HTTP_NOT_SUPPORTED_VERSION;

    req->method = HTTP_UNKNOWN;
    req->method_name = NULL_STRING;

    req->url = NULL_STRING;
    req->host = NULL_STRING;
    req->port = 80;

    req->headers = NULL_STRING;

    req->content_length = 0;
    req->has_body = false;
    req->body = NULL;

    return req;
}


void http_request_destroy(HttpRequest *req) {
    assert(req != NULL);
    buffer_destroy(req->header_in);
    free(req->body);
    free(req);
}


HttpResponse* http_response_create(Connection *conn) {
    assert(conn != NULL);

    assert(conn != NULL);

    HttpResponse *res = malloc(sizeof(res));
    if(res == NULL) {
        return NULL;
    }

    res->header_in = buffer_create(HEADER_IN_BUF_SIZE);
    if(res->header_in == NULL) {
        free(res);
        return NULL;
    }
    res->conn = conn;

    res->status_line = NULL_STRING;

    res->version = HTTP_NOT_SUPPORTED_VERSION;
    res->status_code = HTTP_OK;

    res->headers = NULL_STRING;

    res->content_length = 0;
    res->has_body = false;
    res->body = NULL;

    return res;
}


void http_response_destroy(HttpResponse *res) {
    assert(res != NULL);
    buffer_destroy(res->header_in);
    free(res->body);
    free(res);
}
