#ifndef _STR_H_INCLUDED_
#define _STR_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


typedef struct String {
    unsigned char const *data;
    size_t length;
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


/**
 * Checks strings for equality.
 * @returns true if strings are equal, false otherwise.
*/
bool string_equal_chararray(String str1, char const *str2);


/**
 * Converts string to long.
 * @returns ERRC_OK if conversion was successful, ERRC_FAILED otherwise.
*/
int string_to_long(String str, long *result);


#endif // _STR_H_INCLUDED_
