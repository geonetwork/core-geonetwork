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

package org.fao.geonet.web;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * The Class GeoNetworkHttpSessionRequestCache extends the basic one HttpSessionRequestCache
 * from Spring security to implement a filter on URLs to store in session
 */
public class GeoNetworkHttpSessionRequestCache extends HttpSessionRequestCache {

    /** The Constant SAVED_REQUEST. */
    static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

    /** The port resolver. */
    private PortResolver portResolver = new PortResolverImpl();

    /** The create session allowed. */
    private boolean createSessionAllowed = true;

    /** The allowed urls matcher. */
    private RequestMatcher allowedUrlsMatcher;

    /**
     * Stores the current request, provided the configuration properties allow it.
     *
     * @param request the request
     * @param response the response
     */
    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        // If the url match the pattern
        if (allowedUrlsMatcher.matches(request)) {
            DefaultSavedRequest savedRequest = new DefaultSavedRequest(request, portResolver);

            if (createSessionAllowed || request.getSession(false) != null) {
                // Store the HTTP request itself. Used by AbstractAuthenticationProcessingFilter
                // for redirection after successful authentication (SEC-29)
                request.getSession().setAttribute(SAVED_REQUEST, savedRequest);                
            }
        }
    }

    /**
     * Sets the allowed urls patterns.
     *
     * @param allowedUrlsPatterns the new allowed urls patterns
     */
    public void setAllowedUrlsPatterns(Set<String> allowedUrlsPatterns) {
        this.allowedUrlsMatcher =  new RegexRequestMatcher(String.join("|", allowedUrlsPatterns), null);
    }

    /**
     * Sets the creates the session allowed.
     *
     * @param createSessionAllowed the new creates the session allowed
     */
    @Override
    public void setCreateSessionAllowed(boolean createSessionAllowed) {
        this.createSessionAllowed = createSessionAllowed;
    }

    /**
     * Sets the port resolver.
     *
     * @param portResolver the new port resolver
     */
    @Override
    public void setPortResolver(PortResolver portResolver) {
        this.portResolver = portResolver;
    }

}
