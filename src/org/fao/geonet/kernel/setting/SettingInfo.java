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

package org.fao.geonet.kernel.setting;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

//=============================================================================

public class SettingInfo
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public SettingInfo(ServiceContext context)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		sm = gc.getSettingManager();
	}

	//---------------------------------------------------------------------------

	public SettingInfo(SettingManager sm)
	{
		this.sm = sm;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getSiteName()
	{
		return sm.getValue("system/site/name");
	}

	//---------------------------------------------------------------------------
	/** Return a string like 'http://HOST[:PORT]' */

	public String getSiteUrl()
	{
		String host = sm.getValue("system/server/host");
		String port = sm.getValue("system/server/port");

		StringBuffer sb = new StringBuffer("http://");

		sb.append(host);

		if (port.length() != 0)
			sb.append(":"+ port);

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	public String getFeedbackEmail()
	{
		return sm.getValue("system/feedback/email");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//---------------------------------------------------------------------------

	private SettingManager sm;
}

//=============================================================================

