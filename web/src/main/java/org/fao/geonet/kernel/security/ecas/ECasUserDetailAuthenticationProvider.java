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
package org.fao.geonet.kernel.security.ecas;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;



public class ECasUserDetailAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider 
	implements ApplicationContextAware, UserDetailsService {

	private ApplicationContext applicationContext;
	private PasswordEncoder encoder;
	
	

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		GeonetworkUser gnDetails = (GeonetworkUser) userDetails;
		if (authentication.getCredentials() == null) {
			Log.warning(Log.JEEVES, "Authentication failed: no credentials provided");
			throw new BadCredentialsException("Authentication failed: no credentials provided");
		}
		if (!encoder.matches(authentication.getCredentials().toString(), gnDetails.getPassword())) {
			Log.warning(Log.JEEVES, "Authentication failed: wrong password provided");
			throw new BadCredentialsException("Authentication failed: wrong password provided");
		}
	}

	private static final String CAS_STATEFUL = "_cas_stateful_";
	private static final String VIA_CAS = "Via CAS";
	private static final String CAS_FLAG = "CAS";

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)	throws AuthenticationException {
		//Log.info(Log.JEEVES, "Authentication retrieveUser :" + username);
		Dbms dbms = null;
		ResourceManager resourceManager = null;
		if (CAS_STATEFUL.equalsIgnoreCase(username)) {
			// cas failed
			Log.error(Log.JEEVES, "_cas_stateful_ detected in retrieveUser");
			return null;
		}
		try {
			resourceManager = applicationContext.getBean(ResourceManager.class);
			dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
			// lookup the user in the database with cas login
			Element selectRequest = dbms.select("SELECT * FROM Users WHERE username=? AND authtype = 'CAS'", username);
			Element userXml = selectRequest.getChild("record");
			if (userXml == null) 	//  if user does not exists, add one as guest
			{
				// the user did not exists in the database with cas login.
                String surname = "";
				String firstname = username;
			    //String group    = "";				
		        String profile = ProfileManager.GUEST;
		        
		        // update the user to cas login. If the user does not exists then insert one
	            String query = "UPDATE Users SET name=?, surname=?, profile=?, password=?, authtype=? WHERE username=?";
	            int usersUpdated = dbms.execute(query, firstname, surname, profile, VIA_CAS, CAS_FLAG, username);
	            if (usersUpdated <= 0)
	            {
	            	// 
	                SerialFactory serialFactory = applicationContext.getBean(SerialFactory.class);
					int userId = serialFactory.getSerial(dbms, "Users");
			        query = "INSERT INTO Users (id, username, name, surname, profile, password, authtype) VALUES (?, ?, ?, ?, ?, ?, ?)";
			        usersUpdated = dbms.execute(query, userId, username, firstname, surname, profile, VIA_CAS, CAS_FLAG);
	            }
		        dbms.commit();
		        selectRequest = dbms.select("SELECT * FROM Users WHERE username=? AND authtype = 'CAS'", username);
		        userXml = selectRequest.getChild("record");
		        if (userXml == null)
		        	Log.error(Log.JEEVES, "user not added to database in retrieveUser (CAS)");
			}
			
			if (userXml != null) {
				ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
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
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext; 
		this.encoder = (PasswordEncoder) applicationContext.getBean(PasswordUtil.ENCODER_ID);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return retrieveUser(username, null);
	}

}
