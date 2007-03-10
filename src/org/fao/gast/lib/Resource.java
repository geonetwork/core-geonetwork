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

import jeeves.constants.ConfigFile;
import jeeves.interfaces.Activator;
import jeeves.server.resources.ProviderManager;
import jeeves.server.resources.ResourceManager;
import org.jdom.Element;

//=============================================================================

public class Resource
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Resource(String appPath, Element resource) throws Exception
	{
		        name      = resource.getChildText(ConfigFile.Resource.Child.NAME);
		String  provider  = resource.getChildText(ConfigFile.Resource.Child.PROVIDER);
		Element config    = resource.getChild    (ConfigFile.Resource.Child.CONFIG);
		Element activator = resource.getChild    (ConfigFile.Resource.Child.ACTIVATOR);

		if (activator != null)
		{
			String clas = activator.getAttributeValue(ConfigFile.Activator.Attr.CLASS);

			activ = (Activator) Class.forName(clas).newInstance();
			activ.startup(appPath, activator);
		}

		try
		{
			provMan.register(provider, name, config);
		}
		catch (Exception e)
		{
			//--- in case of error we have to stop the activator

			provMan.end();

			if (activ != null)
				activ.shutdown();

			throw e;
		}

		resMan = new ResourceManager(provMan);
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public Object open() throws Exception
	{
		return resMan.open(name);
	}

	//--------------------------------------------------------------------------

	public void close()
	{
		try
		{
			resMan.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();

			try
			{
				resMan.abort();
			}
			catch (Exception ex)
			{
				e.printStackTrace();
			}
		}

		provMan.end();

		if (activ != null)
			activ.shutdown();
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String          name;
	private Activator       activ;
	private ResourceManager resMan;
	private ProviderManager provMan = new ProviderManager();
}

//=============================================================================


