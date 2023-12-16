#ifndef _HTTP_UTIL_H_INCLUDED_
#define _HTTP_UTIL_H_INCLUDED_


#include "core/connection.h"
#include "http.h"


char const* http_status_tostring(HttpStatusCode status);


int send_error_response(Connection *connection, HttpStatusCode status);


#endif // _HTTP_UTIL_H_INCLUDED_
