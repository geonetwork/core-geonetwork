/*
 * Copyright (C) 2001-2017 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.keycloak;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.*;

public class KeycloakUserUtils {

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

    @Autowired
    private KeycloakConfiguration keycloakConfiguration;

    @Autowired
    AdapterDeploymentContext adapterDeploymentContext;
    /**
     * @return the inserted/updated user or null if no valid user found or any error
     * happened
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    protected UserDetails setupUser(ServletRequest request, KeycloakPrincipal keycloakPrincipal) {
        BaselUser baselUser = new BaselUser(request, keycloakPrincipal, keycloakConfiguration);

        String roleGroupSeparator = keycloakConfiguration.getRoleGroupSeparator();

        Set<String> roleGroupList = new HashSet<>();
        // Get role that are in the format of group:role format access
        // Todo Reevaluate to see if this is how we want to get role groups. It may not be a good idea to place separator in group name and parse it this way.
        if (keycloakPrincipal.getKeycloakSecurityContext().getToken().getResourceAccess(adapterDeploymentContext.resolveDeployment(null).getResourceName()) != null) {
            for (String role : keycloakPrincipal.getKeycloakSecurityContext().getToken().getResourceAccess(adapterDeploymentContext.resolveDeployment(null).getResourceName()).getRoles()) {
                // Only use the profiles we know off
                if (role.contains(roleGroupSeparator)) {
                    Log.debug(Geonet.SECURITY, "Identified role " + role + " from user token.");
                    roleGroupList.add(role);
                }
            }
        }


        if (!StringUtils.isEmpty(baselUser.getUsername())) {
            // Create or update the user
            User user = null;
            boolean newUserFlag = false;
            try {
                user = (User) geonetworkAuthenticationProvider.loadUserByUsername(baselUser.getUsername());
            } catch (UsernameNotFoundException e) {
                user = new User();
                user.setUsername(baselUser.getUsername());
                newUserFlag = true;
                Log.debug(Geonet.SECURITY, "Adding a new user: " + user);
            }

            if (!StringUtils.isEmpty(baselUser.getSurname())) {
                user.setSurname(baselUser.getSurname());
            }
            if (!StringUtils.isEmpty(baselUser.getFirstname())) {
                user.setName(baselUser.getFirstname());
            }
            if (!StringUtils.isEmpty(baselUser.getOrganisation())) {
                user.setOrganisation(baselUser.getOrganisation());
            }

            // Only update email if it does not already exists and email is not empty
            if (!StringUtils.isEmpty(baselUser.getEmail()) && !user.getEmailAddresses().contains(baselUser.getEmail())) {
                // If updating profile then assume emails are in sync with keycloak so replace first email which is all there should be.
                if (keycloakConfiguration.isUpdateProfile()) {
                    user.getEmailAddresses().clear();
                }
                user.getEmailAddresses().add(baselUser.getEmail());
            }

            // Assign the highest profile available
            assignProfile(baselUser.getProfile(), roleGroupList, roleGroupSeparator, user);
            if (newUserFlag || keycloakConfiguration.isUpdateProfile()) {
                // We only get the address information if updating the profile.
                Address address;
                if (keycloakPrincipal.getKeycloakSecurityContext().getToken().getAddress() != null) {
                    if (user.getAddresses().size() > 0) {
                        address = user.getAddresses().iterator().next();
                    } else {
                        address = new Address();
                    }
                    address.setAddress(keycloakPrincipal.getKeycloakSecurityContext().getToken().getAddress().getStreetAddress());
                    address.setCity(keycloakPrincipal.getKeycloakSecurityContext().getToken().getAddress().getLocality());
                    address.setState(keycloakPrincipal.getKeycloakSecurityContext().getToken().getAddress().getRegion());
                    address.setZip(keycloakPrincipal.getKeycloakSecurityContext().getToken().getAddress().getPostalCode());
                    address.setCountry(keycloakPrincipal.getKeycloakSecurityContext().getToken().getAddress().getCountry());
                    user.getAddresses().clear();
                    user.getAddresses().add(address);
                }

                userRepository.save(user);
            }

            if (newUserFlag || keycloakConfiguration.isUpdateGroup()) {
                updateGroups(roleGroupList, roleGroupSeparator, user);
            }

            return user;
        }

        return null;
    }

    private void updateGroups(Set<String> roleGroupList, String separator, User user) {
        // First we remove all previous groups
        userGroupRepository.deleteAll(UserGroupSpecs.hasUserId(user.getId()));

        // Now we add the groups
        int i = 0;

        for (String rg : roleGroupList) {
            String[] rgSplitArray = rg.split(separator);

            if (rgSplitArray.length == 0 || StringUtils.isEmpty(rgSplitArray[0])) {
                continue;
            }

            String rgGroup = rgSplitArray[0];

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
            if (rgSplitArray.length > 1) {
                String rgProfile = rgSplitArray[1];
                Profile profile = Profile.findProfileIgnoreCase(rgProfile);
                // If we cannot find the profile then lets ignore this entry.
                if (profile == null) {
                    continue;
                }
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
            } else {
                // Failback if no profile
                usergroup.setProfile(Profile.Guest);
            }
            userGroupRepository.save(usergroup);
        }
    }

    private void assignProfile(String profile, Set<String> roleGroupList, String roleGroupSeparator, User user) {
        // Assign the highest profile to the user
        user.setProfile(Profile.findProfileIgnoreCase(profile));

        for (String rg : roleGroupList) {
            String[] rg_role_groups = rg.split(roleGroupSeparator);
            Profile p = null;
            if (rg_role_groups.length > 1) {
                p = Profile.findProfileIgnoreCase(rg_role_groups[1]);
            }
            // If we cannot find the profile then lets ignore this entry.
            if (p == null) {
                continue;
            }
            if (user.getProfile() == null) {
                user.setProfile(p);
            } else if (user.getProfile().compareTo(p) >= 0) {
                user.setProfile(p);
            }
        }

        // Failback if no profile
        if (user.getProfile() == null) {
            user.setProfile(Profile.Guest);
        }
    }

    private class BaselUser {

        private String username;
        private String firstname;
        private String surname;
        private String organisation;
        private String profile;
        private String email;

        BaselUser(ServletRequest request, KeycloakPrincipal keycloakPrincipal, KeycloakConfiguration keycloakConfiguration) {

            // Read in the data from the headers
            HttpServletRequest req = (HttpServletRequest) request;

            username = keycloakPrincipal.getKeycloakSecurityContext().getToken().getPreferredUsername();
            if (username == null) {
                username = keycloakPrincipal.getKeycloakSecurityContext().getToken().getName();
            }
            if (username != null) {
                // FIXME: needed? only accept the first 256 chars
                if (username.length() > 256) {
                    username = username.substring(0, 256);
                }
            }

            if (username.length() > 0) {
                surname = keycloakPrincipal.getKeycloakSecurityContext().getToken().getFamilyName();
                firstname = keycloakPrincipal.getKeycloakSecurityContext().getToken().getGivenName();
                email = keycloakPrincipal.getKeycloakSecurityContext().getToken().getEmail();

                organisation = null;
                if (keycloakPrincipal.getKeycloakSecurityContext().getToken().getOtherClaims() != null &&
                        keycloakPrincipal.getKeycloakSecurityContext().getToken().getOtherClaims().containsKey(keycloakConfiguration.getOrganisationKey())) {
                    organisation = (String) keycloakPrincipal.getKeycloakSecurityContext().getToken().getOtherClaims().get(keycloakConfiguration.getOrganisationKey());
                }

                Set<String> profileSet = new HashSet<>();

                // Get role access
                if (keycloakPrincipal.getKeycloakSecurityContext().getToken().getResourceAccess(adapterDeploymentContext.resolveDeployment(null).getResourceName()) != null) {
                    for (String role : keycloakPrincipal.getKeycloakSecurityContext().getToken().getResourceAccess(adapterDeploymentContext.resolveDeployment(null).getResourceName()).getRoles()) {
                        // Only use the profiles we know off
                        if (Profile.findProfileIgnoreCase(role) != null) {
                            profileSet.add(role);
                        }
                    }
                }

                // We only want the max profile in this case.
                if (profileSet.size() > 0) {
                    Profile maxProfile = null;
                    for (String singleProfile : profileSet) {
                        Profile p = Profile.findProfileIgnoreCase(singleProfile);
                        if (p != null && maxProfile == null) {
                            maxProfile = p;
                        } else if (p != null && maxProfile.compareTo(p) >= 0) {
                            maxProfile = p;
                        }
                    }
                    profile = maxProfile.name();
                }
            }
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getOrganisation() {
            return organisation;
        }

        public void setOrganisation(String organisation) {
            this.organisation = organisation;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
