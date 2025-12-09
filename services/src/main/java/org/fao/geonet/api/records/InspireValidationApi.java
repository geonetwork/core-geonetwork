/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.file.Path;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.inspire.validator.InspireValidationRunnable;
import org.fao.geonet.inspire.validator.InspireValidatorUtils;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterApi;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.api.records.formatters.cache.Key;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Source;
import org.fao.geonet.events.history.RecordValidationTriggeredEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;


@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("inspire")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class InspireValidationApi {

    public static final String API_PARAM_INSPIRE_VALIDATION_MODE = "Define the encoding of the record to use. "
        + "By default, ISO19139 are used as is and "
        + "ISO19115-3 are converted to ISO19139."
        + "If mode = csw, a GetRecordById request is used."
        + "If mode = any portal id, then a GetRecordById request is used on this portal "
        + "CSW can only be used on public records as the remote validator request the CSW endpoint."
        + "CSW entry point can define additional CSW post processing (if encoding need adjustments to cope with INSPIRE requirements). "
        + "See https://github.com/geonetwork/core-geonetwork/pull/4493.";
    @Autowired
    SettingManager settingManager;

    @Autowired
    InspireValidatorUtils inspireValidatorUtils;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    SourceRepository sourceRepository;

    String supportedSchemaRegex = "(iso19139|iso19115-3).*";

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private ThreadPool threadPool;

    @Value("#{validatorAdditionalConfig['processing']}")
    public String processing;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get test suites available.",
        description = "TG13, TG2, ...")
    @GetMapping(value = "/{metadataUuid}/validate/inspire/testsuites",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of testsuites available."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    Map<String, String[]> getTestSuites(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid) {
        // TODO: We may at some point propose only testsuite which applies to a record ?
        return inspireValidatorUtils.getTestsuites();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Submit a record to the INSPIRE service for validation.",
        description = "User MUST be able to edit the record to validate it. "
            + "An INSPIRE endpoint must be configured in Settings. "
            + "This activates an asynchronous process, this method does not return any report. "
            + "This method returns an id to be used to get the report.")
    @PutMapping(value = "/{metadataUuid}/validate/inspire",
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Check status of the report."),
        @ApiResponse(responseCode = "404", description = "Metadata not found."),
        @ApiResponse(responseCode = "500", description = "Service unavailable."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    String validateRecordForInspire(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Test suite to run",
            required = false)
        @RequestParam
            String testsuite,
        @Parameter(
            description = API_PARAM_INSPIRE_VALIDATION_MODE,
            required = false)
        @RequestParam(required = false)
            String mode,
        HttpServletResponse response,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true) final NativeWebRequest nativeRequest,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {

        ApplicationContext appContext = ApplicationContextHolder.get();
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        new RecordValidationTriggeredEvent(metadata.getId(), ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), null).publish(appContext);

        if (metadata == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return "";
        }

        String schema = metadata.getDataInfo().getSchemaId();
        if (!schema.matches(supportedSchemaRegex)) {
            response.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
            return String.format("INSPIRE validator does not support records in schema '%s'. Schema must match expression '%s' and have an ISO19139 formatter.",
                schema, supportedSchemaRegex);
        }

        String id = String.valueOf(metadata.getId());

        String inspireValidatorUrl = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);
        String inspireValidatorQueryUrl = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL_QUERY);
        if (StringUtils.isEmpty(inspireValidatorQueryUrl)) {
            inspireValidatorQueryUrl = inspireValidatorUrl;
        }

        ServiceContext context = ApiUtils.createServiceContext(request);
        String getRecordByIdUrl = null;
        String testId = null;

        Element md = (Element) ApiUtils.getUserSession(session).getProperty(Geonet.Session.METADATA_EDITING + id);
        if (md == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return String.format("Metadata with id '%s' not found in session. To be validated, the record must be in edition session.", id);
            // TODO: Add support for such validation from not editing session ?
        }

        if (StringUtils.isEmpty(mode)) {
            // Use formatter to convert the record
            if (!schema.equals("iso19139")) {
                try {
                    Key key = new Key(metadata.getId(), "eng", FormatType.xml, "iso19139", true, FormatterWidth._100);

                    final FormatterApi.FormatMetadata formatMetadata =
                        new FormatterApi().new FormatMetadata(context, key, nativeRequest);
                    final byte[] data = formatMetadata.call().data;
                    md = Xml.loadString(new String(data, StandardCharsets.UTF_8), false);
                } catch (Exception e) {
                    response.setStatus(HttpStatus.SC_NOT_FOUND);
                    return String.format("Metadata with id '%s' is in schema '%s'. No iso19139 formatter found. Error is %s", id, schema, e.getMessage());
                }
            } else {
                // Cleanup metadocument elements
                EditLib editLib = appContext.getBean(DataManager.class).getEditLib();
                editLib.removeEditingInfo(md);
                editLib.contractElements(md);
            }


            if (StringUtils.isNotEmpty(processing)) {
                Path xslProcessing = schemaManager.getSchemaDir(schema).resolve(processing);
                md = Xml.transform(md, xslProcessing);
            }

            md.detach();
            Attribute schemaLocAtt = schemaManager.getSchemaLocation(
                "iso19139", context);

            if (schemaLocAtt != null) {
                if (md.getAttribute(
                    schemaLocAtt.getName(),
                    schemaLocAtt.getNamespace()) == null) {
                    md.setAttribute(schemaLocAtt);
                    // make sure namespace declaration for schemalocation is present -
                    // remove it first (does nothing if not there) then add it
                    md.removeNamespaceDeclaration(schemaLocAtt.getNamespace());
                    md.addNamespaceDeclaration(schemaLocAtt.getNamespace());
                }
            }


            InputStream metadataToTest = convertElement2InputStream(md);
            testId = inspireValidatorUtils.submitFile(context, inspireValidatorUrl, inspireValidatorQueryUrl, metadataToTest, testsuite, metadata.getUuid());
        } else {
            String portal = NodeInfo.DEFAULT_NODE;
            if (!NodeInfo.DEFAULT_NODE.equals(mode)) {
                Source source = sourceRepository.findOneByUuid(mode);
                if (source == null) {
                    response.setStatus(HttpStatus.SC_NOT_FOUND);
                    return String.format(
                        "Portal %s not found. There is no CSW endpoint at this URL " +
                            "that we can send to the validator.", mode);
                }
                portal = mode;
            }
            getRecordByIdUrl = String.format(
                "%s%s/eng/csw?SERVICE=CSW&REQUEST=GetRecordById&VERSION=2.0.2&" +
                    "OUTPUTSCHEMA=%s&ELEMENTSETNAME=full&ID=%s",
                settingManager.getBaseURL(),
                portal,
                ISO19139Namespaces.GMD.getURI(),
                metadataUuid);
            testId = inspireValidatorUtils.submitUrl(context, inspireValidatorUrl, inspireValidatorQueryUrl, getRecordByIdUrl, testsuite, metadata.getUuid());
        }

        threadPool.runTask(new InspireValidationRunnable(context, inspireValidatorUrl, testId, metadata.getId()));

        return testId;
    }

    private InputStream convertElement2InputStream(Element md)
        throws TransformerFactoryConfigurationError, TransformerException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLOutputter xmlOutput = new XMLOutputter();
        try {
            xmlOutput.output(new Document(md), outputStream);
        } catch (IOException e) {
            Log.error(Log.SERVICE, "Error in conversion of document before sending", e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check the status of validation with the INSPIRE service.",
        description = "User MUST be able to edit the record to validate it. "
            + "An INSPIRE endpoint must be configured in Settings. "
            + "If the process is complete an object with status is returned. ")
    @GetMapping(value = "/{testId}/validate/inspire",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report ready."),
        @ApiResponse(responseCode = "201", description = "Report not ready."),
        @ApiResponse(responseCode = "404", description = "Report id not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    Map<String, String> checkValidation(
        @Parameter(
            description = "Test identifier",
            required = true)
        @PathVariable
            String testId,
        HttpServletRequest request,
        @Parameter(hidden = true)
            HttpServletResponse response,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {

        String inspireValidatorUrl = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);
        ServiceContext context = ApiUtils.createServiceContext(request);

        try {
            if (inspireValidatorUtils.isReady(context, inspireValidatorUrl, testId)) {
                Map<String, String> values = new HashMap<>();

                values.put("status", inspireValidatorUtils.isPassed(context, inspireValidatorUrl, testId));
                values.put("report", inspireValidatorUtils.getReportUrl(inspireValidatorUrl, testId));
                response.setStatus(HttpStatus.SC_OK);

                return values;
            }
        } catch (ResourceNotFoundException e) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return new HashMap<>();
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return new HashMap<>();
        }

        response.setStatus(HttpStatus.SC_CREATED);
        return new HashMap<>();
    }
}
