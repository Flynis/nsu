#include "cache_manager.h"


#include <assert.h>
#include <linux/futex.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <time.h>
#include <unistd.h>


#include "core/buffer.h"
#include "core/log.h"
#include "core/status.h"
#include "http/http_process.h"


#define TTL 10 // sec


typedef enum LoadStatus {
    LOADING_RESPONSE,
    UNCACHEABLE_RESPONSE,
    SUCC_LOADED_RESPONSE
} LoadStatus;


typedef struct CachedResponse {
    Buffer *res;
    int last; // for synchronization
    time_t recv_time; // for tracking ttl
    LoadStatus status;
} CachedResponse;


/**
 * Argument for cache_loader function.
*/
typedef struct LoaderArgs {
    HttpRequest *req;
    CacheManager *manager;
} LoaderArgs;


static int futex(int *uaddr, int futex_op, int val, const struct timespec *timeout, int *uaddr2, int val3) {
    return syscall(SYS_futex, uaddr, futex_op, val, timeout, uaddr2, val3);
}


CacheManager* cache_manager_create(size_t cache_capacity) {
    assert(cache_capacity > 0);

    CacheManager *m = malloc(sizeof(m));
    if(m == NULL) {
        return NULL;
    }

    int err;
    err = pthread_mutex_init(&m->lock, NULL);
    if(err) {
        goto fail_lock_init;
    }
    err = pthread_cond_init(&m->cond, NULL);
    if(err) {
        goto fail_cond_init;        
    }

    m->cache = cache_create(cache_capacity);
    if(m->cache == NULL) {
        goto fail_cache_create;
    }

    // all resourses successfully allocated
    return m;

fail_cache_create:
    pthread_cond_destroy(&m->cond);
fail_cond_init:
    pthread_mutex_destroy(&m->lock);
fail_lock_init:
    free(m);
    return NULL;
}


static void* cache_loader(void *data) {
    pthread_detach(pthread_self());
    
    LoaderArgs *args = (LoaderArgs*)data;
    HttpRequest *req = args->req;
    CacheManager *manager = args->manager;

    HttpResponse *res;
    HttpState state = http_load_response(req, &res);
    req->state = state;

    pthread_mutex_lock(&manager->lock);

    if(state == HTTP_PROCESS_REQUEST) {
        int ret = cache_push(manager->cache, req->request_line, res);
        if(ret == ERROR) {
            req->status = HTTP_INTERNAL_SERVER_ERROR;
            req->state = HTTP_TERMINATE_REQUEST;
        }
    }

    pthread_cond_broadcast(&manager->cond);

    pthread_mutex_unlock(&manager->lock);
    free(args);
    return NULL;
}


static int start_connection_handler(CacheManager *m, HttpRequest *req) {
    LoaderArgs *args = malloc(sizeof(args));
    if(args == NULL) {
        LOG_ERR("Failed to create loader args\n");
        return ERROR;
    }

    args->manager = m;
    args->req = req;

    pthread_t tid;
    int ret = pthread_create(&tid, NULL, cache_loader, args);
    if(ret != 0) {
        LOG_ERRNO(ret, "pthread_create() failed");
        free(args);
        return ERROR;
    }

    return OK;
}


HttpState process_cacheable_request(CacheManager *m, HttpRequest* req) {
    assert(m != NULL);
    assert(req != NULL);

    pthread_mutex_lock(&m->lock);

    CachedResponse *cached = cache_peek(m->cache, req->request_line);
    if(cached != NULL && difftime(cached->recv_time, time(NULL)) < TTL) {
        // cache hit
        HttpResponse *res = http_response_clone(cached);
        pthread_mutex_unlock(&m->lock);
        return res;
    }
    // cache miss

    int ret = start_connection_handler(m, req);
    if(ret != OK) {
        LOG_ERR("Failed to start cache loader\n");
        pthread_mutex_unlock(&m->lock);
        return NULL;
    }

    while(cached == NULL) {
        pthread_cond_wait(&m->cond, &m->lock);
        // request already handled
        if(req->state == HTTP_TERMINATE_REQUEST) {
            pthread_mutex_unlock(&m->lock);
            return NULL;
        }
        cached = cache_peek(m->cache, req->request_line);
    }

    HttpResponse *res = http_response_clone(cached);
    pthread_mutex_unlock(&m->lock);
    return res;
}


void cache_manager_destroy(CacheManager *m) {
    assert(m != NULL);
    cache_destroy(m->cache);
    pthread_cond_destroy(&m->cond);
    pthread_mutex_destroy(&m->lock);
    free(m);
}
