#include "cache.h"


#include <assert.h>
#include <errno.h>
#include <limits.h>
#include <linux/futex.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <time.h>
#include <unistd.h>


#include "core/log.h"
#include "core/status.h"
#include "http/http_process.h"


#define TTL 10 // sec
#define RESPONSE_MAX_SIZE 1024UL * 1024UL * 1024UL // 1 gb


/**
 * Argument for cache_loader function.
*/
typedef struct LoaderArgs {
    HttpRequest *req;
    CacheElem *elem;
} LoaderArgs;


static int futex(uint32_t *uaddr, int futex_op, uint32_t val) {
    return syscall(SYS_futex, uaddr, futex_op, val, NULL, NULL, 0);
}


static bool ttl_expired(CacheElem *elem) {
    return elem->status != LOADING_RESPONSE_HEAD &&
            elem->status != LOADING_RESPONSE_BODY && 
            difftime(time(NULL), elem->recv_time) > TTL;
}


static void init_pools(Cache *cache) {
    for(size_t i = 0; i < cache->capacity; i += 1) {
        CacheElem *e = &cache->elem_pool[i];
        e->key = EMPTY_STRING;
        e->last = 0;
        e->nreaders = 0;
        e->recv_time = 0;
        e->res = NULL;
        e->status = UNCACHEABLE_RESPONSE;

        QueueNode *node = &cache->qnode_pool[i];
        node->next = NULL;
        node->prev = NULL;
        node->value = NULL;
    }
}


Cache* cache_create(size_t capacity) {
    assert(capacity > 0);

    Cache *c = malloc(sizeof(Cache));
    if(c == NULL) {
        return NULL;
    }

    int err;
    size_t map_cap = (capacity * 4) / 3; // * 4/3 to avoid hash collisions
    c->map = hashmap_create(map_cap);
    if(c->map == NULL) {
        goto fail_nodes_create;
    } 
    c->lru = queue_create();
    if(c->lru == NULL) {
        goto fail_queue_create;
    }
    c->qnode_pool = malloc(capacity * sizeof(QueueNode));
    if(c->qnode_pool == NULL) {
        goto fail_qnode_pool_create;
    }
    c->elem_pool = malloc(capacity * sizeof(CacheElem));
    if(c->elem_pool == NULL) {
        goto fail_elem_pool_create;
    }
    err = pthread_mutex_init(&c->lock, NULL);
    if(err) {
        goto fail_lock_init;
    }
    err = pthread_cond_init(&c->evict_cond, NULL);
    if(err) {
        goto fail_evict_cond_init;
    }

    // successfully allocate all resources
    c->capacity = capacity;
    c->size = 0;
    c->busy_elems = 0;
    init_pools(c);
    return c;

fail_evict_cond_init:
    pthread_mutex_destroy(&c->lock);
fail_lock_init:
    free(c->elem_pool);
fail_elem_pool_create:
    free(c->qnode_pool);
fail_qnode_pool_create:
    queue_destroy(c->lru);
fail_queue_create:
    hashmap_destroy(c->map);
fail_nodes_create:
    free(c);
    return NULL;
}


static void handle_loader_err(CacheElem *elem, int status) {
    switch (status)
    {
    case CONN_RESET:
        // fall through
    case END_OF_STREAM:
        // fall through
    case ERROR:
        elem->status = BAD_UPSTREAM;
        break;            
    case IO:
        elem->status = INTERNAL_ERROR;
        break;            
    case UNKNOWN_HOST:
        elem->status = HOST_UNREACHEABLE;
        break; 
    default: abort();
    }
}


static void* cache_loader(void *data) {
    pthread_detach(pthread_self());
    LoaderArgs *args = (LoaderArgs*)data;
    HttpRequest *req = args->req;
    CacheElem *elem = args->elem;
    int status, err;
    Buffer *buf;
    size_t res_head_len, capacity, content_len;
    ssize_t n;

    HttpResponse *res = http_response_create();
    if(res == NULL) {
        elem->status = INTERNAL_ERROR;
        goto fail;
    }

    status = http_connect_to_upstream(req, res);
    if(status != OK) {
        handle_loader_err(elem, status);
        goto fail;
    }
    status = http_send_request(req, res->sock);
    if(status != OK) {
        handle_loader_err(elem, status);
        goto fail;
    }
    status = http_read_response_head(res);
    if(status != OK) {
        handle_loader_err(elem, status);
        goto fail;
    }
    if(!res->is_content_len_set || res->content_length >= RESPONSE_MAX_SIZE) {
        elem->status = UNCACHEABLE_RESPONSE;
        goto fail;
    }
    
    buf = res->raw;
    res_head_len = buf->pos - buf->start;
    capacity = buf->end - buf->start;
    content_len = res->content_length;
    if(res_head_len + content_len > capacity) {
        err = buffer_resize(buf, res_head_len + content_len);
        if(err == ERROR) {
            elem->status = INTERNAL_ERROR;
            goto fail;
        }
    }
    elem->res = buf;
    elem->last = buf->last - buf->start;
    elem->status = LOADING_RESPONSE_BODY;
    while(1) {
        err = futex(&elem->last, FUTEX_WAKE_PRIVATE, INT_MAX);
        if(err == -1) {
            LOG_ERRNO(errno, "load futex() wake failed");
            elem->status = INTERNAL_ERROR;
            goto fail;
        }
        if(elem->status == SUCC_LOADED_RESPONSE) {
            break;
        }
        n = buffer_recv(res->sock, buf);
        if(n < 0) {
            if(n == FULL) {
                elem->recv_time = time(NULL);
                elem->status = SUCC_LOADED_RESPONSE;
                continue;
            } else {
                elem->status = (n == END_OF_STREAM) ? BAD_UPSTREAM : INTERNAL_ERROR;
                goto fail;
            }
        }
        elem->last += n;
    }

    res->raw = NULL;
    free(args);
    http_response_destroy(res);
    return NULL;

fail:
    free(args);
    http_response_destroy(res);
    elem->res = NULL;
    elem->recv_time = time(NULL);
    err = futex(&elem->last, FUTEX_WAKE_PRIVATE, INT_MAX);
    if(err == -1) {
        LOG_ERRNO(errno, "loader error futex() wake failed");
    }
    return NULL;
}


