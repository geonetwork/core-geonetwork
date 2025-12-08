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
import org.springframework.util.ObjectUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
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

 * @author dblasby/francois
 */
public class LDAPUserDetailsContextMapperWithProfileSearchEnhanced extends AbstractLDAPUserDetailsContextMapper {

    //Query used to find group membership
    private String ldapMembershipQuery;



    //where to start searching in the LDAP
    // typically this will be "" (search entire directory)
    private String membershipSearchStartObject;

    //Strategy objects to convert a LDAPRole to GN-role (GN-group and GN-profile)
    private List<LDAPRoleConverter> ldapRoleConverters;

    public void setLdapRoleConverters(List<LDAPRoleConverter> vals) {
        this.ldapRoleConverters = vals;
    }

    public void setMembershipSearchStartObject(String membershipSearchStartObject) {
        this.membershipSearchStartObject = membershipSearchStartObject;
    }

    public void setLdapMembershipQuery(String ldapMembershipQuery) {
        this.ldapMembershipQuery = ldapMembershipQuery;
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
    // alternatively,  use userInfo.get("dn") list.
    public String cn_long(Map<String, ArrayList<String>> userInfo) {
        ArrayList<String> cn = userInfo.get("cn");
        if ((cn == null) || (cn.size() == 0))  // bad user!
            return null;

        for(String dn: userInfo.get("dn")) {
            dn = dn.replaceFirst("^cn=",""); // dn will start with "cn="
            cn.add(dn);
        }

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

        if (!ObjectUtils.isEmpty(ldapMembershipQuery)) {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                StringBuffer sb = new StringBuffer("Group and profile search:");
                sb.append("\nLDAP Membership Query query: \t" + ldapMembershipQuery);
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

                Set<LDAPRole> allRoles = new HashSet<>();

                try {
                    //for each found LDAP-Group
                    while (ldapInfoList.hasMore()) {
                        SearchResult sr = (SearchResult) ldapInfoList.next();
                        String ldapGroupName = cn_short(sr.getAttributes());

                        //have the converters process the LDAP-Group
                        //NOTE: they will return an empty list if they don't know what the role means
                        //      allRoles is a set, you can add duplicates to it with no problem...
                        for (LDAPRoleConverter converter : this.ldapRoleConverters) {
                            List<LDAPRole> newRoles = converter.convert(userInfo, userDetails, ldapGroupName, sr.getAttributes());
                            if (newRoles != null)
                                allRoles.addAll(newRoles);
                        }
                    }
                }
                catch (PartialResultException ee) {
                    // do nothing - this occurs when you are searching from the very top of the LDAP.
                    // its not really a problem.  Usually you would use template.setIgnorePartialResultException
                }

                //we have a set of GN-Role, now add them to the user object
                for(LDAPRole role: allRoles) {
                    userDetails.addPrivilege(role.getGroupName(), role.getProfile()); //add the profile info
                }

                //highest access is the "generic" access for the user
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

            } catch (Exception e) {
                Log.error(Geonet.LDAP, "Failed to extract profiles and groups. Error is: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }


}
