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

package com.ericsson.oss.mediation.mplane.policy.ejb.cache.listener;

import org.slf4j.Logger;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.inject.Inject;
import java.util.List;

/**
 * Listener implementation for handling cache entry creation events in MSToNodesMappingRepCache.
 * This listener logs a message when a cache entry is created successfully.
 */
public class MSToNodesMappingRepCacheListener implements CacheEntryCreatedListener<String, List<String>> {

    @Inject
    Logger logger;

    /**
     * Constructs a new instance of MSToNodesMappingRepCacheListener.
     */
    public MSToNodesMappingRepCacheListener() {

    }


    /**
     * Invoked when one or more cache entries have been created.
     * @param iterable The iterable collection of cache entry events.
     * @throws CacheEntryListenerException If an exception occurs while processing the cache entry event.
     */
    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends List<String>>> iterable) throws CacheEntryListenerException {
        try {
            for (CacheEntryEvent<? extends String, ? extends List<String>> event : iterable) {
                String key = event.getKey();
                List<String> value = event.getValue();

                logger.info("Cache entry created - Key: {}, Value: {}", key, value);
            }
        } catch (CacheEntryListenerException e) {
            logger.error("Error processing cache entry events", e);
        }
    }
}
