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

package com.ericsson.oss.mediation.mplane.policy.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache;
import org.slf4j.Logger;

/**
 * This class represents the startup bean for initializing Mplane Router Policy EJB.
 * It initializes caches.
 */
@Startup
@Singleton
public class MplaneRouterPolicyEjbStartUP {

    @Inject
    private MSToNodesMappingRepCache msToNodesMappingRepCache;

    @Inject
    private NodeToMSMappingRepCache nodeToMSMappingRepCache;

    @Inject
    private Logger logger;

    /**
     * Initializes the Mplane Router Policy EJB.
     * It registers listeners for cache initialization.
     */
    @PostConstruct
    public void init() {
        msToNodesMappingRepCache.registerListener();
        nodeToMSMappingRepCache.registerListener();
        logger.info("MSToNodesMappingRepCache and NodeToMSMappingRepCache initialized");
    }

}
