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

package org.fao.geonet.csw.common.util;

import java.net.URL;
import java.util.ArrayList;
import org.fao.geonet.csw.common.Csw;
import org.jdom.Element;
import org.jdom.Namespace;
import java.util.Map;
import java.util.HashMap;
import java.net.MalformedURLException;

//=============================================================================

public class CswServer
{
	public static final String GET_RECORDS      = "GetRecords";
	public static final String GET_RECORD_BY_ID = "GetRecordById";

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public CswServer(Element capab)
	{
		parse(capab);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void parse(Element capab)
	{
		logs      .clear();
		operations.clear();

		parseOperations(capab);
	}

	//---------------------------------------------------------------------------

	public Operation getOperation(String name) { return operations.get(name); }

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void parseOperations(Element capabil)
	{
		Element operMd = capabil.getChild("OperationsMetadata", Csw.NAMESPACE_OWS);

		if (operMd == null)
			log("Missing 'ows:OperationsMetadata' element");

		else
			for(Object e : operMd.getChildren())
			{
				Element elem = (Element) e;

				if ("Operation".equals(elem.getName()))
				{
					Operation oper = extractOperation(elem);

					if (oper != null)
						operations.put(oper.name, oper);
				}
			}
	}

	//---------------------------------------------------------------------------

	private Operation extractOperation(Element oper)
	{
		String name = oper.getAttributeValue("name");

		if (name == null)
		{
			log("Operation has no 'name' attribute");
			return null;
		}

		Element dcp = oper.getChild("DCP", Csw.NAMESPACE_OWS);

		if (dcp == null)
		{
			log("Missing 'ows:DCP' element in operation");
			return null;
		}

		Element http = dcp.getChild("HTTP", Csw.NAMESPACE_OWS);

		if (http == null)
		{
			log("Missing 'ows:HTTP' element in operation/DCP");
			return null;
		}

		Element get  = http.getChild("Get",  Csw.NAMESPACE_OWS);
		Element post = http.getChild("Post", Csw.NAMESPACE_OWS);

		Operation op = new Operation();
		op.name   = name;
		op.getUrl = evaluateUrl(get);
		op.postUrl= evaluateUrl(post);

		return op;
	}

	//---------------------------------------------------------------------------

	private URL evaluateUrl(Element method)
	{
		if (method == null)
			return null;

		Namespace ns = Namespace.getNamespace("http://www.w3.org/1999/xlink");

		String url = method.getAttributeValue("href", ns);

		if (url == null)
		{
			log("Missing 'xlink:href' attribute in operation's http method");
			return null;
		}

		try
		{
			return new URL(url);
		}
		catch (MalformedURLException e)
		{
			log("Malformed 'xlink:href' attribute in operation's http method");
			return null;
		}
	}

	//---------------------------------------------------------------------------

	private void log(String message)
	{
		logs.add(message);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Map<String, Operation> operations = new HashMap<String, Operation>();

	private ArrayList<String> logs = new ArrayList<String>();

	//---------------------------------------------------------------------------

	public static class Operation
	{
		public String name;
		public URL    getUrl;
		public URL    postUrl;
	}
}

//=============================================================================


