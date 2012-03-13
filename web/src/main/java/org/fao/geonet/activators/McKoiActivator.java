//==============================================================================
//===
//===   McKoiActivator
//===
//==============================================================================
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

package org.fao.geonet.activators;

import jeeves.interfaces.Activator;
import org.fao.geonet.util.McKoiDB;
import org.jdom.Element;

//==============================================================================

/**
 * TODO javadoc.
 */
public class McKoiActivator implements Activator {
	private McKoiDB mckoiDB = new McKoiDB();

	//---------------------------------------------------------------------------
	//---
	//--- Activator interface
	//---
	//---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param appPath
     * @param config
     * @throws Exception
     */
	public void startup(String appPath, Element config) throws Exception {
		String configFile = config.getChildText("configFile");
		String address    = config.getChildText("address");

		mckoiDB.setConfigFile(appPath + configFile);

		if (address != null)	mckoiDB.start(address);
			else 					mckoiDB.start();
	}

	//---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     */
	public void shutdown()
	{
		mckoiDB.stop();
	}
}

//==============================================================================

