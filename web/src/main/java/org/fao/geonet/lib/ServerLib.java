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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import jeeves.server.ConfigurationOverrides;

//=============================================================================

public class ServerLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ServerLib(ServletContext servletContext, String appPath) throws IOException
	{
		this.appPath = appPath;

		serverProps = new Properties();
		
		InputStream stream = servletContext.getResourceAsStream(SERVER_PROPS);
		if (stream == null) {
			stream = new FileInputStream(appPath + SERVER_PROPS);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		
		try {
			List<String> lines = ConfigurationOverrides.loadTextFileAndUpdate(
                    SERVER_PROPS, servletContext, appPath, reader);
			StringBuilder b = new StringBuilder();
			for (String string : lines) {
				b.append(string);
				b.append("\n");
			}
			serverProps.load(new StringReader(b.toString()));
		} finally {
			reader.close();
		}

	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getAppPath()    { return appPath; }
	public String getVersion()    { return serverProps.getProperty("version",    "???"); }
	public String getSubVersion() { return serverProps.getProperty("subVersion", "???"); }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String     appPath;
	private Properties serverProps;

	private static final String SERVER_PROPS = File.separator + "WEB-INF" + File.separator + "server.prop";
}

//=============================================================================

