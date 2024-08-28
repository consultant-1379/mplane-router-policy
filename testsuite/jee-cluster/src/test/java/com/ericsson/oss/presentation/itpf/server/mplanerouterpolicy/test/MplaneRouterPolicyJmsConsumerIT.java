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

package com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test;
import com.ericsson.oss.itpf.sdk.eventbus.Channel;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Endpoint;

import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.BEANS_XML_FILE;
import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.MANIFEST_MF_FILE;
import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.addEarRequiredLibraries;
import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.createModuleArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus;
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus;
import com.ericsson.oss.mediation.mplane.policy.ejb.MPlaneConnectionListenerQueueConsumer;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Integration tests for testing the mplane-router-policy JMS consumer resource via Arquillian.
 */
@RunWith(Arquillian.class)
public final class MplaneRouterPolicyJmsConsumerIT {

    private static final Logger logger = LoggerFactory.getLogger(MplaneRouterPolicyJmsConsumerIT.class);
    private final LogChecker logChecker = new LogChecker();

    @Inject
    @Endpoint("jms:/queue/MPlaneConnectionListenerQueue")
    private Channel mplaneConnectionQueueListenerChannel;

    @Inject
    NodeToMSMappingRepCache nodeToMSMappingRepCache;

    @Inject
    MSToNodesMappingRepCache msToNodesMappingRepCache;

    @Inject
    MPlaneConnectionListenerQueueConsumer mPlaneConnectionListenerQueueConsumer;

    @Deployment(name = "MplaneRouterPolicyJmsConsumerDeployment")
    public static Archive<?> createTestArchive() {
        logger.info("Creating deployment: MplaneRouterPolicyJmsConsumerDeployment");
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "MplaneRouterPolicyJmsConsumerTestEar.ear");
        addEarRequiredLibraries(ear);
        ear.addAsModule(createModuleArchive());
        ear.setManifest(MANIFEST_MF_FILE);
        ear.addAsApplicationResource(BEANS_XML_FILE);
        ear.addAsLibrary(ShrinkWrap.create(JavaArchive.class)
            .addClass(LogChecker.class)
            .addClass(MplaneRouterPolicyJmsConsumerIT.class));
        return ear;
    }

    @Rule
    public TestRule watcher = new TestWatcher() {

        @Override
        protected void starting(final Description description) {
            logger.info("*******************************");
            logger.info("Starting test: {}()", description.getMethodName());
        }

        @Override
        protected void finished(final Description description) {
            logger.info("Ending test: {}()", description.getMethodName());
            logger.info("*******************************");
        }
    };

    @Before
    public void setUp() {
        nodeToMSMappingRepCache.clearCache();
        msToNodesMappingRepCache.clearCache();
    }

    @Test
    @InSequence(1)
    public void connectionListenerQueueConsumingNodeConnectivityStatusConnectedTest() throws InterruptedException {
        String fdn = "NetworkElement=ORU123";
        String msmplaneInstance = "msmplane-1";
        ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;
        ArrayList<String> fdnList = new ArrayList<>();

        NodeConnectivityStatus nodeConnectivityStatusMessage = new NodeConnectivityStatus(fdn, connectionStatus, msmplaneInstance);
        fdnList.add(fdn);

        mplaneConnectionQueueListenerChannel.send(nodeConnectivityStatusMessage);
        TimeUnit.SECONDS.sleep(5);

        String consumerLogMessage = String.format("NodeConnectivityStatus %s event received in queue for FDN %s from %s", connectionStatus.toString(), fdn,
            msmplaneInstance);

        String  producerLogMessage= "NodeConnectivityStatus sent Successfully";

        assertTrue(logChecker.serverLogEntryFound("connectionListenerQueueConsumingNodeConnectivityStatusConnectedTest", consumerLogMessage));
        assertTrue(logChecker.serverLogEntryFound("connectionListenerQueueConsumingNodeConnectivityStatusConnectedTest", producerLogMessage));
        assertEquals(msToNodesMappingRepCache.getMSToNodesMapping(msmplaneInstance), fdnList);
        assertEquals(nodeToMSMappingRepCache.getNodeToMSMapping(fdn), msmplaneInstance);
    }

    @Test
    @InSequence(2)
    public void connectionListenerQueueConsumingNodeConnectivityStatusDisconnectedTest() throws InterruptedException {
        String msmplaneInstance = "msmplane-1";
        String fdn = "NetworkElement=ORU123";
        ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
        ArrayList<String> listOfNodes = new ArrayList<>();
        listOfNodes.add(fdn);

        msToNodesMappingRepCache.addToCache(msmplaneInstance, listOfNodes);
        nodeToMSMappingRepCache.addToCache(fdn,msmplaneInstance);
        NodeConnectivityStatus nodeConnectivityStatusMessage = new NodeConnectivityStatus(fdn, connectionStatus, msmplaneInstance);

        mplaneConnectionQueueListenerChannel.send(nodeConnectivityStatusMessage);
        TimeUnit.SECONDS.sleep(5);

        String consumerLogMessage = String.format("NodeConnectivityStatus %s event received in queue for FDN %s from %s", connectionStatus.toString(), fdn,
            msmplaneInstance);

        String  producerLogMessage= "NodeConnectivityStatus sent Successfully";

        assertTrue(logChecker.serverLogEntryFound("connectionListenerQueueConsumingNodeConnectivityStatusDisconnectedTest", consumerLogMessage));
        assertTrue(logChecker.serverLogEntryFound("connectionListenerQueueConsumingNodeConnectivityStatusConnectedTest", producerLogMessage));
        assertTrue(msToNodesMappingRepCache.isEmpty());
        assertTrue(nodeToMSMappingRepCache.isEmpty());
    }

}