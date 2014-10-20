package jeeves.config.springutil;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 11/13/13
 * Time: 5:15 PM
 */
public class JeevesDelegatingFilterProxy extends GenericFilterBean {
    private final static InheritableThreadLocal<String> applicationContextAttributeKey = new InheritableThreadLocal<String>();
    private HashMap<String, Filter> _nodeIdToFilterMap = new HashMap<String, Filter>();

    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            final String nodeName = httpRequest.getServletPath().substring(1);
            String nodeId = User.NODE_APPLICATION_CONTEXT_KEY + nodeName;
            if (getServletContext().getAttribute(nodeId) == null) {
                nodeId =  User.NODE_APPLICATION_CONTEXT_KEY+request.getParameter("node");

                if (getServletContext().getAttribute(nodeId) == null) {
                    nodeId = loadNodeIdFromReferrer(request, httpRequest, nodeId);
                }
                if (nodeId == null || getServletContext().getAttribute(nodeId) == null) {
                    // use default;
                    nodeId = User.NODE_APPLICATION_CONTEXT_KEY;
                }
            }
            applicationContextAttributeKey.set(nodeId);
            final ConfigurableApplicationContext applicationContext = getApplicationContextFromServletContext(getServletContext());
            ApplicationContextHolder.set(applicationContext);
            getDelegateFilter(nodeId, (WebApplicationContext) applicationContext).doFilter(request, response, filterChain);
            ApplicationContextHolder.clear();
        } else {
            response.getWriter().write(request.getClass().getName() + " is not a supported type of request");
        }
    }

    private String loadNodeIdFromReferrer(ServletRequest request, HttpServletRequest httpRequest, String nodeId) {
        final String referer = httpRequest.getHeader("referer");
        if (urlIfFromThisServer(request, referer)) {
            nodeId = User.NODE_APPLICATION_CONTEXT_KEY + extractNodeIdFromUrl(referer);
        }
        return nodeId;
    }

    String trustedHost;
    

    public String getTrustedHost() {
        return trustedHost;
    }

    public void setTrustedHost(String trustedHost) {
        this.trustedHost = trustedHost;
    }

    private String extractNodeIdFromUrl(String referer) {
        final String[] split = referer.split(getServletContext().getContextPath() + "/", 2);
        if (split.length == 1) {    // Referer does not contains node information
            return null;
        } else {
            final int nextSlash = split[1].indexOf('/', 1);
            if (nextSlash > -1 ) {
                return split[1].substring(0, nextSlash);
            } else {
                return null;
            }
        }
    }

    private boolean urlIfFromThisServer(ServletRequest request, String referer) {
        if (referer == null) {
            return false;
        }
        
        try {
            final URL refererUrl = new URL(referer);
            final Set<InetAddress> refererInetAddress = new HashSet<InetAddress>(Arrays.asList(InetAddress.getAllByName(refererUrl
                    .getHost())));
            for (String trusted : getTrustedHost().split(",")) {
                InetAddress[] localINetAddres = InetAddress.getAllByName(trusted.trim());
                
                for (InetAddress localAddress : localINetAddres) {
                    if (refererInetAddress.contains(localAddress)) {
                        return true;
                    }
                }
            }
            
        } catch (UnknownHostException e) {
            return false;
        } catch (MalformedURLException e) {
            return false;
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    private synchronized Filter getDelegateFilter(String nodeId, WebApplicationContext context) {
        Filter filter = this._nodeIdToFilterMap.get(nodeId);

        if (filter == null) {
            filter = new DelegatingFilterProxy(getFilterName(), context);
            this._nodeIdToFilterMap.put(nodeId, filter);
        }

        return filter;
    }

    public static ConfigurableApplicationContext getApplicationContextFromServletContext(ServletContext servletContext) {
        final Object applicationContext = servletContext.getAttribute(applicationContextAttributeKey.get());
        return (ConfigurableApplicationContext) applicationContext;
    }

}
