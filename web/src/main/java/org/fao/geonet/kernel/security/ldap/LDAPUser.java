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

import java.util.ArrayList;

import jeeves.guiservices.session.JeevesUser;
import jeeves.server.ProfileManager;

import org.fao.geonet.kernel.search.spatial.Pair;

public class LDAPUser extends JeevesUser {
	
	private static final long serialVersionUID = -5390558007347570517L;
	
	private ArrayList<Pair<String, String>> groupsAndProfile = new ArrayList<Pair<String, String>>();
	
	public LDAPUser(ProfileManager profileManager, String username) {
		super(profileManager);
		setUsername(username);
		// FIXME Should we here populate the LDAP user with LDAP attributes instead of in the GNLDAPUserDetailsMapper ?
		// TODO : populate userId which should be in session
	}
	
	public void addPrivilege(String group, String profile) {
		groupsAndProfile.add(Pair.read(group, profile));
	}
	public void setPrivileges(ArrayList<Pair<String, String>> privileges) {
		groupsAndProfile = privileges;
	}
	public ArrayList<Pair<String, String>> getPrivileges() {
		return groupsAndProfile;
	}
}
