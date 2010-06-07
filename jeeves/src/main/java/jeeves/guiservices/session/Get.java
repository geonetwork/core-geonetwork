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

package jeeves.guiservices.session;

import java.util.*;
import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.constants.*;

//=============================================================================

/** Service used to return information about the user
  */

public class Get implements Service
{
	String  groupName;
	HashSet outFields;
	
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		groupName = params.getValue(Jeeves.Config.GROUP);
		Iterator i = params.getChildren(Jeeves.Config.OUT_FIELDS, Jeeves.Config.FIELD);
		if (i != null)
		{
			outFields = new HashSet();
			while (i.hasNext())
			{
				Element field = (Element)i.next();
				outFields.add(field.getName());
			}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		UserSession session = context.getUserSession();

		String sUsername = session.getUsername();
		String sName     = session.getName();
		String sSurname  = session.getSurname();
		String sProfile  = session.getProfile();

		if (sUsername == null)
			sUsername = ProfileManager.GUEST;

		if (sName == null)
			sName = ProfileManager.GUEST;

		if (sSurname == null)
			sSurname = "";

		if (sProfile == null)
			sProfile = ProfileManager.GUEST;

		Element userId   = new Element("userId")  .addContent(session.getUserId());
		Element username = new Element("username").addContent(sUsername);
		Element name     = new Element("name")    .addContent(sName);
		Element surname  = new Element("surname") .addContent(sSurname);
		Element profile  = new Element("profile") .addContent(sProfile);

		Element sEl = new Element(Jeeves.Elem.SESSION)
			.addContent(userId)
			.addContent(username)
			.addContent(name)
			.addContent(surname)
			.addContent(profile);

		if (groupName != null)
		{
			Hashtable group = (Hashtable)session.getProperty(groupName);
			if (group != null)
			{
				Element gEl = new Element(groupName);
				for (Enumeration i = group.elements(); i.hasMoreElements();)
				{
					Element child = (Element)i.nextElement();
					if (outFields == null || outFields.contains(child.getName()))
						 gEl.addContent((Element)child.clone());
				}
				sEl.addContent(gEl);
			}
		}
		return sEl;
	}
}

//=============================================================================

