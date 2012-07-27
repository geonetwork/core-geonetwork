package org.fao.geonet.kernel.security.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import jeeves.guiservices.session.JeevesUser;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
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
 * TODO : update group and profile information after login
 * TODO : add LDAP/DB synchronisation task
 * 
 * @author francois
 */
public class GeoNetworkLDAPUserDetailsContextMapper implements
		UserDetailsContextMapper, ApplicationContextAware {

	private String nameAttribute;
	private String surnameAttribute;
	private String mailAttribute;
	private String kindAttribute;
	private String organisationAttribute;
	private String countryAttribute;
	private String zipAttribute;
	private String stateAttribute;
	private String cityAttribute;
	private String addressAttribute;
	private String profileAttribute;
	private String privilegeAttribute;
	private String privilegePattern;
	private int groupIndexInPattern;
	private int profilIndexInPattern;
	private String[] defaultPrivileges;
	
	private ApplicationContext applicationContext;

	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations userCtx,
			String username, Collection<? extends GrantedAuthority> authorities) {

		ResourceManager resourceManager = applicationContext.getBean(ResourceManager.class);
		ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);

		Map<String, ArrayList<String>> userInfo = LDAPUtils.convertAttributes(userCtx.getAttributes()
				.getAll());
		
		LDAPUser userDetails = new LDAPUser(profileManager, username);
		userDetails.setName(getUserInfo(userInfo, nameAttribute))
			.setSurname(getUserInfo(userInfo, surnameAttribute))
			.setEmail(getUserInfo(userInfo, mailAttribute))
			.setOrganisation(getUserInfo(userInfo, organisationAttribute))
			.setAddress(getUserInfo(userInfo, addressAttribute))
			.setState(getUserInfo(userInfo, stateAttribute))
			.setZip(getUserInfo(userInfo, zipAttribute))
			.setCity(getUserInfo(userInfo, cityAttribute))
			.setCountry(getUserInfo(userInfo, countryAttribute));
		// TODO Add info in the DB that this user is from LDAP ?
		// userDetails.save();
		
		// Set privileges for the user:
		if ("".equals(privilegePattern)) {
			// 1. no privilegePattern defined. In that case the user 
			// has the same profile for all groups. The list of groups 
			// is retreived from the privilegeAttribute content
			userDetails.setProfile(getUserInfo(userInfo, profileAttribute));
			for (String group : userInfo.get(privilegeAttribute)) {
				userDetails.addPrivilege(group, userDetails.getProfile());
			}
			
			// Set default privileges
			if (userDetails.getPrivileges().size() == 0) {
				for (String defaultPrivilege : defaultPrivileges) {
					userDetails.addPrivilege(defaultPrivilege, userDetails.getProfile());
				}
			}
		} else {
			// 2. a privilegePattern is defined which define a 
			// combination of group and profile pair.
			Pattern p = Pattern.compile(privilegePattern);
			for (String privilegeDefinition : userInfo.get(privilegeAttribute)) {
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
		
		
		Dbms dbms = null;
		try {
			dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
			LDAPUtils.saveUser(userDetails, dbms);
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
		return getUserInfo(userInfo, attributeName, null);
	}
	private String getUserInfo (Map<String, ArrayList<String>> userInfo, String attributeName, String defaultValue) {
		if (userInfo.get(attributeName) != null ) {
			return userInfo.get(attributeName).get(0);
		} else if (defaultValue != null ){
			return defaultValue;
		} else {
			return "";
		}
	}
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getDefaultRole() {
		return privilegePattern;
	}

	public void setDefaultRole(String defaultRole) {
		this.privilegePattern = defaultRole;
	}

	public String[] getDefaultPrivileges() {
		return defaultPrivileges;
	}

	public void setDefaultPrivileges(String[] defaultPrivileges) {
		this.defaultPrivileges = defaultPrivileges;
	}

	public String getNameAttribute() {
		return nameAttribute;
	}

	public void setNameAttribute(String nameAttribute) {
		this.nameAttribute = nameAttribute;
	}

	public String getSurnameAttribute() {
		return surnameAttribute;
	}

	public void setSurnameAttribute(String surnameAttribute) {
		this.surnameAttribute = surnameAttribute;
	}

	public String getKindAttribute() {
		return kindAttribute;
	}

	public void setKindAttribute(String kindAttribute) {
		this.kindAttribute = kindAttribute;
	}

	public String getOrganisationAttribute() {
		return organisationAttribute;
	}

	public void setOrganisationAttribute(String organisationAttribute) {
		this.organisationAttribute = organisationAttribute;
	}

	public String getCountryAttribute() {
		return countryAttribute;
	}

	public void setCountryAttribute(String countryAttribute) {
		this.countryAttribute = countryAttribute;
	}

	public String getZipAttribute() {
		return zipAttribute;
	}

	public void setZipAttribute(String zipAttribute) {
		this.zipAttribute = zipAttribute;
	}

	public String getStateAttribute() {
		return stateAttribute;
	}

	public void setStateAttribute(String stateAttribute) {
		this.stateAttribute = stateAttribute;
	}

	public String getCityAttribute() {
		return cityAttribute;
	}

	public void setCityAttribute(String cityAttribute) {
		this.cityAttribute = cityAttribute;
	}

	public String getAddressAttribute() {
		return addressAttribute;
	}

	public void setAddressAttribute(String addressAttribute) {
		this.addressAttribute = addressAttribute;
	}

	public String getPrivilegeAttribute() {
		return privilegeAttribute;
	}

	public void setPrivilegeAttribute(String privilegeAttribute) {
		this.privilegeAttribute = privilegeAttribute;
	}
	public String getPrivilegePattern() {
		return privilegePattern;
	}

	public void setPrivilegePattern(String privilegePattern) {
		this.privilegePattern = privilegePattern;
	}

	public String getMailAttribute() {
		return mailAttribute;
	}

	public void setMailAttribute(String mailAttribute) {
		this.mailAttribute = mailAttribute;
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

	public String getProfileAttribute() {
		return profileAttribute;
	}

	public void setProfileAttribute(String profileAttribute) {
		this.profileAttribute = profileAttribute;
	}
}