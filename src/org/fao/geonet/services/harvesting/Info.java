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

package org.fao.geonet.services.harvesting;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;

//=============================================================================

public class Info implements Service
{
	private File cswIconPath;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		cswIconPath = new File(appPath +"/images/csw");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		Element result = new Element("root");

		for (Iterator i=params.getChildren().iterator(); i.hasNext();)
		{
			Element el = (Element) i.next();

			String name = el.getName();
			String type = el.getText();

			if (!name.equals("type"))
				throw new BadParameterEx(name, type);

			if (type.equals("cswIcons"))
				result.addContent(getCswIcons());

			else
				throw new BadParameterEx("type", type);
		}

		return result;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private Element getCswIcons()
	{
		File icons[] = cswIconPath.listFiles(iconFilter);

		Element result = new Element("cswIcons");

		if (icons != null)
			for (File icon : icons)
				result.addContent(new Element("icon").setText(icon.getName()));

		return result;
	}

	//--------------------------------------------------------------------------

	private FileFilter iconFilter = new FileFilter()
	{
		public boolean accept(File icon)
		{
			if (!icon.isFile())
				return false;

			String name = icon.getName();

			for (String ext : iconExt)
				if (name.endsWith(ext))
					return true;

			return false;
		}
	};

	//--------------------------------------------------------------------------

	private static final String iconExt[] = { ".gif", ".png", ".jpg", ".jpeg" };
}

//=============================================================================

