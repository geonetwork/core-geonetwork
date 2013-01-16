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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

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
        htProperties.clear();
       	SecurityContextHolder.clearContext();
       	if(sHttpSession != null) {
       		sHttpSession.invalidate();
       	}
    }

	//--------------------------------------------------------------------------
    
    public void loginAs(JeevesUser user) {
        SecurityContextImpl secContext = new SecurityContextImpl();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null);
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);
    }
    
	public boolean isAuthenticated() {
		return !(auth() instanceof AnonymousAuthenticationToken);
	}

	//--------------------------------------------------------------------------

	public String getUserId() { 
		JeevesUser userDetails = getPrincipal();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getId();
		}
	}
	public String getUsername() {
		JeevesUser userDetails = getPrincipal();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getUsername();
		}
	}
	public String getName() {
		JeevesUser userDetails = getPrincipal();
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
		JeevesUser userDetails = getPrincipal();
		if (userDetails == null) {
			return null;   
		} else {
			return userDetails.getProfile();
		}
	}
	public String getEmailAddr() {
		JeevesUser userDetails = getPrincipal();
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
	public JeevesUser getPrincipal() {
		Authentication auth = auth();
		if (auth != null && auth.getPrincipal() instanceof JeevesUser) {
			return (JeevesUser) auth.getPrincipal(); 
		}
		return null;
	}

}

//=============================================================================