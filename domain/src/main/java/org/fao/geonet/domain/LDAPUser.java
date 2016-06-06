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
package org.fao.geonet.domain;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;

import java.util.Collection;

public class LDAPUser extends InetOrgPerson implements UserDetails {

    private static final long serialVersionUID = -879282571127799714L;
    private final String _userName;

    private Multimap<String, Profile> _groupsAndProfile = HashMultimap.create();

    private User _user;

    public LDAPUser(String username) {
        this._userName = username;
        this._user = new User();
        _user.setProfile(Profile.RegisteredUser);
        _user.setUsername(username);

        // FIXME Should we here populate the LDAP user with LDAP attributes instead of in the GNLDAPUserDetailsMapper ?
        // TODO : populate userId which should be in session
    }

    public void addPrivilege(String group, Profile profile) {
        _groupsAndProfile.put(group, profile);
    }

    public Multimap<String, Profile> getPrivileges() {
        return _groupsAndProfile;
    }

    public void setPrivileges(Multimap<String, Profile> privileges) {
        _groupsAndProfile = privileges;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        this._user = user;
        user.setUsername(_userName);
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return _user.getAuthorities();
    }

    @Override
    public String getPassword() {
        return _user.getUsername();
    }

    @Override
    public String getUsername() {
        return _user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return _user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return _user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return _user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return _user.isEnabled();
    }

    /**
     * @see org.springframework.security.ldap.userdetails.LdapUserDetailsImpl#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        try {
            return super.equals(obj);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see org.springframework.security.ldap.userdetails.LdapUserDetailsImpl#hashCode()
     */
    @Override
    public int hashCode() {
        // Fix for https://github.com/geonetwork/core-geonetwork/issues/708
        if (this.getDn() != null) {
            return super.hashCode();
        } else {
            return this.getUsername().hashCode();
        }
    }
}
