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

import javax.servlet.http.HttpSession;
import java.util.Hashtable;

//=============================================================================

/**
 * Abstraction layer from the user session.
 */
public class UserSession
{
	private Hashtable<String, Object> htProperties = new Hashtable<String, Object>(10, .75f);

	private String  sUserId;
	private String  sUsername;
	private String  sName;
	private String  sSurname;
	private String  sProfile;
	private String  sEmailAddr;
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
        authenticate(null, null, null, null, null, null);

        htProperties = new Hashtable(10, .75f);
    }

	//--------------------------------------------------------------------------
	/**
     * Says that the user is authenticated and this id and profile must be used.
	 */
	public void authenticate(String userId, String username, String name, String surname, String profile, String emailAddr)
	{
		sUserId    = userId;
		sUsername  = username;
		sName      = name;
		sSurname   = surname;
		sProfile   = profile;
		sEmailAddr = emailAddr;
	}

	//--------------------------------------------------------------------------

	public boolean isAuthenticated()
	{
		return sUserId != null;
	}

	//--------------------------------------------------------------------------

	public String getUserId()    { return sUserId;   }
	public String getUsername()  { return sUsername; }
	public String getName()      { return sName;     }
	public String getSurname()   { return sSurname;  }
	public String getProfile()   { return sProfile;  }
	public String getEmailAddr() { return sEmailAddr;  }

}

//=============================================================================
