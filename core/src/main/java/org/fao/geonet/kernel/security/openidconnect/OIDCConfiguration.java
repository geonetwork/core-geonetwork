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
package org.fao.geonet.kernel.security.openidconnect;

import org.apache.jcs.access.exception.InvalidArgumentException;
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

    private String userNameAttribute = "email";

    /**
     * in the ID token, which property contain's the users organization?
     */
    private String organizationProperty = "organization";

    /**
     * For role/groups, we allow them to take the form of "GN-group:GN-profile".
     * this defines the ":" separator.
     * Shouldn't need to change this.
     */
    private String groupPermissionSeparator = ":";

    /**
     * Where, in the id token are the groups/roles stored?
     * This should be a list of group/role names.
     * It can be in the form of "property1.property2"
     */
    private String idTokenRoleLocation = "groups";

    /**
     * Converts roles from the OIOC server to GN profiles (or in the form of "GN-group:GN-profile").
     * You can specify via setRoleConverterString in the form:
     * "OIDCServerRole1=GNProfile,OIDCServerRole2=GROUP:PROFILE"
     */
    private Map<String, String> roleConverter = new HashMap<>();

    /**
     * All users who login via the OIDC will have this profile (at a minimum).
     * This is useful to allow ALL users in an org to login to GN without having to setup all users.
     * Typically, this should be GUEST or REGISTEREDUSER.  But, you can make it higher (i.e. Editor).
     */
    private String minimumProfile = "Guest";

    /**
     *  true -> update the DB with the information from OIDC (don't allow user to edit profile in the UI)
     *  false -> don't update the DB (user must edit profile in UI).
     */
    private boolean updateProfile =true;

    /**
     *  true -> update the DB (user's group) with the information from OIDC (don't allow admin to edit user's groups in the UI)
     *  false -> don't update the DB (admin must edit groups in UI).
     */
    private boolean updateGroup = true;

    private boolean logSensitiveInformation = false;

    public String roleConverterString = null;

    public static class ClientConfig {
        private String scopes = null; //null or empty -> all
        private String clientId;
        private String clientSecret;
        private boolean enabled = true;

        public String getScopes() {
            return scopes;
        }

        public void setScopes(String scopes) {
            this.scopes = scopes;
        }

        public List<String> getScopeSet() {
            if (!StringUtils.hasText(this.getScopes()))
                return null;
            return Arrays.asList(this.getScopes().split(" "));
        }

        public String getClientId() {
            return this.clientId;
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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private final ClientConfig clientconfig = new ClientConfig();
    private final ClientConfig serviceAccountConfig = new ClientConfig();

    public ClientConfig getClientConfig() {
        return clientconfig;
    }

    public ClientConfig getServiceAccountConfig() {
        return serviceAccountConfig;
    }

    private LoginType loginType = LoginType.LINK;

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
            throw new InvalidArgumentException("Configuration error - login type should only be LINK or AUTOLOGIN");
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


    @Override
    public String toString() {
        String result = "OIDC CONFIG: \n";
        result += "      idTokenRoleLocation" + "=" + idTokenRoleLocation + "\n";
        result += "      updateGroup" + "=" + updateGroup + "\n";
        result += "      updateProfile" + "=" + updateProfile + "\n";
        result += "      scopes" + "=" + getClientConfig().getScopes() + "\n";

        if ((roleConverter != null) && (!roleConverter.isEmpty())) {
            result += "      roleConverter: \n";
            for (Map.Entry<String, String> role : roleConverter.entrySet()) {
                result += "            + " + role.getKey() + " -> " + role.getValue();
            }
        }
        return result;
    }
}
