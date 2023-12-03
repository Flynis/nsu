#ifndef _UTIL_H_INCLUDED_
#define _UTIL_H_INCLUDED_


#include <stdbool.h>


void print_error_by_code(int code, char *msg);


void print_error_msg(char *msg);


unsigned long strhash(char *str);


bool streq(char *str1, char *str2);


bool streq_not_zero_terminated(char *str0, char *str);


#endif
