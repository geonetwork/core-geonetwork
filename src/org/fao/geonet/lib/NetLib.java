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

package org.fao.geonet.lib;

import java.net.URL;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import java.net.MalformedURLException;

//=============================================================================

public class NetLib
{
	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------

	public void setupProxy(ServiceContext context, XmlRequest req)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		setupProxy(sm, req);
	}

	//-----------------------------------------------------------------------------
	/** Setup proxy
	  */

	public void setupProxy(SettingManager sm, XmlRequest req)
	{
		boolean enabled = sm.getValueAsBool("system/proxy/use", false);
		String  host    = sm.getValue("system/proxy/host");
		String  port    = sm.getValue("system/proxy/port");
		String  username= sm.getValue("system/proxy/username");
		String  password= sm.getValue("system/proxy/password");

		if (!enabled)
			req.setUseProxy(false);
		else
		{
			if (!Lib.type.isInteger(port))
				Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : "+ port);
			else
			{
	            Log.debug(Geonet.GEONETWORK, "Proxy in use with host " + host + 
	                    " port " + port + " and username : "+ username);
				req.setUseProxy(true);
				req.setProxyHost(host);
				req.setProxyPort(Integer.parseInt(port));
				req.setProxyCredentials(username, password);
			}
		}
	}

	//-----------------------------------------------------------------------------

	public boolean isUrlValid(String url)
	{
		try
		{
			new URL(url);
			return true;
		}
		catch (MalformedURLException e)
		{
			return false;
		}
	}
}

//=============================================================================

