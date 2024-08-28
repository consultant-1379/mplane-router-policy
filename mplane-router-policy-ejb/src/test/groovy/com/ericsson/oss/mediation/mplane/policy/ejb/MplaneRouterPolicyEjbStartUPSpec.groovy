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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache
import org.slf4j.Logger

import javax.inject.Inject

class MplaneRouterPolicyEjbStartUPSpec extends CdiSpecification {

    @MockedImplementation
    private MSToNodesMappingRepCache msToNodesMappingRepCache

    @MockedImplementation
    private NodeToMSMappingRepCache nodeToMSMappingRepCache

    @Inject
    Logger logger

    @ObjectUnderTest
    MplaneRouterPolicyEjbStartUP ejbStartUP

    def "test init method initializes caches and registers listeners"() {

        given: 'set logger'
        ejbStartUP .logger = logger

        when: 'calling init method'
        ejbStartUP.init()

        then: 'registerListener is called on MSToNodesMappingRepCache and NodeToMSMappingRepCache'
        1 * msToNodesMappingRepCache.registerListener()
        1 * nodeToMSMappingRepCache.registerListener()

        and: 'logger.info method is called once'
        1 * logger.info("MSToNodesMappingRepCache and NodeToMSMappingRepCache initialized")
    }
}

