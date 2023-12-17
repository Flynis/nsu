#ifndef _HTTP_PROCESSOR_H_INCLUDED_
#define _HTTP_PROCESSOR_H_INCLUDED_


#include <stddef.h>


#include "core/connection.h"
#include "http/http_parser.h"


typedef enum HttpState {
    HTTP_PROCESS_STATE,
    HTTP_TERMINATE_STATE
} HttpState;


HttpState http_process_request_line(HttpParser *parser);


HttpState http_process_status_line(HttpParser *parser);


HttpState http_process_headers(HttpParser *parser);


HttpState http_send_request(HttpRequest *req);


#endif // _HTTP_PROCESSOR_H_INCLUDED_
