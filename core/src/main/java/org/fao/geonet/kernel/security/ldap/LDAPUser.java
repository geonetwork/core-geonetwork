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

import jeeves.component.ProfileManager;
import jeeves.guiservices.session.JeevesUser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class LDAPUser extends JeevesUser {
	
	private static final long serialVersionUID = -5390558007347570517L;
	
	private Multimap<String, String> groupsAndProfile = HashMultimap.create();

    private String address;

    private String city;

    private String state;

    private String zip;

    private String country;

    private String organisation;

    private String kind;
	
	public LDAPUser(ProfileManager profileManager, String username) {
		super(profileManager);
		setUsername(username);
		// FIXME Should we here populate the LDAP user with LDAP attributes instead of in the GNLDAPUserDetailsMapper ?
		// TODO : populate userId which should be in session
	}
	
	public void addPrivilege(String group, String profile) {
		groupsAndProfile.put(group, profile);
	}
	public void setPrivileges(Multimap<String, String> privileges) {
		groupsAndProfile = privileges;
	}
	public Multimap<String, String> getPrivileges() {
		return groupsAndProfile;
	}

    public String getAddress() {
    	return address;
    }

    public LDAPUser setAddress(String address) {
    	if (address==null) address = "";
    	this.address = address;
    	return this;
    }

    public String getCity() {
    	return city;
    }

    public LDAPUser setCity(String city) {
    	if (city==null) city = "";
    	this.city = city;
    	return this;
    }

    public String getState() {
    	return state;
    }

    public LDAPUser setState(String state) {
    	if (state==null) state = "";
    	this.state = state;
    	return this;
    }

    public String getZip() {
    	return zip;
    }

    public LDAPUser setZip(String zip) {
    	if (zip==null) zip = "";
    	this.zip = zip;
    	return this;
    }

    public String getCountry() {
    	return country;
    }

    public LDAPUser setCountry(String country) {
    	if (country==null) country = "";
    	this.country = country;
    	return this;
    }

    public String getOrganisation() {
    	return organisation;
    }

    public LDAPUser setOrganisation(String organisation) {
    	if (organisation==null) organisation = "";
    	this.organisation = organisation;
    	return this;
    }

    public String getKind() {
    	return kind;
    }

    public LDAPUser setKind(String kind) {
    	if (kind==null) kind = "";
    	this.kind = kind;
    	return this;
    }
}