static int start_loader(CacheElem *elem, HttpRequest *req) {
    LoaderArgs *args = malloc(sizeof(LoaderArgs));
    if(args == NULL) {
        return ERROR;
    }

    args->elem = elem;
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


static CacheElem* cache_peek(Cache *cache, String key) {
    QueueNode *node = hashmap_get(cache->map, key);
    if(node == NULL) {
        // cache miss
        return NULL;
    }
    // move node to head of queue
    queue_remove(cache->lru, node);
    queue_push(cache->lru, node);
    return node->value;
}


static QueueNode* get_empty_node_or_evict(Cache *cache) {
    if(cache->size < cache->capacity) {
        // cache is not full
        // take node and elem from pools
        QueueNode *node = &cache->qnode_pool[cache->size];
        CacheElem *elem = &cache->elem_pool[cache->size];
        cache->size += 1;
        node->value = elem;
        return node;
    } 

    // cache is full need to evict some node
    // search least recently used node 
    while(cache->busy_elems == cache->size) {
        pthread_cond_wait(&cache->evict_cond, &cache->lock);
    }
    QueueNode *node = cache->lru->head;
    while(1) {
        CacheElem *elem = node->value;
        if(elem->nreaders == 0) {
            break;
        }
        node = node->next;
    }

    // evict
    CacheElem *evicted = node->value;
    queue_remove(cache->lru, node);
    hashmap_remove(cache->map, evicted->key);

    return node;
}


static HttpState process_cache_hit(Cache *cache, CacheElem *elem, HttpRequest *req) {
    LOG_DEBUG("Cache hit [%.*s] status: %d time: %.0lf\n", (int)req->request_line.len - 2, 
            req->request_line.data, elem->status, difftime(time(NULL), elem->recv_time));
    if(elem->status == INTERNAL_ERROR || ttl_expired(elem)) {
        while(elem->nreaders != 0) {
            pthread_cond_wait(&cache->evict_cond, &cache->lock);
        }
    }
    if(ttl_expired(elem)) {
        return HTTP_PROCESS_REQUEST;
    }
    switch(elem->status)
    {
    case INTERNAL_ERROR:
        // fall through
    case LOADING_RESPONSE_HEAD:
        // fall through
    case LOADING_RESPONSE_BODY:
        // fall through
    case SUCC_LOADED_RESPONSE:
        return HTTP_PROCESS_REQUEST;

    case UNCACHEABLE_RESPONSE:
        return HTTP_UNCACHEABLE_REQUEST;

    case HOST_UNREACHEABLE:
        req->status = HTTP_NOT_FOUND;
        return HTTP_TERMINATE_REQUEST;

    case BAD_UPSTREAM:
        req->status = HTTP_BAD_GATEWAY;
        return HTTP_TERMINATE_REQUEST;

    default: abort();
    }
}


static CacheElem* prepare_elem_for_loading(Cache *cache, CacheElem *elem, HttpRequest *req) {
    if(elem == NULL) {
        String key_clone = string_clone(req->request_line);
        if(key_clone.data == NULL) {
            return NULL;
        }
        QueueNode *node = get_empty_node_or_evict(cache);
        elem = node->value;
        free(elem->key.data); // free previous key
        elem->key = key_clone;
        queue_push(cache->lru, node);
        hashmap_put(cache->map, elem->key, node);
    }
    elem->last = 0;
    elem->nreaders = 0;
    buffer_destroy(elem->res);
    elem->res = NULL;
    elem->status = LOADING_RESPONSE_HEAD;
    return elem;
}


static HttpState transfer_cache_elem(CacheElem *elem, HttpRequest *req) {
    uint32_t pos = 0;
    // wait until loader not read response head
    while(elem->status == LOADING_RESPONSE_HEAD) {
        int err = futex(&elem->last, FUTEX_WAIT_PRIVATE, pos);
        if(err == -1 && errno != EAGAIN) {
            LOG_ERRNO(errno, "load head futex() wait failed");
            return HTTP_TERMINATE_REQUEST;
        }
    }
    // reponse uncacheable or other error occured
    if(elem->status != LOADING_RESPONSE_BODY && elem->status != SUCC_LOADED_RESPONSE) {
        return (elem->status == UNCACHEABLE_RESPONSE) ? HTTP_UNCACHEABLE_REQUEST : HTTP_TERMINATE_REQUEST;
    }
    Buffer *buf = elem->res;
    uint32_t end = buf->end - buf->start;
    while(pos < end) {
        if(pos == elem->last) {
            int err = futex(&elem->last, FUTEX_WAIT_PRIVATE, pos);
            if(err == -1 && errno != EAGAIN) {
                LOG_ERRNO(errno, "load body futex() wait failed");
                return HTTP_CLOSE_REQUEST;
            }
        }
        if(elem->status != LOADING_RESPONSE_BODY && elem->status != SUCC_LOADED_RESPONSE) {
            return HTTP_CLOSE_REQUEST;
        }
        uint32_t n = elem->last - pos;
        ssize_t sent = buffer_send_range(req->sock, buf, pos, n);
        if(sent < 0) {
            return HTTP_CLOSE_REQUEST;
        }
        pos += n;
    }
    return HTTP_CLOSE_REQUEST;
}


static void handle_cache_elem_transfer_err(CacheElem *elem, HttpRequest *req) {
    switch(elem->status) {
    case INTERNAL_ERROR:
        req->status = HTTP_INTERNAL_SERVER_ERROR;
        break;
    case BAD_UPSTREAM:
        req->status = HTTP_BAD_GATEWAY;
        break;
    case HOST_UNREACHEABLE:
        req->status = HTTP_NOT_FOUND;
        break;
    default: 
        break;
    }
}


HttpState cache_process_request(Cache *cache, HttpRequest* req) {
    assert(cache != NULL);
    assert(req != NULL);

    pthread_mutex_lock(&cache->lock);
    CacheElem *elem = cache_peek(cache, req->request_line);
    if(elem != NULL) {
        HttpState state = process_cache_hit(cache, elem, req);
        if(state != HTTP_PROCESS_REQUEST) {
            pthread_mutex_unlock(&cache->lock);
            return state;
        }
    }
    bool starting_loader = (elem == NULL || elem->status == INTERNAL_ERROR || ttl_expired(elem));
    if(starting_loader) {
        elem = prepare_elem_for_loading(cache, elem, req);
        if(elem == NULL) {
            req->status = HTTP_INTERNAL_SERVER_ERROR;
            pthread_mutex_unlock(&cache->lock);
            return HTTP_TERMINATE_REQUEST;
        }
    }
    elem->nreaders += 1;
    if(elem->nreaders == 1) {
        cache->busy_elems += 1;
    }
    pthread_mutex_unlock(&cache->lock);

    // starting loader or read response
    if(starting_loader) {
        LOG_DEBUG("Starting loader for [%.*s]\n", (int)req->request_line.len - 2, req->request_line.data);
        int ret = start_loader(elem, req);
        if(ret == ERROR) {
            elem->status = INTERNAL_ERROR;
            req->status = HTTP_INTERNAL_SERVER_ERROR;
            int err = futex(&elem->last, FUTEX_WAKE_PRIVATE, INT_MAX);
            if(err == -1) {
                LOG_ERRNO(errno, "start error futex() wake failed");
            }
        }
    }
    HttpState transfer_state;
    if(elem->status != INTERNAL_ERROR) {
        transfer_state = transfer_cache_elem(elem, req);
        if(transfer_state == HTTP_TERMINATE_REQUEST) {
            handle_cache_elem_transfer_err(elem, req);
            if(req->status == HTTP_OK) {
                req->status = HTTP_INTERNAL_SERVER_ERROR;
            }
        }
    }

    pthread_mutex_lock(&cache->lock);
    elem->nreaders -= 1;
    if(elem->nreaders == 0) {
        cache->busy_elems -= 1;
        pthread_cond_broadcast(&cache->evict_cond);
    }
    pthread_mutex_unlock(&cache->lock);
    return transfer_state;
}


void cache_destroy(Cache *cache) {
    if(cache == NULL) return;

    // destroy allocated strings
    QueueNode *cur = cache->lru->head;
    while(cur != NULL) {
        CacheElem *elem = cur->value;
        cur = cur->next;
        free(elem->key.data);
        buffer_destroy(elem->res);
    }

    pthread_cond_destroy(&cache->evict_cond);
    pthread_mutex_destroy(&cache->lock);
    free(cache->elem_pool);
    free(cache->qnode_pool);
    queue_destroy(cache->lru);
    hashmap_destroy(cache->map);
    free(cache);
}
