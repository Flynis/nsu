#include "http_processor.h"


#include "core/errcode.h"
#include "core/str.h"
#include "http/http.h"


int http_process_request_line(HttpProcessor *processor) {
    Connection *c = processor->connection;
    Buffer *buf = processor->buffer;
    HttpParser *parser = processor->parser;
    int status;

    while(1) {
        // fill buffer from connection
        status = connection_recv(c, buf);
        if(status != ERRC_OK) {
            return status;
        }

        status = http_parse_request_line(parser, buf);
        if(status != ERRC_AGAIN) {
            // request line parsed successfully or some error occurred
            return status;
        }
    }

    return ERRC_FAILED;
}


int http_process_status_line(HttpProcessor *processor) {
    Connection *c = processor->connection;
    Buffer *buf = processor->buffer;
    HttpParser *parser = processor->parser;
    int status;

    while(1) {
        // fill buffer from connection
        status = connection_recv(c, buf);
        if(status != ERRC_OK) {
            return status;
        }

        status = http_parse_status_line(parser, buf);
        if(status != ERRC_AGAIN) {
            // status line parsed successfully or some error occurred
            return status;
        }
    }

    return ERRC_FAILED;
}


int http_process_headers(HttpProcessor *processor) {
    Connection *c = processor->connection;
    Buffer *buf = processor->buffer;
    HttpParser *parser = processor->parser;
    int status;

    while(1) {
        // fill buffer from connection
        status = connection_recv(c, buf);
        if(status != ERRC_OK) {
            return status;
        }

        HttpHeader header;
        status = http_parse_header_line(parser, buf, &header);
        if(status == HTTP_MORE_HEADERS) {
            // header parsed successfully
            if(string_equal_chararray(header.name, "Content-Length")) {
                long ret;
                status = string_to_long(header.value, &ret);
                if(status != ERRC_OK) {
                    return HTTP_INVALID_HEADER;
                }
                parser->request->content_length = ret;
                parser->request->has_body = true;
            }
            
            // parse next header line
            continue;
        }
        if(status != ERRC_AGAIN) {
            // all headers parsed successfully or some error occurred
            return status;
        }
    }

    return ERRC_FAILED;
}
