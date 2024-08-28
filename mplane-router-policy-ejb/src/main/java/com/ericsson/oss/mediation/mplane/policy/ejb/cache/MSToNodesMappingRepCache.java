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

package com.ericsson.oss.mediation.mplane.policy.ejb.cache;

import com.ericsson.oss.itpf.modeling.annotation.cache.CacheDefinition;
import com.ericsson.oss.itpf.modeling.annotation.cache.CacheMode;
import com.ericsson.oss.itpf.modeling.annotation.cache.EvictionStrategy;
import com.ericsson.oss.itpf.sdk.cache.classic.CacheConfiguration;
import com.ericsson.oss.itpf.sdk.cache.classic.CacheProviderBean;
import com.ericsson.oss.itpf.sdk.cache.util.ServiceFrameworkCacheEntryListenerConfiguration;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.listener.MSToNodesMappingRepCacheListener;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * A replicated cache that is shared across all mplanerouterpolicy cluster vms The
 * contents of the cache are replicated on each vm (i.e. the data is identical)
 * It stores the MSMPlane Instances which is the mediation instance and maps it to the list of nodes that instance is handling a connection for.
 *
 */
public class MSToNodesMappingRepCache {

    @Inject
    private Logger logger;
    @Inject
    private MSToNodesMappingRepCacheListener listener;

    private static final String CACHE_NAME = "MSToNodesMappingRepCache";
    private final Cache<String, List<String>> cache;

    /**
     * Constructs an instance of MSToNodesMappingRepCache initializing the cache.
     */
    public MSToNodesMappingRepCache() {

        final CacheConfiguration cacheConfiguration = new CacheConfiguration.Builder()
                .transactional(false)
                .evictionStrategy(EvictionStrategy.LRU)
                .cacheMode(CacheMode.REPLICATED_SYNC)
                .timeToLive(CacheDefinition.IMMORTAL)
                .build();

        final CacheProviderBean bean = new CacheProviderBean();
        cache = bean.createOrGetCache(CACHE_NAME, cacheConfiguration);
    }

    /**
     * Registers a cache entry listener for this cache.
     */
    public void registerListener() {
        final CacheEntryListenerConfiguration<String, List<String>> listenerConfig = new ServiceFrameworkCacheEntryListenerConfiguration<>(
                listener);
        cache.registerCacheEntryListener(listenerConfig);
    }

    /**
     * Adds a key-value pair to the cache.
     * @param key The key is the MSMPlane Instance to be added.
     * @param value The value is the List of Nodes to be added.
     */
    public void addToCache(final String key,final List<String> value) {
        this.cache.put(key, value);
    }

    /**
     * Retrieves the value associated with the specified key from the cache.
     * @param key The key whose associated value is to be returned.
     * @return The list of nodes to which the specified key is mapped.
     */
    public List<String> getMSToNodesMapping(final String key) {
        return this.cache.get(key);
    }

    /**
     * Removes the mapping for a key from the cache if it is present.
     * @param key The key whose mapping is to be removed from the cache.
     */
    public void removeNodeToMSMapping(final String key) {
        this.cache.remove(key);
    }

    /**
     * Removes a value from the list in the cache.
     * @param key The key is the MSMPlane Instance.
     * @param value The value is the Node in the list to be removed.
     */
    public void removeNodeFromCache(final String key, final String value) {
        List<String> values = this.cache.get(key);
        if (values != null) {
            values.remove(value);
            if (values.isEmpty()) {
                this.cache.remove(key);
            } else {
                this.cache.put(key, values);
            }
        }
    }

    /**
     * Clears the cache.
     */
    public void clearCache() { this.cache.removeAll(); }

    /**
     * Retrieves all the values from the cache.
     * @return A list containing all the listed nodes stored in the cache.
     */
    public List<List<String>> getAllValues() {
        final List<List<String>> values = new ArrayList<>();
        try {
            for (Cache.Entry<String, List<String>> entry : this.cache) {
                values.add(entry.getValue());
            }
        } catch (final CacheException e) {
            logger.error("A cache exception occurred during cache iteration", e);
        }
        return values;
    }

    /**
     * Checks if the cache is empty.
     * @return true if the cache contains no key-value mappings; false otherwise.
     */
    public boolean isEmpty() { return getAllValues().isEmpty();}
}
