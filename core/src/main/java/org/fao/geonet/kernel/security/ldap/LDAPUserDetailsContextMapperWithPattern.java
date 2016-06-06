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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jeeves.component.ProfileManager;

import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;

/**
 * Get all user information from the LDAP user's attributes (including profile and groups) where
 * profile and groups are stored in one attribute. A custom pattern is defined to extract those
 * information.
 *
 * @author francois
 */
public class LDAPUserDetailsContextMapperWithPattern extends
    AbstractLDAPUserDetailsContextMapper {

    private String privilegePattern;
    private Pattern pattern;
    private int groupIndexInPattern;
    private int profilIndexInPattern;

    protected void setProfilesAndPrivileges(Profile defaultProfile, String defaultGroup, Map<String, ArrayList<String>> userInfo,
                                            LDAPUser userDetails) {

        // a privilegePattern is defined which define a
        // combination of group and profile pair.
        ArrayList<String> privileges = userInfo
            .get(mapping.get("privilege")[0]);
        if (privileges != null) {
            Set<Profile> profileList = new HashSet<Profile>();

            for (String privilegeDefinition : privileges) {
                Matcher m = pattern.matcher(privilegeDefinition);
                boolean b = m.matches();
                if (b) {
                    String group = m.group(groupIndexInPattern);
                    Profile profile = Profile.valueOf(m.group(profilIndexInPattern));

                    if (profile != null) {
                        if (!LDAPConstants.ALL_GROUP_INDICATOR.equals(group)) {
                            if (Log.isDebugEnabled(Geonet.LDAP)) {
                                Log.debug(Geonet.LDAP, "  Adding profile "
                                    + profile + " for group " + group);
                            }
                            userDetails.addPrivilege(group, profile);
                            profileList.add(profile);
                        } else {
                            profileList.add(profile);
                        }
                    }
                } else {
                    Log.error(Geonet.LDAP, "LDAP privilege info '"
                        + privilegeDefinition
                        + "' does not match search pattern '"
                        + privilegePattern + "'. Information ignored.");
                }
            }
            Profile highestUserProfile = ProfileManager.getHighestProfile(profileList.toArray(new Profile[0]));
            if (highestUserProfile != null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  Highest user profile is "
                        + highestUserProfile);
                }
                userDetails.getUser().setProfile(highestUserProfile);
            }
        }
    }

    public String getPrivilegePattern() {
        return privilegePattern;
    }

    public void setPrivilegePattern(String privilegePattern) {
        this.privilegePattern = privilegePattern;
        this.pattern = Pattern.compile(privilegePattern);
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

}
