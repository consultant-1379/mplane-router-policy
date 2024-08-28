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

import com.ericsson.oss.itpf.sdk.eventbus.Channel;
import com.ericsson.oss.itpf.sdk.eventbus.ChannelConfiguration;
import com.ericsson.oss.itpf.sdk.eventbus.ChannelConfigurationBuilder;
import com.ericsson.oss.itpf.sdk.eventbus.ChannelLocator;
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * This is a Producer class which sends @{@link NodeConnectivityStatus} events to MplaneAlarmSupervisionQueue
 *
 */
@ApplicationScoped
public class MplaneRouterPolicyProducer {

    private static Logger logger = LoggerFactory.getLogger(MplaneRouterPolicyProducer.class);
    private static final String MPLANE_ALARM_SUPERVISION_CHANNEL_URI = "jms:/queue/MplaneAlarmSupervisionQueue";

    @Inject
    private ChannelLocator channelLocator;
    private Channel mplaneAlarmSupervisionQueueChannel;

    /**
     *  Looks up queue and configures channel
     */
    @PostConstruct
    private void lookUpChannel() {
        try {
            ChannelConfiguration channelConfiguration = (new ChannelConfigurationBuilder().build());
            this.mplaneAlarmSupervisionQueueChannel = channelLocator.lookupAndConfigureChannel(MPLANE_ALARM_SUPERVISION_CHANNEL_URI, channelConfiguration);
        } catch (Exception e) {
            logger.error("Failed to look up {} : Exception caught, {}", MPLANE_ALARM_SUPERVISION_CHANNEL_URI, e.getMessage());
        }
    }

    /**
     *  Sends event to MplaneAlarmSupervisionQueue
     *
     * @param nodeConnectivityStatusEvent
     */
    public void sendEvent(final NodeConnectivityStatus nodeConnectivityStatusEvent) {
        try {
            if (this.mplaneAlarmSupervisionQueueChannel == null) {
                lookUpChannel();
            }

            if (this.mplaneAlarmSupervisionQueueChannel != null) {
                this.mplaneAlarmSupervisionQueueChannel.send(nodeConnectivityStatusEvent);
                logger.debug("NodeConnectivityStatus sent Successfully");
            } else {
                logger.error("Failed to send NodeConnectivityStatus event: Channel is null after lookup");
            }
        } catch (final Exception e) {
            logger.error("Failed to send NodeConnectivityStatus event: Exception caught, {}", e.getMessage());
        }
    }
}
