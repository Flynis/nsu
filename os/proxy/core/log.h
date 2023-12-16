#ifndef _LOG_H_INCLUDED_
#define _LOG_H_INCLUDED_


#ifdef NDEBUG
#define LOG_DEBUG(...) do {} while (0)
#else
#define LOG_DEBUG(...) do { fprintf(stderr, __VA_ARGS__); } while (0)
#endif


/**
 * Prints error message and code representation.
*/
void log_error_code(int code, char *msg);


/**
 * Prints error message.
*/
void log_error(char *msg);


/**
 * Prints information message.
*/
void log_info(char *msg);


#endif // _LOG_H_INCLUDED_
