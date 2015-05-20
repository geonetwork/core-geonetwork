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
package org.fao.geonet.kernel.security.openam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.authority.mapping.MapBasedAttributes2GrantedAuthoritiesMapper;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.identity.idm.IdUtils;

/**
 * OpenAMAuthoritiesPopulator
 * 	class that populates granted authorities for OpenAM according to the values specified in a role/profiles mapping
 * @author thierry.chevallier (AKKA Informatique et Systèmes) for ingeoclouds : contact@ingeoclouds.eu
 */
public class OpenAMAuthoritiesPopulator {

	private MapBasedAttributes2GrantedAuthoritiesMapper mapper;
	
	/**
     * Lookup the users group memberships and return as an array of GrantedAuthority
     * @param ssoToken users SSOTOken
     * @throws com.sun.identity.idm.IdRepoException
     * @throws com.iplanet.sso.SSOException
     */
    public Collection<GrantedAuthority> getGrantedAuthoritiesCollection(SSOToken ssoToken) 
    		throws IdRepoException, SSOException {
        
    	AMIdentity id = IdUtils.getIdentity(ssoToken);
        Collection<GrantedAuthority> ga = new ArrayList<GrantedAuthority>();
        Set groups = id.getMemberships(IdType.GROUP);

        for (Object group1 : groups) {
            AMIdentity group = (AMIdentity) group1;
            String role = "ROLE_" + group.getName().toUpperCase();
            ga.add(new GrantedAuthorityImpl(role));
        }

        return mapAuthorities(ga);
    }

    /**
     * mapAuthorities
     * @pmaparam input
     * @return
     */
    public Collection<GrantedAuthority> mapAuthorities(Collection<GrantedAuthority> input){
		
    	Map<String, Collection<GrantedAuthority>> att2gaMap = mapper.getAttributes2grantedAuthoritiesMap();
    	
    	Set<GrantedAuthority> output = new HashSet<GrantedAuthority>();
    	
    	for (GrantedAuthority auth : input) {
    		
			for ( Entry<String, Collection<GrantedAuthority>> entry : att2gaMap.entrySet()){
				
				if (auth.toString().equals(entry.getKey())){	
					output.addAll(entry.getValue());
				}
			}
    	}
    	
    	return output;
    	
    }
    

	public MapBasedAttributes2GrantedAuthoritiesMapper getMapper() {
		return mapper;
	}


	public void setMapper(MapBasedAttributes2GrantedAuthoritiesMapper mapper) {
		this.mapper = mapper;
	}
}
