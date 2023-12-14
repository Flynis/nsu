#include "str.h"


#include <assert.h>


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

    if(str1.length != str2.length) {
        return false;
    }

    for(size_t i = 0; i < str1.length; i += 1) {
        if(str1.data[i] != str2.data[i]) {
            return false;
        }
    }

    return true;
}
