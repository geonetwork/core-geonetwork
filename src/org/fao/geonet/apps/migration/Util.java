//==============================================================================
//===
//===   Util
//===
//==============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.apps.migration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Activator;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;
import org.fao.geonet.apps.common.SimpleLogger;
import org.jdom.Element;

//==============================================================================

public class Util
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public static Element getConfigFile(SimpleLogger logger, String file) throws Exception
	{
		try
		{
			return Xml.loadFile(file);
		}
		catch (Exception e)
		{
			logger.logError("Cannot open config file");
			logger.logError("Error is : "+ e.getMessage());

			throw new Exception("error");
		}
	}

	//---------------------------------------------------------------------------

	public static Element getUserProfiles(SimpleLogger logger, String file) throws Exception
	{
		try
		{
			return Xml.loadFile(file);
		}
		catch (Exception e)
		{
			logger.logError("Cannot open user profiles");
			logger.logError("Error is : "+ e.getMessage());

			throw new Exception("error");
		}
	}

	//---------------------------------------------------------------------------

	public static Element getDBResource(Element config) throws Exception
	{
		List resources = config .getChild(ConfigFile.Child.RESOURCES)
										.getChildren(ConfigFile.Resources.Child.RESOURCE);

		for(int i=0; i<resources.size(); i++)
		{
			Element res = (Element) resources.get(i);

			if ("true".equals(res.getAttributeValue("enabled")))
				if ("main-db".equals(res.getChildText("name")))
					return res;
		}

		throw new Exception("error");
	}
}

//==============================================================================

