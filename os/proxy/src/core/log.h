#ifndef _LOG_H_INCLUDED_
#define _LOG_H_INCLUDED_


#include <stdio.h>
#include <string.h>


#ifdef NDEBUG
#define LOG_DEBUG(...) do {} while (0)
#else
#define LOG_DEBUG(...) do { fprintf(stdout, __VA_ARGS__); } while (0)
#endif


#define LOG_ERR(...) do { fprintf(stderr, __VA_ARGS__); } while (0)


#define LOG_ERRNO(er, msg) do { fprintf(stderr, "%s: %s\n", msg, strerror(er)); } while (0)



#endif // _LOG_H_INCLUDED_
