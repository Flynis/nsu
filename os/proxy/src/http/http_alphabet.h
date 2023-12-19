#ifndef _HTTP_ALPHABET_H_INCLUDED_
#define _HTTP_ALPHABET_H_INCLUDED_


#include <stdbool.h>


/**
 * Defined syntax in rfc 1945 section 2.2
*/

       
/**
 * UPALPHA = <any US-ASCII uppercase letter "A".."Z">
 * LOALPHA = <any US-ASCII lowercase letter "a".."z">
 * ALPHA = UPALPHA | LOALPHA
*/
bool is_http_alpha(unsigned char c);


/**
 * DIGIT = <any US-ASCII digit "0".."9">
*/
bool is_http_digit(unsigned char c);


/**
 * token      = 1*<any CHAR except CTLs or tspecials>
 * tspecials  = "(" | ")" | "<" | ">" | "@"
 *            | "," | ";" | ":" | "\" | <">
 *            | "/" | "[" | "]" | "?" | "="
 *            | "{" | "}" | SP  | HT
*/
bool is_http_token(unsigned char c);


#endif // _HTTP_ALPHABET_H_INCLUDED_
