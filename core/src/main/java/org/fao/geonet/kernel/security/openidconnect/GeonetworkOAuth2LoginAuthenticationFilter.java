/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.IllformedLocaleException;
import java.util.Locale;

/**
 * This is a OAuth2LoginAuthenticationFilter successfulAuthentication method.
 * See below for details.
 */
public class GeonetworkOAuth2LoginAuthenticationFilter extends OAuth2LoginAuthenticationFilter {

    @Autowired
    OAuth2SecurityProviderUtil oAuth2SecurityProviderUtil;

    public GeonetworkOAuth2LoginAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService) {
        super(clientRegistrationRepository, authorizedClientService);
    }

    //this doesn't do anything - just calls the super class.  Can change in the future if needed.
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
        throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
    }

    // called when a user successfully authenticates.
    // 1. the user is save in GN (cf. OAuth2SecurityProviderUtil#getUserDetails) on login
    // 2. redirected back to their original URL.
    // 3. GN login even published
    // 4. user's local set
    //
    // most of this taken from GN's keycloak security
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
        throws IOException, ServletException {
        String username = "UNIDENTIFIED";
        if (authResult == null) {
            throw new IOException("authresult is null!"); // this shouldn't happen
        }

        if (!(authResult instanceof OAuth2AuthenticationToken)) {
            return; // this shouldnt happen
        }

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authResult;

        if ((oAuth2AuthenticationToken.getPrincipal() == null) || (!(oAuth2AuthenticationToken.getPrincipal() instanceof OidcUser))) {
            throw new IOException("problem with principle - null or incorrect type"); // this shouldn't happen
        }

        OidcUser oidcUser = (OidcUser) oAuth2AuthenticationToken.getPrincipal();

        //save user
        try {
            UserDetails userDetails = oAuth2SecurityProviderUtil.getUserDetails(authResult, true);
            if(userDetails != null) {
                username = userDetails.getUsername();
                Log.info(Geonet.SECURITY, "User '" + username
                    + "' authenticated via OIDC");
            }

        } catch (Exception e) {
            throw new IOException("OIDC: couldnt save user details",e);
        }

        try{
            SecurityContextHolder.getContext().setAuthentication(authResult);


            //cf GN keycloak
            String redirectURL = findQueryParameter(request, "redirectUrl");
            if (redirectURL != null) {
                try {
                    URI redirectUri = new URI(redirectURL);
                    if (redirectUri != null && !redirectUri.isAbsolute()) {
                        response.sendRedirect(redirectUri.toString());
                    } else {
                        // If the redirect url ends up being null or absolute url then lets redirect back to the context home.
                        Log.warning(Geonet.SECURITY, "Failed to perform login redirect to '" + redirectURL + "'. Redirected to context home");
                        response.sendRedirect(request.getContextPath());
                    }
                } catch (URISyntaxException e) {
                    response.sendRedirect(request.getContextPath());
                }
            } else {
                response.sendRedirect(request.getContextPath());
            }

            // Set users preferred locale if it exists. - cf. keycloak
            String localeString = oidcUser.getLocale();
            if (!StringUtils.isEmpty(localeString)) {
                try {
                    try {
                        //Try to parse the locale as a languageTag i.e. en-CA
                        response.setLocale(new Locale.Builder().setLanguageTag(localeString).build());
                    } catch (IllformedLocaleException e) {
                        // If there are any exceptions try a different approach as it may be in the format of en_CA or simply en
                        response.setLocale(LocaleUtils.toLocale(localeString));
                    }
                } catch (IllegalArgumentException e) {
                    Log.warning(Geonet.SECURITY, "Unable to parse oidc locale " + oidcUser.getLocale() + ": " + e.getMessage());
                }
            }


            // Fire event so that updateTimestampListener can be trigger.
            // It may have been triggered at the beginning of the authentication when the user information was not available for new users.
            // Firing the event again as the user information now exists.
            if (this.eventPublisher != null) {
                eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
            }

        } catch (Exception ex) {
            Log.warning(Geonet.SECURITY, "Error during OIDC login for user "
                + username + ": " + ex.getMessage(), ex);
        }


    }

    // given a request and URL parameter name, find its value.
    // returns null if not found.
    String findQueryParameter(HttpServletRequest request, String parmName) {
        if (request.getQueryString() == null) {
            return null;
        }
        try {
            String uri = request.getContextPath() + "?" + request.getQueryString();
            MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

            if (!parameters.containsKey(parmName)) {
                return null;
            }
            String result = parameters.getFirst(parmName);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
