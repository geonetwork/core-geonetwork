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

}
