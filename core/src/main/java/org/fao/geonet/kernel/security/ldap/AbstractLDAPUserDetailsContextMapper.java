//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.security.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.WritableUserDetailsContextMapper;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.StringUtils;

/**
 * Map LDAP user information to GeoNetworkUser information.
 * 
 * Create the GeoNetworkUser in local database on first login and update all
 * user information on subsequent login.
 * 
 * @author francois
 */
public abstract class AbstractLDAPUserDetailsContextMapper implements
		WritableUserDetailsContextMapper {

	Map<String, String[]> mapping;

	Map<String, Profile> profileMapping;

	protected boolean importPrivilegesFromLdap;

	private boolean createNonExistingLdapGroup = true;
	private boolean createNonExistingLdapUser = false;

	protected DefaultSpringSecurityContextSource contextSource;

	private UserDetailsManager ldapManager;

	private String ldapBaseDnPattern = "uid={0},ou=users";

	private String ldapBaseDn;

	@Autowired
	private LDAPUtils ldapUtils;
	
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		ctx.setAttributeValues("objectclass", new String[] { "top", "person",
				"organizationalPerson", "inetOrgPerson" });
		ctx.setAttributeValue("sn", user.getUsername());
		ctx.setAttributeValue("cn", user.getUsername());
		ctx.setAttributeValue("uid", user.getUsername());
		
		if(user instanceof InetOrgPerson) {
			InetOrgPerson p = (InetOrgPerson) user;
			ctx.setAttributeValue("mail", p.getMail());
			ctx.setAttributeValue("displayName", p.getDisplayName());
			ctx.setAttributeValue("sn", p.getSn());
		}
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations userCtx,
			String username, Collection<? extends GrantedAuthority> authorities) {

		Profile defaultProfile;
		if (mapping.get("profile")[1] != null) {
			defaultProfile = Profile.valueOf(mapping.get("profile")[1]);
		} else {
			defaultProfile = Profile.RegisteredUser;
		}
		String defaultGroup = mapping.get("privilege")[1];

		Map<String, ArrayList<String>> userInfo = ldapUtils
				.convertAttributes(userCtx.getAttributes().getAll());

		LDAPUser userDetails = new LDAPUser(username);
		User user = userDetails.getUser();
		user.setName(getUserInfo(userInfo, "name"))
				.setSurname(getUserInfo(userInfo, "surname"))
				.setOrganisation(getUserInfo(userInfo, "organisation"));
		user.getEmailAddresses().clear();
		user.getEmailAddresses().add(getUserInfo(userInfo, "mail"));
		user.getPrimaryAddress().setAddress(getUserInfo(userInfo, "address"))
				.setState(getUserInfo(userInfo, "state"))
				.setZip(getUserInfo(userInfo, "zip"))
				.setCity(getUserInfo(userInfo, "city"))
				.setCountry(getUserInfo(userInfo, "country"));

		// Set privileges for the user. If not, privileges are handled
		// in local database
		if (importPrivilegesFromLdap) {
			setProfilesAndPrivileges(defaultProfile, defaultGroup, userInfo,
					userDetails);
		}

		// Assign default profile if not set by LDAP info or local database
		if (user.getProfile() == null) {
			user.setProfile(defaultProfile);
		}

		saveUser(userDetails);

		return userDetails;
	}

	abstract protected void setProfilesAndPrivileges(Profile defaultProfile,
			String defaultGroup, Map<String, ArrayList<String>> userInfo,
			LDAPUser userDetails);

	protected void addProfile(@Nonnull LDAPUser userDetails,
			@Nonnull String profileName, @Nullable Set<Profile> profileList) {
		// Check if profile exist in profile mapping table
		if (profileMapping != null) {
			Profile mapped = profileMapping.get(profileName);
			if (mapped != null) {
				profileName = mapped.name();
			}
		}

		if (Log.isDebugEnabled(Geonet.LDAP)) {
			Log.debug(Geonet.LDAP, "  Assigning profile " + profileName);
		}
		Profile profile = Profile.valueOf(profileName);
		if (profile != null) {
			userDetails.getUser().setProfile(profile);
			if (profileList != null) {
				profileList.add(profile);
			}
		} else {
			Log.error(Geonet.LDAP, "  Profile " + profileName
					+ " does not exist.");
		}
	}

	@Override
	public synchronized void saveUser(LDAPUser userDetails) {
		try {

			if (createNonExistingLdapUser
					&& !ldapManager.userExists(userDetails.getUsername())) {
				InetOrgPerson.Essence p = new InetOrgPerson.Essence(userDetails);
				p.setDn(ldapBaseDnPattern.replace("{0}",
						userDetails.getUsername()));
				String surname = userDetails.getUser().getSurname();
				if(StringUtils.isEmpty(surname)) {
					//sn is usually mandatory on LDAP
					surname = userDetails.getUsername();
				}
				p.setSn(surname);
				p.setUid(userDetails.getUsername());
				p.setMail(userDetails.getUser().getEmail());
				String name = userDetails.getUser().getName();
				if(StringUtils.isEmpty(name)) {
					//displayname is usually mandatory too
					name = userDetails.getUsername();
				}
				p.setDisplayName(name);
				String[] cn = ldapBaseDn.split(",");
				for (int i = 0; i < cn.length; i++) {
					cn[i] = cn[i].substring(cn[i].indexOf("=") + 1);
				}
				p.setCn(cn);
				ldapManager.createUser(p.createUserDetails());
			}
			ldapUtils.saveUser(userDetails, importPrivilegesFromLdap, createNonExistingLdapGroup);
		} catch (Exception e) {
			throw new AuthenticationServiceException(
					"Unexpected error while saving/updating LDAP user in database",
					e);
		}
	}

	private String getUserInfo(Map<String, ArrayList<String>> userInfo,
			String attributeName) {
		return getUserInfo(userInfo, attributeName, "");
	}

	/**
	 * Return the first element of userInfo corresponding to the attribute name.
	 * If attributeName mapping is not defined, return empty string. If no value
	 * found in LDAP user info, return default value.
	 * 
	 * @param userInfo
	 * @param attributeName
	 * @param defaultValue
	 * @return
	 */
	private String getUserInfo(Map<String, ArrayList<String>> userInfo,
			String attributeName, String defaultValue) {
		String[] attributeMapping = mapping.get(attributeName);
		String value = "";

		if (attributeMapping != null) {
			String ldapAttributeName = attributeMapping[0];
			String configDefaultValue = attributeMapping[1];

			if (ldapAttributeName != null
					&& userInfo.get(ldapAttributeName) != null
					&& userInfo.get(ldapAttributeName).get(0) != null) {
				value = userInfo.get(ldapAttributeName).get(0);
			} else if (configDefaultValue != null) {
				value = configDefaultValue;
			} else {
				value = defaultValue;
			}
		} else {
			value = defaultValue;
		}

		if (Log.isDebugEnabled(Geonet.LDAP)) {
			Log.debug(Geonet.LDAP, "LDAP attribute '" + attributeName + "' = "
					+ value);
		}
		return value;
	}

	public boolean isCreateNonExistingLdapGroup() {
		return createNonExistingLdapGroup;
	}

	public void setCreateNonExistingLdapGroup(boolean createNonExistingLdapGroup) {
		this.createNonExistingLdapGroup = createNonExistingLdapGroup;
	}

	public String[] getMappingValue(String key) {
		return mapping.get(key);
	}

	public void setMapping(Map<String, String[]> mapping) {
		this.mapping = mapping;
	}

	public Map<String, String[]> getMapping() {
		return mapping;
	}

	public Profile getProfileMappingValue(String key) {
		return profileMapping.get(key);
	}

	public void setProfileMapping(Map<String, Profile> profileMapping) {
		this.profileMapping = profileMapping;
	}

	public Map<String, Profile> getProfileMapping() {
		return profileMapping;
	}

	public boolean isImportPrivilegesFromLdap() {
		return importPrivilegesFromLdap;
	}

	public void setImportPrivilegesFromLdap(boolean importPrivilegesFromLdap) {
		this.importPrivilegesFromLdap = importPrivilegesFromLdap;
	}

	public DefaultSpringSecurityContextSource getContextSource() {
		return contextSource;
	}

	public void setContextSource(
			DefaultSpringSecurityContextSource contextSource) {
		this.contextSource = contextSource;
	}

	public UserDetailsManager getLdapManager() {
		return ldapManager;
	}

	public void setLdapManager(UserDetailsManager ldapManager) {
		this.ldapManager = ldapManager;
	}

	public boolean isCreateNonExistingLdapUser() {
		return createNonExistingLdapUser;
	}

	public void setCreateNonExistingLdapUser(boolean createNonExistingLdapUser) {
		this.createNonExistingLdapUser = createNonExistingLdapUser;
	}

	public String getLdapBaseDnPattern() {
		return ldapBaseDnPattern;
	}

	public void setLdapBaseDnPattern(String ldapBaseDnPattern) {
		this.ldapBaseDnPattern = ldapBaseDnPattern;
	}

	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
	}

}