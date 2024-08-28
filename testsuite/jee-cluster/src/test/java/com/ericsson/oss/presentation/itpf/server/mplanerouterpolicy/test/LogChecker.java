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

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to search for a specific string specified by the calling test in the
 * server.log of the jboss deploying and executing the test
 */
public class LogChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogChecker.class);

    public boolean serverLogEntryFound(String testName, String expectedString) {
        LOGGER.debug("Checking server.log for test: {}", testName);
        LOGGER.debug("Specified expected strings: {}", expectedString);

        boolean logOk = true;
        String jbossLog = System.getProperty("jboss.server.log.dir") + "/server.log";

        boolean found = false;

        try (ReversedLinesFileReader fileReader = new ReversedLinesFileReader(new File(jbossLog), Charset.defaultCharset())) {
            String line;
            while ((line = fileReader.readLine()) != null) {

                if (line.contains(expectedString)) {
                    LOGGER.debug("String encountered in line: {}", line);
                    found = true;
                    break;
                }
            }
        } catch (final Exception e) {
            LOGGER.debug("Encountered exception attempting to check server.log: {}", e.getMessage());
            logOk = false;
        }

        LOGGER.debug("logOk: {}", logOk);
        LOGGER.debug("found: {}", found);

        return logOk ? found : logOk;
    }
}
