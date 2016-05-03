package org.geonetwork.http;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import jeeves.server.UserSession;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.http.JeevesServlet;

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
        HttpSession session = httpReq.getSession(false);
        
        //We don't have already a session. Is it a real user?
        if(session == null) {
            String userAgent = httpReq.getHeader("user-agent");

            Pattern regex = Pattern.compile(ServiceManager.BOT_REGEXP, 
                    Pattern.CASE_INSENSITIVE);
            Matcher m = regex.matcher(userAgent);
            if(!m.find()) {
                //It is not a bot, let's create a session
                //FIXME: really? Should we? Can't we wait? Anonymous users need it?
                session = httpReq.getSession(true);
            }
        }

        //If we are not being accessed by a bot/crawler
        if(session != null) {
            long currTime = System.currentTimeMillis();
    
            Cookie cookie = new Cookie("serverTime", "" + currTime);
            cookie.setPath("/");
            httpResp.addCookie(cookie);
    
            UserSession userSession = null;
            if (session != null) {
                Object tmp = session.getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
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
        }
    
        filterChain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}