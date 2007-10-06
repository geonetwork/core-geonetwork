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

package org.fao.gast.gui.panels.migration.oldinst;

import java.io.File;
import java.io.IOException;
import jeeves.constants.ConfigFile;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

//=============================================================================

public class OldConfigLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public OldConfigLib(String appPath) throws JDOMException, IOException
	{
		webPath = appPath +"/web/";

		String cfgFile = "/web/WEB-INF/config.xml";

		if (!new File(appPath + cfgFile).exists())
		{
			webPath = appPath +"/";
			cfgFile = "/WEB-INF/config.xml";
		}

		config      = Lib.xml.load(appPath + cfgFile);
		dbmsElem    = retrieveDbms(config);
		appHandElem = config.getRootElement().getChild(ConfigFile.Child.APP_HANDLER);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHandlerProp(String name)
	{
		return findInHandler(name);
	}

	//---------------------------------------------------------------------------

	public Resource createResource() throws Exception
	{
		return new Resource(webPath, dbmsElem);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element retrieveDbms(Document config)
	{
		Element resources = config.getRootElement().getChild(ConfigFile.Child.RESOURCES);

		for (Object res : resources.getChildren(ConfigFile.Resources.Child.RESOURCE))
		{
			Element resource = (Element) res;
			String  enabled  = resource.getAttributeValue("enabled");

			if ("true".equals(enabled))
				return resource;
		}

		//--- we should not arrive here

		return null;
	}

	//---------------------------------------------------------------------------

	private String findInHandler(String paramName)
	{
		for (Object o : appHandElem.getChildren("param"))
		{
			Element param = (Element) o;

			String name = param.getAttributeValue("name");
			String value= param.getAttributeValue("value");

			if (paramName.equals(name))
				return value;
		}

		return null;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

//	private String   appPath;
	private String   webPath;
	private Document config;
	private Element  dbmsElem;
	private Element  appHandElem;
}

//=============================================================================


