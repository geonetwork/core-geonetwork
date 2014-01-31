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

import java.util.Collection;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class LDAPUser implements UserDetails {
	
    private static final long serialVersionUID = -879282571127799714L;
    private final String _userName;

    private Multimap<String, Profile> _groupsAndProfile = HashMultimap.create();

	private User _user;

	public LDAPUser(String username) {
        this._userName = username;
		// FIXME Should we here populate the LDAP user with LDAP attributes instead of in the GNLDAPUserDetailsMapper ?
		// TODO : populate userId which should be in session
	}
	
	public void addPrivilege(String group, Profile profile) {
		_groupsAndProfile.put(group, profile);
	}
	public void setPrivileges(Multimap<String, Profile> privileges) {
		_groupsAndProfile = privileges;
	}
	public Multimap<String, Profile> getPrivileges() {
		return _groupsAndProfile;
	}

	public User getUser() {
        return _user;
    }
	public void setUser(User user) {
        this._user = user;
        user.setUsername(_userName);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
}
