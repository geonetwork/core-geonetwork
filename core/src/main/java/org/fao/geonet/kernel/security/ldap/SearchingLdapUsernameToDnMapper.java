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

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.LdapUsernameToDnMapper;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.util.Assert;


//see org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
//
// This   Searches for the user.  If they are found, then we use that DN
// If not, we throw an exception.
//  If not, either they made a typo in the name or they are attempting to create a new user.
//    We should NOT allow users to be created (we have no idea where to put them, and its really dangerous).
//
public class SearchingLdapUsernameToDnMapper implements LdapUsernameToDnMapper {

    private LdapContextSource ldapContextSource;
    private LdapUserSearch ldapUserSearch;

    public void setLdapContextSource(LdapContextSource source) {
        this.ldapContextSource = source;
    }

    public void setLdapUserSearch(LdapUserSearch ldapUserSearch) {
        this.ldapUserSearch = ldapUserSearch;
    }

    @Override
    public DistinguishedName buildDn(String username) {

        Assert.notNull(ldapContextSource, "ldapContextSource is not injected");
        Assert.notNull(ldapUserSearch, "ldapUserSearch is not injected");
        if (username.contains("*"))
            throw new RuntimeException("Security violation - LDAP username contains *!");

        username = username; // might need to escape characters here - like "blasby\, david", but not required by AD
        DirContextOperations result = ldapUserSearch.searchForUser(username);

        return new DistinguishedName(result.getDn());
    }

}
