/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_OP_NOTE_PROCESS;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerConfigurationException;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.api.processing.XslProcessUtils;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.api.records.model.suggestion.SuggestionType;
import org.fao.geonet.api.records.model.suggestion.SuggestionsType;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.events.history.RecordProcessingChangeEvent;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

@RequestMapping(value = { "/{portal}/api/records", "/{portal}/api/" + API.VERSION_0_1 + "/records" })
@Api(value = API_CLASS_RECORD_TAG, tags = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordProcessing")
@ReadWriteController
public class MetadataProcessApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    DataManager dm;

    @Autowired
    SettingManager sm;

    public static final String XSL_SUGGEST_FILE = "suggest.xsl";

    @ApiOperation(value = "Get suggestions", notes = "Analyze the record an suggest processes to improve the quality of the record.<br/>"
            + "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/workflow/batchupdate-xsl.html'>More info</a>", nickname = "getSuggestions")
    @RequestMapping(value = "/{metadataUuid}/processes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Record suggestions."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    public @ResponseBody List<SuggestionType> getSuggestions(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        ServiceContext context = ApiUtils.createServiceContext(request);

        Map<String, Object> xslParameter = new HashMap<String, Object>();
        xslParameter.put("guiLang", request.getLocale().getISO3Language());
        xslParameter.put("siteUrl", sm.getSiteURL(context));
        xslParameter.put("nodeUrl", sm.getNodeURL());
        xslParameter.put("baseUrl", context.getBaseUrl());
        xslParameter.put("action", "analyze");

        // List or analyze all suggestions process registered for this schema
        MetadataSchema metadataSchema = dm.getSchema(metadata.getDataInfo().getSchemaId());
        Path xslProcessing = metadataSchema.getSchemaDir().resolve(XSL_SUGGEST_FILE);
        if (Files.exists(xslProcessing)) {
            // -- here we send parameters set by user from
            // URL if needed.
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element md = dm.getMetadata(context, String.valueOf(metadata.getId()), forEditing, withValidationErrors,
                    keepXlinkAttributes);

            Element xmlSuggestions;
            try {
                xmlSuggestions = Xml.transform(md, xslProcessing, xslParameter);
            } catch (TransformerConfigurationException e) {
                throw new WebApplicationException(String.format("Error while retrieving suggestion for record '%s'. "
                        + "Check your suggest.xsl process (and all its imports).", metadataUuid, xslProcessing), e);
            }
            SuggestionsType suggestions = (SuggestionsType) Xml.unmarshall(xmlSuggestions, SuggestionsType.class);

            return suggestions.getSuggestion();
        } else {
            throw new ResourceNotFoundException(
                    String.format("No %s files available in schema '%s'. No suggestion to provides.", XSL_SUGGEST_FILE,
                            metadata.getDataInfo().getSchemaId()));
        }
    }

    @ApiOperation(value = "Preview process result", notes = API_OP_NOTE_PROCESS, nickname = "processRecordPreview")
    @RequestMapping(value = "/{metadataUuid}/processes/{process}", method = {
            RequestMethod.GET }, produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "A preview of the processed record."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    public @ResponseBody ResponseEntity<Element> processRecordPreview(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = ApiParams.API_PARAM_PROCESS_ID) @PathVariable String process, HttpServletRequest request)
            throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        boolean save = request.getMethod().equals("POST");

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        XsltMetadataProcessingReport report = new XsltMetadataProcessingReport(process);

        Element processedMetadata = process(applicationContext, process, request, metadata, save, context, sm, report);

        return new ResponseEntity<>(processedMetadata, HttpStatus.OK);
    }

    @ApiOperation(value = "Apply a process", notes = API_OP_NOTE_PROCESS, nickname = "processRecord")
    @RequestMapping(value = "/{metadataUuid}/processes/{process}", method = {
            RequestMethod.POST, }, produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Record processed and saved."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    public @ResponseBody ResponseEntity processRecord(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = ApiParams.API_PARAM_PROCESS_ID) @PathVariable String process, HttpServletRequest request)
            throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        boolean save = true;

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        XsltMetadataProcessingReport report = new XsltMetadataProcessingReport(process);

        process(applicationContext, process, request, metadata, save, context, sm, report);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Element process(ApplicationContext applicationContext, String process, HttpServletRequest request,
            AbstractMetadata metadata, boolean save, ServiceContext context, SettingManager sm,
            XsltMetadataProcessingReport report) throws Exception {

        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
        Element processedMetadata;
        Element beforeMetadata = dm.getMetadata(context, Integer.toString(metadata.getId()), false, false, false);
        try {
            final String siteURL = sm.getSiteURL(context);
            processedMetadata = XslProcessUtils.process(context, String.valueOf(metadata.getId()), process, save, true,
                true, report, siteURL, request.getParameterMap());
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed",
                        "Not found:" + report.getNumberOfRecordNotFound() + ", Not owner:"
                                + report.getNumberOfRecordsNotEditable() + ", No process found:"
                                + report.getNoProcessFoundCount() + ".");
            } else {
                UserSession userSession = context.getUserSession();
                if (userSession != null) {
                    XMLOutputter outp = new XMLOutputter();
                    String xmlAfter = outp.outputString(processedMetadata);
                    String xmlBefore = outp.outputString(beforeMetadata);
                    new RecordProcessingChangeEvent(metadata.getId(), Integer.parseInt(userSession.getUserId()),
                            xmlBefore, xmlAfter, process).publish(ApplicationContextHolder.get());
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return processedMetadata;
    }
}
