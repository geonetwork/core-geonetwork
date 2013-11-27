package jeeves.config.springutil;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.User;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 11/13/13
 * Time: 5:15 PM
 */
public class JeevesDelegatingFilterProxy extends org.springframework.web.filter.DelegatingFilterProxy {
    private final static InheritableThreadLocal<String> applicationContextAttributeKey = new InheritableThreadLocal<String>();
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            final String nodeName = httpRequest.getServletPath().substring(1);
            String applicationKey = User.NODE_APPLICATION_CONTEXT_KEY + nodeName;
            if (getServletContext().getAttribute(applicationKey) == null) {
                // use default;
                applicationKey = User.NODE_APPLICATION_CONTEXT_KEY;
            }
            applicationContextAttributeKey.set(applicationKey);
            ApplicationContextHolder.set(getApplicationContextFromServletContext(getServletContext()));
            super.doFilter(request, response, filterChain);
            ApplicationContextHolder.clear();
        } else {
            response.getWriter().write(request.getClass().getName() + " is not a supported type of request");
        }
    }

    @Override
    public String getContextAttribute() {
        return applicationContextAttributeKey.get();
    }

    public static ConfigurableApplicationContext getApplicationContextFromServletContext(ServletContext servletContext) {
        final Object applicationContext = servletContext.getAttribute(applicationContextAttributeKey.get());
        return (ConfigurableApplicationContext) applicationContext;
    }

}
