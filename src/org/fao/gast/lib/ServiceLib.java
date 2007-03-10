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

import jeeves.utils.Xml;
import org.jdom.Element;

//=============================================================================

public class ServiceLib
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void checkError(Element response) throws Exception
	{
		if (!response.getName().equals("error"))
			return;

		System.out.println("*** Error is:\n"+ Xml.getString(response));

		String id = response.getAttributeValue("id");
		String msg= response.getChildText("message");

		if (id.equals("service-not-allowed"))
			throw new Exception("You need to authenticate");

		if (id.equals("user-login"))
			throw new Exception("Invalid username/password");

		throw new Exception(msg);
	}
}

//=============================================================================

