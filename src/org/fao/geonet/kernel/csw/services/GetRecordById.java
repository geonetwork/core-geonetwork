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

package org.fao.geonet.kernel.csw.services;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.jdom.Element;

//=============================================================================

public class GetRecordById extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetRecordById() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "GetRecordById"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException {
		checkService(request);
		checkVersion(request);
		//-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
		checkOutputFormat(request);
		OutputSchema outSchema = OutputSchema.parse(request.getAttributeValue("outputSchema"));
		//--------------------------------------------------------

		ElementSetName setName = getElementSetName(request, ElementSetName.SUMMARY);

		Element response = new Element(getName() +"Response", Csw.NAMESPACE_CSW);

		Iterator ids = request.getChildren("Id", Csw.NAMESPACE_CSW).iterator();

		if (!ids.hasNext())
			throw new MissingParameterValueEx("id");

		while(ids.hasNext())
		{
			String  id = ((Element) ids.next()).getText();
			Element md = retrieveMetadata(context, id, setName, outSchema);

			if (md != null)
				response.addContent(md);
		}

		return response;
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params)
	{
		String service     = params.get("service");
		String version     = params.get("version");
		String elemSetName = params.get("elementsetname");
		String ids         = params.get("id");

		//-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
		String outputFormat = params.get("outputformat");
		String outputSchema = params.get("outputschema");
		//--------------------------------------------------------

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service", service);
		setAttrib(request, "version", version);

		//-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
		setAttrib(request, "outputFormat",  outputFormat);
		setAttrib(request, "outputSchema",  outputSchema);
		//--------------------------------------------------------

		fill(request, "Id", ids);

		addElement(request, "ElementSetName", elemSetName);

		return request;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

    //-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
	private void checkOutputFormat(Element request) throws InvalidParameterValueEx
	{
		String format = request.getAttributeValue("outputFormat");

		if (format == null)
			return;

		if (!format.equals("application/xml"))
			throw new InvalidParameterValueEx("outputFormat", format);
	}


	//---------------------------------------------------------------------------
	private Element retrieveMetadata(ServiceContext context, String uuid, ElementSetName setName, OutputSchema outSchema) throws CatalogException {
		try {
			Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

			//--- get metadata from DB

			Element  res = dbms.select("SELECT schemaId, data FROM Metadata WHERE uuid='"+uuid+"'");
			Iterator i   = res.getChildren().iterator();

			dbms.commit();

			if (!i.hasNext()) {
				//System.out.println("no metadata found with uuid: " + uuid);
				return null;
			}

			Element record = (Element) i.next();
			String schema = record.getChildText("schemaid");

			//--- skip metadata with wrong schemas
			if (schema.equals("fgdc-std") || schema.equals("dublin-core") || schema.equals("iso19115")) {
			    if (outSchema != OutputSchema.OGC_CORE) {
					//System.out.println("metadata has wrong schema for outputschema: " + schema);
					return null;			
			    }
			}
			
			String data   = record.getChildText("data");
			//System.out.println("found metadata with uuid: " + uuid + "\nmetadata is:\n" + data);

			Element md = Xml.loadString(data, false);
			//System.out.println("after xml loadstring : found: " + Xml.getString(md));


			//--- apply stylesheet according to setName and schema

			//--- we cannot use the element set name parameter here. If we use that, we
			//--- should do an XSL transformation but the transformation would be profile
			//--- specific and we don't have profiles for fgdc and dublin-core metadata

			String FS         = File.separator;
			String schemaDir  = context.getAppPath() +"xml"+ FS +"csw"+ FS +"schemas"+ FS +schema+ FS;
			String prefix     = (outSchema == OutputSchema.OGC_CORE) ? "ogc" : "iso";

			String styleSheet = schemaDir + prefix + "-"+setName+".xsl";
			//System.out.println("Using stylesheet: "+ styleSheet);

			md = Xml.transform(md, styleSheet);
			//System.out.println("Transformed metadata: "+ Xml.getString(md));

			//--- needed to detach md from the document
			md.detach();

			return md;
		}
		catch (Exception e) {
			context.error("Raised : "+ e);
			context.error(" (C) Stacktrace is\n"+Util.getStackTrace(e));
			throw new NoApplicableCodeEx(e.toString());
		}
	}
}