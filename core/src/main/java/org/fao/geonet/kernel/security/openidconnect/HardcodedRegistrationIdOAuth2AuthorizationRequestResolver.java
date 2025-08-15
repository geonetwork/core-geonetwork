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

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import static org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationProvider.CLIENTREGISTRATION_NAME;

/**
 * This is to make things work well in Geonetwork.
 * Spring's oauth allows for multiple oauth providers - and the /signin/... path would normally indicate which one
 * (by the name).
 * <p>
 * We're only using one provider (called CLIENTREGISTRATION_NAME - "geonetwork-oidc") and it works better in GN if
 * you just use a simple "/signin" URL instead of a more complicated one.
 * <p>
 * This class bridges between the two methods (spring and GN's).
 * <p>
 * NOTE: this is MUCH more difficult that expected because spring's DefaultOAuth2AuthorizationRequestResolver
 * is a FINAL CLASS and most of its methods are private!
 */
public class HardcodedRegistrationIdOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    ClientRegistrationRepository clientRegistrationRepository;
    String authorizationRequestBaseUri;
    AntPathRequestMatcher authorizationRequestMatcher;

    DefaultOAuth2AuthorizationRequestResolver wrappedResolver;

    public HardcodedRegistrationIdOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, String authorizationRequestBaseUri) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestBaseUri = authorizationRequestBaseUri;
        this.authorizationRequestMatcher = new AntPathRequestMatcher(authorizationRequestBaseUri);
        wrappedResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri);
    }

    //defaults the "action" to "login" and uses the GN default oidc provider name
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {

        if (!this.authorizationRequestMatcher.matches(request)) {
            return null;
        }
        // defaults the "action" to "login"
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (!name.equals("action"))
                    return super.getParameter(name);
                String value = super.getParameter(name);
                if (value == null)
                    return "login";
                return value;
            }
        };
        return wrappedResolver.resolve(wrappedRequest, CLIENTREGISTRATION_NAME);
    }

    //defaults the "action" to "authorize" and uses the GN default oidc provider name
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        if (!this.authorizationRequestMatcher.matches(request)) {
            return null;
        }
        // defaults the "action" to "authorize"
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (!name.equals("action"))
                    return super.getParameter(name);
                String value = super.getParameter(name);
                if (value == null)
                    return "authorize";
                return value;
            }
        };
        return wrappedResolver.resolve(wrappedRequest, CLIENTREGISTRATION_NAME);
    }

}
