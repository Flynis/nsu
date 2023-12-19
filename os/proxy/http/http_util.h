#ifndef _HTTP_UTIL_H_INCLUDED_
#define _HTTP_UTIL_H_INCLUDED_


#include "http.h"
#include "http_parser.h"


/**
 * Converts http status code to string.
 * @returns status code representation.
*/
char const* http_status_tostring(HttpStatus status);


/**
 * Converts http parse code to string.
 * @returns parse code representation.
*/
char const* http_parse_code_tostring(HttpParseCode parse_code);


/**
 * Sends full response with specified status code.
 * @returns OK on success, ERROR otherwise.
*/
int send_error_response(int sock, HttpStatus status);


#endif // _HTTP_UTIL_H_INCLUDED_
