/*
 *  Copyright (C) 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.kernel.security.shibboleth;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.apache.batik.util.resources.ResourceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.security.BaseUserUtils;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.kernel.security.WritableUserDetailsContextMapper;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.ObjectUtils;
import jeeves.component.ProfileManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ETj (etj at geo-solutions.it)
 * @author María Arias de Reyna (delawen)
 */
public class ShibbolethUserUtils extends BaseUserUtils {
    private UserDetailsManager userDetailsManager;
    private WritableUserDetailsContextMapper udetailsmapper;

    static MinimalUser parseUser(ServletRequest request, ResourceManager resourceManager, ProfileManager profileManager,
            ShibbolethUserConfiguration config) {
        return MinimalUser.create(request, config);
    }

    protected static String getHeader(HttpServletRequest req, String name, String defValue) {

        if (name == null || name.trim().isEmpty()) {
            return defValue;
        }

        String value = req.getHeader(name);

        if (value == null)
            return defValue;

        if (value.length() == 0)
            return defValue;

        return value;
    }

    /**
     * @return the inserted/updated user or null if no valid user found or any error
     *         happened
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    protected UserDetails setupUser(ServletRequest request, ShibbolethUserConfiguration config) throws Exception {
        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);
        UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);
        GeonetworkAuthenticationProvider authProvider = ApplicationContextHolder.get()
                .getBean(GeonetworkAuthenticationProvider.class);

        // Read in the data from the headers
        HttpServletRequest req = (HttpServletRequest) request;

        String username = getHeader(req, config.getUsernameKey(), "");
        String surname = getHeader(req, config.getSurnameKey(), "");
        String firstname = getHeader(req, config.getFirstnameKey(), "");
        String organisation = getHeader(req, config.getOrganisationKey(), "");
        String email = getHeader(req, config.getEmailKey(), "");
        String arraySeparator = config.getArraySeparator();
        String roleGroupSeparator = config.getRoleGroupSeparator();

        // RoleGroupKey header format: sample,UserAdmin;sample,Editor
        // It has precedence over individual ProfileKey and GroupKey headers if all are provided.
        //      - ProfileKey header format: UserAdmin;Editor
        //      - GroupKey header format: sample;sample
        String roleGroup_header = getHeader(req, config.getRoleGroupKey(), "");
        String[] roleGroups = new String[0];
        if (!ObjectUtils.isEmpty(roleGroup_header)) {
            roleGroups = roleGroup_header.split(arraySeparator);
        } else {
            String profile_header = getHeader(req, config.getProfileKey(), Profile.Guest.name());
            String[] profiles = new String[0];
            if (!ObjectUtils.isEmpty(profile_header)) {
                profiles = profile_header.split(arraySeparator);
            }

            String group_header = getHeader(req, config.getGroupKey(), config.getDefaultGroup());
            String[] groups = new String[0];
            if (!ObjectUtils.isEmpty(group_header)) {
                groups = group_header.split(arraySeparator);
            }

            List<String> roleGroupsList = new ArrayList<>(groups.length);
            for (int i = 0; i < groups.length; i++) {
                String profile;
                if (profiles.length > i) {
                    profile = profiles[i];
                } else {
                    // Fallback if no profile
                    profile = Profile.Guest.toString();
                }

                roleGroupsList.add(groups[i] + roleGroupSeparator + profile);
            }

            roleGroups = roleGroupsList.stream().toArray(String[]::new);
        }

        if (!ObjectUtils.isEmpty(username)) {

            // FIXME: needed? only accept the first 256 chars
            if (username.length() > 256) {
                username = username.substring(0, 256);
            }

            // Create or update the user
            User user = null;
            try {
                user = (User) authProvider.loadUserByUsername(username);

                if (config.isUpdateGroup()) {
                    // Now we add the groups
                    assignGroups(groupRepository, userGroupRepository, roleGroups,
                            roleGroupSeparator, user);
                }

                // Assign the highest profile available
                if (config.isUpdateProfile()) {
                    assignProfile(roleGroups, roleGroupSeparator, user);
                    userRepository.save(user);
                }

            } catch (UsernameNotFoundException e) {
                user = new User();
                user.setUsername(username);
                user.setSurname(surname);
                user.setName(firstname);
                user.setOrganisation(organisation);

                // Add email
                if (!ObjectUtils.isEmpty(email)) {
                    user.getEmailAddresses().add(email);
                }

                assignProfile(roleGroups, roleGroupSeparator, user);
                userRepository.save(user);

                assignGroups(groupRepository, userGroupRepository, roleGroups, roleGroupSeparator,
                        user);
            }

            if (udetailsmapper != null) {
                // If is not null, we may want to write to ldap if user does not exist
                LDAPUser ldapUserDetails = null;
                try {
                    ldapUserDetails = (LDAPUser) userDetailsManager.loadUserByUsername(username);
                } catch (Throwable t) {
                    Log.error(Geonet.GEONETWORK, "Shibboleth setupUser error: " + t.getMessage(), t);
                }

                if (ldapUserDetails == null) {
                    ldapUserDetails = new LDAPUser(username);
                    ldapUserDetails.getUser().setName(firstname).setSurname(surname);
                    ldapUserDetails.getUser().setOrganisation(organisation);

                    ldapUserDetails.getUser().setProfile(user.getProfile());
                    ldapUserDetails.getUser().getEmailAddresses().clear();
                    if (ObjectUtils.isEmpty(email)) {
                        ldapUserDetails.getUser().getEmailAddresses().add(username + "@unknownIdp");
                    } else {
                        ldapUserDetails.getUser().getEmailAddresses().add(email);
                    }
                }

                udetailsmapper.saveUser(ldapUserDetails);

                user = ldapUserDetails.getUser();
            }

            return user;
        }

        return null;
    }

    private void assignGroups(GroupRepository groupRepository, UserGroupRepository userGroupRepository,
                              String[] role_groups, String separator, User user) {

        Set<UserGroup> userGroups =  new HashSet<>();

        // Assign groups
        int i = 0;

        for (String rg : role_groups) {
            String[] tmp = rg.split(separator);

            if (tmp.length == 0 || ObjectUtils.isEmpty(tmp[0])) {
                continue;
            }

            String group = tmp[0];

            Group g = getOrCreateGroup(group);

            UserGroup usergroup = new UserGroup();
            usergroup.setGroup(g);
            usergroup.setUser(user);
            if (tmp.length > 1) {
                Profile profile = Profile.findProfileIgnoreCase(tmp[1]);
                if (profile.equals(Profile.Administrator)) {
                    // As we are assigning to a group, it is UserAdmin instead
                    profile = Profile.UserAdmin;
                }
                usergroup.setProfile(profile);

                if (profile.equals(Profile.Reviewer)) {
                    UserGroup ug = new UserGroup();
                    ug.setGroup(g);
                    ug.setUser(user);
                    ug.setProfile(Profile.Editor);
                    userGroups.add(ug);
                }
            } else {
                // Failback if no profile
                usergroup.setProfile(Profile.Guest);
            }
            userGroups.add(usergroup);
        }

        userGroupRepository.updateUserGroups(user.getId(), userGroups);
    }

    private void assignProfile(String[] role_groups, String roleGroupSeparator, User user) {
        // Assign the highest profile to the user
        user.setProfile(null);

        for (String rg : role_groups) {
            String[] tmp = rg.split(roleGroupSeparator);
            Profile p = Profile.findProfileIgnoreCase(tmp[1]);
            if (p != null && user.getProfile() == null) {
                user.setProfile(p);
            } else if (p != null && user.getProfile().compareTo(p) >= 0) {
                user.setProfile(p);
            }
        }

        // Failback if no profile
        if (user.getProfile() == null) {
            user.setProfile(Profile.Guest);
        }
    }

    public static class MinimalUser {

        private String username;
        private String name;
        private String surname;
        private String organisation;
        private String profile;

        static MinimalUser create(ServletRequest request, ShibbolethUserConfiguration config) {

            // Read in the data from the headers
            HttpServletRequest req = (HttpServletRequest) request;

            String username = getHeader(req, config.getUsernameKey(), "");
            String surname = getHeader(req, config.getSurnameKey(), "");
            String firstname = getHeader(req, config.getFirstnameKey(), "");
            String organisation = getHeader(req, config.getOrganisationKey(), "");
            String profile = getHeader(req, config.getProfileKey(), "");

            if (username.trim().length() > 0) {

                MinimalUser user = new MinimalUser();
                user.setUsername(username);
                user.setName(firstname);
                user.setSurname(surname);
                user.setOrganisation(organisation);
                user.setProfile(profile);
                return user;

            } else {
                return null;
            }
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
    }

}
