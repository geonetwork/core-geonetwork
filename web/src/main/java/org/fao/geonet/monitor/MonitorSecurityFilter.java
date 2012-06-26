package org.fao.geonet.monitor;

import jeeves.server.UserSession;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: jeichar Date: 3/29/12 Time: 9:04 PM
 */
public class MonitorSecurityFilter implements Filter {
    private Set<String> whiteList;

    public void init(FilterConfig filterConfig) throws ServletException {
        String list = filterConfig.getInitParameter("white-list");
        if (list != null) {
            this.whiteList = new HashSet<String>(Arrays.asList(list.split(",")));
        } else {
            whiteList = Collections.emptySet();
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean useWhiteList = !Boolean.parseBoolean(request.getParameter("ignorewhitelist"));
        if (useWhiteList && isInWhileList(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;

            HttpSession httpSession = servletRequest.getSession(false);
            if (httpSession != null) {
                UserSession userSession = (UserSession) httpSession.getAttribute("session");
                if (userSession != null &&
                        (Geonet.Profile.ADMINISTRATOR.equals(userSession.getProfile()) 
                                || Geonet.Profile.MONITOR.equals(userSession.getProfile()))) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        final String errorMessage = "Only administrator or monitor are permitted to access monitoring data";
        if (response instanceof HttpServletResponse) {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.sendError(401, errorMessage);
        } else {
            response.getOutputStream().write(errorMessage.getBytes("UTF-8"));
        }
    }

    public void destroy() {
    }

    private boolean isInWhileList(ServletRequest servletRequest) throws UnknownHostException {
        InetAddress[] remoteHostAddresses = InetAddress.getAllByName(servletRequest.getRemoteHost());

        for (String host : whiteList) {
            try {
                InetAddress[] acceptedAddresses = InetAddress.getAllByName(host);
                for (InetAddress acceptedAddress : acceptedAddresses) {
                    for (InetAddress remoteHostAddress : remoteHostAddresses) {
                        if (remoteHostAddress.equals(acceptedAddress)) {
                            return true;
                        }
                    }
                }
            } catch (UnknownHostException e) {
                Log.error(Log.MONITOR, host + " is not a valid host.  MonitorSecurityFilter's configuration in web.xml is not valid", e);
            }
        }
        return false;
    }

}
