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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.batik.util.resources.ResourceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.kernel.security.WritableUserDetailsContextMapper;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.StringUtils;

import jeeves.component.ProfileManager;

/**
 * @author ETj (etj at geo-solutions.it)
 * @author María Arias de Reyna (delawen)
 */
public class ShibbolethUserUtils {
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
		String email = getHeader(req, config.getEmailKey(), "");
		String arraySeparator = config.getArraySeparator();

		String profile_header = getHeader(req, config.getProfileKey(), Profile.Guest.name());
		String[] profiles = new String[0];
		if (!StringUtils.isEmpty(profile_header)) {
			profiles = profile_header.split(arraySeparator);
		}

		String group_header = getHeader(req, config.getGroupKey(), config.getDefaultGroup());
		String[] groups = new String[0];
		if (!StringUtils.isEmpty(group_header)) {
			groups = group_header.split(arraySeparator);
		}

		if (!StringUtils.isEmpty(username)) {

			// FIXME: needed? only accept the first 256 chars
			if (username.length() > 256) {
				username = username.substring(0, 256);
			}

			// Create or update the user
			User user = null;
			try {
				user = (User) authProvider.loadUserByUsername(username);

				if (config.isUpdateGroup()) {
					// First we remove all previous groups
					userGroupRepository.deleteAll(UserGroupSpecs.hasUserId(user.getId()));

					// Now we add the groups
					assignGroups(groupRepository, userGroupRepository, profiles, groups, user);
				}

				//Assign the highest profile available
				if (config.isUpdateProfile()) {
					assignProfile(profiles, user);
					userRepository.save(user);
				}

			} catch (UsernameNotFoundException e) {
				user = new User();
				user.setUsername(username);
				user.setSurname(surname);
				user.setName(firstname);

				// Add email
				if (!StringUtils.isEmpty(email)) {
					user.getEmailAddresses().add(email);
				}
				
				assignProfile(profiles, user);
				userRepository.save(user);
				
				assignGroups(groupRepository, userGroupRepository, profiles, groups, user);
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

					ldapUserDetails.getUser().setProfile(user.getProfile());
					ldapUserDetails.getUser().getEmailAddresses().clear();
					if (StringUtils.isEmpty(email)) {
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
			String[] profiles, String[] groups, User user) {
		// Assign groups
		int i = 0;

		for (String group : groups) {
			Group g = groupRepository.findByName(group);
			
			if(g == null) {
				g = new Group();
				g.setName(group);
				groupRepository.save(g);
			}

			UserGroup usergroup = new UserGroup();
			usergroup.setGroup(g);
			usergroup.setUser(user);
			if (profiles.length > i) {
				Profile profile = Profile.findProfileIgnoreCase(profiles[i]);
				if(profile.equals(Profile.Administrator)) {
					//As we are assigning to a group, it is UserAdmin instead
					profile = Profile.UserAdmin;
				}
				usergroup.setProfile(profile);
				
				if(profile.equals(Profile.Reviewer)) {
					UserGroup ug = new UserGroup();
					ug.setGroup(g);
					ug.setUser(user);
					ug.setProfile(Profile.Editor);
					userGroupRepository.save(ug);
				}
			} else {
				//Failback if no profile
				usergroup.setProfile(Profile.Guest);
			}
			userGroupRepository.save(usergroup);
			i++;
		}
	}

	private void assignProfile(String[] profiles, User user) {
		// Assign the highest profile to the user
		user.setProfile(null);
		
		for (String profile : profiles) {
			Profile p = Profile.findProfileIgnoreCase(profile);
			if (p != null && user.getProfile() == null) {
				user.setProfile(p);
			} else if (p != null && user.getProfile().compareTo(p) >= 0) {
				user.setProfile(p);
			} 
		}
		
		//Failback if no profile
		if(user.getProfile() == null) {
			user.setProfile(Profile.Guest);
		}
	}

	public static class MinimalUser {

		private String username;
		private String name;
		private String surname;
		private String profile;

		static MinimalUser create(ServletRequest request, ShibbolethUserConfiguration config) {

			// Read in the data from the headers
			HttpServletRequest req = (HttpServletRequest) request;

			String username = getHeader(req, config.getUsernameKey(), "");
			String surname = getHeader(req, config.getSurnameKey(), "");
			String firstname = getHeader(req, config.getFirstnameKey(), "");
			String profile = getHeader(req, config.getProfileKey(), "");

			if (username.trim().length() > 0) {

				MinimalUser user = new MinimalUser();
				user.setUsername(username);
				user.setName(firstname);
				user.setSurname(surname);
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

		public String getProfile() {
			return profile;
		}

		public void setProfile(String profile) {
			this.profile = profile;
		}
	}

}
