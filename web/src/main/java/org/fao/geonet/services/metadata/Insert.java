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

package org.fao.geonet.services.metadata;

import java.io.File;
import jeeves.constants.Jeeves;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.fao.geonet.kernel.SchemaManager;

/**
 * Inserts a new metadata to the system (data is validated).
 */
public class Insert extends NotInReadOnlyModeService {
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

    private String stylePath;

	public void init(String appPath, ServiceConfig params) throws Exception
    {
        this.stylePath = appPath + Geonet.Path.IMPORT_STYLESHEETS;
    }

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		String data       = Util.getParam(params, Params.DATA);
		String group      = Util.getParam(params, Params.GROUP);
		String isTemplate = Util.getParam(params, Params.TEMPLATE, "n");
		String title      = Util.getParam(params, Params.TITLE, "");
		String style      = Util.getParam(params, Params.STYLESHEET, "_none_");

		boolean validate = Util.getParam(params, Params.VALIDATE, "off").equals("on");

//		Sub template does not need a title.
//		 if (isTemplate.equals("s") && title.length() == 0)
//			 throw new MissingParameterEx("title");

		//-----------------------------------------------------------------------
		//--- add the DTD to the input xml to perform validation

		Element xml = Xml.loadString(data, false);

        xml = applyImportStylesheet(xml, style, gc, stylePath);

        String schema = dataMan.autodetectSchema(xml);
        if (schema == null)
        	throw new BadParameterEx("Can't detect schema for metadata automatically.", schema);

		if (validate) dataMan.validateMetadata(schema, xml, context);

		//-----------------------------------------------------------------------
		//--- if the uuid does not exist and is not a template we generate it

		String uuid;
		if (isTemplate.equals("n"))
		{
			uuid = dataMan.extractUUID(schema, xml);
			if (uuid.length() == 0) uuid = UUID.randomUUID().toString();
		}
		else uuid = UUID.randomUUID().toString();

		String uuidAction = Util.getParam(params, Params.UUID_ACTION,
				Params.NOTHING);

		String date = new ISODate().toString();

		final List<String> id = new ArrayList<String>();
		final List<Element> md = new ArrayList<Element>();
		String localId = null;
		md.add(xml);
		

        DataManager dm = gc.getDataManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        
		// Import record
		Importer.importRecord(uuid, localId , uuidAction, md, schema, 0,
				gc.getSiteId(), gc.getSiteName(), context, id, date,
				date, group, isTemplate, dbms);
		
		int iId = Integer.parseInt(id.get(0));
		
		
		// Set template
		dm.setTemplate(dbms, iId, isTemplate, null);

		
		// Import category
		String category   = Util.getParam(params, Params.CATEGORY, "");

		if (!category.equals("_none_") || !category.equals("")) {
			Element categs = new Element("categories");
			categs.addContent((new Element("category")).setAttribute(
					"name", category));

			Importer.addCategories(context, dm, dbms, id.get(0), categs);
		} 

		// Index
        dm.indexInThreadPool(context, id.get(0), dbms);
        
		// Return response
		Element response = new Element(Jeeves.Elem.RESPONSE);
		response.addContent(new Element(Params.ID).setText(String.valueOf(iId)));
	        response.addContent(new Element(Params.UUID).setText(String.valueOf(dm.getMetadataUuid(dbms, id.get(0)))));

		return response;
	};

    /**
     *  Apply an import stylesheet transformation if requested.
     *
     * If the stylename is "_none", no transformation will be performed. <br/>
     * If the stylename contains a slash, then format is SCHEMANAME/XSLFILENAME.<br/>
     * Otherwise the stylename will be searched inside the fixed import style path.
     *
     * @param xml The metadata to be transformed
     * @param styleName The xsl file to be applied; if it contains a slash, then format is SCHEMANAME/XSLFILENAME
     * @param gc The context
     * @param stylePath The fixed import style path
     * @return The transformed metadata, or the original one if no transformation was needed
     * @throws Exception
     */
       public static Element applyImportStylesheet(Element xml, String styleName, GeonetContext gc, String stylePath) throws Exception {

        if (!styleName.equals("_none_")) {

            if(! styleName.contains("/")) {
                // this is one of the default transformations
                xml = Xml.transform(xml, stylePath + File.separator + styleName);
            } else
            {
                // it's a transformation defined in a schema plugin
                int pos = styleName.indexOf("/");
                String schemaName = styleName.substring(0, pos);
                String xslName = styleName.substring(pos+1);

                SchemaManager schemaMan = gc.getSchemamanager();

                String schemaDir = schemaMan.getSchemaDir(schemaName);
                File convertDir = new File(schemaDir, Geonet.Path.CONVERT_STYLESHEETS);
                File importDir = new File(convertDir, "import");
                File xsl = new File(importDir, xslName);

                xml = Xml.transform(xml, xsl.getAbsolutePath());
            }
        }
        return xml;
    }

	//---------------------------------------------------------------------------

	private void fixNamespace(Element md, Namespace ns)
	{
		if (md.getNamespaceURI().equals(ns.getURI()))
			md.setNamespace(ns);

		for (Object o : md.getChildren())
			fixNamespace((Element) o, ns);
	}
}