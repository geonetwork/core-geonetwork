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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//=============================================================================

public class Info implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(Path appPath, ServiceConfig config) throws Exception
	{
		importXslPath = appPath.resolve(Geonet.Path.IMPORT_STYLESHEETS);
		oaiSchema  = appPath.resolve("xml/validation/oai/OAI-PMH.xsd");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		Element result = new Element("root");
		
		String schema = org.fao.geonet.Util.getParam(params, "schema", "");
		String serviceType = Util.getParam(params, "serviceType", "");

		@SuppressWarnings("unchecked")
        List<Element> paramChildren = params.getChildren();
		for (Element el : paramChildren) {
			String name = el.getName();
			String type = el.getText();

			if (name.equals("type")) {

				if (type.equals("icons"))
					result.addContent(getIcons(context));

				else if (type.equals("harvesterTypes"))
				    result.addContent(getHarvesterTypes(context));
				
				else if (type.equals("oaiPmhServer"))
					result.addContent(getOaiPmhServer(el, context));

				else if (type.equals("wfsFragmentStylesheets"))
					result.addContent(getSchemaFragmentStylesheets(el, context, Geonet.Path.WFS_STYLESHEETS, schema));

				else if (type.equals("threddsFragmentStylesheets"))
					result.addContent(getSchemaFragmentStylesheets(el, context, Geonet.Path.TDS_STYLESHEETS, schema));

				else if (type.equals("threddsFragmentSchemas"))
					result.addContent(getSchemas(el, context, Geonet.Path.TDS_STYLESHEETS));

				else if (type.equals("ogcwxsOutputSchemas"))
					result.addContent(getSchemas(el, context, getGetCapXSLPath(serviceType)));

				else if (type.equals("wfsFragmentSchemas"))
					result.addContent(getSchemas(el, context, Geonet.Path.WFS_STYLESHEETS));

				else if (type.equals("threddsDIFSchemas"))
					result.addContent(getSchemas(el, context, Geonet.Path.DIF_STYLESHEETS));

				else if (type.equals("importStylesheets"))
					result.addContent(getStylesheets(importXslPath));

				else if (type.equals("validation"))
					result.addContent(getValidationOptions());

				else
					throw new BadParameterEx("type", type);
			} else if (name.equals("schema")||(name.equals("serviceType"))) { // do nothing
//			} else {
//					throw new BadParameterEx(name, type);
			}
		}
				
		return result;
	}

    private Element getValidationOptions() {
        Element validationOptions = new Element("validationOptions");
        for (HarvestValidationEnum validationEnum : HarvestValidationEnum.values()) {
            validationOptions.addContent(new Element("validation").setText(validationEnum.name()));
        }
        return validationOptions;
    }

    //--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private Element getHarvesterTypes(ServiceContext context) {
	    Element types = new Element("types");
	    for (String type : AbstractHarvester.getHarvesterTypes(context)) {
            types.addContent(new Element("type").setText(type));
        }
        return types;
    }

    private Element getIcons(ServiceContext context)
	{
		Set<Path> icons = Resources.listFiles(context, "harvesting", iconFilter);
		List<Path> list = new ArrayList<>(icons);
		Collections.sort(list);
		Element result = new Element("icons");

		for (Path icon : list)
			result.addContent(new Element("icon").setText(icon.getFileName().toString()));

		return result;
	}

	//--------------------------------------------------------------------------

	private DirectoryStream.Filter<Path> iconFilter = new DirectoryStream.Filter<Path>()
	{
        @Override
        public boolean accept(Path icon) throws IOException {
            if (icon == null || !Files.isRegularFile(icon))
                return false;

            String name = icon.getFileName().toString();

            for (String ext : iconExt)
                if (name.endsWith(ext))
                    return true;

            return false;
        }
    };

	
	//--------------------------------------------------------------------------
	//--- Get Metadata fragment stylesheets from each schema
	//--------------------------------------------------------------------------

	private Element getSchemaFragmentStylesheets(Element el, ServiceContext context, String xslFragmentDir, String schemaFilter) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager schemaMan = gc.getBean(SchemaManager.class);

		Element elRoot = new Element("stylesheets");

		for (String schema : schemaMan.getSchemas()) {
			if (!schemaFilter.equals("") && !schema.equals(schemaFilter)) continue;
			Path xslPath = schemaMan.getSchemaDir(schema).resolve(xslFragmentDir);
			if (!Files.exists(xslPath)) continue;

			@SuppressWarnings("unchecked")
            List<Element> elSheets = getStylesheets(xslPath).getChildren();
			for (Element elSheet : elSheets) {
				elSheet = (Element)elSheet.clone();
				elSheet.addContent(new Element(Geonet.Elem.SCHEMA).setText(schema));
				elRoot.addContent(elSheet);
			}
		}

		return elRoot;
	}

	//--------------------------------------------------------------------------
	//--- Get List of Schemas that contain the xslPath               
	//--------------------------------------------------------------------------

	private Element getSchemas(Element el, ServiceContext context, String xslPathStr) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager schemaMan = gc.getBean(SchemaManager.class);

		Element elRoot = new Element("schemas");

		for (String schema : schemaMan.getSchemas()) {
			File xslPath = new File(schemaMan.getSchemaDir(schema)+xslPathStr);	
			if (xslPath.exists()) {
				Element res = new Element(Jeeves.Elem.RECORD);
				
				res.addContent(new Element(Geonet.Elem.ID)  .setText(schema));
				res.addContent(new Element(Geonet.Elem.NAME).setText(schema));

				elRoot.addContent(res);
			}
		}

		return elRoot;
	}

	//--------------------------------------------------------------------------

	private Element getStylesheets(Path xslPath) throws Exception {
        try (DirectoryStream<Path> sheets = Files.newDirectoryStream(xslPath)) {
            Element elRoot = new Element("stylesheets");
            for (Path sheet : sheets) {

                    String id = sheet.toString();
                    if (id.endsWith(".xsl")) {
                        String name = com.google.common.io.Files.getNameWithoutExtension(sheet.getFileName().toString());

                        Element res = new Element(Jeeves.Elem.RECORD);

                        res.addContent(new Element(Geonet.Elem.ID)  .setText(id));
                        res.addContent(new Element(Geonet.Elem.NAME).setText(name));

                        elRoot.addContent(res);
                    }
            }
            return elRoot;
        }
	}

	//--------------------------------------------------------------------------
	//--- OGC GetCapabilities to iso19119 stylesheet path for OGC service type              
	//--------------------------------------------------------------------------

	private String getGetCapXSLPath(String serviceType) {
		return Geonet.Path.OGC_STYLESHEETS 
				+ "/OGC"
				+ serviceType.substring(0,3)
				+ "GetCapabilities-to-ISO19119_ISO19139.xsl";
	}

	//--------------------------------------------------------------------------
	//--- OaiPmhServer
	//--------------------------------------------------------------------------

	private Element getOaiPmhServer(Element el, ServiceContext context) throws BadInputEx
	{
		String url = el.getAttributeValue("url");

		if (url == null)
			throw new MissingParameterEx("attribute:url", el);

		if (!Lib.net.isUrlValid(url))
			throw new BadParameterEx("attribute:url", el);

		Element res = new Element("oaiPmhServer");

		try
		{
			res.addContent(getMdFormats(url, context));
			res.addContent(getSets(url, context));
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

	private Element getMdFormats(String url, ServiceContext context) throws Exception
	{
		ListMetadataFormatsRequest req = new ListMetadataFormatsRequest(context.getBean(GeonetHttpRequestFactory.class));
		req.setSchemaPath(oaiSchema);

        XmlRequest t = req.getTransport();
		t.setUrl(new URL(url));
		Lib.net.setupProxy(context, t);
		ListMetadataFormatsResponse res = req.execute();

		//--- build response

		Element root = new Element("formats");

		for (MetadataFormat mf : res.getFormats())
			root.addContent(new Element("format").setText(mf.prefix));

		return root;
	}

	//--------------------------------------------------------------------------

	private Element getSets(String url, ServiceContext context) throws Exception
	{
		Element root = new Element("sets");

		try
		{
			ListSetsRequest req = new ListSetsRequest(context.getBean(GeonetHttpRequestFactory.class));
			req.setSchemaPath(oaiSchema);
            XmlRequest t = req.getTransport();
			t.setUrl(new URL(url));
			Lib.net.setupProxy(context, t);
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

	private Path oaiSchema;
	private Path importXslPath;
	
	private static final String iconExt[] = { ".gif", ".png", ".jpg", ".jpeg" };
}

//=============================================================================

