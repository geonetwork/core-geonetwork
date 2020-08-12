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

package org.fao.geonet.kernel.security.keycloak;

import org.apache.commons.lang.LocaleUtils;
import org.apache.http.client.utils.URIBuilder;

import org.fao.geonet.utils.Log;
import org.keycloak.KeycloakPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class KeycloakAuthenticationProcessingFilter extends org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter {

    @Autowired
    private KeycloakUserUtils keycloakUserUtils;

    public KeycloakAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    public KeycloakAuthenticationProcessingFilter(AuthenticationManager authenticationManager, RequestMatcher requiresAuthenticationRequestMatcher) {
        super(authenticationManager, requiresAuthenticationRequestMatcher);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        if (Log.isDebugEnabled(Log.JEEVES)) {
            try {
                Log.debug(Log.JEEVES,
                        "Performing Keycloak auth check. Existing auth is "
                                + SecurityContextHolder.getContext().getAuthentication());
            } catch (Throwable t) {
            }
        }

        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) authResult.getPrincipal();
        if (keycloakPrincipal != null) {

            String username = "UNIDENTIFIED";
            try {
                UserDetails userDetails = keycloakUserUtils.setupUser(request, keycloakPrincipal);

                if (userDetails != null) {
                    username = userDetails.getUsername();
                    if (Log.isDebugEnabled(Log.JEEVES)) {
                        Log.debug(
                                Log.JEEVES,
                                "Keycloak user found " + userDetails.getUsername()
                                        + " with authorities: "
                                        + userDetails.getAuthorities());
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(keycloakPrincipal.getKeycloakSecurityContext());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    Log.info(Log.JEEVES, "User '" + userDetails.getUsername()
                            + "' properly authenticated via Keycloak");


                    //Todo the redirect is currently hard coded to to to the context root
                    // It needs to be corrected to later go to the gn redirection url
                    try {
                        URIBuilder uribuilder = new URIBuilder();
                        uribuilder.setScheme(request.getScheme());
                        uribuilder.setPort(request.getServerPort()); //set for all?
                        uribuilder.setHost(request.getServerName());
                        uribuilder.setPath(request.getContextPath());
                        response.sendRedirect(uribuilder.build().toString());
                    } catch (URISyntaxException e) {
                        Log.error(Log.JEEVES, "Error creating redirect url", e);
                    }

                    if (keycloakPrincipal.getKeycloakSecurityContext().getToken().getLocale() != null) {
                        try {
                            response.setLocale(LocaleUtils.toLocale(keycloakPrincipal.getKeycloakSecurityContext().getToken().getLocale()));
                        } catch (IllegalArgumentException e) {
                            Log.warning(Log.JEEVES, "Unable to parse keycloak locale " + LocaleUtils.toLocale(keycloakPrincipal.getKeycloakSecurityContext().getToken().getLocale() +
                                    " for use " + username + ": " + e.getMessage()));
                        }
                    }
                }

                // Fire event so that updateTimestampListener can be trigger.
                // It may have been triggered at the beginning of the authentication when the user information was not available for new users.
                // Firing the event again as the user information now exists.
                if (this.eventPublisher != null) {
                    eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
                }
                return;
            } catch (Exception ex) {
                Log.warning(Log.JEEVES, "Error during Keycloak login for user "
                        + username + ": " + ex.getMessage(), ex);
            }
        }
        chain.doFilter(request, response);
    }
}
