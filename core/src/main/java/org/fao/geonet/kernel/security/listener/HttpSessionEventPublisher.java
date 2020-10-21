package org.fao.geonet.kernel.security.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fao.geonet.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/* This is just a copy of the org.springframework.security.web.session.HttpSessionEventPublisher
    with fixes to prevent NPE. Newer version of spring security may not have this bug and it will be possible
    to remove this class and replace usage with org.springframework.security.web.session.HttpSessionEventPublisher
    It should only be used in web.xml
 */

/**
 * Declared in web.xml as
 * <pre>
 * &lt;listener&gt;
 *     &lt;listener-class&gt;org.springframework.security.web.session.HttpSessionEventPublisher&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 *
 * Publishes <code>HttpSessionApplicationEvent</code>s to the Spring Root WebApplicationContext. Maps
 * javax.servlet.http.HttpSessionListener.sessionCreated() to {@link HttpSessionCreatedEvent}. Maps
 * javax.servlet.http.HttpSessionListener.sessionDestroyed() to {@link HttpSessionDestroyedEvent}.
 *
 * @author Ray Krueger
 */
public class HttpSessionEventPublisher implements HttpSessionListener {
    //~ Static fields/initializers =====================================================================================

    private static final String LOGGER_NAME = org.springframework.security.web.session.HttpSessionEventPublisher.class.getName();

    //~ Methods ========================================================================================================

    ApplicationContext getContext(ServletContext servletContext) {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        // This is the bug fix
        if (applicationContext == null) {
            // If application context is null then it will cause errors.  So lets try to get the application context from the context holder.
            applicationContext = ApplicationContextHolder.get();
        }
        return applicationContext;
    }

    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionCreatedEvent} to the application
     * appContext.
     *
     * @param event HttpSessionEvent passed in by the container
     */
    public void sessionCreated(HttpSessionEvent event) {
        HttpSessionCreatedEvent e = new HttpSessionCreatedEvent(event.getSession());
        Log log = LogFactory.getLog(LOGGER_NAME);

        if (log.isDebugEnabled()) {
            log.debug("Publishing event: " + e);
        }

        getContext(event.getSession().getServletContext()).publishEvent(e);
    }

    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionDestroyedEvent} to the application
     * appContext.
     *
     * @param event The HttpSessionEvent pass in by the container
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSessionDestroyedEvent e = new HttpSessionDestroyedEvent(event.getSession());
        Log log = LogFactory.getLog(LOGGER_NAME);

        if (log.isDebugEnabled()) {
            log.debug("Publishing event: " + e);
        }
        ApplicationContext applicationContext = getContext(event.getSession().getServletContext());
        if (applicationContext != null) {
            applicationContext.publishEvent(e);
        }
    }
}