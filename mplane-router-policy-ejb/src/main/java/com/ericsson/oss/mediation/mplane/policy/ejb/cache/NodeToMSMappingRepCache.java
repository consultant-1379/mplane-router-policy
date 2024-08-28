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
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.listener.NodeToMSMappingRepCacheListener;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * A replicated cache that is shared across both mplanerouterpolicy cluster VMs.
 * The contents of the cache are replicated on each VM (i.e., the data is identical).
 * It stores node instance to MSMPLane instance which refers to the managing MSMplane instance that manages that specific node instance's connection.
 */
public class NodeToMSMappingRepCache {

    @Inject
    private Logger logger;
    @Inject
    private NodeToMSMappingRepCacheListener listener;

    private static final String CACHE_NAME = "NodeToMSMappingRepCache";
    private final Cache<String, String> cache;

    /**
     * Constructs an instance of NodeToMSMappingRepCache initializing the cache.
     */
    public NodeToMSMappingRepCache() {

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
        final CacheEntryListenerConfiguration<String, String> listenerConfig = new ServiceFrameworkCacheEntryListenerConfiguration<>(listener);
        cache.registerCacheEntryListener(listenerConfig);
    }

    /**
     * Adds a key-value pair to the cache.
     * @param key The key is the node instance to be added.
     * @param value The value is the MSMPlane instance to be added.
     */
    public void addToCache(final String key,final String value) {
        this.cache.put(key, value);
    }

    /**
     * Retrieves the value associated with the specified key from the cache.
     * @param key The key is the node instance whose associated value is to be returned.
     * @return The value is the MSMPlane instance to which the specified key is mapped.
     */
    public String getNodeToMSMapping(final String key) {
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
     * Clears the cache.
     */
    public void clearCache() { this.cache.removeAll(); }

    /**
     * Retrieves all the values from the cache.
     * @return A list containing all the values stored in the cache.
     */
    public List<String> getAllValues() {
        final List<String> values = new ArrayList<>();
        try {
            for (Cache.Entry<String, String> entry : this.cache) {
                if (entry != null) {
                    values.add(entry.getValue());
                }
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