#ifndef _HTTP_PROCESSOR_H_INCLUDED_
#define _HTTP_PROCESSOR_H_INCLUDED_


#include "core/buffer.h"
#include "core/connection.h"
#include "http/http_parser.h"


/**
 * Struct for combine function arguments.
*/
typedef struct HttpProcessor {
    Connection *connection;
    HttpParser *parser;
    Buffer *buffer;
} HttpProcessor;


int http_process_request_line(HttpProcessor *processor);


int http_process_status_line(HttpProcessor *processor);


int http_process_headers(HttpProcessor *processor);


#endif // _HTTP_PROCESSOR_H_INCLUDED_
