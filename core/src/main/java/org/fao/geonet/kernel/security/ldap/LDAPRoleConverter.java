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

//given information about a LDAP-Group/User, return the gn-groups/gn-profile associated with that person
public interface LDAPRoleConverter {

    /**
     * Convert LDAP information (person info as well as info about a group they are a memember of) to a list of
     * roles.
     *
     * In general, the person info (userInfo, userDetails) are not used - they're included for future special cases.
     *     eg. an LDAP attribute inside the person that gives a role.
     *
     *
     * @param userInfo       information about the user (all attributes from LDAP person object)
     * @param userDetails    information about the user (constructed/paresed from LDAP person object by GN)
     * @param ldapGroupName  name of the ldap group the user is a member of
     * @param ldapGroupAttributes  attributes of the group the user is a member of
     * @return List of ldap roles (GN-group name and GN-Profile)
     */
    List<LDAPRole> convert(Map<String, ArrayList<String>> userInfo,
                           LDAPUser userDetails,
                           String ldapGroupName,
                           Attributes ldapGroupAttributes) throws Exception;

}
