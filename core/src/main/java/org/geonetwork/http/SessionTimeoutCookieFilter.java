/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.geonetwork.http;

import java.io.IOException;

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
import jeeves.server.sources.http.JeevesServlet;

/**
 * Add server time and session expiration time to cookie to track on the client side if session is
 * about to be cancelled. If user is not authenticated, the server time is the same as expiration
 * time.
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

        //If we are not being accessed by a bot/crawler
        if (session != null) {
            long currTime = System.currentTimeMillis();

            String cookiePath = StringUtils.isBlank(httpReq.getContextPath()) ? "/" : httpReq.getContextPath();

            Cookie cookie = new Cookie("serverTime", "" + currTime);
            cookie.setPath(cookiePath);
            cookie.setSecure(req.getServletContext().getSessionCookieConfig().isSecure());
            httpResp.addCookie(cookie);

            UserSession userSession = null;

            Object tmp = session.getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
            if (tmp instanceof UserSession) {
                userSession = (UserSession) tmp;
            }

            // If user is authenticated, then set expiration time
            if (userSession != null && StringUtils.isNotEmpty(userSession.getName())) {
                long expiryTime = currTime + session.getMaxInactiveInterval() * 1000;
                cookie = new Cookie("sessionExpiry", "" + expiryTime);
            } else {
                cookie = new Cookie("sessionExpiry", "" + currTime);
            }
            cookie.setPath(cookiePath);
            cookie.setSecure(req.getServletContext().getSessionCookieConfig().isSecure());
            httpResp.addCookie(cookie);
        }

        filterChain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}
