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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.responses.IdResponse;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
@Controller("metadata.processing")
@ReadWriteController
public class XslProcessing { //extends NotInReadOnlyModeService {
    private String _appPath;
    private static XslProcessing instance;
    
    public void init(String appPath, ServiceConfig params) throws Exception {
        _appPath = appPath;

        // TODO : here we could register process on startup
        // in order to not to check process each time.
    }
    
    /**
     * To replace static calls
     * @return
     */
    public static XslProcessing get() {
    	if(XslProcessing.instance == null) {
    		XslProcessing.instance = new XslProcessing();
    	}
    	return XslProcessing.instance;
    }

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public @ResponseBody Response serviceSpecificExec(@RequestParam(defaultValue= Params.PROCESS) String process, 
    		@RequestParam(defaultValue=Params.SAVE) Boolean save, 
    		@RequestParam(defaultValue="") String id, 
    		@RequestParam(defaultValue="") String uuid,
    		HttpServletRequest request)
            throws Exception {        
        XslProcessingReport xslProcessingReport = new XslProcessingReport(process);

        if(id.isEmpty()) {
			id = dataMan.getMetadataId(uuid);
        }
        Element processedMetadata;
        try {
            final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
            processedMetadata = process(id, process, save, _appPath, xslProcessingReport, false, siteURL, request);
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed", "Not found:"
                        + xslProcessingReport.getNotFoundMetadataCount() + 
                        ", Not owner:" + xslProcessingReport.getNotEditableMetadataCount() + 
                        ", No process found:" + xslProcessingReport.getNoProcessFoundCount() + ".");
            }
        } catch (Exception e) {
            throw e;
        }
        // -- return the processed metadata id
        Response res = new Response(id);
        // and the processed metadata if not saved.
        if (!save) {
            //response.addContent(new Element("record").addContent(processedMetadata));
        }
        
        return res;

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
     * @param report
     * @return
     * @throws Exception
     */
    public Element process(String id, String process, boolean save,
                                  String appPath,
                                  XslProcessingReport report, boolean useIndexGroup,
                                  String siteUrl,
                                  HttpServletRequest request) throws Exception {

        report.incrementProcessedRecords();
        
        // When a record is deleted the UUID is in the selection manager
        // and when retrieving id, return null
        if (id == null) {
            report.incrementNullRecords();
            return null;
        }
        
        int iId = Integer.valueOf(id);
        Metadata info = metadataRepository.findOne(id);
        
        
        if (info == null) {
            report.addNotFoundMetadataId(iId);
        } else if (!accessMan.canEdit(context, id)) {
            report.addNotEditableMetadataId(iId);
        } else {

            // -----------------------------------------------------------------------
            // --- check processing exist for current schema
            String schema = info.getDataInfo().getSchemaId();

            String filePath = schemaMan.getSchemaDir(schema) + "/process/" + process + ".xsl";
            File xslProcessing = new File(filePath);
            if (!xslProcessing.exists()) {
                Log.info("org.fao.geonet.services.metadata","  Processing instruction not found for " + schema + " schema. Looking for "+filePath);
                report.addNoProcessFoundMetadataId(iId);
                return null;
            }
            
            
            // --- Process metadata
            Element processedMetadata = null;
            try {
                boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
	            Element md = dataMan.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
	
	            Map<String, Object> xslParameter = new HashMap<String, Object>();

	            xslParameter.put("guiLang", context.getLanguage());
	            xslParameter.put("baseUrl", context.getBaseUrl());
                xslParameter.put("catalogUrl", settingsMan.getSiteURL(context));
                xslParameter.put("nodeId", context.getNodeId());
                
	            for (Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
	            	String value = parameter.getValue()[0].trim();
	            	String key = parameter.getKey();
	                // Add extra metadata
	                if (key.equals("extra_metadata_uuid")
	                        && !value.equals("")) {
	                    String extraMetadataId = dataMan.getMetadataId(value);
	                    if (extraMetadataId != null) {
	                        Element extraMetadata = dataMan.getMetadata(context,
	                                extraMetadataId, forEditing,
	                                withValidationErrors, keepXlinkAttributes);
	                        md.addContent(new Element("extra")
	                                .addContent(extraMetadata));
	                        xslParameter.put(key, value);
	                    }
	                } else {
	                    // Or add parameter
	                    xslParameter.put(key, value);
	                }
	            }
	
	
	            xslParameter.put("siteUrl", siteUrl);
	
	            processedMetadata = Xml.transform(md, filePath, xslParameter);
	
	            // --- save metadata and return status
	            if (save) {
	                Lib.resource.checkEditPrivilege(context, id);
	
	                boolean validate = false;
	                boolean ufo = true;
	                boolean index = false;
	                String language = context.getLanguage();
	                // Always udpate metadata date stamp on metadata processing (minor edit has no effect).
	                boolean updateDateStamp = true;
	                dataMan.updateMetadata(context, id, processedMetadata, validate, ufo, index, language, new ISODate().toString(), updateDateStamp);
                    dataMan.indexMetadata(id, true);
	            }
	
	            report.addMetadataId(iId);
	            // TODO : it could be relevant to list at least
	            // if there was any change in the record or not.
	            // Using hash on processMd and metadata ?
            } catch (Exception e) {
            	report.addMetadataError(iId, e);
            	context.error("  Processing failed with error " + e.getMessage());
                e.printStackTrace();
            }
            return processedMetadata;
        }
        return null;
    }
    
    @Autowired
    private DataManager dataMan;
    @Autowired
    private SchemaManager schemaMan;
    @Autowired
    private AccessManager accessMan;
    @Autowired
    private SettingManager settingsMan;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private ServiceContext context;
}

class Response  extends IdResponse{

    public Response(String id) {

    }
	
}