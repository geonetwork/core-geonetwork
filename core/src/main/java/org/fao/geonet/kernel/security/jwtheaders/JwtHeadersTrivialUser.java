/*
 * Copyright (C) 2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.jwtheaders;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.utils.Log;
import org.geoserver.security.jwtheaders.JwtConfiguration;
import org.geoserver.security.jwtheaders.roles.JwtHeadersRolesExtractor;
import org.geoserver.security.jwtheaders.token.TokenValidator;
import org.geoserver.security.jwtheaders.username.JwtHeaderUserNameExtractor;
import org.springframework.util.ObjectUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * uses the GS library to process the headers.
 * This returns a GN-compliant "user" (JwtHeadersTrivialUser) that
 * has the header-derived username and roles (profile and profileGroups).
 * <p>
 * Most of the code, here is for processing profileGroups (Map<Profile, List<String>>).
 */
public class JwtHeadersTrivialUser {

    static String ROLE_GROUP_SEPARATOR = ":";
    static Profile MIN_PROFILE = Profile.RegisteredUser;
    Map<Profile, List<String>> profileGroups;
    private String username;

    //----------------------
    private Profile profile;

    public JwtHeadersTrivialUser(String userName) {
        setUsername(userName);
        profileGroups = new HashMap<>();
    }

    public static JwtHeadersTrivialUser create(JwtConfiguration config, HttpServletRequest request) throws IOException {
        if (request == null || config == null || config.getUserNameHeaderAttributeName() == null) {
            Log.debug(Geonet.SECURITY, "JwtHeadersUser.create called with null args!");
            return null; // nothing to do
        }

        var userNameHeader = request.getHeader(config.getUserNameHeaderAttributeName());
        if (userNameHeader == null) {
            return null; // no username in request!
        }

        //get the username from the headers (pay attention to config)
        JwtHeaderUserNameExtractor userNameExtractor = new JwtHeaderUserNameExtractor(config);
        var userName = userNameExtractor.extractUserName(userNameHeader);

        if (userName == null) {
            return null; // no username
        }

        var tokenValidator = new TokenValidator(config);
        try {
//            var accessToken = userNameHeader.replaceFirst("^Bearer", "");
//            accessToken = accessToken.replaceFirst("^bearer", "");
//            accessToken = accessToken.trim();
            tokenValidator.validate(userNameHeader);
        } catch (Exception e) {
            throw new IOException("JWT Token is invalid", e);
        }

        //get roles from the headers (pay attention to config)
        var result = new JwtHeadersTrivialUser(userName);
        handleRoles(result, config, request);

        return result;
    }

    /**
     * @param user    user to be modified
     * @param config  configuration (i.e. where to get the roles from and how to convert them)
     * @param request header to get the roles from
     */
    public static void handleRoles(JwtHeadersTrivialUser user, JwtConfiguration config, HttpServletRequest request) {
        if (!config.getJwtHeaderRoleSource().equals("JSON") && !config.getJwtHeaderRoleSource().equals("JWT"))
            return; // nothing to do - we aren't configured to handle roles extraction (get from GN DB).

        if (config.getRolesHeaderName() == null)
            return; //misconfigured

        //get the header value and extract the set of roles in it (processed by the RoleConverter)
        var rolesHeader = request.getHeader(config.getRolesHeaderName());
        JwtHeadersRolesExtractor rolesExtractor = new JwtHeadersRolesExtractor(config);
        var roles = rolesExtractor.getRoles(rolesHeader);


        updateUserWithRoles(user, roles);
    }

    public static void updateUserWithRoles(JwtHeadersTrivialUser user, Collection<String> roles) {
        //need to convert the simple roles into profileGroups
        // i.e.   group1:Reviewer  means user has "Reviewer" Profile for group "group1"
        Map<Profile, List<String>> profileGroups = extractProfileRoles(roles);

        //get the "max" profile (for User#Profile)
        if (profileGroups != null && profileGroups.size() > 0) {
            String profile = getMaxProfile(profileGroups).name();
            if (profile != null) {
                user.profile = Profile.valueOf(profile);
            }
        }
        else {
            user.profile = Profile.RegisteredUser;
        }

        //set the profileGroups
        user.profileGroups = profileGroups;
    }

    /**
     * Get the profiles, and the list of groups for that profile, from the access token.
     * <p>
     * i.e. ["Administrator","g2:Editor"]   ->   {"Administrator":[], "Editor":["g2"]}
     *
     * @param rolesInToken list of roles for the user (from headers + gone through the JWT Headers RoleConverter)
     * @return map object with the profile and related groups.
     */
    //from GN keycloak plugin
    public static Map<Profile, List<String>> extractProfileRoles(Collection<String> rolesInToken) {
        Map<Profile, List<String>> profileGroups = new HashMap<>();

        Set<String> roleGroupList = new HashSet<>();

        // Get role that are in the format of group:role format access
        // Todo Reevaluate to see if this is how we want to get role groups. It may not be a good idea to place separator in group name and parse it this way.
        for (String role : rolesInToken) {
            if (role.contains(ROLE_GROUP_SEPARATOR)) {
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
            String[] rg_role_groups = rg.split(ROLE_GROUP_SEPARATOR);

            if (rg_role_groups.length == 0 || ObjectUtils.isEmpty(rg_role_groups[0])) {
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

    //----------------------

    public static Profile getMaxProfile(Map<Profile, List<String>> profileGroups) {
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
            maxProfile = MIN_PROFILE;
        }
        return maxProfile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<Profile, List<String>> getProfileGroups() {
        return profileGroups;
    }

    public void setProfileGroups(Map<Profile, List<String>> profileGroups) {
        this.profileGroups = profileGroups;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

}
