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
import java.net.URL;
import java.util.Iterator;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.exceptions.JeevesException;
import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.lib.Lib;
import org.fao.oaipmh.exceptions.NoSetHierarchyException;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.requests.ListMetadataFormatsRequest;
import org.fao.oaipmh.requests.ListSetsRequest;
import org.fao.oaipmh.responses.ListMetadataFormatsResponse;
import org.fao.oaipmh.responses.ListSetsResponse;
import org.fao.oaipmh.responses.MetadataFormat;
import org.fao.oaipmh.responses.SetInfo;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

//=============================================================================

public class Info implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		iconPath = new File(appPath +"/images/harvesting");
		oaiSchema= new File(appPath +"/xml/validation/oai/OAI-PMH.xsd");
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

			if (type.equals("icons"))
				result.addContent(getIcons());

			else if (type.equals("oaiPmhServer"))
				result.addContent(getOaiPmhServer(el));

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

	private Element getIcons()
	{
		File icons[] = iconPath.listFiles(iconFilter);

		Element result = new Element("icons");

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
	//--- OaiPmhServer
	//--------------------------------------------------------------------------

	private Element getOaiPmhServer(Element el) throws BadInputEx
	{
		String url = el.getAttributeValue("url");

		if (url == null)
			throw new MissingParameterEx("attribute:url", el);

		if (!Lib.net.isUrlValid(url))
			throw new BadParameterEx("attribute:url", el);

		Element res = new Element("oaiPmhServer");

		try
		{
			res.addContent(getMdFormats(url));
			res.addContent(getSets(url));
		}
		catch(JDOMException e)
		{
			res.setContent(JeevesException.toElement(new BadXmlResponseEx(e.getMessage())));
		}
		catch(SAXException e)
		{
			res.setContent(JeevesException.toElement(new BadXmlResponseEx(e.getMessage())));
		}
		catch(OaiPmhException e)
		{
			res.setContent(org.fao.geonet.kernel.oaipmh.Lib.toJeevesException(e));
		}
		catch(Exception e)
		{
			res.setContent(JeevesException.toElement(e));
		}

		return res;
	}

	//--------------------------------------------------------------------------

	private Element getMdFormats(String url) throws Exception
	{
		ListMetadataFormatsRequest req = new ListMetadataFormatsRequest();
		req.setValidationSchema(oaiSchema);
		req.getTransport().setUrl(new URL(url));
		ListMetadataFormatsResponse res = req.execute();

		//--- build response

		Element root = new Element("formats");

		for (MetadataFormat mf : res.getFormats())
			root.addContent(new Element("format").setText(mf.prefix));

		return root;
	}

	//--------------------------------------------------------------------------

	private Element getSets(String url) throws Exception
	{
		Element root = new Element("sets");

		try
		{
			ListSetsRequest req = new ListSetsRequest();
			req.setValidationSchema(oaiSchema);
			req.getTransport().setUrl(new URL(url));
			ListSetsResponse res = req.execute();

			//--- build response

			while (res.hasNext())
			{
				SetInfo si = res.next();

				Element el = new Element("set");

				el.addContent(new Element("name") .setText(si.getSpec()));
				el.addContent(new Element("label").setText(si.getName()));

				root.addContent(el);
			}
		}
		catch(NoSetHierarchyException e)
		{
			//--- if the server does not support sets, simply returns an empty set
		}

		return root;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private File iconPath;
	private File oaiSchema;


	private static final String iconExt[] = { ".gif", ".png", ".jpg", ".jpeg" };
}

//=============================================================================

