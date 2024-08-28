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

package com.ericsson.oss.mediation.mplane.policy.ejb

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache
import org.slf4j.Logger

import javax.inject.Inject

class NodeToMSMappingRepCacheSpec extends CdiSpecification {

    @Inject
    NodeToMSMappingRepCache nodeToMSMappingRepCache

    @Inject
    Logger logger

    def setup() {
        nodeToMSMappingRepCache.registerListener()
    }

    def 'add mapping to cache and then get value'(){

        when: 'adding mapping to cache'
        nodeToMSMappingRepCache.addToCache("Node1","M-plane 1")

        then: 'cache correctly updated'
        assert nodeToMSMappingRepCache.getNodeToMSMapping("Node1") == "M-plane 1"

    }

    def 'add multiple mappings to cache and then get cache size'(){

        when: 'adding multiple mappings to cache'
        nodeToMSMappingRepCache.addToCache("Node1","M-plane 1")
        nodeToMSMappingRepCache.addToCache("Node2","M-plane 2")

        then: 'cache size correctly updated'
        assert nodeToMSMappingRepCache.getAllValues().size() == 2

    }

    def 'When removing an entry in cache by a specific key, it will be emptied from cache'(){

        given: 'a list of two nodes'
        nodeToMSMappingRepCache.clearCache()
        nodeToMSMappingRepCache.addToCache("Node1","M-plane 1")

        when: 'removing from cache'
        nodeToMSMappingRepCache.removeNodeToMSMapping("Node1")

        then: 'cache correctly updated'
        assert nodeToMSMappingRepCache.empty

    }

}
