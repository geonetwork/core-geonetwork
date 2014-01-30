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
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Profile;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.user.Update;
import org.jdom.Element;

public class LDAPUtils {
	/**
	 * Save or update an LDAP user to the local GeoNetwork database.
	 * 
	 * TODO : test when a duplicate username is in the local DB and from an LDAP
	 * Unique key constraint should return errors.
	 * 
	 * @param user
	 * @param dbms
	 * @param serialFactory 
	 * @throws Exception 
	 */
	static void saveUser(LDAPUser user, Dbms dbms, SerialFactory serialFactory, boolean importPrivilegesFromLdap, boolean createNonExistingLdapGroup) throws Exception {
		Element selectRequest = dbms.select("SELECT * FROM Users WHERE username=?", user.getUsername());
		Element userXml = selectRequest.getChild("record");
		String id;
		if (Log.isDebugEnabled(Geonet.LDAP)){
			Log.debug(Geonet.LDAP, "LDAP user sync for " + user.getUsername() + " ...");
		}
		// FIXME : this part of the code is a bit redundant with update user code.
		if (userXml == null) {
			if (Log.isDebugEnabled(Geonet.LDAP)){
				Log.debug(Geonet.LDAP, "  - Create LDAP user " + user.getUsername() + " in local database.");
			}
			 
			id = serialFactory.getSerial(dbms, "Users") + "";
			
			String query = "INSERT INTO Users (id, username, password, surname, name, profile, "+
						"address, city, state, zip, country, email, organisation, kind, authtype) "+
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			dbms.execute(query, Integer.valueOf(id), user.getUsername(), "LDAP_PASSWORD", user.getSurname(), user.getName(), 
					user.getProfile(), user.getAddress(), user.getCity(), user.getState(), user.getZip(), 
					user.getCountry(), user.getEmail(), user.getOrganisation(), user.getKind(), LDAPConstants.LDAP_FLAG);
		} else {
			// Update existing LDAP user
			
			// Retrieve user id
			Element nextIdRequest = dbms.select("SELECT id FROM Users WHERE username = ?", user.getUsername());
			id = nextIdRequest.getChild("record").getChildText("id");

			if (Log.isDebugEnabled(Geonet.LDAP)){
				Log.debug(Geonet.LDAP, "  - Update LDAP user " + user.getUsername() + " (" + id + ") in local database.");
			}
			
			// User update
			String query = "UPDATE Users SET username=?, password=?, surname=?, name=?, profile=?, address=?,"+
						" city=?, state=?, zip=?, country=?, email=?, organisation=?, kind=? WHERE id=?";
			dbms.execute (query, user.getUsername(), "LDAP_PASSWORD", user.getSurname(), user.getName(), 
					user.getProfile(), user.getAddress(), user.getCity(), user.getState(), user.getZip(), 
					user.getCountry(), user.getEmail(), user.getOrganisation(), user.getKind(), Integer.valueOf(id));
			
			// Delete user groups
			if (importPrivilegesFromLdap) {
				dbms.execute("DELETE FROM UserGroups WHERE userId=?", Integer.valueOf(id));
			}
		}

		// Add user groups
		if (importPrivilegesFromLdap && !Profile.ADMINISTRATOR.equals(user.getProfile())) {
			dbms.execute("DELETE FROM UserGroups WHERE userId=?", Integer.valueOf(id));
			for(Map.Entry<String, String> privilege : user.getPrivileges().entries()) {
				// Add group privileges for each groups
				
				// Retrieve group id
				String groupName = privilege.getKey();
				String profile = privilege.getValue();
				
				Element groupIdRequest = dbms.select("SELECT id FROM Groups WHERE name = ?", groupName);
				Element groupRecord = groupIdRequest.getChild("record");
				String groupId = null;
				
				if (groupRecord == null && createNonExistingLdapGroup) {
					if (Log.isDebugEnabled(Geonet.LDAP)){
						Log.debug(Geonet.LDAP, "  - Add non existing group '" + groupName + "' in local database.");
					}
					
					// If LDAP group does not exist in local database, create it
					groupId = serialFactory.getSerial(dbms, "Groups") + "";
					String query = "INSERT INTO GROUPS(id, name) VALUES(?,?)";
					dbms.execute(query, Integer.valueOf(groupId), groupName);
					Lib.local.insert(dbms, "Groups", Integer.valueOf(groupId), groupName);
				} else if (groupRecord != null) {
					groupId = groupRecord.getChildText("id");
				}
				
				if (groupId != null || createNonExistingLdapGroup) {
					if (Log.isDebugEnabled(Geonet.LDAP)){
						Log.debug(Geonet.LDAP, "  - Add LDAP group " + groupName + " for user.");
					}
					
					Update.addGroup(dbms, Integer.valueOf(id), Integer.valueOf(groupId), profile);
					
					try {
						if (profile.equals(Profile.REVIEWER)) {
							Update.addGroup(dbms, Integer.valueOf(id), Integer.valueOf(
									groupId), Profile.EDITOR);
						}
					} catch (Exception e) {
						Log.debug(Geonet.LDAP,
								"  - User is already editor for that group."
										+ e.getMessage());
					}
				} else {
					if (Log.isDebugEnabled(Geonet.LDAP)){
						Log.debug(Geonet.LDAP, "  - Can't create LDAP group " + groupName + " for user. " +
												"Group does not exist in local database or createNonExistingLdapGroup is set to false.");
					}
				}
			}
		}
		user.setId(id);
		
		dbms.commit();
	}
	
	static Map<String, ArrayList<String>> convertAttributes(
			NamingEnumeration<? extends Attribute> attributesEnumeration) {
		Map<String, ArrayList<String>> userInfo = new HashMap<String, ArrayList<String>>();
		try {
			while (attributesEnumeration.hasMore()) {
				Attribute attr = attributesEnumeration.next();
				String id = attr.getID();
				
				ArrayList<String> values = userInfo.get(id);
				if (values == null) {
					values = new ArrayList<String>();
					userInfo.put(id, values);
				}
				
				// --- loop on all attribute's values
				NamingEnumeration valueEnum;
				valueEnum = attr.getAll();
				
				while (valueEnum.hasMore()) {
					Object value = valueEnum.next();
					// Only retrieve String attribute
					if (value instanceof String) {
						values.add((String) value);
					}
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return userInfo;
	}
}
