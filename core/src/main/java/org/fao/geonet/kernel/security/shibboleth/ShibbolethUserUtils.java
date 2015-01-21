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

import jeeves.component.ProfileManager;

import org.apache.batik.util.resources.ResourceManager;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.kernel.security.WritableUserDetailsContextMapper;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.ServletRequest;
import org.springframework.security.provisioning.UserDetailsManager;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class ShibbolethUserUtils {

	@Autowired
	private UserRepository _userRepository;

	@Autowired
	private GroupRepository _groupRepository;

	@Autowired
	private GeonetworkAuthenticationProvider authProvider;

	private UserDetailsManager userDetailsManager;
	private WritableUserDetailsContextMapper udetailsmapper;

	static MinimalUser parseUser(ServletRequest request,
			ResourceManager resourceManager, ProfileManager profileManager,
			ShibbolethUserConfiguration config) {
		return MinimalUser.create(request, config);
	}

	public static class MinimalUser {

		private String username;
		private String name;
		private String surname;
		private String profile;

		static MinimalUser create(ServletRequest request,
				ShibbolethUserConfiguration config) {

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

	/**
	 * @return the inserted/updated user or null if no valid user found or any
	 *         error happened
	 */
	protected UserDetails setupUser(ServletRequest request,
			ShibbolethUserConfiguration config) throws Exception {
		
		// Read in the data from the headers
		HttpServletRequest req = (HttpServletRequest) request;

		String username = getHeader(req, config.getUsernameKey(), "");
		String surname = getHeader(req, config.getSurnameKey(), "");
		String firstname = getHeader(req, config.getFirstnameKey(), "");
		Profile profile = Profile.findProfileIgnoreCase(getHeader(req,
				config.getProfileKey(), ""));
		// TODO add group to user
		//String group = getHeader(req, config.getGroupKey(), "");

		if (username != null && username.trim().length() > 0) { 
			// TODO ....add other constraints to be sure it's
			// a real shibboleth login and not fake
			//Depends on the shibboleth configuration?

			// Make sure the profile name is an exact match
			if (profile == null) {
				profile = Profile.Guest;
			}

			// TODO add group to user
			//if (group.equals("")) {
			//	group = config.getDefaultGroup();
			//}


			// FIXME: needed? only accept the first 256 chars
			if (username.length() > 256) {
				username = username.substring(0, 256);
			}

			// Create or update the user
			User user = new User();
			try {
				user = (User) authProvider.loadUserByUsername(username);
			} catch (UsernameNotFoundException e) {
				user.setUsername(username);
				user.setSurname(surname);
				user.setName(firstname);
				user.setProfile(profile);

				// TODO add group to user
				// Group g = _groupRepository.findByName(group);

			}

			if (udetailsmapper != null) { 
				//If is not null, we may want to write to ldap if user does not exist
				LDAPUser ldapUserDetails = null;
				try {
					ldapUserDetails = (LDAPUser) userDetailsManager
							.loadUserByUsername(username);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				if(ldapUserDetails == null) {
					ldapUserDetails = new LDAPUser(username);
					ldapUserDetails.getUser().setName(firstname)
							.setSurname(surname);
					ldapUserDetails.getUser().setProfile(profile);
					ldapUserDetails.getUser().getEmailAddresses().clear();
					ldapUserDetails.getUser().getEmailAddresses().add(username + "@unknownIdp");
				}
				
				udetailsmapper.saveUser(ldapUserDetails);

				user = ldapUserDetails.getUser();
			} else {
				_userRepository.saveAndFlush(user);
			}

			return user;
		}

		return null;
	}

	protected static String getHeader(HttpServletRequest req, String name,
			String defValue) {

		String value = req.getHeader(name);

		if (value == null)
			return defValue;

		if (value.length() == 0)
			return defValue;

		return value;
	}

	public UserRepository get_userRepository() {
		return _userRepository;
	}

	public void set_userRepository(UserRepository _userRepository) {
		this._userRepository = _userRepository;
	}

	public GroupRepository get_groupRepository() {
		return _groupRepository;
	}

	public void set_groupRepository(GroupRepository _groupRepository) {
		this._groupRepository = _groupRepository;
	}

	public GeonetworkAuthenticationProvider getAuthProvider() {
		return authProvider;
	}

	public void setAuthProvider(GeonetworkAuthenticationProvider authProvider) {
		this.authProvider = authProvider;
	}

	public WritableUserDetailsContextMapper getUdetailsmapper() {
		return udetailsmapper;
	}

	public void setUdetailsmapper(
			WritableUserDetailsContextMapper udetailsmapper) {
		this.udetailsmapper = udetailsmapper;
	}

	public UserDetailsManager getUserDetailsManager() {
		return userDetailsManager;
	}

	public void setUserDetailsManager(
			UserDetailsManager ldapUserDetailsManager) {
		this.userDetailsManager = ldapUserDetailsManager;
	}

}
