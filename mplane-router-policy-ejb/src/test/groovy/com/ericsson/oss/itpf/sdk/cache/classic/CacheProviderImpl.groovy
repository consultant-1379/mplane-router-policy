/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.itpf.sdk.cache.classic

import com.ericsson.cds.cdi.support.providers.stubs.InMemoryCache
import com.ericsson.oss.itpf.sdk.cache.classic.CacheConfiguration
import com.ericsson.oss.itpf.sdk.cache.classic.CacheProviderSPI

import javax.cache.Cache

class CacheProviderImpl<K, V> implements CacheProviderSPI {

    private static Map<String, Cache<K, V>> cacheMap = new HashMap<>();

    @Override
    def <K, V> Cache<K, V> getCache(String cacheName) {
        return getOrCreateCache(cacheName)
    }

    @Override
    def <K, V> Cache<K, V> getSingletonCache(String cacheName) {
        return getOrCreateCache(cacheName)
    }

    @Override
    def <K, V> Cache<K, V> createCache(String cacheName, CacheConfiguration cacheConfiguration) {
        return getOrCreateCache(cacheName)
    }

    @Override
    def <K, V> Cache<K, V> createSingletonCache(String cacheName, CacheConfiguration cacheConfiguration) {
        return getOrCreateCache(cacheName)
    }

    @Override
    def <K, V> Cache<K, V> createCache(String modeledCacheName) {
        return getOrCreateCache(modeledCacheName)
    }

    def static <K, V>  Cache<K, V> getOrCreateCache(String cacheName) {
        def cache = cacheMap.get(cacheName)
        if(cache == null) {
            cache = new InMemoryCache(cacheName);
            cacheMap.put(cacheName, cache);
        }
        return cache as Cache<K, V>;
    }

}
