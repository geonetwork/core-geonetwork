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
