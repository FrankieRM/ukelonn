package no.priv.bang.ukelonn.impl;

import java.util.EventListener;

import javax.inject.Provider;
import org.apache.shiro.web.env.EnvironmentLoaderListener;

import no.steria.osgi.jsr330activator.Jsr330Activator;

/**
 * This class will be be picked and instantiated up by the {@link Jsr330Activator} and be presented
 * in OSGi as an {@link EnvironmentLoaderListener} service.  This listener service works together
 * with the filter exposed by {@link ShiroFilterProvider} to provide authentication and authorization
 * for the servlet exposed by the {@link UkelonnServletProvider}.
 *
 * The way it works, is:
 *  1. The Jsr330Activator will start by instantiating this class
 *  2. The Jsr330Activator will call the get() method of this class to get the listener instance,
 *     which is then registered as an OSGi service, which is picked up by the pax web whiteboard extender
 *  3. If the JsrActivator is stopped (e.g. when unloading the bundle), the Jsr330Activator will retract the
 *     servlet OSGi service, and release its hold on the two injected services
 *
 *  See also: {@link UkelonnServletProvider}, {@link ShiroFilterProvider}
 *
 * @author Steinar Bang
 *
 */
public class ShiroEnvironmentLoaderListenerProvider implements Provider<EventListener> {

    private EnvironmentLoaderListener listener;

    @Override
    public EventListener get() {
        if (listener == null) {
            listener = new EnvironmentLoaderListener();
        }

        return listener;
    }

}
