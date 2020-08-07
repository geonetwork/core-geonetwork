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

import org.fao.geonet.domain.Profile;

/**
 * very simple class that just holds a user's group/profile information.
 */
public class LDAPRole {

    String groupName;
    Profile profile;


    public LDAPRole(String groupName, Profile profile) throws Exception {
        if ((groupName == null) || (profile == null))
            throw new Exception("groupName and profile must not be null");
        this.groupName = groupName;
        this.profile = profile;
    }

    public LDAPRole(String groupName, String profileName) throws Exception {
        this.groupName = groupName;
        this.profile = Profile.findProfileIgnoreCase(profileName);
        if ((groupName == null) || (profile == null))
            throw new Exception("groupName and profile must not be null");
    }


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) throws Exception {
        if (groupName == null)
            throw new Exception("groupName must not be null");
        this.groupName = groupName;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) throws Exception {
        if (profile == null)
            throw new Exception("profile must not be null");
        this.profile = profile;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LDAPRole))
            return false;

        boolean groupNameEqual =  this.getGroupName().equals(((LDAPRole) obj).getGroupName());
        boolean profileNameEqual =  this.getProfile().name().equals(((LDAPRole) obj).getProfile().name());


        return groupNameEqual && profileNameEqual;
    }

    @Override
    public int hashCode() {
        return this.getGroupName().hashCode() ^ this.getProfile().name().hashCode() ;
    }
}
