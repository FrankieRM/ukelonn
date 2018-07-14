/*
 * Copyright 2018 Steinar Bang
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
package no.priv.bang.ukelonn.api;

import static no.priv.bang.ukelonn.testutils.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.subject.WebSubject;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import no.priv.bang.ukelonn.api.beans.LoginCredentials;
import no.priv.bang.ukelonn.api.beans.LoginResult;
import no.priv.bang.ukelonn.beans.Account;
import no.priv.bang.ukelonn.beans.PerformedJob;
import no.priv.bang.ukelonn.beans.TransactionType;
import no.priv.bang.ukelonn.mocks.MockHttpServletResponse;
import no.priv.bang.ukelonn.mocks.MockLogService;

public class UkelonnRestApiServletTest extends ServletTestBase {

    @BeforeClass
    public static void setupForAllTests() {
        setupFakeOsgiServices();
    }

    @AfterClass
    public static void teardownForAllTests() throws Exception {
        releaseFakeOsgiServices();
    }

    @Test
    public void testLoginOk() throws Exception {
        // Set up the request
        LoginCredentials credentials = new LoginCredentials("jad", "1ad");
        HttpServletRequest request = buildLoginRequest(credentials);

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());
        createSubjectAndBindItToThread(request, response);

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the login
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertThat(result.getRoles().length).isGreaterThan(0);
        assertEquals("", result.getErrorMessage());
    }

    @Test
    public void testAdminLoginOk() throws Exception {
        // Set up the request
        LoginCredentials credentials = new LoginCredentials("admin", "admin");
        HttpServletRequest request = buildLoginRequest(credentials);

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet and do the login
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());
        createSubjectAndBindItToThread(request, response);

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the login
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertThat(result.getRoles().length).isGreaterThan(0);
        assertEquals("", result.getErrorMessage());
    }

    @Ignore("Gets wrong password exception instead of unknown user exception, don't know why")
    @Test
    public void testLoginUnknownUser() throws Exception {
        // Set up the request
        LoginCredentials credentials = new LoginCredentials("unknown", "unknown");
        HttpServletRequest request = buildLoginRequest(credentials);

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet and do the login
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());
        createSubjectAndBindItToThread(request, response);

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the login
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertEquals(0, result.getRoles().length);
        assertEquals("Unknown account", result.getErrorMessage());
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        // Set up the request
        LoginCredentials credentials = new LoginCredentials("jad", "wrong");
        HttpServletRequest request = buildLoginRequest(credentials);

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet and do the login
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());
        createSubjectAndBindItToThread(request, response);

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the login
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertEquals(0, result.getRoles().length);
        assertEquals("Wrong password", result.getErrorMessage());
    }

    @Test
    public void testLoginWrongJson() throws Exception {
        // Set up the request
        HttpServletRequest request = buildRequestFromStringBody("xxxyzzy");

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet and do the login
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());
        createSubjectAndBindItToThread(request, response);

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the login
        servlet.service(request, response);

        // Check the response
        assertEquals(400, response.getStatus());
        assertEquals("text/plain", response.getContentType());
    }

    /**
     * Shiro fails because there is no WebSubject bound to the thread.
     * @throws Exception
     */
    @Test
    public void testLoginInternalServerError() throws Exception {
        // Set up the request
        LoginCredentials credentials = new LoginCredentials("jad", "1ad");
        HttpServletRequest request = buildLoginRequest(credentials);

        // Create the response that will cause a NullPointerException when
        // trying to write the body
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);
        when(response.getWriter()).thenReturn(null);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Clear the Subject to ensure that Shiro will fail
        // no matter what order test methods are run in
        ThreadContext.remove();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the login
        servlet.service(request, response);

        // Check the response
        assertEquals(500, response.getStatus());
    }

    /**
     * Verify that a GET to the LoginServlet will return the current state
     * when a user is logged in
     *
     * Used to initialize webapp if the webapp is reloaded.
     *
     * @throws Exception
     */
    @Test
    public void testGetLoginStateWhenLoggedIn() throws Exception {
        // Set up the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/login"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/login");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create the response that will cause a NullPointerException
        // when trying to print the body
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Set up Shiro to be in a logged-in state
        WebSubject subject = createSubjectAndBindItToThread(request, response);
        UsernamePasswordToken token = new UsernamePasswordToken("jad", "1ad".toCharArray(), true);
        subject.login(token);

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Check the login state with HTTP GET
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertThat(result.getRoles().length).isGreaterThan(0);
        assertEquals("", result.getErrorMessage());
    }

    /**
     * Verify that a GET to the LoginServlet will return the current state
     * when no user is logged in
     *
     * Used to initialize webapp if the webapp is reloaded.
     *
     * @throws Exception
     */
    @Test
    public void testGetLoginStateWhenNotLoggedIn() throws Exception {
        // Set up the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/login"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/login");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create the response that will cause a NullPointerException
        // when trying to print the body
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Set up Shiro to be in a logged-in state
        WebSubject subject = createSubjectAndBindItToThread(request, response);
        subject.logout();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Check the login state with HTTP GET
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertEquals(0, result.getRoles().length);
        assertEquals("", result.getErrorMessage());
    }

    @Test
    public void testLogoutOk() throws Exception {
        // Set up the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/logout"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/logout");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Set up Shiro to be in a logged-in state
        loginUser(request, response, "jad", "1ad");

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the logout
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertEquals(0, result.getRoles().length);
        assertEquals("", result.getErrorMessage());
    }

    /**
     * Verify that logging out a not-logged in shiro, is harmless.
     *
     * @throws Exception
     */
    @Test
    public void testLogoutNotLoggedIn() throws Exception {
        // Set up the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/logout"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/logout");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create the response that will receive the login result
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Set up shiro
        createSubjectAndBindItToThread(request, response);

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Do the logout
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        LoginResult result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), LoginResult.class);
        assertEquals(0, result.getRoles().length);
        assertEquals("", result.getErrorMessage());
    }
    @Test
    public void testGetJobtypes() throws Exception {
        // Set up the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/jobtypes"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/jobtypes");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create the servlet that is to be tested
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();

        // Create mock OSGi services to inject and inject it
        MockLogService logservice = new MockLogService();
        servlet.setLogservice(logservice);

        // Inject fake OSGi service UkelonnService
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Call the method under test
        servlet.service(request, response);

        // Check the output
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        List<TransactionType> jobtypes = mapper.readValue(response.getOutput().toByteArray(), new TypeReference<List<TransactionType>>() {});
        assertEquals(4, jobtypes.size());
    }

    @Test
    public void testGetAccount() throws Exception {
        // Create the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/account/jad"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/account/jad");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Log the user in to shiro
        loginUser(request, response, "jad", "1ad");

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        double expectedAccountBalance = getUkelonnServiceSingleton().getAccount("jad").getBalance();
        Account result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), Account.class);
        assertEquals("jad", result.getUsername());
        assertEquals(expectedAccountBalance, result.getBalance(), 0.0);
    }

    /**
     * Test that verifies that a regular user can't access other users than the
     * one they are logged in as.
     *
     * @throws Exception
     */
    @Test
    public void testGetAccountOtherUsername() throws Exception {
        // Create the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/account/jod"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/account/jod");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Log the user in to shiro
        loginUser(request, response, "jad", "1ad");

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(403, response.getStatus());
    }

    /**
     * Test that verifies that an admin user can access other users than the
     * one they are logged in as.
     *
     * @throws Exception
     */
    @Test
    public void testGetAccountWhenLoggedInAsAdministrator() throws Exception {
        // Create the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/account/jad"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/account/jad");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Log the admin user in to shiro
        loginUser(request, response, "admin", "admin");

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        double expectedAccountBalance = getUkelonnServiceSingleton().getAccount("jad").getBalance();
        Account result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), Account.class);
        assertEquals("jad", result.getUsername());
        assertEquals(expectedAccountBalance, result.getBalance(), 0.0);
    }

    @Test
    public void testGetAccountNoUsername() throws Exception {
        // Create the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/account"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/account");
        when(request.getContextPath()).thenReturn("/ukelonn");
        when(request.getServletPath()).thenReturn("/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Log the user in to shiro
        loginUser(request, response, "jad", "1ad");

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        // (Looks like Jersey enforces the pathinfo element so the response is 404 "Not Found"
        // rather than the expected 400 "Bad request" (that the resource would send if reached))
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testRegisterJob() throws Exception {
        // Create the request
        Account account = getUkelonnServiceSingleton().getAccount("jad");
        double originalBalance = account.getBalance();
        List<TransactionType> jobTypes = getUkelonnServiceSingleton().getJobTypes();
        PerformedJob job = new PerformedJob(account, jobTypes.get(0).getId(), jobTypes.get(0).getTransactionAmount());
        String jobAsJson = ServletTestBase.mapper.writeValueAsString(job);
        HttpServletRequest request = buildRequestFromStringBody(jobAsJson);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/registerjob"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/registerjob");

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Log the user in to shiro
        loginUser(request, response, "jad", "1ad");

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Account result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), Account.class);
        assertEquals("jad", result.getUsername());
        assertThat(result.getBalance()).isGreaterThan(originalBalance);
    }

    /**
     * Test that verifies that a regular user can't update the job list of
     * other users than the one they are logged in as.
     *
     * @throws Exception
     */
    @Test
    public void testRegisterJobOtherUsername() throws Exception {
        // Create the request
        Account account = getUkelonnServiceSingleton().getAccount("jod");
        List<TransactionType> jobTypes = getUkelonnServiceSingleton().getJobTypes();
        PerformedJob job = new PerformedJob(account, jobTypes.get(0).getId(), jobTypes.get(0).getTransactionAmount());
        String jobAsJson = ServletTestBase.mapper.writeValueAsString(job);
        HttpServletRequest request = buildRequestFromStringBody(jobAsJson);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/registerjob"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/registerjob");

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Log the user in to shiro
        loginUser(request, response, "jad", "1ad");

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(403, response.getStatus());
    }

    /**
     * Test that verifies that an admin user register a job on the behalf
     * of a different user.
     *
     * @throws Exception
     */
    @Test
    public void testRegisterJobtWhenLoggedInAsAdministrator() throws Exception {
        // Create the request
        Account account = getUkelonnServiceSingleton().getAccount("jad");
        double originalBalance = account.getBalance();
        List<TransactionType> jobTypes = getUkelonnServiceSingleton().getJobTypes();
        PerformedJob job = new PerformedJob(account, jobTypes.get(0).getId(), jobTypes.get(0).getTransactionAmount());
        String jobAsJson = ServletTestBase.mapper.writeValueAsString(job);
        HttpServletRequest request = buildRequestFromStringBody(jobAsJson);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/registerjob"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/registerjob");

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Log the admin user in to shiro
        loginUser(request, response, "admin", "admin");

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());


        Account result = ServletTestBase.mapper.readValue(response.getOutput().toString(StandardCharsets.UTF_8.toString()), Account.class);
        assertEquals("jad", result.getUsername());
        assertThat(result.getBalance()).isGreaterThan(originalBalance);
    }

    @Test
    public void testRegisterJobNoUsername() throws Exception {
        // Create the request
        Account account = new Account();
        List<TransactionType> jobTypes = getUkelonnServiceSingleton().getJobTypes();
        PerformedJob job = new PerformedJob(account, jobTypes.get(0).getId(), jobTypes.get(0).getTransactionAmount());
        String jobAsJson = ServletTestBase.mapper.writeValueAsString(job);
        HttpServletRequest request = buildRequestFromStringBody(jobAsJson);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/registerjob"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/registerjob");

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Log the user in to shiro
        loginUser(request, response, "jad", "1ad");

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testRegisterJobUnparsablePostData() throws Exception {
        // Create the request
        HttpServletRequest request = buildRequestFromStringBody("this is not json");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/registerjob"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/registerjob");

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(400, response.getStatus());
    }

    /**
     * To provoked the internal server error, the user isn't logged in.
     * This causes a NullPointerException in the user check.
     *
     * (In a production environment this request without a login,
     * will be stopped by Shiro)
     *
     * @throws Exception
     */
    @Test
    public void testRegisterJobInternalServerError() throws Exception {
        // Create the request
        Account account = new Account();
        List<TransactionType> jobTypes = getUkelonnServiceSingleton().getJobTypes();
        PerformedJob job = new PerformedJob(account, jobTypes.get(0).getId(), jobTypes.get(0).getTransactionAmount());
        String jobAsJson = ServletTestBase.mapper.writeValueAsString(job);
        HttpServletRequest request = buildRequestFromStringBody(jobAsJson);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/ukelonn/api/registerjob"));
        when(request.getRequestURI()).thenReturn("/ukelonn/api/registerjob");

        // Create a response object that will receive and hold the servlet output
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        // Create mock OSGi services to inject
        MockLogService logservice = new MockLogService();

        // Create the servlet
        UkelonnRestApiServlet servlet = new UkelonnRestApiServlet();
        servlet.setLogservice(logservice);
        servlet.setUkelonnService(getUkelonnServiceSingleton());

        // Activate the servlet DS component
        servlet.activate();

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Clear the Subject to ensure that Shiro will fail
        // no matter what order test methods are run in
        ThreadContext.remove();

        // Run the method under test
        servlet.service(request, response);

        // Check the response
        assertEquals(500, response.getStatus());
    }

    private ServletConfig createServletConfigWithApplicationAndPackagenameForJerseyResources() {
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Arrays.asList(ServerProperties.PROVIDER_PACKAGES)));
        when(config.getInitParameter(eq(ServerProperties.PROVIDER_PACKAGES))).thenReturn("no.priv.bang.ukelonn.api.resources");
        ServletContext servletContext = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());
        return config;
    }

}
