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

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.Utils;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================

/**
 * Process a metadata with an XSL transformation declared for the metadata
 * schema. All parameters sent to the service are forwarded to XSL process.
 * <br/>
 * Parameters are:
 * <ul>
 * <li>process: the process identifier (aka. file name without extension)</li>
 * <li>save: (optional) 1 to save the results (default), 0 to only process and return the processed record</li>
 * </ul>
 * <br/>
 * 
 * In each xml/schemas/schemaId directory, a process could be added in a
 * directory called process. Then the process could be called using the
 * following URL :
 * http://localhost:8080/geonetwork/srv/en/metadata.processing?process
 * =keywords-comma-exploder&url=http://xyz
 * <br/>
 * <br/>
 * 
 * In that example the process has to be named keywords-comma-exploder.xsl.
 * 
 * To retrieve parameters in XSL process use the following: 
 * <pre>
 * {@code
 *     <xsl:param name="url">http://localhost:8080/</xsl:param>
 * }
 * </pre>
 * 
 * 
 *  TODO : it could be nice to add an option to return a diff
 *  so we could preview the change before applying them.
 * 
 * @author fxprunayre
 */

public class XslProcessing implements Service {
	private String _appPath;

	public void init(String appPath, ServiceConfig params) throws Exception {
		_appPath = appPath;

		// TODO : here we could register process on startup
		// in order to not to check process each time.
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager   dataMan = gc.getDataManager();

		String process = Util.getParam(params, Params.PROCESS);
		boolean save = "1".equals(Util.getParam(params, Params.SAVE, "1"));

		Set<Integer> metadata = new HashSet<Integer>();
		Set<Integer> notFound = new HashSet<Integer>();
		Set<Integer> notOwner = new HashSet<Integer>();
		Set<Integer> notProcessFound = new HashSet<Integer>();

		String id = Utils.getIdentifierFromParameters(params, context);
		Element processedMetadata;
		try {
			processedMetadata = process(id, process, save, _appPath, params,
				context, metadata, notFound, notOwner, notProcessFound, false, dataMan.getSiteURL());
			if (processedMetadata == null) {
				throw new BadParameterEx("Processing failed", 
						"Not found:" + notFound.size() +
						", Not owner:" + notOwner.size() +
						", No process found:" + notProcessFound.size() +
						".");
			}
		} catch (Exception e) {
			throw e;
		}
		// -- return the processed metadata id
		Element response = new Element(Jeeves.Elem.RESPONSE)
                                .addContent(new Element(Geonet.Elem.ID).setText(id));
		// and the processed metadata if not saved.
		if (!save) {
		    response.addContent(new Element("record").addContent(processedMetadata));
		}
		return response;
		
	}

	/**
	 * Process a metadata record and add information about the processing
	 * to one or more sets for reporting.
	 * 
	 * @param id		The metadata identifier corresponding to the metadata record to process
	 * @param process	The process name
	 * @param appPath	The application path (use to get the process XSL)
	 * @param params	The input parameters
	 * @param context	The current context
	 * @param metadata	
	 * @param notFound
	 * @param notOwner
	 * @param notProcessFound
	 * @return
	 * @throws Exception
	 */	
	public static Element process(String id, String process, boolean save,
	        String appPath, Element params, ServiceContext context, 
	        Set<Integer> metadata, Set<Integer> notFound, Set<Integer> notOwner,
			Set<Integer> notProcessFound, boolean useIndexGroup, String siteUrl) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		UserSession session = context.getUserSession();
		DataManager dataMan = gc.getDataManager();
		SchemaManager schemaMan = gc.getSchemamanager();
		AccessManager accessMan = gc.getAccessManager();
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		MdInfo info = dataMan.getMetadataInfo(dbms, id);

		if (info == null) {
			notFound.add(new Integer(id));
		} else if (!accessMan.isOwner(context, id)) {
			notOwner.add(new Integer(id));
		} else {

			// -----------------------------------------------------------------------
			// --- check processing exist for current schema
			String schema = info.schemaId;
            
			String filePath = schemaMan.getSchemaDir(schema) + "/process/" + process + ".xsl";
			File xslProcessing = new File(filePath);
			if (!xslProcessing.exists()) {
				context.info("  Processing instruction not found for " + schema + " schema. Looking for "+filePath);
				notProcessFound.add(new Integer(id));
				return null;
			}
			// --- Process metadata
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
            Element md = dataMan.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

            // -- here we send parameters set by user from URL if needed.
			List<Element> children = params.getChildren();
			Map<String, String> xslParameter = new HashMap<String, String>();
	        xslParameter.put("guiLang", context.getLanguage());
	        xslParameter.put("baseUrl", context.getBaseUrl());
	        for (Element param : children) {
				xslParameter.put(param.getName(), param.getTextTrim());
			}

            xslParameter.put("siteUrl", siteUrl);

			Element processedMetadata = Xml.transform(md, filePath, xslParameter);
			
			// --- save metadata and return status
            if (save) {
                boolean validate = true;
                boolean ufo = true;
                boolean index = false;
                String language = context.getLanguage();
                // Always udpate metadata date stamp on metadata processing (minor edit has no effect).
                boolean updateDateStamp = true;
                dataMan.updateMetadata(context, dbms, id, processedMetadata, validate, ufo, index, language, new ISODate().toString(), updateDateStamp);
    			if (useIndexGroup) {
    				dataMan.indexMetadata(dbms, id);
    			}
                else {
                    dataMan.indexInThreadPool(context, id, dbms);
    			}
            }

			metadata.add(new Integer(id));

			return processedMetadata;
		}
		return null;
	}
}
