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

package jeeves.services.session;

import java.util.*;
import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.constants.*;

//=============================================================================

/** Stores input fields in the session
  */

public class Put implements Service
{
	String  groupName;
	HashSet inFields;
	
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		groupName = params.getMandatoryValue(Jeeves.Config.GROUP);
		Iterator i = params.getChildren(Jeeves.Config.IN_FIELDS, Jeeves.Config.FIELD);
		if (i != null)
		{
			inFields = new HashSet();
			while (i.hasNext())
			{
				Element field = (Element)i.next();
				inFields.add(field.getName());
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
		
		Hashtable group = (Hashtable)session.getProperty(groupName);
		if (group == null) group = new Hashtable();
		for (Iterator i = params.getChildren().iterator(); i.hasNext(); )
		{
			Element child = (Element)i.next();
			if (inFields == null || inFields.contains(child.getName()))
				 group.put(child.getName(), child);
		}
		session.setProperty(groupName, group);
		
		return params;
	}
}

//=============================================================================

