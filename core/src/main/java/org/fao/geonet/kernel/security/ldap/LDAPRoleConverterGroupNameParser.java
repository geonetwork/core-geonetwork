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
// @author dblasby geocat

package org.fao.geonet.kernel.security.ldap;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.utils.Log;

import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * The found "LdapGroupName" will be parsed by `ldapMembershipQueryParser` (i.e. GCAT_(.*)_(.*))
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
 */
public class LDAPRoleConverterGroupNameParser implements LDAPRoleConverter{

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



    //map a ldap group name profile to an actual profile
    // i.e. admin --> Administrator
    Map<String, Profile> profileMapping;

    //This will take a role like "GCAT_general_admin" and convert it GN-Group=general and GN-Profile=admin
    @Override
    public List<LDAPRole> convert(Map<String, ArrayList<String>> userInfo, LDAPUser userDetails, String ldapGroupName, Attributes LdapGroupAttributes)
        throws Exception {
        if (ldapMembershipQueryParser == null)
            return new ArrayList<LDAPRole>(); // nothing to do

        Matcher matcher = ldapMembershipQueryParser.matcher(ldapGroupName); // does this match the parser?

        if (!matcher.matches())  //LDAP group name not in correct format...
            return new ArrayList<LDAPRole>(); // nothing to do;

        String group = matcher.group(this.groupIndexInPattern);
        String profile_str = matcher.group(this.getProfileIndexInPattern());

        Profile profile = getProfile(profile_str); //convert to a GN `Profile` object (simple and with conversion)

        if (profile == null)
            return new ArrayList<LDAPRole>(); // nothing to do;

        Log.debug(Geonet.LDAP, "for ldap user " + userDetails.getUsername() + " for LDAP group " + ldapGroupName +
            " gives group= " + group + " with profile= " + profile.name());

        List<LDAPRole> result = new ArrayList<>(1);
        result.add(new LDAPRole(group,profile));

        return result;
    }

    //given a profile name, find the Profile that it matches
    //see `profileMapping` for transitions
    public Profile getProfile(String pname) {
        if ((this.profileMapping != null) && (this.profileMapping.containsKey(pname)))
            return this.profileMapping.get(pname);

        Profile p = Profile.findProfileIgnoreCase(pname);
        return p;
    }


    public String getLdapMembershipQueryParser() {
        return ldapMembershipQueryParser.toString();
    }

    public void setLdapMembershipQueryParser(String ldapMembershipQueryParser) {
        this.ldapMembershipQueryParser = Pattern.compile(ldapMembershipQueryParser);
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

    public Map<String, Profile> getProfileMapping() {
        return profileMapping;
    }

    public void setProfileMapping(Map<String, Profile> profileMapping) {
        this.profileMapping = profileMapping;
    }
}
