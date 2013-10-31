package org.fao.geonet.kernel.security.openam;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.identity.idm.IdUtils;

import java.util.*;
import java.util.Map.Entry;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.MapBasedAttributes2GrantedAuthoritiesMapper;

/**
 * OpenAMAuthoritiesPopulator
 * 	class that populates granted authorities for OpenAM according to the values specified in a role/profiles mapping
 * @author tx.chevallier
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
