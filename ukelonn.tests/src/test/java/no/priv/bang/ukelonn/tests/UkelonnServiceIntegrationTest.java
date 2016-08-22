package no.priv.bang.ukelonn.tests;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import no.priv.bang.ukelonn.UkelonnService;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class UkelonnServiceIntegrationTest extends UkelonnServiceIntegrationTestBase {
    private Bundle installWarBundle;
    String paxSwissboxVersion = "1.8.2";
    String paxWebVersion = "4.2.7";
    String xbeanVersion = "4.1";
    String httpcomponentsVersion = "4.3.3";
    String jettyVersion = "9.2.17.v20160517";
    String myFacesVersion = "2.2.6";

    @Inject
    private BundleContext bundleContext;

    private UkelonnService ukelonnService;
    private ServiceListener ukelonnServiceListener;

    @Configuration
    public Option[] config() {
        return options(
                       junitBundles(),
                       systemProperty("logback.configurationFile").value("file:src/test/resources/logback.xml"),
                       systemProperty("org.osgi.service.http.port").value("8081"),
                       systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
                       systemProperty("org.osgi.service.http.hostname").value("127.0.0.1"),
                       systemProperty("org.osgi.service.http.port").value("8181"),
                       systemProperty("java.protocol.handler.pkgs").value("org.ops4j.pax.url"),
                       systemProperty("org.ops4j.pax.url.war.importPaxLoggingPackages").value("true"),
                       systemProperty("org.ops4j.pax.web.log.ncsa.enabled").value("true"),
                       systemProperty("org.ops4j.pax.web.log.ncsa.directory").value("target/logs"),
                       systemProperty("org.ops4j.pax.web.jsp.scratch.dir").value("target/paxexam/scratch-dir"),
                       systemProperty("org.ops4j.pax.url.mvn.certificateCheck").value("false"),
                       mavenBundle("org.ops4j.pax.logging", "pax-logging-api", paxSwissboxVersion),
                       mavenBundle("org.ops4j.pax.logging", "pax-logging-service", paxSwissboxVersion),
                       mavenBundle("org.ops4j.pax.url", "pax-url-war", "2.4.7").type("jar").classifier("uber"),
                       mavenBundle("org.ops4j.pax.web", "pax-web-spi", paxWebVersion),
                       mavenBundle("org.ops4j.pax.web", "pax-web-api", paxWebVersion),
                       mavenBundle("org.ops4j.pax.web", "pax-web-extender-war", paxWebVersion),
                       mavenBundle("org.ops4j.pax.web", "pax-web-extender-whiteboard", paxWebVersion),
                       mavenBundle("org.ops4j.pax.web", "pax-web-runtime", paxWebVersion),
                       mavenBundle("org.ops4j.pax.web", "pax-web-jsp", paxWebVersion),
                       mavenBundle("org.eclipse.jdt.core.compiler", "ecj", "4.4"),
                       mavenBundle("org.apache.xbean", "xbean-reflect", xbeanVersion),
                       mavenBundle("org.apache.xbean", "xbean-finder", xbeanVersion),
                       mavenBundle("org.apache.xbean", "xbean-bundleutils", xbeanVersion),
                       mavenBundle("org.ow2.asm", "asm-all", "5.0.2"),
                       mavenBundle("commons-codec", "commons-codec", "1.10"),
                       mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.3.2"),
                       mavenBundle("org.apache.httpcomponents", "httpcore", httpcomponentsVersion),
                       mavenBundle("org.apache.httpcomponents", "httpmime", httpcomponentsVersion),
                       mavenBundle("org.apache.httpcomponents", "httpclient", httpcomponentsVersion),
                       mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
                       mavenBundle("org.ops4j.pax.web", "pax-web-jetty", paxWebVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-util", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-io", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-http", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-continuation", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-server", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-client", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-security", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-xml", jettyVersion),
                       mavenBundle("org.eclipse.jetty", "jetty-servlet", jettyVersion),
                       mavenBundle("commons-beanutils", "commons-beanutils", "1.8.3"),
                       mavenBundle("commons-collections", "commons-collections", "3.2.1"),
                       mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-digester", "1.8_4"),
                       mavenBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jsr303-api-1.0.0", "1.8.0"),
                       mavenBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jsr250-1.0", "2.0.0"),
                       mavenBundle("org.apache.geronimo.bundles", "commons-discovery", "0.4_1"),
                       mavenBundle("javax.enterprise", "cdi-api", "1.2"),
                       mavenBundle("javax.interceptor", "javax.interceptor-api", "1.2"),
                       mavenBundle("org.apache.myfaces.core", "myfaces-api", myFacesVersion),
                       mavenBundle("org.apache.myfaces.core", "myfaces-impl", myFacesVersion),
                       mavenBundle("org.primefaces", "primefaces", "5.1"),
                       mavenBundle("no.priv.bang.ukelonn", "ukelonn.api", getMavenProjectVersion()));
    }

    @Before
    public void setup() throws BundleException{
    	// Can't use injection for the Webapp service since the bundle isn't
    	// started until setUp and all injections are resolved
    	// before the tests start.
    	//
    	// Creating a service listener instead.
        ukelonnServiceListener = new ServiceListener() {

        	@Override
                public void serviceChanged(ServiceEvent event) {
                    @SuppressWarnings("rawtypes")
                        ServiceReference sr = event.getServiceReference();
                    @SuppressWarnings("unchecked")
                        UkelonnService service = (UkelonnService) bundleContext.getService(sr);
                    switch(event.getType()) {
                      case ServiceEvent.REGISTERED:
                	ukelonnService = service;
                        break;
                      default:
                        break;
                    }
                }
            };
        bundleContext.addServiceListener(ukelonnServiceListener);

        // The war bundle has to be manually started or it won't work
        final String bundlePath = "mvn:no.priv.bang.ukelonn/ukelonn.bundle/" + getMavenProjectVersion() + "/war";
        installWarBundle = bundleContext.installBundle(bundlePath);
        installWarBundle.start();
    }

    @After
    public void tearDown() throws BundleException {
    	installWarBundle.stop();
    }

    @Test
    public void ukelonnServiceIntegrationTest() {
    	// Verify that the service could be injected
    	assertNotNull(ukelonnService);
    	assertEquals("Hello world!", ukelonnService.getMessage());
    }

}
