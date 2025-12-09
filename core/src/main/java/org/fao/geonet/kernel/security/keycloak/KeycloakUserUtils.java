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

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.security.BaseUserUtils;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class KeycloakUserUtils extends BaseUserUtils {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private GeonetworkAuthenticationProvider geonetworkAuthenticationProvider;

    @Autowired
    private KeycloakConfiguration keycloakConfiguration;

    @Autowired
    AdapterDeploymentContext adapterDeploymentContext;

    // User details of 1000 to help quickly retrieve userDetails from the token.
    // Access tokens are short-lived so 5 minutes should be long enough.
    private final Cache<String, UserDetails> UserDetailsCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(1000).build();

    /**
     * Get the userDetail from the access token
     * It will cache the process for faster access.
     * Note: Accessing a web page may cause multiple noCache calls due to bearer token calls from the page being called.
     *       caching will only work when the same token is being used.
     * @param accessToken - Access token to get the user details from
     * @param withDbUpdate - As we don't want to update the database everytime we convert the token to userDetails
     *                     This flag is used to indicate that db updates can be applied or not.
     *                     Note: that this will only work on first request for the token due to caching.
     *                           It should be ok as we generally only want to write the data on the first request.
     * @return the userDetails object or null if no valid user found or any error happened
     */
    public UserDetails getUserDetails(AccessToken accessToken, boolean withDbUpdate) {
        try {
            return UserDetailsCache.get(accessToken.getId(), new Callable<UserDetails>() {
                @Override
                public UserDetails call()  {
                    return getUserDetailsNoCache(accessToken, withDbUpdate);
                }
            });
        } catch (ExecutionException e) {
            Log.debug(Geonet.SECURITY, "Error getting user details from token.", e);
            return null;
        }
    }

   /**
    * Get the userDetail from the access token
    * No cache is used by this version
    * @param accessToken - Access token to get the user details from
    * @param withDbUpdate - As we don't want to update the database everytime we convert the token to userDetails
    *                     This flag is used to indicate that db updates can be applied or not.
    * @return the userDetails object or null if no valid user found or any error happened
    */
    @Transactional(value = TxType.REQUIRES_NEW)
    protected UserDetails getUserDetailsNoCache(AccessToken accessToken, boolean withDbUpdate) {
        BaselUser baselUser = new BaselUser(accessToken, keycloakConfiguration);

        if (!ObjectUtils.isEmpty(baselUser.getUsername())) {
            // Create or update the user
            User user;
            boolean newUserFlag = false;
            try {
                user = (User) geonetworkAuthenticationProvider.loadUserByUsername(baselUser.getUsername());
            } catch (UsernameNotFoundException e) {
                user = new User();
                user.setUsername(baselUser.getUsername());
                newUserFlag = true;
                Log.debug(Geonet.SECURITY, "Adding a new user: " + user);
            }

            if (!ObjectUtils.isEmpty(baselUser.getSurname())) {
                user.setSurname(baselUser.getSurname());
            }
            if (!ObjectUtils.isEmpty(baselUser.getFirstname())) {
                user.setName(baselUser.getFirstname());
            }
            if (!ObjectUtils.isEmpty(baselUser.getOrganisation())) {
                user.setOrganisation(baselUser.getOrganisation());
            }

            // Only update email if it does not already exist and email is not empty
            if (!ObjectUtils.isEmpty(baselUser.getEmail()) && !user.getEmailAddresses().contains(baselUser.getEmail())) {
                // If updating profile then assume emails are in sync with keycloak so replace first email which is all there should be.
                if (keycloakConfiguration.isUpdateProfile()) {
                    user.getEmailAddresses().clear();
                }
                user.getEmailAddresses().add(baselUser.getEmail());
            }

            Address address;
            if (accessToken.getAddress() != null) {
                if (user.getAddresses().size() > 0) {
                    address = user.getAddresses().iterator().next();
                } else {
                    address = new Address();
                }
                address.setAddress(accessToken.getAddress().getStreetAddress());
                address.setCity(accessToken.getAddress().getLocality());
                address.setState(accessToken.getAddress().getRegion());
                address.setZip(accessToken.getAddress().getPostalCode());
                address.setCountry(accessToken.getAddress().getCountry());
                user.getAddresses().clear();
                user.getAddresses().add(address);
            }

            // Assign the highest profile available
            Map<Profile, List<String>> profileGroups = getProfileGroups(accessToken);
            if (newUserFlag || keycloakConfiguration.isUpdateProfile()) {
                user.setProfile(getMaxProfile(baselUser.getProfile(), profileGroups));
            }

            //Apply changes to database is required.
            if (withDbUpdate) {
                if (newUserFlag || keycloakConfiguration.isUpdateProfile()) {
                    userRepository.save(user);
                }

                if (newUserFlag || keycloakConfiguration.isUpdateGroup()) {
                    updateGroups(profileGroups, user);
                }
            }

            return user;
        }

        return null;
    }

    /**
     * Get the profiles, and the list of groups for that profile, from the access token.
     * @param accessToken - keycloak access token to get profile and group information from.
     * @return map object with the profile and related groups.
     */
    private Map<Profile, List<String>> getProfileGroups(AccessToken accessToken) {

        String roleGroupSeparator = keycloakConfiguration.getRoleGroupSeparator();
        Map<Profile, List<String>> profileGroups = new HashMap<>();

        Set<String> roleGroupList = new HashSet<>();
        // Get role that are in the format of group:role format access
        // Todo Reevaluate to see if this is how we want to get role groups. It may not be a good idea to place separator in group name and parse it this way.
        if (accessToken.getResourceAccess(adapterDeploymentContext.resolveDeployment(null).getResourceName()) != null) {
            for (String role : accessToken.getResourceAccess(adapterDeploymentContext.resolveDeployment(null).getResourceName()).getRoles()) {
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
        }

        for (String rg : roleGroupList) {
            String[] rg_role_groups = rg.split(roleGroupSeparator);

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
                groups =  new ArrayList<>();
            }
            if (rg_role_groups.length > 1) {
                groups.add(rg_role_groups[0]);
            }
            profileGroups.put(p, groups);
        }

        return profileGroups;
    }

    /**
     * Update users group information in the database.
     * @param profileGroups object containing the profile and related groups.
     * @param user to apply the changes to.
     */
    private void updateGroups(Map<Profile, List<String>> profileGroups, User user) {
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

    /**
     * Assign the best profile for the user based on the profileGroups available.
     * @param defaultProfile default profile used if nothing found.  If null then Guest will be used.
     * @param profileGroups profile to scan for best profile.
     */
    private Profile getMaxProfile(String defaultProfile, Map<Profile, List<String>> profileGroups) {
        Profile maxProfile = null;
        if (defaultProfile != null) {
            maxProfile = Profile.findProfileIgnoreCase(defaultProfile);
        }

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

    private class BaselUser {

        private String username;
        private String firstname;
        private String surname;
        private String organisation;
        private String profile;
        private String email;

        BaselUser(AccessToken accessToken, KeycloakConfiguration keycloakConfiguration) {

            username = accessToken.getPreferredUsername();
            if (username == null) {
                username = accessToken.getName();
            }
            if (username != null) {
                // FIXME: needed? only accept the first 256 chars
                if (username.length() > 256) {
                    username = username.substring(0, 256);
                }
            }

            if (username != null && username.length() > 0) {
                surname = accessToken.getFamilyName();
                firstname = accessToken.getGivenName();
                email = accessToken.getEmail();

                organisation = null;
                if (accessToken.getOtherClaims() != null &&
                    accessToken.getOtherClaims().containsKey(keycloakConfiguration.getOrganisationKey())) {
                    organisation = (String) accessToken.getOtherClaims().get(keycloakConfiguration.getOrganisationKey());
                }

                Map<Profile, List<String>> profileGroups = getProfileGroups(accessToken);
                if (profileGroups != null && profileGroups.size() > 0) {
                    profile = getMaxProfile(null, profileGroups).name();
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
