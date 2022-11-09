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
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Class for handling Oidc User and the Geonetwork User.
 * <p>
 * Most of the work, here, is to save the user's profile-groups.  See SimpleOidcUser for other details.
 */
public class OidcUser2GeonetworkUser {

    @Autowired
    OIDCConfiguration oidcConfiguration;
    @Autowired
    OIDCRoleProcessor oidcRoleProcessor;
    @Autowired
    SimpleOidcUserFactory simpleOidcUserFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private LanguageRepository langRepository;
    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired
    private GeonetworkAuthenticationProvider geonetworkAuthenticationProvider;


    public UserDetails getUserDetails(Map attributes, boolean withDbUpdate) {
        SimpleOidcUser simpleUser = simpleOidcUserFactory.create(attributes);
        if (!StringUtils.hasText(simpleUser.getUsername()))
            return null;

        User user;
        boolean newUserFlag = false;
        try {
            user = (User) geonetworkAuthenticationProvider.loadUserByUsername(simpleUser.getUsername());
        } catch (UsernameNotFoundException e) {
            user = new User();
            user.setUsername(simpleUser.getUsername());
            newUserFlag = true;
            Log.debug(Geonet.SECURITY, "Adding a new user: " + user);
        }

        simpleUser.updateUser(user); // copy attributes from the IDToken to the GN user

        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(attributes);
        user.setProfile(oidcRoleProcessor.getProfile(attributes));


        //Apply changes to database is required.
        if (withDbUpdate) {
            if (newUserFlag || oidcConfiguration.isUpdateProfile()) {
                userRepository.save(user);
            }
            if (newUserFlag || oidcConfiguration.isUpdateGroup()) {
                updateGroups(profileGroups, user);
            }
        }
        return user;
    }

    /**
     * given an oidc ID token, get the spring user details for the user.
     * If this is a new user, it will be saved to the GN database (as a GN user).
     * <p>
     * If withDbUpdate is true, and  isUserProfileUpdateEnabled (oidc configuration),
     * then the user will be re-saved to the database (with info from the ID Token)
     * <p>
     * If withDbUpdate is true, and  isUserGroupUpdateEnabled (oidc configuration),
     * then the user's profile-groups will be re-saved to the database (with info from the ID Token)
     *
     * @param idToken
     * @param withDbUpdate
     * @return
     */
    public UserDetails getUserDetails(OidcIdToken idToken, boolean withDbUpdate) {
        SimpleOidcUser simpleUser = simpleOidcUserFactory.create(idToken);
        if (!StringUtils.hasText(simpleUser.getUsername()))
            return null;

        User user;
        boolean newUserFlag = false;
        try {
            user = (User) geonetworkAuthenticationProvider.loadUserByUsername(simpleUser.getUsername());
        } catch (UsernameNotFoundException e) {
            user = new User();
            user.setUsername(simpleUser.getUsername());
            newUserFlag = true;
            Log.debug(Geonet.SECURITY, "Adding a new user: " + user);
        }

        simpleUser.updateUser(user); // copy attributes from the IDToken to the GN user

        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(idToken);
        user.setProfile(oidcRoleProcessor.getProfile(idToken));


        //Apply changes to database is required.
        if (withDbUpdate) {
            if (newUserFlag || oidcConfiguration.isUpdateProfile()) {
                userRepository.save(user);
            }
            if (newUserFlag || oidcConfiguration.isUpdateGroup()) {
                updateGroups(profileGroups, user);
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
    private void updateGroups(Map<Profile, List<String>> profileGroups, User user) {
        // First we remove all previous groups
        userGroupRepository.deleteAll(UserGroupSpecs.hasUserId(user.getId()));

        // Now we add the groups
        for (Profile p : profileGroups.keySet()) {
            List<String> groups = profileGroups.get(p);
            for (String rgGroup : groups) {

                Group group = groupRepository.findByName(rgGroup);

                if (group == null) {
                    group = new Group();
                    group.setName(rgGroup);

                    // Populate languages for the group
                    for (Language l : langRepository.findAll()) {
                        group.getLabelTranslations().put(l.getId(), group.getName());
                    }

                    groupRepository.save(group);
                }

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
                    userGroupRepository.save(ug);
                }

                userGroupRepository.save(usergroup);
            }
        }
    }


}
