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

import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class LDAPRoleConverterTest {

    //simple test for parsing "GCAT_GENERAL_Administrator"
    @Test
    public void test_parse_GCAT_GENERAL_Administrator() throws Exception{
        LDAPRoleConverterGroupNameParser out = new LDAPRoleConverterGroupNameParser();

        LDAPUser userDetails = new LDAPUser("dblasby@example.com");

        out.setLdapMembershipQueryParser("GCAT_(.*)_(.*)");
        out.setGroupIndexInPattern(1);
        out.setProfileIndexInPattern(2);
        out.setProfileMapping(null);

        List<LDAPRole> result = out.convert(null,userDetails,"GCAT_GENERAL_Administrator",null);
        assertEquals(1, result.size());
        assertEquals("GENERAL", result.get(0).getGroupName());
        assertEquals(Profile.Administrator, result.get(0).getProfile());
    }

    //tests profile mapping (admin -> Administrator)
    @Test
    public void test_profile_mapping() throws Exception {
        LDAPRoleConverterGroupNameParser out = new LDAPRoleConverterGroupNameParser();

        LDAPUser userDetails = new LDAPUser("dblasby@example.com");

        out.setLdapMembershipQueryParser("GCAT_(.*)_(.*)");
        out.setGroupIndexInPattern(1);
        out.setProfileIndexInPattern(2);

        Map<String,Profile> profileMap = new HashMap<>();
        profileMap.put("admin",Profile.Administrator);
        profileMap.put("editor",Profile.Editor);

        out.setProfileMapping(profileMap);

        List<LDAPRole> result = out.convert(null,userDetails,"GCAT_GENERAL_admin",null);
        assertEquals(1, result.size());
        assertEquals("GENERAL", result.get(0).getGroupName());
        assertEquals(Profile.Administrator, result.get(0).getProfile());
    }

    //testswhen the LDAP role doesn't match the pattern (shouldn't return anything)
    @Test
    public void test_no_matching_roles() throws Exception {
        LDAPRoleConverterGroupNameParser out = new LDAPRoleConverterGroupNameParser();

        LDAPUser userDetails = new LDAPUser("dblasby@example.com");

        out.setLdapMembershipQueryParser("GCAT_(.*)_(.*)");
        out.setGroupIndexInPattern(1);
        out.setProfileIndexInPattern(2);

        Map<String,Profile> profileMap = new HashMap<>();
        profileMap.put("admin", Profile.Administrator);
        profileMap.put("editor", Profile.Editor);

        out.setProfileMapping(profileMap);

        List<LDAPRole> result = out.convert(null,userDetails,"BAD GROUP NAME",null);
        assertEquals(0,result.size());
    }

    //sets up a direct link between an LDAP group and a list of GN-Roles (gn-group and gn-profile)
    //also tests when the LDAP role doesn't match, LDAPRoleConverterGroupNameConverter doesn't return anything
    @Test
    public void test_LDAPRoleConverterGroupNameConverter_direct_match() throws Exception {
        LDAPRoleConverterGroupNameConverter out = new LDAPRoleConverterGroupNameConverter();

        Map<String,List<LDAPRole>> map = new HashMap<>();
        List<LDAPRole> roles = new ArrayList<LDAPRole>(
                                        Arrays.asList( new LDAPRole("group1","Administrator"),
                                                       new LDAPRole("group2","Editor")
                                        ));
        map.put("ldap_abc",roles);

        out.setConvertMap(map);

        LDAPUser userDetails = new LDAPUser("dblasby@example.com");

        List<LDAPRole> result = out.convert(null,userDetails,"ldap_abc",null);
        assertEquals(2, result.size());
        assertEquals("group1", result.get(0).getGroupName());
        assertEquals(Profile.Administrator, result.get(0).getProfile());

        assertEquals("group2", result.get(1).getGroupName());
        assertEquals(Profile.Editor, result.get(1).getProfile());


        result = out.convert(null,userDetails,"BAD_GROUP_NAME",null);
        assertEquals(0, result.size());
    }



}
