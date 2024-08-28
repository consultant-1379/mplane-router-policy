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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.listener.NodeToMSMappingRepCacheListener
import org.slf4j.Logger
import javax.cache.event.CacheEntryEvent
import javax.inject.Inject

class NodeToMSMappingRepCacheListenerSpec extends CdiSpecification {

    @Inject
    Logger logger

    @ObjectUnderTest
    NodeToMSMappingRepCacheListener listener

    def "test onCreated method logs message"() {

        given: 'a mock CacheEntryEvent'
        def cacheEntryEvent = Mock(CacheEntryEvent)

        when: 'calling onCreated method'
        listener.onCreated([cacheEntryEvent])

        then: 'logger.info method is called once'
        1 * logger.info('Cache entry created - Key: {}, Value: {}', _, _) >> null
    }
}

