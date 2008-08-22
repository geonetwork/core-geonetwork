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

package org.fao.geonet.csw.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.jdom.Element;
import org.jdom.Namespace;

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

	public CswOperation getOperation(String name) { return operations.get(name); }

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
					CswOperation oper = extractOperation(elem);

					if (oper != null)
						operations.put(oper.name, oper);
				}
			}
	}

	//---------------------------------------------------------------------------

	private CswOperation extractOperation(Element oper)
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

		List<Element> parameters = oper.getChildren("Parameter", Csw.NAMESPACE_OWS);
		log("Found " + parameters.size() + " parameters for operation: " + name);
		List<Element> outputSchemas = null ;
		for(Iterator<Element> i = parameters.iterator();i.hasNext();) {
			Element parameter = i.next();
			String parameterName = parameter.getAttributeValue("name"); 
			log("Processing parameter: " + parameterName);
			if(parameterName != null && parameterName.equals("OutputSchema")) {
				Element outputSchemaListing = parameter;
				outputSchemas = outputSchemaListing.getChildren("Value", Csw.NAMESPACE_OWS);
				log("Found " + outputSchemas.size() + " OutputSchemas for operation: " + name);
			}
		}
		
		CswOperation op = new CswOperation();
		op.name   = name;
		op.getUrl = evaluateUrl(get);
		op.postUrl= evaluateUrl(post);
		if(outputSchemas != null) {
			for(Iterator<Element> i = outputSchemas.iterator(); i.hasNext();) {
				Element outputSchema = i.next();
				String outputSchemaValue = outputSchema.getValue(); 
				log("Adding OutputSchema: " + outputSchemaValue + " to operation: "+ name);
				op.outputSchemaList.add(outputSchemaValue);				
			}
			op.choosePreferredOutputSchema();
		}
		else {
			log("No OutputSchema for operation: " + name);
		}

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

	private Map<String, CswOperation> operations = new HashMap<String, CswOperation>();

	private List<String> logs = new ArrayList<String>();

	//---------------------------------------------------------------------------
}

//=============================================================================


