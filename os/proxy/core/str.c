#include "str.h"


#include <assert.h>
#include <string.h>


#include "status.h"


void string_set(String *s, unsigned char const *start, unsigned char const *end) {
    assert(s != NULL);
    assert(start != NULL);
    assert(end != NULL);

    s->len = end - start;
    s->data = (s->len == 0) ? NULL : start;
}


// djb2 algorithm
unsigned int string_hash(String str) {
    assert(str.data != NULL);

    unsigned int hash = 5381;
    unsigned char *s = str.data;
    int c;

    while (c = *s++)
        hash = ((hash << 5) + hash) + c; /* hash * 33 + c */

    return hash;
}


bool string_equals(String str1, String str2) {
    assert(str1.data != NULL);
    assert(str2.data != NULL);

    if(str1.len != str2.len) {
        return false;
    }

    return strncmp(str1.data, str2.data, str1.len) == 0;
}


bool string_equal_chararray(String str1, char const *str2) {
    assert(str1.data != NULL);
    assert(str2 != NULL);

    return strncmp(str1.data, str2, str1.len) == 0;
}


int string_to_long(String str, long *result) {
    assert(str.data != NULL);
    assert(result != NULL);

	// Expect that digit representation of LONG_MAX/MIN
	// not greater than this buffer 
	char buf[24];
	const char *s = str.data;
    size_t len = str.len;

	// Skip leading spaces 
    size_t i = 0;
    while(i < len && isspace(s[i])) {
        i += 1;
    }

    // Skip trailing spaces
    size_t end = len - 1;
    while(end != i && isspace(s[end])) {
        end -= 1;
    }

    size_t remaining = end - i + 1;
	if (remaining == 0 || remaining >= sizeof(buf)) {
        if(result != NULL) {
            *result = 0;
        }
		return ERROR;
	}

    // make nul terminated string
	memcpy(buf, s + i, remaining);
	buf[remaining] = '\0';

	long ret = strtol(buf, NULL, 10);
	if(result != NULL) {
        *result = 0;
    }
	return OK;
}
