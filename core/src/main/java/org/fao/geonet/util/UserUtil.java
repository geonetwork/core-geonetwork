/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.util;

import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class UserUtil {

    private UserUtil() {

    }

    /**
     * Checks if the current user has a role using the role hierarchy.
     *
     * @param role  Role to check.
     * @param roleHierarchy  Role hierarchy.
     * @return true if the current user has a role using the role hierarchy, otherwise false.
     */
    public static boolean hasHierarchyRole(String role, RoleHierarchy roleHierarchy) {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        Collection<? extends GrantedAuthority> hierarchyAuthorities = roleHierarchy.getReachableGrantedAuthorities(authorities);

        for (GrantedAuthority authority : hierarchyAuthorities) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the user session's profile is allowed to do the transaction
     *
     * @param userSession current user session
     * @param settingManager setting manager bean
     * @param roleHierarchy role hierarchy bean
     * @param settingConfigPath setting config path check org.fao.geonet.kernel.setting.Settings
     * @param defaultProfile default configuration profile is no configuration found
     * @param errorText error text to the exception
     */
    public static void checkUserProfileLevel(UserSession userSession, SettingManager settingManager, RoleHierarchy roleHierarchy, String settingConfigPath, Profile defaultProfile, String errorText) {
        if (userSession.getProfile() != Profile.Administrator) {
            String allowedUserProfileFromConfiguration =
                StringUtils.defaultIfBlank(settingManager.getValue(settingConfigPath), defaultProfile.toString());

            // Is the user profile higher than the configuration profile allowed to do the transaction?
            if (!UserUtil.hasHierarchyRole(allowedUserProfileFromConfiguration, roleHierarchy)) {
                throw new NotAllowedException("The user has no permissions to " + errorText);
            }
        }

    }

}
