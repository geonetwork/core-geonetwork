/**
 * 
 */
package org.fao.geonet.kernel.security;

import org.fao.geonet.domain.LDAPUser;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public interface WritableUserDetailsContextMapper extends
		UserDetailsContextMapper {

	void saveUser(LDAPUser userDetails);

}
