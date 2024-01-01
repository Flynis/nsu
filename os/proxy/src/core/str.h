#ifndef _STR_H_INCLUDED_
#define _STR_H_INCLUDED_


#include <stdbool.h>
#include <stddef.h>


/**
 * Not nul terminated string
*/
typedef struct String {
    unsigned char *data;
    size_t len;
} String;


static const String EMPTY_STRING = { NULL, 0};


/**
 * Sets string data and length by two points. 
 * Pointers should be from same array.
*/
void string_set(String *s, unsigned char *start, unsigned char *end);


/**
 * Allocates new string and copy s into it.
 * @returns new string or EMPTY_STRING on failure.
*/
String string_clone(String s);


/**
 * Returns hash of string.
 * @returns hash of string.
*/
unsigned long string_hash(String str);


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
 * @returns OK if conversion was successful, ERROR otherwise.
*/
int string_to_long(String str, long *result);


#endif // _STR_H_INCLUDED_
