/*
 * Copyright (C) 2001-2017 Food and Agriculture Organization of the
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

import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.keycloak.KeycloakPrincipal;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.filter.QueryParamPresenceRequestMatcher;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.IllformedLocaleException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KeycloakAuthenticationProcessingFilter extends org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter {

    private RequestCache requestCache;

    @Autowired
    private KeycloakUserUtils keycloakUserUtils;

    // Change login from DEFAULT_LOGIN_URL to geonetwork signin url
    public static final RequestMatcher DEFAULT_REQUEST_MATCHER =
        new OrRequestMatcher(
            new AntPathRequestMatcher(KeycloakUtil.getSigninPath()),
            new RequestHeaderRequestMatcher(AUTHORIZATION_HEADER),
            new QueryParamPresenceRequestMatcher(OAuth2Constants.ACCESS_TOKEN)
        );

    public KeycloakAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager, DEFAULT_REQUEST_MATCHER);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        if (Log.isDebugEnabled(Geonet.SECURITY)) {
            try {
                Log.debug(Geonet.SECURITY,
                        "Performing Keycloak auth check. Existing auth is "
                                + SecurityContextHolder.getContext().getAuthentication());
            } catch (Throwable t) {
            }
        }

        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) authResult.getPrincipal();
        if (keycloakPrincipal != null) {

            String username = "UNIDENTIFIED";
            try {
                // Get the user details from the token and also apply changes to the database if needed.
                UserDetails userDetails = keycloakUserUtils.getUserDetails(keycloakPrincipal.getKeycloakSecurityContext().getToken(), true);

                if (userDetails != null) {
                    username = userDetails.getUsername();
                    if (Log.isDebugEnabled(Geonet.SECURITY)) {
                        Log.debug(
                                Geonet.SECURITY,
                                "Keycloak user found " + userDetails.getUsername()
                                        + " with authorities: "
                                        + userDetails.getAuthorities());
                    }

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authResult);
                    SecurityContextHolder.setContext(context);

                    Log.info(Geonet.SECURITY, "User '" + userDetails.getUsername()
                            + "' authenticated via Keycloak");


                    if (((KeycloakAuthenticationToken) authResult).isInteractive()) {
                        if (requestCache != null) {
                            String redirect = null;

                            SavedRequest savedReq = requestCache.getRequest(request,
                                response);
                            if (savedReq != null) {
                                redirect = savedReq.getRedirectUrl();
                                Log.debug(Geonet.SECURITY,
                                    "Found saved request location: " + redirect);
                            } else {
                                Log.debug(Geonet.SECURITY, "No saved request found");
                            }

                            if (redirect != null) {
                                Log.info(Geonet.SECURITY, "Redirecting to " + redirect);

                                // Removing original request, since we want to
                                // retain current headers.
                                // If request remains in cache, requestCacheFilter
                                // will reinstate the original headers and we don't
                                // want it.
                                requestCache.removeRequest(request, response);

                                response.sendRedirect(redirect);
                            }
                        } else {
                            Map<String, List<String>> qsMap = splitQueryString(request.getQueryString());
                            if (qsMap.containsKey("redirectUrl")) {
                                URI redirectUri = new URI(qsMap.get("redirectUrl").get(0));
                                // redirectUrl should only be relative to the current server. So only redirect if it is not an absolute path
                                if (redirectUri != null && !redirectUri.isAbsolute()) {
                                    response.sendRedirect(redirectUri.toString());
                                } else {
                                    // If the redirect url ends up being null or absolute url then lets redirect back to the context home.
                                    Log.warning(Geonet.SECURITY, "Failed to perform login redirect to '" + qsMap.get("redirectUrl").get(0) + "'. Redirected to context home");
                                    response.sendRedirect(request.getContextPath());
                                }
                            } else {
                                // If the redirect url did not exist then lets redirect back to the context home.
                                response.sendRedirect(request.getContextPath());
                            }
                        }

                    } else {
                        // For bearer token
                        try {
                            chain.doFilter(request, response);
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    }

                    // Set users preferred locale if it exists.
                    String localeString = keycloakPrincipal.getKeycloakSecurityContext().getToken().getLocale();
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

                            Log.warning(Geonet.SECURITY, "Unable to parse keycloak locale " + localeString +
                                    " for user " + username + ": " + e.getMessage());
                        }
                    }
                }

                // Fire event so that updateTimestampListener can be trigger.
                // It may have been triggered at the beginning of the authentication when the user information was not available for new users.
                // Firing the event again as the user information now exists.
                if (this.eventPublisher != null) {
                    eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
                }
                // No further action required as we are redirecting to new page or handling auth token
                return;
            } catch (Exception ex) {
                Log.warning(Geonet.SECURITY, "Error during Keycloak login for user "
                        + username + ": " + ex.getMessage(), ex);
            }
        }
        chain.doFilter(request, response);
    }

    public RequestCache getRequestCache() {
        return requestCache;
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    /**
     * parse query string into a map.
     * Source: https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
     *
     * @param queryString to be parse into a map.
     * @return a map containing the values from the querystring.
     * @throws UnsupportedEncodingException
     */
    private static Map<String, List<String>> splitQueryString(String queryString) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }
}
