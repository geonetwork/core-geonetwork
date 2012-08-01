package org.fao.geonet.kernel.security.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Profile;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
/**
 * Map LDAP user information to GeoNetworkUser information.
 * 
 * Create the GeoNetworkUser in local database on first login.
 * 
 * @author francois
 */
public class GeoNetworkLDAPUserDetailsContextMapper implements
		UserDetailsContextMapper, ApplicationContextAware {
	
	Map<String, String[]> mapping;
	
	private String privilegePattern;
	private int groupIndexInPattern;
	private int profilIndexInPattern;
	
	private boolean createNonExistingLdapGroup = true;
	
	private ApplicationContext applicationContext;

	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations userCtx,
			String username, Collection<? extends GrantedAuthority> authorities) {
		ResourceManager resourceManager = applicationContext.getBean(ResourceManager.class);
		ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
		SerialFactory serialFactory = applicationContext.getBean(SerialFactory.class);

		String defaultProfile = (mapping.get("profile")[1] != null ? mapping.get("profile")[1] : Profile.GUEST);
		
		Map<String, ArrayList<String>> userInfo = LDAPUtils.convertAttributes(userCtx.getAttributes()
				.getAll());
		
		LDAPUser userDetails = new LDAPUser(profileManager, username);
		userDetails.setName(getUserInfo(userInfo, "name"))
			.setSurname(getUserInfo(userInfo, "surname"))
			.setEmail(getUserInfo(userInfo, "mail"))
			.setOrganisation(getUserInfo(userInfo, "organisation"))
			.setAddress(getUserInfo(userInfo, "address"))
			.setState(getUserInfo(userInfo, "state"))
			.setZip(getUserInfo(userInfo, "zip"))
			.setCity(getUserInfo(userInfo, "city"))
			.setCountry(getUserInfo(userInfo, "country"));
		
		// Set privileges for the user:
		if ("".equals(privilegePattern)) {
			// 1. no privilegePattern defined. In that case the user 
			// has the same profile for all groups. The list of groups 
			// is retreived from the privilegeAttribute content
			userDetails.setProfile(getUserInfo(userInfo, mapping.get("profile")[0]));
			for (String group : userInfo.get(mapping.get("privilege")[0])) {
				userDetails.addPrivilege(group, userDetails.getProfile());
			}
			
			// Set default privileges
			if (userDetails.getPrivileges().size() == 0) {
				userDetails.addPrivilege(mapping.get("privilege")[1], defaultProfile);
			}
		} else {
			// 2. a privilegePattern is defined which define a 
			// combination of group and profile pair.
			Pattern p = Pattern.compile(privilegePattern);
			ArrayList<String> privileges = userInfo.get(mapping.get("privilege")[0]);
			
			for (String privilegeDefinition : privileges) {
				Matcher m = p.matcher(privilegeDefinition);
				boolean b = m.matches();
				if (b) {
					String group = m.group(groupIndexInPattern);
					String profil = m.group(profilIndexInPattern);
					
					// TODO : skip undeclared profil
					if (group != null && profil != null) {
						userDetails.setProfile(profil);
						userDetails.addPrivilege(group, profil);
					}
				} else {
					System.out.println("LDAP privilege info '" + privilegeDefinition + "' does not match search pattern '" + privilegePattern + "'. Information ignored.");
				}
			}
		}
		
		// Assign default profile if not set by LDAP info
		if (userDetails.getProfile() == null) {
			userDetails.setProfile(defaultProfile);
		}
		
		Dbms dbms = null;
		try {
			dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
			LDAPUtils.saveUser(userDetails, dbms, serialFactory, createNonExistingLdapGroup);
		} catch (Exception e) {
			try {
				resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
				dbms = null;
			} catch (Exception e2) {
				e.printStackTrace();
				Log.error(Log.JEEVES, "Error closing dbms" + dbms, e2);
			}
			Log.error(Log.JEEVES, "Unexpected error while saving/updating LDAP user in database", e);
			throw new AuthenticationServiceException("Unexpected error while saving/updating LDAP user in database", e);
		} finally {
			if (dbms != null){
				try {
					resourceManager.close(Geonet.Res.MAIN_DB, dbms);
				} catch (Exception e) {
					e.printStackTrace();
					Log.error(Log.JEEVES, "Error closing dbms" + dbms, e);
				}
			}
		}
		
		return userDetails;
	}
	private String getUserInfo (Map<String, ArrayList<String>> userInfo, String attributeName) {
		return getUserInfo(userInfo, attributeName, "");
	}
	/**
	 * Return the first element of userInfo corresponding to the attribute name.
	 * If attributeName mapping is not defined, return empty string.
	 * If no value found in LDAP user info, return default value.
	 * 
	 * @param userInfo
	 * @param attributeName
	 * @param defaultValue
	 * @return
	 */
	private String getUserInfo (Map<String, ArrayList<String>> userInfo, String attributeName, String defaultValue) {
		String[] attributeMapping = mapping.get(attributeName);
		String value = "";
		
		if (attributeMapping != null ) {
			String ldapAttributeName = attributeMapping[0];
			String configDefaultValue = attributeMapping[1];
			
			if (ldapAttributeName != null && userInfo.get(ldapAttributeName) != null && userInfo.get(ldapAttributeName).get(0) != null) {
				value = userInfo.get(ldapAttributeName).get(0);
			} else if (configDefaultValue != null) {
				value = configDefaultValue;
			} else {
				value = defaultValue;
			}
		} else {
			value = defaultValue;
		}
		
		if (Log.isDebugEnabled(Log.JEEVES)){
			Log.debug(Log.JEEVES, "LDAP attribute '" + attributeName + "' = " + value);
		}
		return value;
	}
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	public String getPrivilegePattern() {
		return privilegePattern;
	}
	
	public void setPrivilegePattern(String privilegePattern) {
		this.privilegePattern = privilegePattern;
	}
	
	public int getGroupIndexInPattern() {
		return groupIndexInPattern;
	}
	
	public void setGroupIndexInPattern(int groupIndexInPattern) {
		this.groupIndexInPattern = groupIndexInPattern;
	}
	
	public int getProfilIndexInPattern() {
		return profilIndexInPattern;
	}
	
	public void setProfilIndexInPattern(int profilIndexInPattern) {
		this.profilIndexInPattern = profilIndexInPattern;
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
}