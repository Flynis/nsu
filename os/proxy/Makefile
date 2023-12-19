TARGET = hproxy
CC = gcc

SRCS = src/main.c
SRCS += src/proxy/proxy.c
SRCS += src/proxy/cache.c
SRCS += src/proxy/cache_manager.c
SRCS += src/http/http.c
SRCS += src/http/http_process.c
SRCS += src/http/http_alphabet.c
SRCS += src/http/http_util.c
SRCS += src/http/http_parser.c
SRCS += src/core/buffer.c
SRCS += src/core/str.c
SRCS += src/core/hashmap.c
SRCS += src/core/queue.c
SRCS += src/core/socket.c

CFLAGS = -g -std=c11 -Wall -Wextra -Wpedantic \
          -Wformat=2 -Wno-unused-parameter -Wshadow \
          -Wwrite-strings -Wstrict-prototypes -Wold-style-definition \
          -Wredundant-decls -Wnested-externs -Wmissing-include-dirs \
          -Wjump-misses-init -Wlogical-op

INCLUDE_DIR = "./src"
LIBS = -lpthread

all: ${TARGET}

${TARGET}: ${SRCS}
	${CC} ${CFLAGS} -I ${INCLUDE_DIR} ${SRCS} ${LIBS} -o ${TARGET}

clean:
	rm -f *.o ${TARGET}
