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

package org.fao.gast.gui.panels.migration.oldinst;

import java.io.IOException;
import org.jdom.JDOMException;

//==============================================================================

public class GNSource
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GNSource(String oldAppPath) throws JDOMException, IOException
	{
		config  = new OldConfigLib(oldAppPath);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API Methods
	//---
	//---------------------------------------------------------------------------

	public String getSiteId()     { return config.getHandlerProp("siteId");     }
	public String getNetwork()    { return config.getHandlerProp("network");    }
	public String getNetmask()    { return config.getHandlerProp("netmask");    }
	public String getPublicHost() { return config.getHandlerProp("publicHost"); }
	public String getPublicPort() { return config.getHandlerProp("publicPort"); }
	public String getZ3950Port()  { return config.getHandlerProp("z3950Port");  }

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	public OldConfigLib config;
}

//==============================================================================


