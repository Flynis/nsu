#include "log.h"


#include <stdio.h>
#include <string.h>


void log_error_code(int code, char *msg) {
    fprintf(stderr, "%s: %s\n", msg, strerror(code));
}


void log_error(char *msg) {
    fprintf(stderr, "%s\n", msg);
}


void log_info(char *msg) {
    fprintf(stdin, "%s\n", msg);
}
