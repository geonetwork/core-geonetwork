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
import org.springframework.util.StringUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get all user information from the LDAP user's attributes excluding profiles and groups which are
 * searched in another LDAP location. For profiles and groups, define the search location and the
 * extraction pattern.
 * <p>
 * This search (including subtrees) in the LDAP starting at 'membershipSearchStartObject'
 * It will execute the query ldapMembershipQuery.
 * for ldapMembershipQuery;
 * {0} = username (i.e. what the user types in to login)
 * {1} = cn for the ldap user object (short version)  i.e. "blasby, david"
 * {2} = cn for the ldap user object (full version)   i.e. "blasby, david,ou=GIS Department,ou=Corporate Users,dc=example,dc=com"
 * ** typically you'll be using {2}
 * <p>
 * The found ldap groups will be parsed by `ldapMembershipQueryParser` (i.e. GCAT_(.*)_(.*))
 * groupIndexInPattern, profileIndexInPattern are group numbers in the `ldapMembershipQueryParser`
 * <p>
 * For example,
 * ldapMembershipQueryParser = GCAT_(.*)_(.*))
 * groupIndexInPattern=1
 * profileIndexInPattern=2
 * <p>
 * For a group called "GCAT_GENERAL_EDITOR", then;
 * GN-Group = "GENERAL"
 * GN-PROFILE = "EDITOR" (which is converted to "Editor" via the `profileMapping`
 *
 * @author dblasby/francois
 */
public class LDAPUserDetailsContextMapperWithProfileSearchEnhanced extends AbstractLDAPUserDetailsContextMapper {

    //Query used to find group membership
    private String ldapMembershipQuery;

    //How to parse the groups into GN-GROUP and GN-PROFILE
    //i.e. GCAT_(.*)_(.*) to parse GCAT_GENERAL_EDITOR
    private Pattern ldapMembershipQueryParser;

    //these go with the ldapMembershipQueryParser
    // they correspond to the groups returned by the parser
    // i.e. GCAT_(.*)_(.*) to parse GCAT_GENERAL_EDITOR
    //        1=> GENERAL
    //        2=> EDITOR
    private int groupIndexInPattern;
    private int profileIndexInPattern;

    //where to start searching in the LDAP
    // typically this will be "" (search entire directory)
    private String membershipSearchStartObject;


    public void setMembershipSearchStartObject(String membershipSearchStartObject) {
        this.membershipSearchStartObject = membershipSearchStartObject;
    }

    public void setLdapMembershipQuery(String ldapMembershipQuery) {
        this.ldapMembershipQuery = ldapMembershipQuery;
    }

    public void setLdapMembershipQueryParser(String ldapMembershipQueryParser) {
        this.ldapMembershipQueryParser = Pattern.compile(ldapMembershipQueryParser);
    }

    //This will find the shortest "cn" attribute given in the object
    // typically, there are >1 of these.  I.e. "blasby, david" and "blasby, david,ou=GIS Department,ou=Corporate Users,dc=example,dc=com"
    public String cn_short(Attributes atts) throws NamingException {
        Attribute value = atts.get("cn");
        List values = Collections.list(value.getAll());
        Comparator<String> comparator = (str1, str2) -> str1.length() > str2.length() ? 1 : -1;
        String shortest = (String) values.stream().sorted(comparator).findFirst().get();
        return shortest;
    }

    //This will find the longest "cn" attribute given in the object
    // typically, there are >1 of these.  I.e. "blasby, david" and "blasby, david,ou=GIS Department,ou=Corporate Users,dc=example,dc=com"
    public String cn_short(Map<String, ArrayList<String>> userInfo) {
        ArrayList<String> cn = userInfo.get("cn");
        if ((cn == null) || (cn.size() == 0))  // bad user!
            return null;
        Comparator<String> comparator = (str1, str2) -> str1.length() > str2.length() ? 1 : -1;
        String shortest = cn.stream().sorted(comparator).findFirst().get();
        return shortest;
    }

    //This will find the longest "cn" value given in the map
    // typically, there are >1 of these.  I.e. "blasby, david" and "blasby, david,ou=GIS Department,ou=Corporate Users,dc=example,dc=com"
    public String cn_long(Map<String, ArrayList<String>> userInfo) {
        ArrayList<String> cn = userInfo.get("cn");
        if ((cn == null) || (cn.size() == 0))  // bad user!
            return null;
        Comparator<String> comparator = (str1, str2) -> str1.length() > str2.length() ? -1 : 1;
        String longest = cn.stream().sorted(comparator).findFirst().get();
        return longest;
    }

    //escape a string for the query
    //"blasby\, david"  ==> "blasby\\, david"
    //This is required for membership searches in AD
    public String escape(String str) {
        return str.replace("\\", "\\\\");
    }

    //given a profile name, find the Profile that it matches
    //see `profileMapping` for transitions
    public Profile getProfile(String pname) {
        if ((this.profileMapping != null) && (this.profileMapping.containsKey(pname)))
            return this.profileMapping.get(pname);

        Profile p = Profile.findProfileIgnoreCase(pname);
        return p;
    }


    //main method to find the user's ldap group memberships
    // a) will populate the GN-Group and GN-Profile in userDetails
    // b) will look for the "highest" Profile given and set that as the user's main profile  -userDetails.getUser().setProfile(highestUserProfile)
    protected void setProfilesAndPrivileges(Profile defaultProfile,
                                            String defaultGroup, Map<String, ArrayList<String>> userInfo,
                                            LDAPUser userDetails) {

        if (!StringUtils.isEmpty(ldapMembershipQuery)) {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                StringBuffer sb = new StringBuffer("Group and profile search:");
                sb.append("\nLDAP Membership Query query: \t" + ldapMembershipQuery);
                sb.append("\nldapMembershipQueryParser: \t" + ldapMembershipQueryParser.toString());
                sb.append("\ngroupIndexInPattern: \t" + groupIndexInPattern);
                sb.append("\nprofileIndexInPattern: \t" + profileIndexInPattern);
                sb.append("\nmembershipSearchStartObject: \t" + membershipSearchStartObject);

                Log.debug(Geonet.LDAP, sb.toString());
            }
            // TODO: add more control on values
            NamingEnumeration<?> ldapInfoList;
            try {
                DirContext dc = contextSource.getReadOnlyContext();


                String username = escape(userDetails.getUsername());
                String cn_short = escape(cn_short(userInfo));
                String cn_long = escape(cn_long(userInfo));

                String groupsQuery = MessageFormat.format(this.ldapMembershipQuery,
                    username, cn_short, cn_long);

                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); //recursive

                ldapInfoList = dc.search(membershipSearchStartObject, groupsQuery, searchControls);
                while (ldapInfoList.hasMore()) {
                    SearchResult sr = (SearchResult) ldapInfoList.next();
                    String ldapGroupName = cn_short(sr.getAttributes());

                    Matcher matcher = ldapMembershipQueryParser.matcher(ldapGroupName); // does this match the parser?

                    if (!matcher.matches())  //LDAP group name not in correct format...
                        continue;

                    String group = matcher.group(this.groupIndexInPattern);
                    String profile_str = matcher.group(this.getProfileIndexInPattern());

                    Profile profile = getProfile(profile_str); //convert to a GN `Profile` object (simple and with conversion)

                    if (profile == null)
                        continue;

                    Log.debug(Geonet.LDAP, "for ldap user " + username + " for LDAP group " + ldapGroupName +
                        " gives group= " + group + " with profile= " + profile.name());

                    userDetails.addPrivilege(group, profile); //add the profile info

                }

                //highest access is the "generic" access for the user
                // TODO: what are the impacts of this?
                Profile highestUserProfile = ProfileManager.getHighestProfile(userDetails.getPrivileges().values().toArray(new Profile[0]));
                if (highestUserProfile != null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  Highest user profile is " + highestUserProfile);
                    }
                    userDetails.getUser().setProfile(highestUserProfile);
                }

                // If no profile defined, use default profile
                if (userDetails.getUser().getProfile() == null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  No profile defined in LDAP, using default profile " + defaultProfile);
                    }
                    userDetails.getUser().setProfile(defaultProfile);
                }

            } catch (NamingException e) {
                Log.error(Geonet.LDAP, "Failed to extract profiles and groups. Error is: " + e.getMessage(), e);
            }
        }
    }


    public int getGroupIndexInPattern() {
        return groupIndexInPattern;
    }

    public void setGroupIndexInPattern(int groupIndexInPattern) {
        this.groupIndexInPattern = groupIndexInPattern;
    }

    public int getProfileIndexInPattern() {
        return profileIndexInPattern;
    }

    public void setProfileIndexInPattern(int profileIndexInPattern) {
        this.profileIndexInPattern = profileIndexInPattern;
    }
}
