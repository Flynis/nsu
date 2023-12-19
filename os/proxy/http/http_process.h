#ifndef _HTTP_PROCESS_H_INCLUDED_
#define _HTTP_PROCESS_H_INCLUDED_


#include "http.h"


typedef enum HttpState {
    HTTP_READ_REQUEST_HEAD,
    HTTP_PROCESS_REQUEST,
    HTTP_TERMINATE_REQUEST
} HttpState;


HttpState http_read_request_head(HttpRequest *req);


HttpState http_process_request(HttpRequest *req);


#endif // _HTTP_PROCESS_H_INCLUDED_
