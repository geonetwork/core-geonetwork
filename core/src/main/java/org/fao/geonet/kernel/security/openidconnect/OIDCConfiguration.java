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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OIDCConfiguration implements SecurityProviderConfiguration {

    public String organizationProperty = "organization";
    public String groupPermissionSeparator = ":";
    public String idTokenRoleLocation = "groups";
    public Map<String, String> roleConverter = new HashMap<>();
    public String minimumProfile = "Guest";
    public boolean userProfileUpdateEnabled = true;
    public boolean userGroupUpdateEnabled = true;
    public boolean addAccessTokenClaimsToUser = false;  //usually false - take the Access Token claims and put in the user

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopeSet() {
        if (!StringUtils.hasText(scopes))
           return null;
        return Arrays.asList(scopes.split(","));
    }

    public String  scopes = null; //null or empty -> all

    public String roleConverterString = null;


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
        return LoginType.LINK.toString();
    }

    @Override
    public String getSecurityProvider() {
        return "OIDC";
    }

    @Override
    public boolean isUserProfileUpdateEnabled() {
        return userProfileUpdateEnabled;
    }

    public void setUserProfileUpdateEnabled(boolean userProfileUpdateEnabled) {
        this.userProfileUpdateEnabled = userProfileUpdateEnabled;
    }

    @Override
    public boolean isUserGroupUpdateEnabled() {
        return userGroupUpdateEnabled;
    }

    public void setUserGroupUpdateEnabled(boolean userGroupUpdateEnabled) {
        this.userGroupUpdateEnabled = userGroupUpdateEnabled;
    }

    // "group1:role1,group2:role2,..."
    public void updateRoleConverterString(String serialized) {
        Map<String, String> result = new HashMap<>();
        if (!StringUtils.hasText(serialized)) {
            return;
        }
        serialized = serialized.trim();
        String[] items = serialized.split(",");
        for (String item : items) {
            String[] keyValue = item.split(":");
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

    public boolean isAddAccessTokenClaimsToUser() {
        return addAccessTokenClaimsToUser;
    }

    public void setAddAccessTokenClaimsToUser(boolean addAccessTokenClaimsToUser) {
        this.addAccessTokenClaimsToUser = addAccessTokenClaimsToUser;
    }
}
