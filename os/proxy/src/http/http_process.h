#ifndef _HTTP_PROCESS_H_INCLUDED_
#define _HTTP_PROCESS_H_INCLUDED_


#include "http.h"


void http_terminate_request(HttpRequest *req);


HttpState http_read_request_head(HttpRequest *req);


HttpState http_process_request(HttpRequest *req);


int http_read_response_head(HttpResponse *res);


int http_connect_to_upstream(HttpRequest *req, HttpResponse *res);


int http_send_request(HttpRequest *req, int dest);


#endif // _HTTP_PROCESS_H_INCLUDED_
