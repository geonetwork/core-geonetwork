//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.login;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;

//=============================================================================

class LDAPContext
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public LDAPContext(SettingManager sm)
	{
		String prefix = "system/ldap";

		use           = sm.getValueAsBool(prefix +"/use");
		host          = sm.getValue      (prefix +"/host");
		port          = sm.getValueAsInt (prefix +"/port");
		defProfile    = sm.getValue      (prefix +"/defaultProfile");
		baseDN        = sm.getValue      (prefix +"/distinguishedNames/base");
		usersDN       = sm.getValue      (prefix +"/distinguishedNames/users");
		nameAttr      = sm.getValue      (prefix +"/userAttribs/name");
		profileAttr   = sm.getValue      (prefix +"/userAttribs/profile");

		if (profileAttr.trim().length() == 0)
			profileAttr = null;

		//--- init set of allowed profiles

		profiles.add("Reviewer");
		profiles.add("Editor");
		profiles.add("RegisteredUser");
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public boolean isInUse() { return use; }

	//--------------------------------------------------------------------------

	public LDAPInfo lookUp(String username, String password)
	{
		try
		{
			String     path = "uid="+ username +","+ usersDN +","+ baseDN;
			DirContext dc   = LDAPUtil.openContext(getUrl(), path, password);

			Map<String, ? extends List<Object>> attr = LDAPUtil.getNodeInfo(dc, path);
			dc.close();

			if (attr == null)
			{
				Log.info(Geonet.LDAP, "Username not found :"+ username);
				return null;
			}
			else
			{
				LDAPInfo info = new LDAPInfo();

				info.username = username;
				info.password = password;
				info.name     = get(attr, nameAttr);
				info.profile  = (profileAttr == null)
										? defProfile
										: get(attr, profileAttr);

				if (!profiles.contains(info.profile))
				{
					Log.info(Geonet.LDAP, "Skipping user with unknown profile");
					Log.info(Geonet.LDAP, "  (C) Username :"+ info.username);
					Log.info(Geonet.LDAP, "  (C) Profile  :"+ info.profile);
					return null;
				}

				return info;
			}
		}
		catch(NamingException e)
		{
			Log.info(Geonet.LDAP, "Raised exception during LDAP access");
			Log.info(Geonet.LDAP, "  (C) Message :"+ e.getMessage());
			return null;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private String getUrl()
	{
		return "ldap://"+ host +":" + ((port != null) ? port : "389");
	}

	//--------------------------------------------------------------------------

	private String get(Map<String, ? extends List<Object>> attr, String name)
	{
		List<Object> values = attr.get(name);

		if (values == null)
		{
			Log.debug(Geonet.LDAP, "Attribute '"+ name +"' does not exist");
			return null;
		}

		Object obj = values.get(0);

		if (obj != null)
			Log.debug(Geonet.LDAP, "Attribute '"+ name +"' is of type : "+obj.getClass().getSimpleName());
		else
			Log.debug(Geonet.LDAP, "Attribute '"+ name +"' is null");

		return (obj == null) ? null : obj.toString();
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private boolean use;
	private String  host;
	private Integer port;
	private String  defProfile;
	private String  baseDN;
	private String  usersDN;
	private String  nameAttr;
	private String  profileAttr;

	private HashSet<String> profiles = new HashSet<String>();
}

//=============================================================================

class LDAPInfo
{
	public String username;
	public String password;
	public String profile;
	public String name;
}

//=============================================================================

