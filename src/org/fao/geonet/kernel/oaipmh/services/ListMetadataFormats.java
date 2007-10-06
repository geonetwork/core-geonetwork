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

import java.io.IOException;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.ListMetadataFormatsRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.ListMetadataFormatsResponse;
import org.fao.oaipmh.responses.MetadataFormat;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

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

		String uuid = req.getIdentifier();

		if (uuid != null)
		{
			String schema = Lib.getMetadataSchema(context, uuid);

			res.addFormat(getSchemaInfo(context, schema));
		}
		else
		{
			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
			DataManager   dm = gc.getDataManager();

			for (String schema : dm.getSchemas())
				res.addFormat(getSchemaInfo(context, schema));
		}

		res.addFormat(getDefaultFormat(context));

		return res;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private MetadataFormat getSchemaInfo(ServiceContext context, String name) throws IOException, JDOMException
	{
		String schemaFile = SCHEMA_PATH + name + "/schema.xsd";

		//--- extract namespace

		Element elem   = Xml.loadFile(context.getAppPath() + schemaFile);
		String  nsPref = elem.getAttributeValue("targetNamespace");

		//--- create object

		MetadataFormat mf = new MetadataFormat();

		mf.prefix    = name;
		mf.schema    = Lib.getSchemaUrl(context, schemaFile);
		mf.namespace = Namespace.getNamespace(nsPref != null ? nsPref : "");

		return mf;
	}

	//---------------------------------------------------------------------------

	private MetadataFormat getDefaultFormat(ServiceContext context)
	{
		String url = Lib.getSchemaUrl(context, OAI_PATH);

		return new MetadataFormat("oai_dc", url, "http://www.openarchives.org/OAI/2.0/");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static final String OAI_PATH    = "xml/validation/oai/dc/oai_dc.xsd";
	private static final String SCHEMA_PATH = "xml/schemas/";
}

//=============================================================================

