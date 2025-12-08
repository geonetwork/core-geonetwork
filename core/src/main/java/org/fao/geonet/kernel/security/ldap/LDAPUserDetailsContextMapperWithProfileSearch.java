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

import jeeves.component.ProfileManager;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.utils.Log;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.ObjectUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

/**
 * Get all user information from the LDAP user's attributes excluding profiles and groups which are
 * searched in another LDAP location. For profiles and groups, define the search location and the
 * extraction pattern.
 *
 * @author francois
 */
public class LDAPUserDetailsContextMapperWithProfileSearch extends
    AbstractLDAPUserDetailsContextMapper {

    private String groupAttribute;
    private String groupObject;
    private String groupQuery;
    private String groupQueryPattern;

    private String privilegeAttribute;
    private String privilegeObject;
    private String privilegeQuery;
    private String privilegeQueryPattern;

    private String privilegePattern;
    private Pattern pattern = null;
    private int groupIndexInPattern;
    private int profilIndexInPattern;

    private Pattern groupQueryPatternCompiled;

    private Pattern privilegeQueryPatternCompiled;

    protected void setProfilesAndPrivileges(Profile defaultProfile,
                                            String defaultGroup, Map<String, ArrayList<String>> userInfo,
                                            LDAPUser userDetails) {

        if (!ObjectUtils.isEmpty(groupQuery)) {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                StringBuffer sb = new StringBuffer("Group and profile search:");
                sb.append("\nGroup attribute: \t" + groupAttribute);
                sb.append("\nGroup query: \t" + groupQuery);
                sb.append("\nGroup query pattern: \t" + groupQueryPattern);
                sb.append("\nProfile attribute: \t" + privilegeAttribute);
                sb.append("\nProfile query: \t" + privilegeQuery);
                sb.append("\nProfile attribute: \t" + privilegeQueryPattern);
                Log.debug(Geonet.LDAP, sb.toString());
            }
            // TODO: add more control on values
            NamingEnumeration<?> ldapInfoList;
            try {
                DirContext dc = contextSource.getReadOnlyContext();

                // Extract profile first
                Set<Profile> profileList = new HashSet<Profile>();
                String groupsQuery = MessageFormat.format(this.privilegeQuery,
                    userDetails.getUsername());
                ldapInfoList = dc.search(privilegeObject, groupsQuery, null);
                while (ldapInfoList.hasMore()) {
                    SearchResult sr = (SearchResult) ldapInfoList.next();
                    String profileName = (String) sr.getAttributes()
                        .get(privilegeAttribute).get();

                    Matcher m = privilegeQueryPatternCompiled.matcher(profileName);
                    boolean b = m.matches();
                    if (b) {
                        // Try to figure out the profile
                        Profile p = Profile.findProfileIgnoreCase(m.group(1));
                        if (p == null) {
                            Log.debug(Geonet.LDAP, "profile is null " + getClass() + ".setProfilesAndPrivileges()");
                            // Else try to figure it out via the mapping
                            // provided
                            if (profileMapping != null) {
                                Profile mapped = profileMapping.get(m.group(1));
                                if (mapped != null) {
                                    p = mapped;
                                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                                        Log.debug(Geonet.LDAP, "ldap profileName is " + profileName
                                            + ", pattern matched " + m.group(1) + " adding profile " + p.name());
                                    }
                                }
                            }
                        }
                        if (p != null) {
                            profileList.add(p);
                        }
                    } else {
                        Log.error(Geonet.LDAP, "LDAP profile '" + profileName + "' does not match search pattern '"
                            + privilegeQueryPattern + "'. Information ignored.");
                    }
                }

                //First time, to get a default profile in case profilIndexInPattern fails
                Profile highestUserProfile = ProfileManager.getHighestProfile(profileList.toArray(new Profile[profileList.size()]));
                if (highestUserProfile != null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  Highest user profile is "
                            + highestUserProfile);
                    }
                    userDetails.getUser().setProfile(highestUserProfile);
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

                // Get groups
                String groupQuery = MessageFormat.format(this.groupQuery,
                    userDetails.getUsername());
                ldapInfoList = dc.search(this.groupObject, groupQuery, null);
                while (ldapInfoList.hasMore()) {
                    SearchResult sr = (SearchResult) ldapInfoList.next();
                    String groupName = (String) sr.getAttributes()
                        .get(groupAttribute).get();

                    if (this.pattern != null) {
                        Matcher m = pattern.matcher(groupName);
                        if (m.matches()) {
                            String group = m.group(groupIndexInPattern);
                            Profile profile = null;

                            if (profilIndexInPattern > 0) {
                                profile = Profile.findProfileIgnoreCase(m.group(profilIndexInPattern));
                            }

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
                            } else {
                                userDetails.addPrivilege(group,
                                    userDetails.getUser().getProfile());
                            }
                        } else {
                            Matcher m2 = groupQueryPatternCompiled.matcher(groupName);
                            if (m2.matches()) {
                                String group = m2.group(1);
                                userDetails.addPrivilege(group,
                                    userDetails.getUser().getProfile());
                            } else {
                                Log.error(Geonet.LDAP, "LDAP group '" + groupName
                                    + "' does not match search pattern '"
                                    + groupQueryPattern + "'. Information ignored.");
                            }
                        }
                    } else {

                        Matcher m = groupQueryPatternCompiled.matcher(groupName);
                        boolean b = m.matches();
                        if (b) {
                            String group = m.group(1);
                            userDetails.addPrivilege(group,
                                userDetails.getUser().getProfile());
                        } else {
                            Log.error(Geonet.LDAP, "LDAP group '" + groupName
                                + "' does not match search pattern '"
                                + groupQueryPattern + "'. Information ignored.");
                        }
                    }
                }

                highestUserProfile = ProfileManager.getHighestProfile(profileList.toArray(new Profile[profileList.size()]));
                if (highestUserProfile != null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  Highest user profile is "
                            + highestUserProfile);
                    }
                    userDetails.getUser().setProfile(highestUserProfile);
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

            } catch (NamingException e) {
                Log.error(Geonet.LDAP, "Failed to extract profiles and groups. Error is: " + e.getMessage(), e);
            }
        }
    }

    public String getGroupQuery() {
        return groupQuery;
    }

    public void setGroupQuery(String groupQuery) {
        this.groupQuery = groupQuery;
    }

    public String getGroupQueryPattern() {
        return groupQueryPattern;
    }

    public void setGroupQueryPattern(String groupQueryPattern) {
        this.groupQueryPattern = groupQueryPattern;
        this.groupQueryPatternCompiled = Pattern.compile(groupQueryPattern);
    }

    public String getPrivilegeQuery() {
        return privilegeQuery;
    }

    public void setPrivilegeQuery(String privilegeQuery) {
        this.privilegeQuery = privilegeQuery;
    }

    public String getPrivilegeQueryPattern() {
        return privilegeQueryPattern;
    }

    public void setPrivilegeQueryPattern(String privilegeQueryPattern) {
        this.privilegeQueryPattern = privilegeQueryPattern;
        this.privilegeQueryPatternCompiled = Pattern
            .compile(privilegeQueryPattern);
    }

    public String getGroupObject() {
        return groupObject;
    }

    public void setGroupObject(String groupObject) {
        this.groupObject = groupObject;
    }

    public String getPrivilegeObject() {
        return privilegeObject;
    }

    public void setPrivilegeObject(String privilegeObject) {
        this.privilegeObject = privilegeObject;
    }

    public String getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(String groupAttribute) {
        this.groupAttribute = groupAttribute;
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
