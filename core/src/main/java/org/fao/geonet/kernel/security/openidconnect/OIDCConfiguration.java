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

import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This provides the configuration for the OIDC security.
 * see the spring-security-openidconnect.xml and spring-security-openidconnect-overrides.properties files.
 */
public class OIDCConfiguration implements SecurityProviderConfiguration {

    public String userNameAttribute = "email";

    /**
     * in the ID token, which property contain's the users organization?
     */
    public String organizationProperty = "organization";

    /**
     * For role/groups, we allow them to take the form of "GN-group:GN-profile".
     * this defines the ":" separator.
     * Shouldn't need to change this.
     */
    public String groupPermissionSeparator = ":";

    /**
     * Where, in the id token are the groups/roles stored?
     * This should be a list of group/role names.
     * It can be in the form of "property1.property2"
     */
    public String idTokenRoleLocation = "groups";

    /**
     * Converts roles from the OIOC server to GN profiles (or in the form of "GN-group:GN-profile").
     * You can specify via setRoleConverterString in the form:
     * "OIDCServerRole1=GNProfile,OIDCServerRole2=GROUP:PROFILE"
     */
    public Map<String, String> roleConverter = new HashMap<>();

    /**
     * All users who login via the OIDC will have this profile (at a minimum).
     * This is useful to allow ALL users in an org to login to GN without having to setup all users.
     * Typically, this should be GUEST or REGISTEREDUSER.  But, you can make it higher (i.e. Editor).
     */
    public String minimumProfile = "Guest";

    /**
     *  true -> update the DB with the information from OIDC (don't allow user to edit profile in the UI)
     *  false -> don't update the DB (user must edit profile in UI).
     */
    public boolean updateProfile =true;

    /**
     *  true -> update the DB (user's group) with the information from OIDC (don't allow admin to edit user's groups in the UI)
     *  false -> don't update the DB (admin must edit groups in UI).
     */
    public boolean updateGroup = true;

    public boolean logSensitiveInformation = false;

    public String clientId;
    public String clientSecret;
    public String scopes = null; //null or empty -> all
    public String roleConverterString = null;
    public LoginType loginType = LoginType.LINK;

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopeSet() {
        if (!StringUtils.hasText(scopes))
            return null;
        return Arrays.asList(scopes.split(" "));
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public boolean isLogSensitiveInformation() {
        return logSensitiveInformation;
    }

    public void setLogSensitiveInformation(boolean logSensitiveInformation) {
        this.logSensitiveInformation = logSensitiveInformation;
    }

    public String getOrganizationProperty() {
        return organizationProperty;
    }

    public void setOrganizationProperty(String organizationProperty) {
        this.organizationProperty = organizationProperty;
    }

    public String getGroupPermissionSeparator() {
        return groupPermissionSeparator;
    }

    public void setGroupPermissionSeparator(String groupPermissionSeparator) {
        this.groupPermissionSeparator = groupPermissionSeparator;
    }

    public String getIdTokenRoleLocation() {
        return idTokenRoleLocation;
    }

    public void setIdTokenRoleLocation(String idTokenRoleLocation) {
        this.idTokenRoleLocation = idTokenRoleLocation;
    }

    public Map<String, String> getRoleConverter() {
        return roleConverter;
    }

    public void setRoleConverter(Map<String, String> roleConverter) {
        this.roleConverter = roleConverter;
    }

    public Profile getMinimumProfile() {
        return Profile.findProfileIgnoreCase(minimumProfile);
    }

    public void setMinimumProfile(String minimumProfile) {
        this.minimumProfile = minimumProfile;
    }

    @Override
    public String getLoginType() {
        return loginType.toString();
    }

    public void setLoginType(String loginType) throws Exception {
        this.loginType = LoginType.parse(loginType);
        if ( !this.loginType.equals(LoginType.AUTOLOGIN) && !this.loginType.equals(LoginType.LINK)) {
            throw new Exception("Configuration error - login type should only be LINK or AUTOLOGIN");
        }
    }

    @Override
    public String getSecurityProvider() {
        return "OIDC";
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


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    // "group1=role1,group2=role2,..."
    public void updateRoleConverterString(String serialized) {
        Map<String, String> result = new HashMap<>();
        if (!StringUtils.hasText(serialized)) {
            return;
        }
        serialized = serialized.trim();
        String[] items = serialized.split(",");
        for (String item : items) {
            String[] keyValue = item.split("=");
            result.put(keyValue[0].trim(), keyValue[1].trim());
        }
        this.roleConverter = result;
    }

    //provided to make it easier to set the roleConverter based on a environment variables (string)
    // instead of having to create a spring Map
    public void setRoleConverterString(String roleConverterString) {
        this.roleConverterString = roleConverterString;
        updateRoleConverterString(roleConverterString);
    }


}
