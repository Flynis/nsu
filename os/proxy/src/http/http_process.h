#ifndef _HTTP_PROCESS_H_INCLUDED_
#define _HTTP_PROCESS_H_INCLUDED_


#include "http.h"


void http_terminate_request(HttpRequest *req);


HttpState http_read_request_head(HttpRequest *req);


HttpState http_process_request(HttpRequest *req);


HttpState http_load_response(HttpRequest *req, HttpResponse **out_res);


void http_send_response(HttpRequest *req, HttpResponse *res);


#endif // _HTTP_PROCESS_H_INCLUDED_
