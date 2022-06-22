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
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This gets a users roles from the results of the UserInfo (or from the Access Token).
 * Keycloak can be configured to put the user's roles/groups in the Access Token and/or the userinfo result.
 * For Azure AD, you must use the Graph API (cf MSGraphUserRolesResolver)
 */
public class UserInfoAccessTokenRolesResolver implements UserRolesResolver {

    @Autowired
    OIDCConfiguration oidcConfiguration;

    @Autowired
    OIDCRoleProcessor roleProcessor;


    List<String> resolveRoles(OidcUserInfo userInfo, String pathToRoles) {
        if ((userInfo == null) || (pathToRoles == null))
            return null;

        return roleProcessor.getTokenRoles(userInfo.getClaims(), pathToRoles);
    }

    List<String> resolveRoles(Map claims, String pathToRoles) {
        if ((claims == null) || (pathToRoles == null))
            return null;
        return roleProcessor.getTokenRoles(claims, pathToRoles);
    }


    @Override
    public List<String> resolveRoles(String tokenValue, Map claims, OidcUserInfo userInfo) {
        String pathToRoles = oidcConfiguration.getIdTokenRoleLocation();
        List<String> result = resolveRoles(userInfo, pathToRoles);
        if ((result == null) || (result.isEmpty()))
            result = resolveRoles(claims, pathToRoles);
        if ((result == null) || (result.isEmpty()))
            return new ArrayList<>();
        return result;
    }


}
