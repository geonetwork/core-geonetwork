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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;

//=============================================================================
/** Embedded Servlet Container lib */

public class EmbeddedSCLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public EmbeddedSCLib(String appPath) throws Exception
	{
		
		this.appPath = appPath;

		String jetty  = appPath + JETTY_FILE;
		webXml = Lib.xml.load(appPath + WEBXML_FILE);

		//--- retrieve 'host', 'port' and 'servlet' parameters from jetty/web.xml

		System.out.println("Loading "+jetty);
		XmlConfiguration config = new XmlConfiguration(Resource.newResource(jetty).getURL());
		Server server = new Server();
		config.configure(server);
		Connector[] conns = server.getConnectors();
		//--- assume connector we want is the first one?
		if (conns.length == 1) {
			host = conns[0].getHost();
			System.out.println("Jetty on Host: "+host);
			port = conns[0].getPort()+"";
			System.out.println("Jetty on Port: "+port);
		} else {
			throw new Exception("Confusion: more than one connector in "+jetty);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()
	{
		return host;
	}

	//---------------------------------------------------------------------------

	public String getPort()
	{
		return port;
	}

	//---------------------------------------------------------------------------

	public String getServlet()
	{
		return "geonetwork";
	}

	//---------------------------------------------------------------------------

	public void setHost(String host)
	{
		// Disabled for Jetty 6.x
	}

	//---------------------------------------------------------------------------

	public void setPort(String port)
	{
		// Disabled for Jetty 6.x
	}

	//---------------------------------------------------------------------------

	public void setServlet(String name)
	{
		// Disabled for Jetty 6.x
		//             if (servletElem != null)
		//                     servletElem.setText("/"+name);
		//
		//             for (Object e : webXml.getRootElement().getChildren())
		//             {
		//                     Element elem = (Element) e;
		//
		//                     if (elem.getName().equals("display-name"))
		//                     {
		//                             elem.setText(name);
		//                             return;
		//                     }
		//             }
		//
	}

	//---------------------------------------------------------------------------

	public void save() throws FileNotFoundException, IOException
	{
		Lib.xml.save(appPath + WEBXML_FILE, webXml);

		//--- create proper index.html file to point to correct servlet

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("$SERVLET", getServlet());

		List<String> lines = Lib.text.load(appPath + INDEX_SRC_FILE);
		Lib.text.replace(lines, vars);
		Lib.text.save(appPath + INDEX_DES_FILE, lines);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String   appPath;
	private String   host;
	private String   port;
	private Document webXml;

	private static final String JETTY_FILE  = "/bin/jetty.xml";
	private static final String WEBXML_FILE = "/web/geonetwork/WEB-INF/web.xml";

	private static final String INDEX_SRC_FILE = "/gast/data/index.html";
	private static final String INDEX_DES_FILE = "/web/geonetwork/index.html";
}

//=============================================================================

