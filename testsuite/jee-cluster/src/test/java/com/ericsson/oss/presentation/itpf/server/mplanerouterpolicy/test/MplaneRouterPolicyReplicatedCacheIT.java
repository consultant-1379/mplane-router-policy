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

import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.BEANS_XML_FILE;
import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.MANIFEST_MF_FILE;
import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.addEarRequiredLibraries;
import static com.ericsson.oss.presentation.itpf.server.mplanerouterpolicy.test.Artifact.createModuleArchive;

import com.ericsson.oss.mediation.mplane.policy.ejb.cache.MSToNodesMappingRepCache;
import com.ericsson.oss.mediation.mplane.policy.ejb.cache.NodeToMSMappingRepCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for testing the mplane-router-policy resource via Arquillian.
 */
@RunWith(Arquillian.class)
public final class MplaneRouterPolicyReplicatedCacheIT {

    private static final Logger logger = LoggerFactory.getLogger(MplaneRouterPolicyReplicatedCacheIT.class);

    @Inject
    MSToNodesMappingRepCache msToNodesMappingRepCache;

    @Inject
    NodeToMSMappingRepCache nodeToMSMappingRepCache;

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
    public void clearCache() {
        msToNodesMappingRepCache.clearCache();
        nodeToMSMappingRepCache.clearCache();
    }

    @Deployment(name = "MplaneRouterPolicyReplicatedCacheDeployment")
    public static Archive<?> createTestArchive() {
        logger.info("Creating deployment: MplaneRouterPolicyReplicatedCacheDeployment");
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "MplaneRouterPolicyReplicatedCacheTestEar.ear");
        addEarRequiredLibraries(ear);
        ear.addAsModule(createModuleArchive());
        ear.setManifest(MANIFEST_MF_FILE);
        ear.addAsApplicationResource(BEANS_XML_FILE);
        ear.addAsLibrary(ShrinkWrap.create(JavaArchive.class).addClass(MplaneRouterPolicyReplicatedCacheIT.class));
        return ear;
    }

    @Test
    @InSequence(1)
    public void testAddMSToNodesMappingToCache() {
        List<String> nodes = Arrays.asList("Node 1", "Node 2");

        msToNodesMappingRepCache.addToCache("M-plane 1", nodes);
        msToNodesMappingRepCache.addToCache("M-plane 2", nodes);

        List<List<String>> expectedMappings = Arrays.asList(nodes, nodes);

        List<List<String>> mappings = msToNodesMappingRepCache.getAllValues();
        assertEquals("The expected values were returned", expectedMappings, mappings);
    }

    @Test
    @InSequence(2)
    public void testAddNodesToMSMappingToCache() {
        nodeToMSMappingRepCache.addToCache("Node 1", "M-plane 1");
        nodeToMSMappingRepCache.addToCache("Node 2", "M-plane 2");

        List<String> expectedMappings = Arrays.asList("M-plane 2", "M-plane 1");

        List<String> mappings = nodeToMSMappingRepCache.getAllValues();

        assertEquals("The expected values were returned", expectedMappings, mappings);
    }

    @Test
    @InSequence(3)
    public void testRemoveMSToNodesMappingFromCache() {
        List<String> nodes = Arrays.asList("Node 1", "Node 2");

        msToNodesMappingRepCache.addToCache("M-plane 1", nodes);
        msToNodesMappingRepCache.addToCache("M-plane 2", nodes);
        msToNodesMappingRepCache.addToCache("M-plane 3", nodes);
        msToNodesMappingRepCache.removeNodeToMSMapping("M-plane 2");

        List<List<String>> expectedMappings = Arrays.asList(nodes, nodes);

        List<List<String>> mappings = msToNodesMappingRepCache.getAllValues();
        List<String> getReturn = msToNodesMappingRepCache.getMSToNodesMapping("M-plane 2");

        assertEquals("The expected values were returned", expectedMappings, mappings);
        assertNull("The Expected null returned", getReturn);
    }

    @Test
    @InSequence(4)
    public void testRemoveNodesToMSMappingFromCache() {
        nodeToMSMappingRepCache.addToCache("Node 1", "M-plane 1");
        nodeToMSMappingRepCache.addToCache("Node 2", "M-plane 2");
        nodeToMSMappingRepCache.addToCache("Node 3", "M-plane 3");
        nodeToMSMappingRepCache.removeNodeToMSMapping("Node 2");

        List<String> expectedMappings = Arrays.asList("M-plane 3", "M-plane 1");

        List<String> mappings = nodeToMSMappingRepCache.getAllValues();
        String getReturn = nodeToMSMappingRepCache.getNodeToMSMapping("Node 2");

        assertEquals("The expected values were returned", expectedMappings, mappings);
        assertNull("The Expected null returned", getReturn);
    }
}
