#include "util.h"

#include <stdio.h>


void print_error_by_code(int code, char *msg) {
    fprintf(stderr, "%s: %s\n", msg, strerror(code));
}


void print_error_msg(char *msg) {
    fprintf(stderr, "%s\n", msg);
}


unsigned long strhash(char *str) {
    unsigned long hash = 5381;
    unsigned char *s = (unsigned char*) str;
    int c;

    while (c = *s++)
        hash = ((hash << 5) + hash) + c; /* hash * 33 + c */

    return hash;
}

bool streq(char *str1, char *str2) {
    while(*str1 != '\0' && *str2 != '\0') {
        if(*str1 != *str2) {
            return false;
        }
        str1++;
        str2++;
    }
    return (*str1 == '\0' && *str2 == '\0');
}

bool streq_not_zero_terminated(char *str0, char *str) {
    while(*str0 != '\0') {
        if(*str0 != *str) {
            return false;
        }
        str0++;
        str++;
    }
    return true;
}
