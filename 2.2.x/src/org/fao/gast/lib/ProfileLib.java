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

package org.fao.gast.lib;

import java.io.IOException;
import java.util.List;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

//=============================================================================

public class ProfileLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ProfileLib(String appPath) throws JDOMException, IOException
	{
		this.appPath = appPath;

		profiles = Xml.loadFile(appPath +"/web/geonetwork/xml/user-profiles.xml");
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public boolean existsProfile(String name)
	{
		for(Object p : profiles.getChildren())
		{
			Element profile = (Element) p;

			if (name.equals(profile.getAttributeValue("name")))
				return true;
		}

		return false;
	}

	//---------------------------------------------------------------------------

	public String getDefaultProfile()
	{
		List list = profiles.getChildren();

		Element last = (Element) list.get(list.size() -1);

		return last.getAttributeValue("name");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String  appPath;
	private Element profiles;
}

//=============================================================================


