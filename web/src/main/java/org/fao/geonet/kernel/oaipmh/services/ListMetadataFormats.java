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

package org.fao.geonet.kernel.oaipmh.services;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.ListMetadataFormatsRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.ListMetadataFormatsResponse;
import org.fao.oaipmh.responses.MetadataFormat;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//=============================================================================

public class ListMetadataFormats implements OaiPmhService
{
	public String getVerb() { return ListMetadataFormatsRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------

	public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception
	{
		ListMetadataFormatsRequest  req = (ListMetadataFormatsRequest) request;
		ListMetadataFormatsResponse res = new ListMetadataFormatsResponse();

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager   sm = gc.getSchemamanager();

		String uuid = req.getIdentifier();
		if (uuid != null)
		{
			String schema = Lib.getMetadataSchema(context, uuid);
			res.addFormat(getSchemaInfo(context, sm, schema));
		}
		else
		{
			for (String schema : sm.getSchemas())
				res.addFormat(getSchemaInfo(context, sm, schema));
		}

		for (MetadataFormat mdf : getDefaultFormats(context)) {
			res.addFormat(mdf);
		}

		return res;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private MetadataFormat getSchemaInfo(ServiceContext context, SchemaManager sm, String name) throws IOException, JDOMException
	{
		String schemaFile = sm.getSchemaDir(name) + "schema.xsd";

		//--- extract namespace if schema.xsd file exists (could be DTD schema)

		String nsPref = null;
		boolean schemaExists = new File(schemaFile).exists();
		if (schemaExists) {
			Element elem   = Xml.loadFile(schemaFile);
			nsPref = elem.getAttributeValue("targetNamespace");
		}

		//--- create object

		MetadataFormat mf = new MetadataFormat();

		mf.prefix    = name;
		if (schemaExists) {
			mf.schema    = Lib.getSchemaUrl(context, schemaFile);
		} else {
			mf.schema		 = "";
		}
		mf.namespace = Namespace.getNamespace(nsPref != null ? nsPref : "");

		return mf;
	}

	//---------------------------------------------------------------------------

	private List<MetadataFormat> getDefaultFormats(ServiceContext context) throws IOException, JDOMException
	{
	
		Element elem = Xml.loadFile(context.getAppPath() + DEFAULT_SCHEMAS_FILE);
		List<Element> defaultSchemas = elem.getChildren();

		List <MetadataFormat> defMdfs = new ArrayList<MetadataFormat>();
		for (Element schema : defaultSchemas) {
			defMdfs.add(new MetadataFormat(schema.getAttributeValue("prefix"), schema.getAttributeValue("schemaLocation"), schema.getAttributeValue("nsUrl")));
		}
		return defMdfs; 
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static final String DEFAULT_SCHEMAS_FILE = "xml/validation/oai/schemas.xml";
}

//=============================================================================

