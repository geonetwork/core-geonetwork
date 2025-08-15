/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.oidclogout;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.SecurityContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.security.web.csrf.GeonetworkCsrfSecurityRequestMatcher;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Filter for handling OpenID Connect (OIDC) backchannel logout requests.
 * This filter processes logout requests by validating the logout token and invalidating sessions in compliance with OIDC standards.
 * https://openid.net/specs/openid-connect-backchannel-1_0.html
 *
 * Referencing the following classes from Spring Security 6.5.x:
 * org.springframework.security.config.annotation.web.configurers.oauth2.client.OidcBackChannelLogoutFilter
 * org.springframework.security.config.annotation.web.configurers.oauth2.client.OidcBackChannelLogoutHandler
 *
 */
public class OidcBackchannelLogoutFilter extends OncePerRequestFilter {

    @Autowired
    private OidcSessionRegistry oidcSessionRegistry;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;


    private final RequestMatcher requestMatcher;
    private String sessionCookieName = "JSESSIONID";
    private RestOperations restOperations = new RestTemplate();
    private String registrationId;

    public OidcBackchannelLogoutFilter(CsrfFilter csrfFilter, GeonetworkCsrfSecurityRequestMatcher csrfRequestMatcher) {
        this.requestMatcher = new AntPathRequestMatcher("/**/logout/connect/back-channel/{registrationId}", HttpMethod.POST.name());
        csrfRequestMatcher.addRequestMatcher(new NegatedRequestMatcher(this.requestMatcher));
        csrfFilter.setRequireCsrfProtectionMatcher(csrfRequestMatcher);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        RequestMatcher.MatchResult result = requestMatcher.matcher(request);
        registrationId = result.getVariables().get("registrationId");
        return !requestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws IOException {
        try {
            String logoutToken = request.getParameter("logout_token");
            if (logoutToken == null) {
                Log.error(Geonet.SECURITY,"Missing logout_token parameter in request");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing logout_token");
                return;
            }

            ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);

            if(registrationId == null || clientRegistration == null) {
                Log.error(Geonet.SECURITY,"Invalid or missing registrationId: " + registrationId);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing registrationId");
                return;
            }

            //handle null uri
            if (clientRegistration.getProviderDetails().getJwkSetUri() == null) {
                Log.error(Geonet.SECURITY,"JWK Set URI is null for client registration: " + registrationId);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing JWK Set URI in client registration");
                return;
            }

            JOSEObjectTypeVerifier<SecurityContext> typeVerifier = new DefaultJOSEObjectTypeVerifier<>(null,
                JOSEObjectType.JWT, new JOSEObjectType("logout+jwt"));
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(clientRegistration.getProviderDetails().getJwkSetUri())
                .jwtProcessorCustomizer((processor) -> processor.setJWSTypeVerifier(typeVerifier))
                .build();
            jwtDecoder.setClaimSetConverter(new ClaimTypeConverter(OidcIdTokenDecoderFactory.createDefaultClaimTypeConverters()));

            Jwt jwt = jwtDecoder.decode(logoutToken);
            validateLogoutToken(jwt);

            // Handle logout logic here (e.g., session invalidation)
            logout(request, response, jwt, registrationId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception ex) {
            Log.error(Geonet.SECURITY,"Error processing OIDC Backchannel Logout request: " + ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid logout_token: " + ex.getMessage());
        }
    }

    private void validateLogoutToken(Jwt jwt)  {
        Map<String, Object> claims = jwt.getClaims();

        // Required claims

        if (!claims.containsKey("iss")) {
            throw new JwtException("Logout Token must contain 'iss' claim");
        }

        if (!claims.containsKey("aud")) {
            throw new JwtException("Logout Token must contain 'aud' claim");
        }

        if (!claims.containsKey("iat")) {
            throw new JwtException("Logout Token must contain 'iat' claim");
        }

        if (!claims.containsKey("jti")) {
            throw new JwtException("Logout Token must contain 'jti' claim");
        }
        if (!(claims.containsKey("sub") || claims.containsKey("sid"))) {
            throw new JwtException("Logout Token must contain 'sub' or 'sid'");
        }

        if (!claims.containsKey("events")) {
            throw new JwtException("Missing 'events' claim in logout token");
        }

        Map<String, Object> events = (Map<String, Object>) claims.get("events");
        if (!events.containsKey("http://schemas.openid.net/event/backchannel-logout")) {
            throw new JwtException("Invalid 'events' claim value");
        }

        if (claims.containsKey("nonce")) {
            throw new JwtException("Logout Token must not contain 'nonce'");
        }


    }


    public void logout(HttpServletRequest request, HttpServletResponse response, Jwt token, String registrationId) throws Exception {
            Log.debug(Geonet.SECURITY,"Invalidating OIDC sessions for token: " + token.getTokenValue());
            Iterable sessions = this.oidcSessionRegistry.removeSessionInformation(token);
            ArrayList errors = new ArrayList();
            int totalCount = 0;
            int invalidatedCount = 0;
            Iterator var9 = sessions.iterator();

            while(var9.hasNext()) {
                OidcSessionInformation session = (OidcSessionInformation)var9.next();
                ++totalCount;

                try {

                    this.eachLogout(request, token, session, registrationId);
                    ++invalidatedCount;
                } catch (RestClientException var12) {
                    RestClientException ex = var12;
                    Log.error(Geonet.SECURITY,"Failed to invalidate session", ex);
                    errors.add(ex.getMessage());
                    this.oidcSessionRegistry.saveSessionInformation(session);
                }
            }

            Log.debug(Geonet.SECURITY,String.format("Invalidated %d out of %d sessions", invalidatedCount, totalCount));

            if (!errors.isEmpty()) {
                response.setStatus(400);
                Log.error(Geonet.SECURITY,"Failed to invalidate some sessions: " + String.join(", ", errors));
                throw new Exception("Failed to invalidate some sessions: " + String.join(", ", errors));
            }
    }

    private void eachLogout(HttpServletRequest request, Jwt token, OidcSessionInformation session, String registrationId) {
        HttpHeaders headers = new HttpHeaders();
        String var10002 = this.sessionCookieName;
        headers.add("Cookie", var10002 + "=" + session.getSessionId());
        Iterator var5 = session.getAuthorities().entrySet().iterator();

        while(var5.hasNext()) {
            Map.Entry<String, String> credential = (Map.Entry)var5.next();
            headers.add((String)credential.getKey(), (String)credential.getValue());
        }

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String logout = this.computeLogoutEndpoint(request,  registrationId);
        MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("logout_token", token.getTokenValue());
        body.add("_spring_security_internal_logout", "true");
        HttpEntity<?> entity = new HttpEntity(body, headers);
        Log.debug(Geonet.SECURITY,"Sending internal logout request to: " + logout);
        this.restOperations.postForEntity(logout, entity, Object.class, new Object[0]);
    }

    String computeLogoutEndpoint(HttpServletRequest request, String registrationId) {
        String internalLogoutUri = "http://localhost:8080" + request.getContextPath() + "/signout";
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request)).replacePath(request.getContextPath()).replaceQuery((String)null).fragment((String)null).build();
        Map<String, String> uriVariables = new HashMap();
        String scheme = uriComponents.getScheme();
        uriVariables.put("baseScheme", scheme != null ? scheme : "");
        uriVariables.put("baseUrl", uriComponents.toUriString());
        String host = uriComponents.getHost();
        uriVariables.put("baseHost", host != null ? host : "");
        String path = uriComponents.getPath();
        uriVariables.put("basePath", path != null ? path : "");
        int port = uriComponents.getPort();
        uriVariables.put("basePort", port == -1 ? "" : ":" + port);

        uriVariables.put("registrationId", registrationId);
        return UriComponentsBuilder.fromUriString(internalLogoutUri).buildAndExpand(uriVariables).toUriString();
    }
}
