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
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.security.BaseUserUtils;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles GeoNetwork related User (and Group/UserGroup) activities.
 */
public class JwtHeadersUserUtil extends BaseUserUtils {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    GeonetworkAuthenticationProvider authProvider;

    /**
     * Gets a user.
     * 1. if the user currently existing in the GN DB:
     * - user is retrieved from the GN DB
     * - if the profile/profileGroup update is true then the DB is updated with info from `userFromHeaders`
     * - otherwise, the header roles are ignored and profile/profileGroups are taken from the GN DB
     * <p>
     * 2. if the user doesn't existing in the DB:
     * - user is created and saved to the DB
     * - if the profile/profileGroup update is true then the DB is updated with info from `userFromHeaders`
     * - otherwise, the header roles are ignored and profile/profileGroups are taken from the GN DB
     * - NOTE: in this case, the user will not have any profile/profileGraoup -
     * an admin will have to manually set them in GN GUI
     *
     * @param userFromHeaders This is user info supplied in the request headers
     * @param configuration   Configuration of the JWT Headers filter
     * @return
     */
    public User getUser(JwtHeadersTrivialUser userFromHeaders, JwtHeadersConfiguration configuration) {
        try {
            User userFromDb = (User) authProvider.loadUserByUsername(userFromHeaders.getUsername());
            injectRoles(userFromDb, userFromHeaders, configuration);
            return userFromDb;
        } catch (UsernameNotFoundException e) {
            return createUser(userFromHeaders, configuration);
        }
    }

    /**
     * given an existing user (both from GN DB and from the Request Headers),
     * update roles (profile/profileGroups).
     * <p>
     * isUpdateProfile/isUpdateGroup control if the DB is updated from the request Headers
     *
     * @param userFromDb
     * @param userFromHeaders
     * @param configuration
     */
    public void injectRoles(User userFromDb, JwtHeadersTrivialUser userFromHeaders, JwtHeadersConfiguration configuration) {
        if (configuration.isUpdateProfile()) {
            userFromDb.setProfile(userFromHeaders.getProfile());
            userRepository.save(userFromDb);
            Log.trace(Geonet.SECURITY, String.format("JwtHeaders: existing user (%s) with profile: '%s'", userFromDb.getUsername(), userFromHeaders.getProfile()));
        }
        if (configuration.isUpdateGroup()) {
            var profileGroups = userFromHeaders.getProfileGroups();
            if (profileGroups != null) {
                updateGroups(profileGroups, userFromDb);
                if (!profileGroups.isEmpty()) {
                    Log.trace(Geonet.SECURITY, "JwtHeaders: existing user profile groups: ");
                    for (var group : profileGroups.entrySet()) {
                        Log.debug(Geonet.SECURITY,
                            String.format("   + Profile '%s' has groups: '%s'",
                                group.getKey(),
                                String.join(",", group.getValue())
                            ));
                    }
                }
            }
        }

    }

    /**
     * creates a new user based on what was in the request headers.
     * <p>
     * profile updating (in GN DB) is controlled by isUpdateGroup
     * profileGroup updating (in GN DB) is controlled by isUpdateGroup
     * <p>
     * cf. updateGroups for how the profile/profileGroups are updated
     *
     * @param userFromHeaders
     * @param configuration
     * @return
     */
    public User createUser(JwtHeadersTrivialUser userFromHeaders, JwtHeadersConfiguration configuration) {
        //create user
        User user = new User();
        user.setUsername(userFromHeaders.getUsername());

        // Add email
        if (userFromHeaders.getUsername().contains("@")) {
            user.getEmailAddresses().add(userFromHeaders.getUsername());
            // dave@example.com --> dave
            user.setName(user.getUsername().substring(0, user.getUsername().indexOf("@")));
        }

        Log.debug(Geonet.SECURITY, "JwtHeaders: Creating new User in GN DB: " + user);

        if (configuration.isUpdateProfile()) {
            user.setProfile(userFromHeaders.getProfile());
            Log.debug(Geonet.SECURITY, String.format("JwtHeaders: new user profile: '%s'", userFromHeaders.getProfile()));
        } else {
            user.setProfile(Profile.RegisteredUser);//default to registered user
        }

        userRepository.save(user);


        if (configuration.isUpdateGroup()) {
            var profileGroups = userFromHeaders.getProfileGroups();
            if (profileGroups != null) {
                updateGroups(profileGroups, user);
                if (!profileGroups.isEmpty()) {
                    Log.debug(Geonet.SECURITY, "JwtHeaders: new user profile groups: ");
                    for (var group : profileGroups.entrySet()) {
                        Log.debug(Geonet.SECURITY,
                            String.format("   + Profile '%s' has groups: '%s'",
                                group.getKey(),
                                String.join(",", group.getValue())
                            ));
                    }
                }
            }
        }

        return user;
    }


    /**
     * Update users group information in the database.
     *
     * @param profileGroups object containing the profile and related groups.
     * @param user          to apply the changes to.
     */
    //from keycloak
    public void updateGroups(Map<Profile, List<String>> profileGroups, User user) {
        Set<UserGroup> userGroups = new HashSet<>();

        // Now we add the groups
        for (Profile p : profileGroups.keySet()) {
            List<String> groups = profileGroups.get(p);
            for (String rgGroup : groups) {

                Group group = getOrCreateGroup(rgGroup);

                UserGroup usergroup = new UserGroup();
                usergroup.setGroup(group);
                usergroup.setUser(user);

                Profile profile = p;
                if (profile.equals(Profile.Administrator)) {
                    // As we are assigning to a group, it is UserAdmin instead
                    profile = Profile.UserAdmin;
                }
                usergroup.setProfile(profile);

                //Todo - It does not seem necessary to add the user to the editor profile
                // since the reviewer is the parent of the editor
                // Seems like the permission checks should be smart enough to know that if a user
                // is a reviewer then they are also an editor.  Need to test and fix if necessary
                if (profile.equals(Profile.Reviewer)) {
                    UserGroup ug = new UserGroup();
                    ug.setGroup(group);
                    ug.setUser(user);
                    ug.setProfile(Profile.Editor);
                    userGroups.add(ug);
                }

                userGroups.add(usergroup);
            }
        }

        userGroupRepository.updateUserGroups(user.getId(), userGroups);
    }

}
