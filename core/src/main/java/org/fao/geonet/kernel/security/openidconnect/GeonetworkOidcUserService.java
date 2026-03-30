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

import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * This class overrides OidcUserService#loadUser
 * <p>
 * This implementation uses the OIDCRoleProcessor to get the authorities
 * for the user (i.e. Administrator, Reviewer, etc...).
 * NOTE: this includes ALL the lower-level permissions - so an "Administrator" will also have
 * Reviewer, Editor, UserAdmin, RegisteredUser, Guest, etc...
 */
public class GeonetworkOidcUserService extends OidcUserService {

    @Autowired
    OIDCConfiguration oidcConfiguration;

    @Autowired
    OIDCRoleProcessor oidcRoleProcessor;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Autowired
    GeonetworkAuthenticationProvider geonetworkAuthenticationProvider;

    @Autowired
    protected SimpleOidcUserFactory simpleOidcUserFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = super.loadUser(userRequest);

        Collection<? extends GrantedAuthority> authorities;

        if (!oidcConfiguration.isUpdateProfile()) {
            // Retrieve the authorities from the local user
            try {
                SimpleOidcUser simpleUser = simpleOidcUserFactory.create(user.getAttributes());
                UserDetails userDetails = geonetworkAuthenticationProvider.loadUserByUsername(simpleUser.getUsername());

                authorities = userDetails.getAuthorities();
            } catch (Exception ex) {
                authorities = createAuthorities(user);
            }
        } else {
            authorities = createAuthorities(user);
        }

        OidcUserInfo userInfo = user.getUserInfo();

        //get the user name from a specific attribute (if specified) or use default.
        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        if (StringUtils.hasText(userNameAttributeName)) {
            user = new DefaultOidcUser(authorities, userRequest.getIdToken(), userInfo, userNameAttributeName);
        } else {
            user = new DefaultOidcUser(authorities, userRequest.getIdToken(), userInfo);
        }

        return user;
    }

    //get the authorities
    //  for the user (i.e. Administrator, Reviewer, etc...).
    //      NOTE: this includes ALL the lower-level permissions - so an "Administrator" will also have
    //            Reviewer, Editor, UserAdmin, RegisteredUser, Guest, etc...
    Collection<? extends GrantedAuthority> createAuthorities(OAuth2User user) {
        return oidcRoleProcessor.createAuthorities(roleHierarchy, user);
    }
}
