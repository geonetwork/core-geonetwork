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

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
public class XslProcessing {

    @RequestMapping(value = {"/{lang}/md.processing", "/{lang}/xml.metadata.processing", "/{lang}/metadata.processing.new"}, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public IdResponse xmlProcessing(@RequestParam(value = Params.PROCESS) String process,
                                  @PathVariable String lang,
                                  @RequestParam(value = Params.SAVE, defaultValue = "1") String saveParam,
                                  @RequestParam(defaultValue = "") String id,
                                  @RequestParam(defaultValue = "") String uuid,
                                  HttpServletRequest request)
            throws Exception {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();

        DataManager dataMan = appContext.getBean(DataManager.class);
        SettingManager settingsMan = appContext.getBean(SettingManager.class);
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);

        ServiceContext context = serviceManager.createServiceContext("md.processing", lang, request);
        XslProcessingReport xslProcessingReport = new XslProcessingReport(process);

        if (id.isEmpty()) {
            id = dataMan.getMetadataId(uuid);
        }
        final boolean save = "1".equals(saveParam);
        Element processedMetadata;
        try {
            final String siteURL = settingsMan.getSiteURL(context);
            processedMetadata = process(context, id, process, save, xslProcessingReport, siteURL, request.getParameterMap());
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

        // and the processed metadata if not saved.
        if (!save) {
            return new Response(id, Xml.getString(processedMetadata));
        } else {
            return new IdResponse(id);
        }
    }

    /**
     * Process a metadata record and add information about the processing
     * to one or more sets for reporting.
     *
     *
     * @param context
     * @param id        The metadata identifier corresponding to the metadata record to process
     * @param process    The process name
     * @param report
     * @return
     * @throws Exception
     */
    public Element process(ServiceContext context, String id, String process, boolean save,
                           XslProcessingReport report,
                           String siteUrl,
                           Map<String, String[]> params) throws Exception {
        SchemaManager schemaMan = context.getBean(SchemaManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        DataManager dataMan = context.getBean(DataManager.class);
        SettingManager settingsMan = context.getBean(SettingManager.class);
        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

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

            Path xslProcessing = schemaMan.getSchemaDir(schema).resolve("process").resolve(process + ".xsl");
            if (!Files.exists(xslProcessing)) {
                Log.info("org.fao.geonet.services.metadata","  Processing instruction not found for " + schema +
                                                            " schema. Looking for " + xslProcessing);
                report.addNoProcessFoundMetadataId(iId);
                return null;
            }
            
            
            // --- Process metadata
            Element processedMetadata = null;
            try {
                boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
	            Element md = dataMan.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
	
	            Map<String, Object> xslParameter = new HashMap<>();

	            xslParameter.put("guiLang", context.getLanguage());
	            xslParameter.put("baseUrl", context.getBaseUrl());
                xslParameter.put("catalogUrl", settingsMan.getSiteURL(context));
                xslParameter.put("nodeId", context.getNodeId());
                
	            for (Entry<String, String[]> parameter : params.entrySet()) {
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
	
	            processedMetadata = Xml.transform(md, xslProcessing, xslParameter);
	
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
}

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.PROPERTY)
class Response extends IdResponse{

    private String processedMetadata;

    public Response(String id, String processedMetadata) {
        super(id);
        this.processedMetadata = processedMetadata;
    }

    public String getProcessedMetadata() {
        return processedMetadata;
    }
}