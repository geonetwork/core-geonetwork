//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server;

import java.util.Hashtable;

import javax.servlet.http.HttpSession;

import jeeves.guiservices.session.JeevesUser;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

//=============================================================================

/**
 * Abstraction layer from the user session.
 */
public class UserSession
{
	private Hashtable<String, Object> htProperties = new Hashtable<String, Object>(10, .75f);

	private HttpSession sHttpSession;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public UserSession() {}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------
	
	/**
	 * @return the sHttpSession
	 */
	public HttpSession getsHttpSession() {
		return sHttpSession;
	}

	/**
	 * @param sHttpSession the sHttpSession to set
	 */
	public void setsHttpSession(HttpSession sHttpSession) {
		this.sHttpSession = sHttpSession;
	}

	/**
     * Sets a generic property.
	 */
	public void setProperty(String name, Object value)
	{
		htProperties.put(name, value);
	}

	//--------------------------------------------------------------------------
	/**
     * Gets a generic property.
	 */
	public Object getProperty(String name)
	{
		return htProperties.get(name);
	}

	//--------------------------------------------------------------------------
	/**
     * Removes a generic property.
	 */
	public void removeProperty(String name)
	{
		htProperties.remove(name);
	}

    /**
     * Clears user session properties and authentication.
     */
    public void clear() {
        htProperties = new Hashtable(10, .75f);
       	SecurityContextHolder.clearContext();
       	sHttpSession.invalidate();
    }
/*	public void authenticate(String userId, String username, String name, String surname, String profile, String emailAddr) {
		JeevesUser user = new JeevesUser()
			.setId(userId)
			.setUsername(username)
			.setName(name)
			.setProfile(profile)
			.setEmail(emailAddr);
		
		Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_"+profile));
		Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities ) ;
		secContext().setAuthentication(authentication);
	}*/

	//--------------------------------------------------------------------------

	public boolean isAuthenticated() {
		return !(auth() instanceof AnonymousAuthenticationToken);
	}

	//--------------------------------------------------------------------------

	public String getUserId() { 
		JeevesUser userDetails = getUserDetails();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getId();
		}
	}
	public String getUsername() {
		JeevesUser userDetails = getUserDetails();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getUsername();
		}
	}
	public String getName() {
		JeevesUser userDetails = getUserDetails();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getName();
		}
	}
	public String getSurname() { 
		Authentication auth = auth();
		if (auth == null) {
			return null;   
		} else {
			return auth.getName();
		}
	}
	public String getProfile() {
		JeevesUser userDetails = getUserDetails();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getProfile();
		}
	}
	public String getEmailAddr() {
		JeevesUser userDetails = getUserDetails();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getEmail();
		}
	}

	public int getUserIdAsInt()  { 
		String id = getUserId();
		return id == null? -1 : Integer.parseInt(getUserId()); }
	
	private SecurityContext secContext() { return SecurityContextHolder.getContext(); }
	private Authentication auth() {
		SecurityContext secContext = secContext();
		if (secContext == null) {
			return null;
		} else {
			Authentication authentication = secContext.getAuthentication();
			return authentication;
		}
	}
	public JeevesUser getUserDetails() {
		Authentication auth = auth();
		if (auth != null && auth.getDetails() instanceof JeevesUser) {
			return (JeevesUser) auth.getDetails(); 
		}
		return null;
	}

}

//=============================================================================