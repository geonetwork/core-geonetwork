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
package org.fao.geonet.kernel.security.openidconnect.bearer;


import org.fao.geonet.kernel.security.openidconnect.OIDCConfiguration;
import org.fao.geonet.kernel.security.openidconnect.OIDCRoleProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationProvider.CLIENTREGISTRATION_NAME;
import static org.fao.geonet.kernel.security.openidconnect.bearer.RoleInserter.insertRoles;

/**
 * This is the main class that does all the work.
 *
 * Note - if this throws, then a 401 will be issued with a header like this;
 *
 * WWW-Authenticate: Bearer error="invalid_token", error_description="access token has expired!", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
 *
 */
public class GeonetworkJwtAuthenticationProvider implements AuthenticationProvider {
    static UserInfoCache userInfoCache = new UserInfoCache();
    private final OAuth2UserService<OidcUserRequest, OidcUser> userService;
    AccessTokenParser accessTokenParser;
    UserRolesResolver userRolesResolver;
    List<AccessTokenValidator> accessTokenValidators;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    OIDCRoleProcessor oidcRoleProcessor;
    @Autowired
    RoleHierarchy roleHierarchy;
    @Autowired
    OIDCConfiguration oidcConfiguration;
    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = new DefaultOAuth2UserService();
    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();

    public GeonetworkJwtAuthenticationProvider(AccessTokenParser accessTokenParser,
                                               OAuth2UserService<OidcUserRequest, OidcUser> userService,
                                               UserRolesResolver userRolesResolver,
                                               List<AccessTokenValidator> accessTokenValidators
    ) {
        Assert.notNull(accessTokenParser, "accessTokenParser cannot be null");
        Assert.notNull(userService, "userService cannot be null");
        Assert.notNull(accessTokenValidators, "accessTokenValidators cannot be null");

        this.userRolesResolver = userRolesResolver;
        this.accessTokenParser = accessTokenParser;
        this.userService = userService;
        this.accessTokenValidators = accessTokenValidators;
    }

    private static final OAuth2Error DEFAULT_INVALID_TOKEN =
        invalidToken("An error occurred while attempting to decode the Jwt: Invalid token");

    /**
     * from spring - creates an error return
     */
    private static OAuth2Error invalidToken(String message) {
        try {
            return new BearerTokenError(
                BearerTokenErrorCodes.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED,
                message,
                "https://tools.ietf.org/html/rfc6750#section-3.1");
        } catch (IllegalArgumentException malformed) {
            // some third-party library error messages are not suitable for RFC 6750's error message charset
            return DEFAULT_INVALID_TOKEN;
        }
    }

    /**
     * runs all the AccessTokenValidator on the token.
     *
     * @param claims
     * @param userInfoClaims
     * @throws Exception
     */
    public void verifyToken(Map claims, Map userInfoClaims) throws Exception {
        for (AccessTokenValidator validator : accessTokenValidators) {
            validator.verifyToken(claims, userInfoClaims);
        }
    }


    /**
     * First time using the token, we have to do quite a bit of work.
     * i.e. call the userinfo endpoint (and for azure the graph api).
     * We cache the results since they shouldn't change over the lifetime of the token.
     *
     * @param authentication
     * @return
     */
    public UserInfoCacheItem createCacheItem(Authentication authentication) {
        //this is the actual access token
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;

        Map jwt;
        try {
            //parse it (using either the signature checking or non-signature checking version).
            //NOTE: we will use this token with the userinfo endpoint, so the server will signature-validate it
            //      (meaning its not a big deal if we don't).
            jwt = accessTokenParser.parseToken(bearer.getToken());
        } catch (Exception failed) {
            OAuth2Error invalidToken = invalidToken(failed.getMessage());
            throw new OAuth2AuthenticationException(invalidToken, invalidToken.getDescription(), failed);
        }

        //when is this token valid until
        Instant expireTime = Instant.ofEpochMilli((Long) jwt.get("exp") * 1000);

        //if expired, throw
        if (expireTime.compareTo(Instant.now()) < 0) {
            throw new OAuth2AuthenticationException(invalidToken("access token has expired"));
        }

        //execute the userinfo endpoint
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(CLIENTREGISTRATION_NAME);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            bearer.getToken(),
            Instant.ofEpochMilli((Long) jwt.get("iat") * 1000)//issuedAt
            , Instant.ofEpochMilli((Long) jwt.get("exp") * 1000)//ExpiresAt
        );
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        OAuth2User oAuth2User = oauth2UserService.loadUser(oAuth2UserRequest); //executes userinfo endpoint


        OidcUserInfo userInfo = new OidcUserInfo(oAuth2User.getAttributes());

        try {
            verifyToken(jwt, userInfo.getClaims()); //verify token with all the configured validators
        } catch (Exception failed) {
            OAuth2Error invalidToken = invalidToken(failed.getMessage());
            throw new OAuth2AuthenticationException(invalidToken, invalidToken.getDescription(), failed);
        }


        List<String> userRoles = null;
        try {
            //get  the list of roles (either from the jwt or the userInfo).  Or, you can contact an external API (i.e. MS Graph)
            userRoles = userRolesResolver.resolveRoles(bearer.getToken(), jwt, userInfo);
            // inject the roles inside the userInfo
            userInfo = insertRoles(oidcConfiguration.getIdTokenRoleLocation(), userInfo, userRoles);
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("userRolesResolver.resolveRoles exception", e);
        }
        //user's GrantedAuthorities (i.e. "Administrator" -> ["Administrator","Reviewer","Editor", "RegisteredUser", "Guest"]
        Collection<? extends GrantedAuthority> authorities = oidcRoleProcessor.createAuthorities(roleHierarchy, userRoles);

        //final user
        OAuth2User user = new DefaultOAuth2User(authorities, userInfo.getClaims(), oidcConfiguration.getUserNameAttribute());

        // create a cachable item for storing all this information
        return new UserInfoCacheItem(bearer.getToken(), expireTime, user, authorities);
    }

    /**
     * Decode and validate the
     * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>.
     *
     * @param authentication the authentication request object.
     * @return A successful authentication
     * @throws AuthenticationException if authentication failed for some reason
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;

        // has this access token been evaluated before?
        UserInfoCacheItem item = userInfoCache.getItem(bearer.getToken());
        if (item == null) {
            // never used - re-create
            item = createCacheItem(authentication);
            userInfoCache.putItem(item); //store in cache
        }

        // final result
        OAuth2User user = item.getUser();
        Collection<? extends GrantedAuthority> authorities = item.getAuthorities();
        OAuth2AuthenticationToken authenticationResult = new OAuth2AuthenticationToken(user, authorities, CLIENTREGISTRATION_NAME);
        authenticationResult.setDetails(authentication.getDetails());

        // user logs in event
        if (this.applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authenticationResult, this.getClass()));
        }

        return authenticationResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
