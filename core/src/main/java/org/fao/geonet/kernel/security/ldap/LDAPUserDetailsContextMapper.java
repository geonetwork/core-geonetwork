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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;

/**
 * Get all user information from the LDAP user's attributes (including profile and groups)
 *
 * @author francois
 */
public class LDAPUserDetailsContextMapper extends
    AbstractLDAPUserDetailsContextMapper {

    protected void setProfilesAndPrivileges(Profile defaultProfile,
                                            String defaultGroup, Map<String, ArrayList<String>> userInfo,
                                            LDAPUser userDetails) {

        // no privilegePattern defined. In that case the user
        // has the same profile for all groups. The list of groups
        // is retreived from the privilegeAttribute content
        // getUserInfo(userInfo, mapping.get("profile")[0]));

        // Usually only one profile is defined in the profile attribute
        List<String> ldapProfiles = userInfo.get(mapping.get("profile")[0]);
        if (ldapProfiles != null) {
            Collections.sort(ldapProfiles);
            for (String profile : ldapProfiles) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  User profile " + profile
                        + " found in attribute "
                        + mapping.get("profile")[0]);
                }
                addProfile(userDetails, profile, null);
            }
        }

        // If no profile defined, use default profile
        if (userDetails.getUser().getProfile() == null) {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP,
                    "  No profile defined in LDAP, using default profile "
                        + defaultProfile);
            }
            userDetails.getUser().setProfile(defaultProfile);
        }

        if (userDetails.getUser().getProfile() != Profile.Administrator) {
            List<String> ldapGroups = userInfo.get(mapping.get("privilege")[0]);
            if (ldapGroups != null) {
                for (String group : ldapGroups) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP,
                            "  Define group privilege for group " + group
                                + " as " + userDetails.getUser().getProfile());
                    }
                    userDetails.addPrivilege(group, userDetails.getUser().getProfile());
                }
            }

            // Set default privileges
            if (userDetails.getPrivileges().size() == 0 && defaultGroup != null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(
                        Geonet.LDAP,
                        "  No privilege defined, setting privilege for group "
                            + defaultGroup + " as "
                            + userDetails.getUser().getProfile());
                }
                userDetails
                    .addPrivilege(defaultGroup, userDetails.getUser().getProfile());
            }
        }
    }
}
