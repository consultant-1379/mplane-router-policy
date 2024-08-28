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
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache

import javax.inject.Inject

class MSToNodesMappingRepCacheSpec extends CdiSpecification {

    @Inject
    MSToNodesMappingRepCache msToNodesMappingRepCache

    def setup() {
        msToNodesMappingRepCache.registerListener()
    }

    def 'add mapping to cache and then get value'(){

        given: 'a list of two nodes'
        def nodes = (1..2).collect { "Node$it" }

        when: 'adding mapping to cache'
        msToNodesMappingRepCache.addToCache("M-plane 1",nodes)

        then: 'cache correctly updated'
        assert msToNodesMappingRepCache.getMSToNodesMapping("M-plane 1") == nodes

    }

    def 'add multiple mappings to cache and then get cache size'(){

        given: 'a list of two nodes'
        def nodes = Arrays.asList("Node1","Node2")

        when: 'adding mutiple mappings to cache'
        msToNodesMappingRepCache.addToCache("M-plane 1",nodes)
        msToNodesMappingRepCache.addToCache("M-plane 2",nodes)

        then: 'cache size correctly updated'
        assert msToNodesMappingRepCache.getAllValues().size() == 2

    }

    def 'When removing an entry in cache by a specific key, it will be emptied from cache'(){

        given: 'a list of two nodes'
        def nodes = (1..2).collect { "Node$it" }
        msToNodesMappingRepCache.clearCache()
        msToNodesMappingRepCache.addToCache("M-plane 1",nodes)

        when: 'removing from cache'
        msToNodesMappingRepCache.removeNodeToMSMapping("M-plane 1")

        then: 'cache correctly updated'
        assert msToNodesMappingRepCache.empty

    }

}
