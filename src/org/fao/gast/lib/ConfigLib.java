//=============================================================================
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

package org.fao.gast.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import jeeves.constants.ConfigFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

//=============================================================================

public class ConfigLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ConfigLib(String appPath) throws JDOMException, IOException
	{
		this.appPath = appPath;

		config   = Lib.xml.load(appPath +"/web/WEB-INF/config.xml");
		dbmsElem = retrieveDbms(config);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getDbmsURL()
	{
		return dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChildText("url");
	}

	//---------------------------------------------------------------------------

	public String getDbmsDriver()
	{
		return dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChildText("driver");
	}

	//---------------------------------------------------------------------------

	public String getDbmsUser()
	{
		return dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChildText("user");
	}
	//---------------------------------------------------------------------------

	public String getDbmsPassword()
	{
		return dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChildText("password");
	}

	//---------------------------------------------------------------------------
	//--- Setters
	//---------------------------------------------------------------------------

	public void setDbmsURL(String url)
	{
		dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChild("url").setText(url);
	}

	//---------------------------------------------------------------------------

	public void setDbmsDriver(String driver)
	{
		dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChild("driver").setText(driver);
	}

	//---------------------------------------------------------------------------

	public void setDbmsUser(String user)
	{
		dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChild("user").setText(user);
	}

	//---------------------------------------------------------------------------

	public void setDbmsPassword(String password)
	{
		dbmsElem.getChild(ConfigFile.Resource.Child.CONFIG).getChild("password").setText(password);
	}

	//---------------------------------------------------------------------------


	//---------------------------------------------------------------------------
	//--- Other
	//---------------------------------------------------------------------------

	public void addActivator()
	{
		removeActivator();

		Element activ = new Element(ConfigFile.Resource.Child.ACTIVATOR);
		activ.setAttribute("class", "org.fao.geonet.activators.McKoiActivator");

		Element config = new Element("configFile");
		config.setText("WEB-INF/db/db.conf");

		activ   .addContent(config);
		dbmsElem.addContent(activ);
	}

	//---------------------------------------------------------------------------

	public void removeActivator()
	{
		dbmsElem.removeChild(ConfigFile.Resource.Child.ACTIVATOR);
	}

	//---------------------------------------------------------------------------

	public void save() throws FileNotFoundException, IOException
	{
		Lib.xml.save(appPath +"/web/WEB-INF/config.xml", config);
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
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String   appPath;
	private Document config;
	private Element  dbmsElem;
}

//=============================================================================

