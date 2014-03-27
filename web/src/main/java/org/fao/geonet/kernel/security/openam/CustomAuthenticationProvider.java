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
package org.fao.geonet.kernel.security.openam;

import java.sql.SQLException;
import java.util.Collection;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.PasswordUtil;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.security.GeonetworkUser;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.iplanet.sso.SSOToken;


/**
 * CustomAuthenticationProvider
 * 	Custom class for SSO authentication
 * @author thierry.chevallier (AKKA Informatique et Systèmes) for ingeoclouds : contact@ingeoclouds.eu
 */
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider 
	implements ApplicationContextAware, UserDetailsService
{

	private ApplicationContext applicationContext;
	private PasswordEncoder encoder;
	
	private OpenAMAuthoritiesPopulator authoritiesPopulator;
	private String defaultUserGroup;


	@Override
	protected UserDetailsChecker getPreAuthenticationChecks() {
		// TODO Auto-generated method stub
		return super.getPreAuthenticationChecks();
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		
		GeonetworkUser gnDetails = (GeonetworkUser) userDetails;
		if (authentication.getCredentials() == null) {
			Log.warning(Log.JEEVES, "Authentication failed: no credentials provided");
			throw new BadCredentialsException("Authentication failed: no credentials provided");
		}

	}

	@Override
	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		
		Dbms dbms = null;
		ResourceManager resourceManager = null;
        ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
		
		try {
			resourceManager = applicationContext.getBean(ResourceManager.class);
			dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
			
			Element userXml = getUserElementFromUserName(dbms, username);
			
			/* no user found in database --> create it */
			if (userXml == null) {
			
				OpenAMUser user = new OpenAMUser(profileManager, username);
				
		        UsernamePasswordAuthenticationToken token = 
		        		(UsernamePasswordAuthenticationToken) authentication;
		        String principal =  (String) token.getPrincipal();
		        
				// We pass in the SSOToken as the credential (.e.g the password)
		        // this is probably confusing - and we should refactor to use a proper OpenSSOAuthenitcationToken.
		        //SSOToken ssoToken = OpenSSOProcessingFilter.getToken(request);
		        SSOToken ssoToken = (SSOToken) token.getCredentials();

		        Collection<GrantedAuthority> ga = 
		        		authoritiesPopulator.getGrantedAuthoritiesCollection(ssoToken);

		        // set the last profile found in list as the user profile
		        GrantedAuthority auth = null;
		        for (GrantedAuthority gauth : ga){
		        	auth = gauth;
		        }

		        user.setProfile(auth.toString());

		        String userId = createUser(dbms, user);
		        
		        if (defaultUserGroup!=null){
		        	int groupId = getGroupId(dbms, defaultUserGroup);
		        	attachUserToDefaultGroup(dbms, userId, groupId, user.getProfile());
		        }
				
			} else {
				
				// update user profile
				
				
			}
			
			userXml = getUserElementFromUserName(dbms, username);
			
			if (userXml != null) {
				if (authentication != null && authentication.getCredentials() != null) {
					String oldPassword = authentication.getCredentials().toString();
					Integer iUserId = new Integer(userXml.getChildText(Geonet.Elem.ID));
					if(PasswordUtil.hasOldHash(userXml)) {
						userXml = PasswordUtil.updatePasswordWithNew(true, oldPassword , oldPassword, iUserId , encoder, dbms);
					}
				}
				GeonetworkUser userDetails = new GeonetworkUser(profileManager, username, userXml);
				return userDetails;
			}
			
		} catch (Exception e) {
			try {
				resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
				dbms = null;
			} catch (Exception e2) {
				e.printStackTrace();
				Log.error(Log.JEEVES, "Error closing dbms"+dbms, e2);
			}
			Log.error(Log.JEEVES, "Unexpected error while loading user", e);
			throw new AuthenticationServiceException("Unexpected error while loading user",e);
		} finally {
			if (dbms != null){
				try {
					resourceManager.close(Geonet.Res.MAIN_DB, dbms);
				} catch (Exception e) {
					e.printStackTrace();
					Log.error(Log.JEEVES, "Error closing dbms"+dbms, e);
				}
			}
		}
		throw new UsernameNotFoundException(username+" is not a valid username");
	}

	
	/**
	 * getGroupId
	 * 
	 * @param dbms
	 * @param groupName
	 * @return
	 * @throws SQLException
	 */
	private int getGroupId(Dbms dbms, String groupName) 
			throws SQLException {
		
		if (!groupName.equals("")){
		
			String query = "SELECT id FROM Groups WHERE name=?";
			Element listXml = dbms.select(query, groupName).getChild("record");
			
			if (listXml != null){
				return new Integer(listXml.getChildText("id"));
			
			} else {
				
				SerialFactory serialFactory = applicationContext.getBean(SerialFactory.class);
				
                // If default user group does not exist in local database, create it
                String groupId = serialFactory.getSerial(dbms, "Groups") + "";
                query = "INSERT INTO GROUPS(id, name) VALUES(?,?)";
                dbms.execute(query, new Integer(groupId), groupName);
                
                return Integer.parseInt(groupId);
                
			}
		} else {
		
			return 0;
		}
		
	}

	/**
	 * attachUserToDefaultGroup
	 * @param dbms
	 * @param id
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 */
	private void attachUserToDefaultGroup(Dbms dbms, String id, int groupId, String profile) 
			throws NumberFormatException, SQLException {

		if (!defaultUserGroup.equals("")){
			
			String query = "SELECT groupId AS id FROM UserGroups WHERE groupId > 1 AND userId=?";
			Element listXml = dbms.select(query, Integer.parseInt(id)).getChild("record");
		
			if (listXml == null){
	
				query = "INSERT INTO UserGroups(userid, groupid, profile) values(?,?,?)";
				dbms.execute(query, new Integer(id), groupId, profile);
				
			}
		}
		
	}

	/**
	 * getUserElementFromUserName
	 * 	Only check user with local db user (ie. authtype is '')
	 * @param dbms
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	private Element getUserElementFromUserName(Dbms dbms, String username) 
			throws SQLException {
		
		// Only check user with local db user (ie. authtype is '')
		Element selectRequest = dbms.select("SELECT * FROM Users WHERE username=? AND authtype IS NULL", username);
		Element userXml = selectRequest.getChild("record");
		
		return userXml;
		
	}

	/**
	 * createUser
	 * @param dbms 
	 * @param user
	 * @throws SQLException 
	 */
	private String createUser(Dbms dbms, OpenAMUser user) 
			throws SQLException 
	{

	    if (Log.isDebugEnabled(Geonet.LDAP)){
				Log.debug(Geonet.LDAP, "  - Create user from token " + user.getUsername() + " in local database.");
			}
	    
	    SerialFactory serialFactory = applicationContext.getBean(SerialFactory.class);	
		
        String id = serialFactory.getSerial(dbms, "Users") + "";
		
        String query = "INSERT INTO Users (id, username, password, surname, name, profile, "+
					"address, city, state, zip, country, email, organisation, kind, authtype) "+
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		dbms.execute(query, new Integer(id), user.getUsername(), "", user.getSurname(), user.getName(), 
				user.getProfile(), user.getAddress(), user.getCity(), user.getState(), user.getZip(), 
				user.getCountry(), user.getEmail(), user.getOrganisation(), user.getKind(), null);

		return id;
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext; 
		this.encoder = (PasswordEncoder) applicationContext.getBean(PasswordUtil.ENCODER_ID);
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		return retrieveUser(username, null);
	}


	public OpenAMAuthoritiesPopulator getAuthoritiesPopulator() {
		return authoritiesPopulator;
	}

	public void setAuthoritiesPopulator(
			OpenAMAuthoritiesPopulator authoritiesPopulator) {
		this.authoritiesPopulator = authoritiesPopulator;
	}
	
	public String getDefaultUserGroup() {
		return defaultUserGroup;
	}

	public void setDefaultUserGroup(String defaultUserGroup) {
		this.defaultUserGroup = defaultUserGroup;
	}

}
