/*
 * Copyright 2016-2017 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.ukelonn.backend;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;
import no.priv.bang.ukelonn.UkelonnService;

import static no.priv.bang.ukelonn.testutils.TestUtils.*;

public class CommonServiceMethodsTest {
    @Before
    public void setup() throws Exception {
        releaseFakeOsgiServices();
    }

    @AfterClass
    static public void completeCleanup() throws Exception {
        releaseFakeOsgiServices();
    }

    @Test(expected=RuntimeException.class)
    public void testConnectionCheckNoConnection() {
        UkelonnService service = CommonServiceMethods.connectionCheck(getClass(), null);
        assertNull(service);
    }

    @Test
    public void testConnectionCheck() throws Exception {
        setupFakeOsgiServices();
        UkelonnServiceProvider provider = getUkelonnServiceSingleton();
        UkelonnService service = CommonServiceMethods.connectionCheck(getClass(), provider);
        assertNotNull(service);
    }

    @Test
    public void testLogError() throws Exception {
        // First log when there are no services available
        MockLogService logservice = new MockLogService();
        CommonServiceMethods.logError(getClass(), null, "This is an error");
        assertEquals("Expect nothing to be logged", 0, logservice.getLogmessages().size());

        // Test the case where there is an UkelonnService but is no logservice
        setupFakeOsgiServices();
        UkelonnServiceProvider provider = getUkelonnServiceSingleton();
        CommonServiceMethods.logError(getClass(), provider, "This is another error");
        assertEquals("Still expected nothing logged", 0, logservice.getLogmessages().size());

        // Test the case where there is an UkelonnService with an injected logservice
        UkelonnServiceProvider ukelonnService = new UkelonnServiceProvider();
        ukelonnService.setLogservice(logservice);
        CommonServiceMethods.logError(getClass(), ukelonnService, "This is yet another error");
        assertEquals("Expected a single message to have been logged", 1, logservice.getLogmessages().size());
    }

    @Test
    public void testLogErrorWithException() throws Exception {
        // First log when there are no services available
        Exception exception = new Exception("This is a fake exception");
        MockLogService logservice = new MockLogService();
        CommonServiceMethods.logError(getClass(), null, "This is an error", exception);
        assertEquals("Expect nothing to be logged", 0, logservice.getLogmessages().size());

        // Test the case where there is an UkelonnService but is no logservice
        setupFakeOsgiServices();
        UkelonnServiceProvider provider = getUkelonnServiceSingleton();
        CommonServiceMethods.logError(getClass(), provider, "This is another error", exception);
        assertEquals("Still expected nothing logged", 0, logservice.getLogmessages().size());

        // Test the case where there is an UkelonnService with an injected logservice
        UkelonnServiceProvider ukelonnService = new UkelonnServiceProvider();
        ukelonnService.setLogservice(logservice);
        CommonServiceMethods.logError(getClass(), ukelonnService, "This is yet another error", exception);
        assertEquals("Expected a single message to have been logged", 1, logservice.getLogmessages().size());
    }

}
