package org.geonetwork.http;

import jeeves.server.UserSession;
import jeeves.server.sources.http.JeevesServlet;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Add server time and session expiration time to cookie to
 * track on the client side if session is about to be cancelled.
 * If user is not authenticated, the server time is the same
 * as expiration time.
 *
 * Created by francois on 29/07/15.
 */
public class SessionTimeoutCookieFilter implements javax.servlet.Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpSession session = httpReq.getSession();

        long currTime = System.currentTimeMillis();

        Cookie cookie = new Cookie("serverTime", "" + currTime);
        cookie.setPath("/");
        httpResp.addCookie(cookie);

        HttpSession httpSession = httpReq.getSession(false);
        UserSession userSession = null;
        if (httpSession != null) {
            Object tmp = httpSession.getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
            if (tmp instanceof UserSession) {
                userSession = (UserSession) tmp;
            }
        }
        // If user is authenticated, then set expiration time
        if (userSession != null && StringUtils.isNotEmpty(userSession.getName())) {
            long expiryTime = currTime + session.getMaxInactiveInterval() * 1000;
            cookie = new Cookie("sessionExpiry", "" + expiryTime);
        } else {
            cookie = new Cookie("sessionExpiry", "" + currTime);
        }
        cookie.setPath("/");
        httpResp.addCookie(cookie);

        filterChain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}