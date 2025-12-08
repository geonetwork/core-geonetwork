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

import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.springframework.util.ObjectUtils;

import static org.fao.geonet.kernel.security.SecurityProviderConfiguration.LoginType.*;

/**
 * Some basic configuration info for keycloak logins.
 *
 * Mainly header names mapping to attributes.
 *
 */
public class KeycloakConfiguration implements SecurityProviderConfiguration {
    public static final String REDIRECT_PLACEHOLDER = "{RedirecUrl}";
    private final String SECURITY_PROVIDER = "KEYCLOAK";
    private String DEFAULT_ROLE_GROUP_SEPARATOR = ":";

    private String loginType;
    private String publicClientId;

    private String organisationKey;

    // IDP logout url.  If null then it will not be used.
    // If supplied then it will redirect to this url before returning to the application.
    // This can be used if the IDP does not support back channel logout.
    // It required {RedirecUrl} to appear
    // i.e. https://idp.example.com/logout?redirect={RedirecUrl}
    private String IDPLogoutUrl;

    private boolean updateProfile;
    private boolean updateGroup;

    private String roleGroupSeparator;

    public String getIDPLogoutUrl() {
        return IDPLogoutUrl;
    }

    public void setIDPLogoutUrl(String IDPLogoutUrl) {
        if (IDPLogoutUrl != null && !IDPLogoutUrl.contains(REDIRECT_PLACEHOLDER)) {
            // IDPLogoutUrl must contain  REDIRECT_PLACEHOLDER
            throw new BadParameterEx("IDPLogoutUrl", IDPLogoutUrl);
        }
        this.IDPLogoutUrl = IDPLogoutUrl;
    }

    public String getOrganisationKey() {
        return organisationKey;
    }

    public void setOrganisationKey(String organisationKey) {
        this.organisationKey = organisationKey;
    }

    public boolean isUpdateProfile() {
        return updateProfile;
    }

    public void setUpdateProfile(boolean updateProfile) {
        this.updateProfile = updateProfile;
    }

    public boolean isUpdateGroup() {
        return updateGroup;
    }

    public void setUpdateGroup(boolean updateGroup) {
        this.updateGroup = updateGroup;
    }

    public String getPublicClientId() {
        return publicClientId;
    }

    public void setPublicClientId(String publicClientId) {
        this.publicClientId = publicClientId;
    }

    public String getRoleGroupSeparator() {
        return roleGroupSeparator;
    }

    public void setRoleGroupSeparator(String roleGroupSeparator) {
        if(ObjectUtils.isEmpty(roleGroupSeparator)) {
            roleGroupSeparator = DEFAULT_ROLE_GROUP_SEPARATOR;
        }
        this.roleGroupSeparator = roleGroupSeparator;
    }

    @Override
    public String getSecurityProvider() {
       return SECURITY_PROVIDER;
    }

    @Override
    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        LoginType parsedLoginType = parse(loginType);
        switch(parsedLoginType) {
            // support LINK and AUTOLOGIN
            case LINK:
            case AUTOLOGIN:
                break;
            case DEFAULT:
                // Default to link
                parsedLoginType= LINK;
                break;
            default:
                // Currently don't support anything else
                throw new BadParameterEx("loginType", parsedLoginType.toString());
        }
        this.loginType=parsedLoginType.toString();
    }

    @Override
    public boolean isUserProfileUpdateEnabled() {
        // If updating profile from the security provider then disable the profile updates in the interface
        return !updateProfile;
    }

    @Override
    public boolean isUserGroupUpdateEnabled() {
        // If updating group from the security provider then disable the group updates in the interface
        return !updateGroup;
    }
}


