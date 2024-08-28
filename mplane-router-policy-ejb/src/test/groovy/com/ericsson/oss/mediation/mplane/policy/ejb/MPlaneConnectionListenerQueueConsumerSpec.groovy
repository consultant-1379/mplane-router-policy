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
import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus

import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache
import javax.inject.Inject

import org.slf4j.Logger

class MPlaneConnectionListenerQueueConsumerSpec extends CdiSpecification {

    @ObjectUnderTest
    private MPlaneConnectionListenerQueueConsumer mPlaneConnectionListenerQueueConsumer

    @Inject
    MSToNodesMappingRepCache msToNodesMappingRepCache

    @Inject
    NodeToMSMappingRepCache nodeToMSMappingRepCache

    def cleanup(){
        msToNodesMappingRepCache.clearCache()
        nodeToMSMappingRepCache.clearCache()
    }

    def "When NodeConnectivityStatus message received on queue, the message is logged"() {

        given: 'NodeConnectivityStatus message gets created'
            def message = createNodeConnectivityStatusMessage(status)
            mPlaneConnectionListenerQueueConsumer.LOGGER = Mock(Logger)

        when: 'Message is received by consumer'
            mPlaneConnectionListenerQueueConsumer.nodeConnectivityStatusConsumer(message)

        then: 'Verify NodeConnectivityStatus message is logged'
            1 * mPlaneConnectionListenerQueueConsumer.LOGGER.debug('NodeConnectivityStatus {} event received in queue for FDN {} from {}', status, message.getNetworkElementFdn(), message.getMsmplaneInstance())

        where:
            status                        | _
            ConnectionStatus.CONNECTED    | _
            ConnectionStatus.DISCONNECTED | _

    }
    def "When NodeConnectivityStatus is received as CONNECTED, the node and instance are added to the ReplicatedCache tables"() {

        given: 'NodeConnectivityStatus gets created, Node and Instance are already in ReplicatedCache tables'
            def status = ConnectionStatus.CONNECTED
            def message = createNodeConnectivityStatusMessage(status)
            nodeToMSMappingRepCache.addToCache("NetworkElement=ORU123","msmplane-2")
            msToNodesMappingRepCache.addToCache("msmplane-1",["NetworkElement=ORU123"])

        when: 'Message is received by the consumer'
            mPlaneConnectionListenerQueueConsumer.nodeConnectivityStatusConsumer(message)

        then: 'Verify duplicates for msToNode cache are not added and existing mapping for nodeToMs cache is overwritten'
            assert nodeToMSMappingRepCache.getNodeToMSMapping("NetworkElement=ORU123") == "msmplane-1"
            assert msToNodesMappingRepCache.getMSToNodesMapping("msmplane-1") == ["NetworkElement=ORU123"]

    }

    def "When NodeConnectivityStatus is received as DISCONNECTED, the node and instance are removed from the ReplicatedCache tables"() {
        given: 'NodeConnectivityStatus gets created'
            def status = ConnectionStatus.DISCONNECTED
            def message = createNodeConnectivityStatusMessage(status)

            nodeToMSMappingRepCache.addToCache("NetworkElement=ORU123","msmplane-1")
            msToNodesMappingRepCache.addToCache("msmplane-1",["NetworkElement=ORU123"])

        when: 'Message is received by the consumer'
            mPlaneConnectionListenerQueueConsumer.nodeConnectivityStatusConsumer(message)

        then: 'msmplane instance and node get removed from the ReplicatedCache tables.'
            assert nodeToMSMappingRepCache.isEmpty()
            assert msToNodesMappingRepCache.isEmpty()
    }

    def createNodeConnectivityStatusMessage(ConnectionStatus status) {
        def networkElement = "NetworkElement=ORU123"
        def msmplaneInstance = "msmplane-1"

        return new NodeConnectivityStatus(networkElement, status, msmplaneInstance)
    }
}