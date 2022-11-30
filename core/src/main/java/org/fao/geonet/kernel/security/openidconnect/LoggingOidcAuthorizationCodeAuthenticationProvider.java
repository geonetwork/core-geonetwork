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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Map;

/**
 * Logs information about the interaction with the OIDC server.  This is VERY useful for determining problems.
 *
 * NOTE: LOGGING THIS INFORMATION IS PROBABLY A SECURITY AND PERSONAL INFORMATION RISK.
 *       DO NOT TURN THIS ON IN A SYSTEM THAT IS ACTUALLY BEING USED.
 *
 * We try not to log very sensitive information - we don't log the full access or id token (just the claims part).
 * We log the single-use CODE, but it should have already been deactivated by the server before we log it.
 *
 * NOTE: the access token, userinfo, and id token contain sensitive information (i.e. real names, email address, etc...)
 *
 *  Logs: CODE, ACCESS TOKEN, ID TOKEN, userinfo endpoint result, and calculated GeoNetwork authorities.
 */
public class LoggingOidcAuthorizationCodeAuthenticationProvider extends OidcAuthorizationCodeAuthenticationProvider {

    OIDCConfiguration oidcConfiguration;

    /**
     * IF oidcConfiguration.isLogSensitiveInformation() THEN log a bunch of information.
     *
     * @param accessTokenResponseClient the client used for requesting the access token credential from the Token Endpoint
     * @param userService               the service used for obtaining the user attributes of the End-User from the UserInfo Endpoint
     * @param oidcConfiguration         GeoNetwork OIDC config (so can tell if logging is on)
     */
    public LoggingOidcAuthorizationCodeAuthenticationProvider(
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
        OAuth2UserService<OidcUserRequest, OidcUser> userService,
        OIDCConfiguration oidcConfiguration) {
        super(accessTokenResponseClient, userService);
        this.oidcConfiguration = oidcConfiguration;
    }

    /**
     *   Does the super.authenticate(), and if logging is turned on, then do logging.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication result = super.authenticate(authentication);
        if (oidcConfiguration.isLogSensitiveInformation() && (authentication instanceof OAuth2LoginAuthenticationToken)) {
            log((OAuth2LoginAuthenticationToken) result);
        }
        return result;
    }

    /**
     * Log information about the login
     * @param authentication
     */
    void log(OAuth2LoginAuthenticationToken authentication) {
        Log.debug(Geonet.SECURITY, "OIDC LOGIN");
        Log.debug(Geonet.SECURITY, "----------");

        Log.debug(Geonet.SECURITY,this.oidcConfiguration);

        OAuth2AuthorizationResponse authorizationResponse = authentication.getAuthorizationExchange().getAuthorizationResponse();
        if (authorizationResponse != null) {
            // logging the CODE should be ok at this stage, because it should have already called the OIDC server with
            // the code and it's single use.
            // possible problem: code wasn't accepted by server and hasn't been "deactivated" by the server (unlikely).
            Log.debug(Geonet.SECURITY, "CODE FROM OIDC: " + authorizationResponse.getCode());
        }
        if (authentication.getAccessToken() != null) {
            Log.debug(Geonet.SECURITY, "ACCESS TOKEN: " + saferJWT(authentication.getAccessToken()));
        }
        if ((authentication.getPrincipal() != null) && (authentication.getPrincipal() instanceof DefaultOidcUser)) {
            DefaultOidcUser principal = (DefaultOidcUser) authentication.getPrincipal();
            Log.debug(Geonet.SECURITY, "ID TOKEN: " + saferJWT(principal.getIdToken()));
            if (principal.getAuthorities() != null) {
                Log.debug(Geonet.SECURITY, "Authorities:");
                for (GrantedAuthority authority : principal.getAuthorities()) {
                    Log.debug(Geonet.SECURITY, "   + " + authority.getAuthority());
                }
            }
            log(principal.getUserInfo());
        }


    }

    //log information from the OIDC server's "userinfo" endpoint
    private void log(OidcUserInfo userInfo) {
        if ((userInfo == null) || (userInfo.getClaims() == null))
            return;
        Log.debug(Geonet.SECURITY, "USER INFO CLAIMS:");
        for (Map.Entry<String, Object> claim : userInfo.getClaims().entrySet()) {
            Log.debug(Geonet.SECURITY, "   + " + claim.getKey() + "=" + claim.getValue().toString());
        }
    }

    // log an ID TOKEN.  see  saferJWT(String)
    public String saferJWT(OidcIdToken jwt) {
        if ((jwt != null) && (jwt.getTokenValue() != null))
            return saferJWT(jwt.getTokenValue());
        return "UNKNOWN";
    }

    // log an ACCESS TOKEN.  see  saferJWT(String)
    public String saferJWT(OAuth2AccessToken jwt) {
        if ((jwt != null) && (jwt.getTokenValue() != null))
            return saferJWT(jwt.getTokenValue());
        return "UNKNOWN";
    }


    // logs the string value of a token
    // if its a JWT token - it should be in 3 parts, separated by a "."
    // These 3 sections are: header, claims, signature
    // We only log the 2nd (claims) part.
    // This is safer because without the signature the token will not validate.
    public String saferJWT(String jwt) {
        String[] JWTParts = jwt.split("\\.");
        if (JWTParts.length > 1)
            return JWTParts[1]; // this is the claims part
        return "NOT A JWT"; // not a JWT
    }

}
