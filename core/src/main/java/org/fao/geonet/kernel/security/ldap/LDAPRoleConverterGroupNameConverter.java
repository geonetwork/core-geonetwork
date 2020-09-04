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

import org.fao.geonet.domain.LDAPUser;

import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This does a direct conversion from a LDAP group name to a list of LDAPRoles.
 * See LDAPRoleConverterGroupNameParser for another example.
 *
 */
public class LDAPRoleConverterGroupNameConverter implements LDAPRoleConverter {
    //groupName (from LDAP) to a list of LDAPRoles (GN-Group and GN-Profile)
    Map<String, List<LDAPRole>> convertMap;


    //given an LDAP role name, find the list of LDAPRoles that are assigned to them.
    @Override
    public List<LDAPRole> convert(Map<String, ArrayList<String>> userInfo, LDAPUser userDetails, String ldapGroupName, Attributes ldapGroupAttributes) {
        if ((convertMap == null) || (!convertMap.containsKey(ldapGroupName))) {
            return new ArrayList<LDAPRole>();
        }
        return convertMap.get(ldapGroupName);
    }


    public void setConvertMap(Map<String, List<LDAPRole>> convertMap) {
        this.convertMap = convertMap;
    }

    public Map<String, List<LDAPRole>> getConvertMap() {
        return convertMap;
    }

}
