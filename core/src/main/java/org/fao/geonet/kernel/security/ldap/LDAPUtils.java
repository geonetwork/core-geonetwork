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

import org.fao.geonet.domain.*;
import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;

import static java.util.Collections.singleton;

public class LDAPUtils {
	/**
	 * Save or update an LDAP user to the local GeoNetwork database.
	 * 
	 * TODO : test when a duplicate username is in the local DB and from an LDAP
	 * Unique key constraint should return errors.
	 * 
	 * @param user
	 * @throws Exception
	 */
    static void saveUser(LDAPUser user, UserRepository userRepo, GroupRepository groupRepo, UserGroupRepository userGroupRepo,
            boolean importPrivilegesFromLdap, boolean createNonExistingLdapGroup) throws Exception {
        String userName = user.getUsername();
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "LDAP user sync for " + userName + " ...");
        }
        User loadedUser = userRepo.findOneByUsername(userName);
        User toSave;
        if (loadedUser != null) {
            if (importPrivilegesFromLdap) {
                loadedUser.setProfile(user.getUser().getProfile());
            }
            loadedUser.mergeUser(user.getUser(), true);
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP, "  - Update LDAP user " + user.getUsername() + " (" + loadedUser.getId() + ") in local database.");
            }
            toSave = loadedUser;

            // Delete user groups
            if (importPrivilegesFromLdap) {
                userGroupRepo.deleteAllByIdAttribute(UserGroupId_.userId, singleton(toSave.getId()));
            }
        } else {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP, "  - Saving new LDAP user " + user.getUsername() + " to database.");
            }
            toSave = user.getUser();
        }
        toSave.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);
        userRepo.save(toSave);

		// Add user groups
		if (importPrivilegesFromLdap && !Profile.Administrator.equals(user.getUser().getProfile())) {
            userGroupRepo.deleteAllByIdAttribute(UserGroupId_.userId, singleton(user.getUser().getId()));
			for(Map.Entry<String, Profile> privilege : user.getPrivileges().entries()) {
				// Add group privileges for each groups
				
				// Retrieve group id
				String groupName = privilege.getKey();
				Profile profile = privilege.getValue();
				
				Group group = groupRepo.findByName(groupName);
				
				if (group == null && createNonExistingLdapGroup) {
				    group = new Group().setName(groupName);
				    groupRepo.save(group);
				    
				    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  - Add LDAP group " + groupName + " for user.");
                    }
				}
				if (group != null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  - Add LDAP group " + groupName + " for user.");
                    }
				    userGroupRepo.save(new UserGroup().setId(new UserGroupId(user.getUser(), group)).setProfile(profile));
					
						if (profile == Profile.Reviewer) {
						    try {
                                if (Log.isDebugEnabled(Geonet.LDAP)) {
                                    Log.debug(Geonet.LDAP, "  - Profile is Reviewer; also adding Editor");
                                }
						    userGroupRepo.save(new UserGroup().setId(new UserGroupId(user.getUser(), group)).setProfile(Profile.Editor));
						} catch (Exception e) {
						    Log.debug(Geonet.LDAP,
						            "  - User is already editor for that group."
						                    + e.getMessage());
						}
					}
				} else {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  - Can't create LDAP group " + groupName + " for user. "
                                + "Group does not exist in local database or createNonExistingLdapGroup is set to false.");
                    }
				}
            }
        }
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
				NamingEnumeration<?> valueEnum = attr.getAll();
				
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
