#ifndef _HTTP_UTIL_H_INCLUDED_
#define _HTTP_UTIL_H_INCLUDED_


#include "core/connection.h"
#include "http.h"
#include "http_parser.h"


char const* http_status_tostring(HttpStatusCode status);


char const* http_parse_code_tostring(HttpParseCode parse_code);


int send_error_response(Connection *connection, HttpStatusCode status);


#endif // _HTTP_UTIL_H_INCLUDED_
