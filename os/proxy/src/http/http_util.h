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
 * Prints full response with specified status code.
 * @param buf dest buffer, should be at least 256 in size.
*/
void print_error_response(char *buf, HttpStatus status);


#endif // _HTTP_UTIL_H_INCLUDED_
