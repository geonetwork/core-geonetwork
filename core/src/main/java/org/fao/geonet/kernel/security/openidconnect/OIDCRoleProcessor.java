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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OIDCRoleProcessor {

    @Autowired
    public OIDCConfiguration oidcConfiguration;


    public Map<Profile, List<String>> getProfileGroups(OidcIdToken accessToken) {
        List<String> oidcOriginalRoleNames = getTokenRoles(accessToken);
        oidcOriginalRoleNames.add(oidcConfiguration.minimumProfile);
        List<String> roleNames = simpleConvertRoles(oidcOriginalRoleNames);

        return getProfileGroups(roleNames);
    }

    public Map<Profile, List<String>> getProfileGroups(OAuth2User user) {
        List<String> oidcOriginalRoleNames = getTokenRoles(user);
        oidcOriginalRoleNames.add(oidcConfiguration.minimumProfile);
        List<String> roleNames = simpleConvertRoles(oidcOriginalRoleNames);

        return getProfileGroups(roleNames);
    }


    public Profile getProfile(OidcIdToken accessToken) {
        return getProfile(accessToken.getClaims());
    }

    public Profile getProfile(OAuth2User user) {
        return getProfile(user.getAttributes());
    }


    public List<String> getTokenRoles(OidcIdToken accessToken) {
        return getTokenRoles(accessToken.getClaims());
    }


    public List<String> getTokenRoles(OAuth2User user) {
        return getTokenRoles(user.getAttributes());
    }

    //---

    public List<String> simpleConvertRoles(List<String> originalRoleNames) {
        return originalRoleNames.stream()
            .map(x -> oidcConfiguration.roleConverter.get(x) == null ? x : oidcConfiguration.roleConverter.get(x))
            .distinct()
            .collect(Collectors.toList());
    }


    public Collection<? extends GrantedAuthority> createAuthorities(RoleHierarchy roleHierarchy, OAuth2User user) {
        return createAuthorities(roleHierarchy, user.getAttributes());
    }

    public Collection<? extends GrantedAuthority> createAuthorities(RoleHierarchy roleHierarchy, Map<String, Object> claims) {
        List<String> oidcOriginalRoleNames = getTokenRoles(claims);
        oidcOriginalRoleNames.add(oidcConfiguration.minimumProfile);

        List<String> roleNames = simpleConvertRoles(oidcOriginalRoleNames);
        Map<Profile, List<String>> profileGroups = getProfileGroups(roleNames);

        List<SimpleGrantedAuthority> authorities = profileGroups.keySet().stream()
            .map(x -> new SimpleGrantedAuthority(x.toString()))
            .collect(Collectors.toList());

        return roleHierarchy.getReachableGrantedAuthorities(authorities);
    }

    public Profile getProfile(Map<String, Object> attributes) {
        List<String> oidcOriginalRoleNames = getTokenRoles(attributes);
        oidcOriginalRoleNames.add(oidcConfiguration.minimumProfile);

        List<String> roleNames = simpleConvertRoles(oidcOriginalRoleNames);

        Map<Profile, List<String>> profileGroups = getProfileGroups(roleNames);
        Profile profile = getMaxProfile(profileGroups);
        return profile;
    }

    /**
     * Assign the best profile for the user based on the profileGroups available.
     *
     * @param profileGroups profile to scan for best profile.
     */
    //from keycloak
    public Profile getMaxProfile(Map<Profile, List<String>> profileGroups) {
        Profile maxProfile = null;

        for (Profile p : profileGroups.keySet()) {
            if (maxProfile == null) {
                maxProfile = p;
            } else if (maxProfile.compareTo(p) >= 0) {
                maxProfile = p;
            }
        }

        // Fallback if no profile
        if (maxProfile == null) {
            maxProfile = Profile.Guest;
        }
        return maxProfile;
    }

    /**
     * Get the profiles, and the list of groups for that profile, from the access token.
     *
     * @return map object with the profile and related groups.
     */
    //from keycloak
    public Map<Profile, List<String>> getProfileGroups(List<String> rolesInToken) {

        String roleGroupSeparator = oidcConfiguration.groupPermissionSeparator;
        Map<Profile, List<String>> profileGroups = new HashMap<>();

        Set<String> roleGroupList = new HashSet<>();

        // Get role that are in the format of group:role format access
        // Todo Reevaluate to see if this is how we want to get role groups. It may not be a good idea to place separator in group name and parse it this way.
        for (String role : rolesInToken) {
            if (role.contains(roleGroupSeparator)) {
                Log.debug(Geonet.SECURITY, "Identified group:profile (" + role + ") from user token.");
                roleGroupList.add(role);
            } else {
                // Only use the profiles we know of and don't add duplicates.
                Profile p = Profile.findProfileIgnoreCase(role);
                if (p != null && !profileGroups.containsKey(p)) {
                    profileGroups.put(p, new ArrayList<>());
                }
            }
        }


        for (String rg : roleGroupList) {
            String[] rg_role_groups = rg.split(roleGroupSeparator);

            if (rg_role_groups.length == 0 || StringUtils.isEmpty(rg_role_groups[0])) {
                continue;
            }

            Profile p = null;
            if (rg_role_groups.length >= 1) {
                p = Profile.findProfileIgnoreCase(rg_role_groups[1]);
            }
            // If we cannot find the profile then lets ignore this entry.
            if (p == null) {
                continue;
            }

            List<String> groups;
            if (profileGroups.containsKey(p)) {
                groups = profileGroups.get(p);
            } else {
                groups = new ArrayList<>();
            }
            if (rg_role_groups.length > 1) {
                groups.add(rg_role_groups[0]);
            }
            profileGroups.put(p, groups);
        }

        return profileGroups;
    }


    /**
     * This extracts a list of roles from the token.
     * Basically, uses the oidcConfiguration.getIdTokenRoleLocation() as a simple xpath (into a map).
     *
     * @param attributes - hashmap (i.e. from json) of the attributes in token
     * @return
     */
    public List<String> getTokenRoles(Map<String, Object> attributes) {
        String pathToRoles = oidcConfiguration.getIdTokenRoleLocation();
        if ((pathToRoles == null) || (pathToRoles.trim().isEmpty())) {
            Log.debug(Geonet.SECURITY, "oidc: pathToRoles is null/empty - cannot process");
            return new ArrayList<>();
        }

        String[] paths = pathToRoles.trim().split("\\.");
        Map<String, Object> info = attributes;

        for (int t = 0; t < paths.length; t++) {
            String path = paths[t];
            Object o = info.get(path);
            if (o == null) {
                Log.debug(Geonet.SECURITY, "oidc: pathToRoles - cannot find path component named: " + path);
                return new ArrayList<>();
            }
            if (o instanceof Map) {
                info = (Map<String, Object>) o;
            }
            if (o instanceof List) {
                if (t == paths.length - 1)
                    return new ArrayList<>((List<String>) o);
                Log.debug(Geonet.SECURITY, "oidc: pathToRoles - found a list instead of item at " + path);
                return new ArrayList<>(); // not expecting to see this
            }
        }
        Log.debug(Geonet.SECURITY, "oidc: pathToRoles - couldnt find role list - " + pathToRoles);
        return new ArrayList<>(); // unexpected...
    }

}
