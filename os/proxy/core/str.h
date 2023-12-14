#ifndef _STR_H_INCLUDED_
#define _STR_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


#define null_string(_) {.length = 0,.data = NULL }


typedef struct String {
    unsigned char           *data;
    size_t                   length;
} String;


/**
 * Returns hash of string.
 * @returns hash of string.
*/
unsigned int string_hash(String str);


/**
 * Checks strings for equality.
 * @returns true if strings are equal, false otherwise.
*/
bool string_equals(String str1, String str2);


bool is_null_string(String str);


#endif
