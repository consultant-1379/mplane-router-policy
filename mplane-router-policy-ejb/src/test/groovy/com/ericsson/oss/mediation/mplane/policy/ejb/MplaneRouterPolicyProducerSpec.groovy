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
import com.ericsson.oss.itpf.sdk.eventbus.Channel
import com.ericsson.oss.itpf.sdk.eventbus.ChannelLocator
import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus
import org.slf4j.Logger


class MplaneRouterPolicyProducerSpec extends CdiSpecification {

    @ObjectUnderTest
    private MplaneRouterPolicyProducer mplaneRouterPolicyProducer

    private ChannelLocator channelLocator = Mock(ChannelLocator)
    private Channel channel = Mock(Channel)
    private Logger logger = Mock(Logger)

    def setup() {
        mplaneRouterPolicyProducer.channelLocator = channelLocator
        mplaneRouterPolicyProducer.logger = logger
        mplaneRouterPolicyProducer.lookUpChannel()
    }

    def "When lookUpChannel fails, an error is logged"() {
        given: 'A ChannelLocator that throws an exception'
            channelLocator.lookupAndConfigureChannel(_, _) >> { throw new RuntimeException("Lookup failed") }

        when: 'lookUpChannel method is called'
            mplaneRouterPolicyProducer.lookUpChannel()

        then: 'An error is logged'
            1 * logger.error("Failed to look up {} : Exception caught, {}", MplaneRouterPolicyProducer.MPLANE_ALARM_SUPERVISION_CHANNEL_URI, "Lookup failed")
    }

    def "When sendEvent is called with a valid event, the event is sent"() {
        given: 'A non-null channel and a NodeConnectivityStatus event'
            def message = createNodeConnectivityStatusMessage(status)
            mplaneRouterPolicyProducer.mplaneAlarmSupervisionQueueChannel = channel

        when: 'sendEvent method is called'
            mplaneRouterPolicyProducer.sendEvent(message)

        then: 'The event is sent through the channel'
            1 * channel.send(message)
            0 * logger.error(_, _)

        where:
            status                        | _
            ConnectionStatus.CONNECTED    | _
            ConnectionStatus.DISCONNECTED | _
    }

    def "When sendEvent fails, an error is logged"() {
        given: 'A channel that throws an exception on send and a NodeConnectivityStatus event'
            def message = createNodeConnectivityStatusMessage(status)
            mplaneRouterPolicyProducer.mplaneAlarmSupervisionQueueChannel = channel
            channel.send(_) >> { throw new RuntimeException("Send failed") }

        when: 'sendEvent method is called'
            mplaneRouterPolicyProducer.sendEvent(message)

        then: 'An error is logged'
            1 * logger.error("Failed to send NodeConnectivityStatus event: Exception caught, {}", "Send failed")

        where:
            status                        | _
            ConnectionStatus.CONNECTED    | _
            ConnectionStatus.DISCONNECTED | _
    }

    def "When sendEvent is called with a null channel, nothing happens"() {
        given: 'A null channel and a NodeConnectivityStatus event'
            def message = createNodeConnectivityStatusMessage(status)
            mplaneRouterPolicyProducer.mplaneAlarmSupervisionQueueChannel = null

        when: 'sendEvent method is called'
            mplaneRouterPolicyProducer.sendEvent(message)

        then: 'Nothing happens and no errors are logged'
            0 * logger.error(_, _)
            0 * channel.send(_)

        where:
            status                        | _
            ConnectionStatus.CONNECTED    | _
            ConnectionStatus.DISCONNECTED | _
    }

    def createNodeConnectivityStatusMessage(ConnectionStatus status) {
        def networkElement = "NetworkElement=ORU123"
        def msmplaneInstance = "msmplane-1"

        return new NodeConnectivityStatus(networkElement, status, msmplaneInstance)
    }
}