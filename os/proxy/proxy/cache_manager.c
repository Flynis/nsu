#include "cache_manager.h"


#include <assert.h>
#include <stdlib.h>


#include "core/log.h"
#include "core/status.h"
#include "http/http_util.h"


#define PENDING_REQUESTS_INITIAL_SIZE 512


/**
 * Argument for cache_loader function.
*/
typedef struct LoaderArgs {
    HttpRequest *req;
    CacheManager *manager;
} LoaderArgs;


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
    size_t size = PENDING_REQUESTS_INITIAL_SIZE;
    m->pending_requests = hashmap_create(size, true);
    if(m->pending_requests == NULL) {
        goto fail_pending_req_create;
    }

    // all resourses successfully allocated
    return m;

fail_pending_req_create:
    cache_destroy(m->cache);
fail_cache_create:
    pthread_cond_destroy(&m->cond);
fail_cond_init:
    pthread_mutex_destroy(&m->lock);
fail_lock_init:
    free(m);
    return NULL;
}


static void* cache_loader(void *data) {
    // TODO
}


static int start_connection_handler(CacheManager *m, HttpRequest *req) {
    LoaderArgs *args = malloc(args);
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


HttpResponse* cache_manager_get_response(CacheManager *m, HttpRequest* req) {
    assert(m != NULL);
    assert(req != NULL);

    HttpResponse *res;
    pthread_mutex_lock(&m->lock);

    HttpResponse *cached = cache_peek(m->cache, req->request_line);
    if(cached != NULL) {
        // cache hit

        // TODO check TTL
        res = http_response_dup(cached);
        pthread_mutex_unlock(&m->lock);
        return res;
    }

    // cache miss

    HttpRequest *pending = hashmap_get(m->pending_requests, req->request_line);
    if(pending == NULL) {
        // this request isn't pending, so start loader
        int ret = hashmap_put(m->pending_requests, req->request_line, req);
        if(ret != OK) {
            LOG_ERR("Failed to put request into pending requests\n");
            pthread_mutex_unlock(&m->lock);
            return NULL;
        }
        ret = start_connection_handler(m, req);
        if(ret != OK) {
            LOG_ERR("Failed to start cache loader\n");
            pthread_mutex_unlock(&m->lock);
            return NULL;
        }
    } 

    while(cached == NULL) {
        pthread_cond_wait(&m->cond, &m->lock);
        cached = cache_peek(m->cache, req->request_line);
    }

    // TODO

    pthread_mutex_unlock(&m->lock);
    return res;
}


void cache_manager_destroy(CacheManager *m) {
    assert(m != NULL);
    hashmap_destroy(m->pending_requests);
    cache_destroy(m->cache);
    pthread_cond_destroy(&m->cond);
    pthread_mutex_destroy(&m->lock);
    free(m);
}
