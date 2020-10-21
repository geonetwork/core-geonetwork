/*
 *  Copyright (C) 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.kernel.security.keycloak;

import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.springframework.util.StringUtils;

import static org.fao.geonet.kernel.security.SecurityProviderConfiguration.LoginType.*;

/**
 * Some basic configuration info for keycloak logins.
 *
 * Mainly header names mapping to attributes.
 *
 */
public class KeycloakConfiguration implements SecurityProviderConfiguration {
    private final String SECURITY_PROVIDER = "KEYCLOAK";
    private String DEFAULT_ROLE_GROUP_SEPARATOR = ":";

    private String loginType;
    private String publicClientId;

    private String organisationKey;

    private boolean updateProfile;
    private boolean updateGroup;

    private String roleGroupSeparator;

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
        if(StringUtils.isEmpty(roleGroupSeparator)) {
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
                // Default to autologin
                parsedLoginType= AUTOLOGIN;
                break;
            default:
                // Currently don't support anything else
                throw new BadParameterEx("loginType", parsedLoginType.toString());
        }
        this.loginType=parsedLoginType.toString();
    }
}


