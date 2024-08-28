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

import com.ericsson.oss.itpf.sdk.eventbus.annotation.Consumes;
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus;
import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import javax.cache.CacheException;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a consumer class listening to MPlaneConnectionListenerQueue for @{@link NodeConnectivityStatus} events
 *
 */
@ApplicationScoped
public class MPlaneConnectionListenerQueueConsumer {

    private static Logger LOGGER = LoggerFactory.getLogger(MPlaneConnectionListenerQueueConsumer.class);
    private static final String QUEUE_URI = "jms:/queue/MPlaneConnectionListenerQueue";

    @Inject
    private MSToNodesMappingRepCache msToNodeMappingCache;

    @Inject
    private NodeToMSMappingRepCache nodeToMsMappingCache;

    @Inject
    private MplaneRouterPolicyProducer mplaneRouterPolicyProducer;


    /**
     * Listener for MPlaneConnectionListenerQueue
     *
     * @param nodeConnectivityStatusEvent
     */
    public void nodeConnectivityStatusConsumer(@Observes @Consumes(endpoint = QUEUE_URI) final NodeConnectivityStatus nodeConnectivityStatusEvent) {
        LOGGER.debug("NodeConnectivityStatus {} event received in queue for FDN {} from {}", nodeConnectivityStatusEvent.getConnectionStatus(), nodeConnectivityStatusEvent.getNetworkElementFdn(), nodeConnectivityStatusEvent.getMsmplaneInstance());
        LOGGER.debug("NodeConnectivityStatus event received in queue {}", nodeConnectivityStatusEvent);
        processEventOnConnectionStatus(nodeConnectivityStatusEvent);
        mplaneRouterPolicyProducer.sendEvent(nodeConnectivityStatusEvent);
    }

    private void processEventOnConnectionStatus(NodeConnectivityStatus nodeConnectivityStatusEvent) {
        final ConnectionStatus status = nodeConnectivityStatusEvent.getConnectionStatus();
        final String node = nodeConnectivityStatusEvent.getNetworkElementFdn();
        final String instance = nodeConnectivityStatusEvent.getMsmplaneInstance();
        List<String> listOfNodes = msToNodeMappingCache.getMSToNodesMapping(instance);

        try {
            if (status == ConnectionStatus.CONNECTED) {
                synchronized (this) {
                    nodeToMsMappingCache.addToCache(node, instance);

                    if (listOfNodes == null) {
                        listOfNodes = new ArrayList<String>();
                    }
                    if (!listOfNodes.contains(node)) {
                        listOfNodes.add(node);
                        msToNodeMappingCache.addToCache(instance, listOfNodes);
                    }

                }
            } else if (status == ConnectionStatus.DISCONNECTED) {
                synchronized (this) {
                    nodeToMsMappingCache.removeNodeToMSMapping(node);
                    msToNodeMappingCache.removeNodeFromCache(instance, node);
                }
            }
        } catch (CacheException e) {
            LOGGER.error("A cache exception occurred when modifying the cache.");
        }
    }

}