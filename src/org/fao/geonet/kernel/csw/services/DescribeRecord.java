//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.csw.services;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.jdom.Element;
import org.jdom.JDOMException;

//=============================================================================

public class DescribeRecord extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public DescribeRecord() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "DescribeRecord"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException
	{
		checkService(request);
		checkVersion(request);

		String outputFormat   = request.getAttributeValue("outputFormat");
		String schemaLanguage = request.getAttributeValue("schemaLanguage");

		if (outputFormat != null && !outputFormat.equals("application/xml"))
			throw new InvalidParameterValueEx("outputFormat", outputFormat);

		if (schemaLanguage != null && !schemaLanguage.equals(Csw.SCHEMA_LANGUAGE))
			throw new InvalidParameterValueEx("schemaLanguage", schemaLanguage);

		//--- build output

		Element response = new Element(getName() +"Response", Csw.NAMESPACE_CSW);

		Iterator i = request.getChildren("TypeName", Csw.NAMESPACE_CSW).iterator();

		if (!i.hasNext())
		{
//			response.addContent(getSchemaComponent(context, "dataset"));
//			response.addContent(getSchemaComponent(context, "service"));
			response.addContent(getSchemaComponent(context, null));
		}
		else while(i.hasNext())
		{
			Element elem = (Element) i.next();
			String  typeNS   = elem.getAttributeValue("targetNamespace");
			String  typeName = elem.getText();
			Element schema   = getSchemaComponent(context, typeName);

			if (typeNS == null)
				throw new MissingParameterValueEx("targetNamespace");

			if (schema != null)
				response.addContent(schema);
		}

		return response;
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params) throws CatalogException
	{
		String service      = params.get("service");
		String version      = params.get("version");
		String outputFormat = params.get("outputformat");
		String schemaLang   = params.get("schemalanguage");
		String typeNames    = params.get("typename");
		String namespace    = params.get("namespace");

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service",        service);
		setAttrib(request, "version",        version);
		setAttrib(request, "outputFormat",   outputFormat);
		setAttrib(request, "schemaLanguage", schemaLang);

		//--- setup type names

		Map<String, String> hmTypeNames = retrieveTypeNames(typeNames, namespace);

		for(Map.Entry<String, String> entry : hmTypeNames.entrySet())
		{
			Element el = new Element("TypeName", Csw.NAMESPACE_CSW);
			el.setText(entry.getKey());
			el.setAttribute("targetNamespace", entry.getValue());

			request.addContent(el);
		}

		return request;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element getSchemaComponent(ServiceContext context, String typeName)
													throws NoApplicableCodeEx
	{
		String dir = context.getAppPath() + Geonet.Path.VALIDATION + "csw/2.0.1/csw-2.0.1.xsd";

		try
		{
			Element schema = Xml.loadFile(dir);

			Element sc = new Element("SchemaComponent", Csw.NAMESPACE_CSW);

			sc.setAttribute("targetNamespace", Csw.NAMESPACE_CSW.getURI());
//			sc.setAttribute("parentSchema",    "?");
			sc.setAttribute("schemaLanguage",  Csw.SCHEMA_LANGUAGE);

			if (typeName == null)
			{
				sc.addContent(schema);
				return sc;
			}
			else
			{
				Element typeElem = findElement(schema, typeName);

				if (typeElem == null)
					return null;

				sc.addContent(typeElem);

				return sc;
			}
		}
		catch (IOException e)
		{
			context.error("Cannot get schema file : "+ dir);
			context.error("  (C) StackTrace\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Cannot get schema file for : "+ typeName);
		}
		catch (JDOMException e)
		{
			context.error("Schema file is not well formed : "+ dir);
			context.error("  (C) StackTrace\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Schema file not well formed : "+ typeName);
		}
	}

	//---------------------------------------------------------------------------

	private Element findElement(Element schema, String typeName)
	{
		Iterator i = schema.getChildren().iterator();

		while (i.hasNext())
		{
			Element elem = (Element) i.next();

			String name = elem.getAttributeValue("name");

			if (elem.getName().equals("element"))
				if (name.equals(typeName))
					return (Element) elem.detach();
		}

		return null;
	}

	//---------------------------------------------------------------------------

//	private Element getSchemaComponent(ServiceContext context, String typeName)
//													throws NoApplicableCodeEx
//	{
//		String file = typeName.equals("service") ? "services.xsd" : "identification.xsd";
//		String dir  = context.getAppPath() + Geonet.Path.CSW + file;
//
//		try
//		{
//			Element schema = Xml.loadFile(dir);
//
//			Element sc = new Element("SchemaComponent", Csw.NAMESPACE_CSW);
//
//			sc.setAttribute("targetNamespace", Csw.NAMESPACE_CSW.getURI());
////			sc.setAttribute("parentSchema",    "?");
//			sc.setAttribute("schemaLanguage",  Csw.SCHEMA_LANGUAGE);
//
//			sc.addContent(schema);
//
//			return sc;
//		}
//		catch (Exception e)
//		{
//			context.error("Cannot get schema file : "+ dir);
//			context.error("  (C) StackTrace\n"+ Util.getStackTrace(e));
//
//			throw new NoApplicableCodeEx("Cannot get schema file for : "+ typeName);
//		}
//	}
}

//=============================================================================

