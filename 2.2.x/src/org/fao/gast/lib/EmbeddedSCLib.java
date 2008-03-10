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

//=============================================================================
/** Embedde Servlet Container lib */

public class EmbeddedSCLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public EmbeddedSCLib(String appPath) throws JDOMException, IOException
	{
		this.appPath = appPath;

		jetty  = Lib.xml.load(appPath + JETTY_FILE);
		webXml = Lib.xml.load(appPath + WEBXML_FILE);

		//--- retrieve 'host', 'port' and 'servlet' parameters from jetty

		for (Object call : jetty.getRootElement().getChildren("Call"))
		{
			Element elCall = (Element) call;

			if ("addListener".equals(elCall.getAttributeValue("name")))
			{
				Element elNew = elCall.getChild("Arg").getChild("New");

				for (Object set : elNew.getChildren("Set"))
				{
					Element elSet = (Element) set;

					if ("host".equals(elSet.getAttributeValue("name")))
						hostElem = elSet;

					else if ("port".equals(elSet.getAttributeValue("name")))
						portElem = elSet;
				}
			}

			else if ("addWebApplication".equals(elCall.getAttributeValue("name")))
			{
				if (servletElem == null)
					servletElem = elCall.getChild("Arg");
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()
	{
		return (hostElem == null) ? null : hostElem.getText();
	}

	//---------------------------------------------------------------------------

	public String getPort()
	{
		return (portElem == null) ? null : portElem.getText();
	}

	//---------------------------------------------------------------------------

	public String getServlet()
	{
		//--- we have to skip the initial '/'
		return (servletElem == null) ? null : servletElem.getText().substring(1);
	}

	//---------------------------------------------------------------------------

	public void setHost(String host)
	{
	}

	//---------------------------------------------------------------------------

	public void setPort(String port)
	{
		if (portElem != null)
			portElem.setText(port);
	}

	//---------------------------------------------------------------------------

	public void setServlet(String name)
	{
		if (servletElem != null)
			servletElem.setText("/"+name);

		for (Object e : webXml.getRootElement().getChildren())
		{
			Element elem = (Element) e;

			if (elem.getName().equals("display-name"))
			{
				elem.setText(name);
				return;
			}
		}
	}

	//---------------------------------------------------------------------------

	public void save() throws FileNotFoundException, IOException
	{
		Lib.xml.save(appPath + JETTY_FILE,  jetty);
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
	private Document jetty;
	private Element  hostElem;
	private Element  portElem;
	private Element  servletElem;
	private Document webXml;

	private static final String JETTY_FILE  = "/bin/jetty.xml";
	private static final String WEBXML_FILE = "/web/geonetwork/WEB-INF/web.xml";

	private static final String INDEX_SRC_FILE = "/gast/data/index.html";
	private static final String INDEX_DES_FILE = "/web/geonetwork/index.html";
}

//=============================================================================

