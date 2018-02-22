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

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.HttpStatus;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.editing.InspireValidatorUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.NotFoundException;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
        "/api/records",
        "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
tags = API_CLASS_RECORD_TAG)
@Controller("inspire")
@PreAuthorize("hasRole('Editor')")
@ReadWriteController
public class InspireValidationApi {

    @ApiOperation(
            value = "Submit a record to the INSPIRE service for validation.",
            notes = "User MUST be able to edit the record to validate it. "
                    + "An INSPIRE endpoint must be configured in Settings. "
                    + "This activates an asyncronous process, this method does not return any report. "
                    + "This method returns an id to be used to get the report.",
                    nickname = "submitValidate")
    @RequestMapping(value = "/{metadataUuid}/validate/inspire",
    method = RequestMethod.PUT,
    produces = {
            MediaType.TEXT_PLAIN_VALUE
    }
            )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Check status of the report."),
            @ApiResponse(code = 404, message = "Metdata not found."),
            @ApiResponse(code = 500, message = "Service unavailable."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    String validateRecord(
            @ApiParam(
                    value = API_PARAM_RECORD_UUID,
                    required = true)
            @PathVariable
            String metadataUuid,
            HttpServletResponse response,
            @ApiParam(hidden = true)
            @ApiIgnore
            HttpServletRequest request,
            @ApiParam(hidden = true)
            @ApiIgnore
            HttpSession session
            ) throws Exception {

        ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        if(metadata==null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return "";
        }

        if(!metadata.getDataInfo().getSchemaId().startsWith("iso19139")) {
            response.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
            return "";
        }

        String id = String.valueOf(metadata.getId());

        String URL = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);

        try {
            Element md = (Element) ApiUtils.getUserSession(session).getProperty(Geonet.Session.METADATA_EDITING + id);
            if (md == null) {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return "";
            }
            md.detach();
            ServiceContext context = ApiUtils.createServiceContext(request);
            Attribute schemaLocAtt =  appContext.getBean(SchemaManager.class).getSchemaLocation(
                    metadata.getDataInfo().getSchemaId(), context);

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

            EditLib editLib = appContext.getBean(DataManager.class).getEditLib();

            editLib.removeEditingInfo(md);
            editLib.contractElements(md);

            InputStream metadataToTest = convertElement2InputStream(md);

            String testId = InspireValidatorUtils.submitFile(URL, metadataToTest, metadata.getUuid());

            return testId;
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return "";
        }
    }

    private InputStream convertElement2InputStream(Element md)
            throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLOutputter xmlOutput = new XMLOutputter();
        try {
            xmlOutput.output(new Document(md), outputStream);
        } catch (IOException e) {
            Log.error(Log.SERVICE, "Error in conversion of document before sending", e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @ApiOperation(
            value = "Check the status of validation with the INSPIRE service.",
            notes = "User MUST be able to edit the record to validate it. "
                    + "An INSPIRE endpoint must be configured in Settings. "
                    + "If the process is complete an object with status is returned. ",
                    nickname = "checkValidateStatus")
    @RequestMapping(value = "/{testId}/validate/inspire",
    method = RequestMethod.GET,
    produces = {
            MediaType.APPLICATION_JSON_VALUE
    }
            )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Report ready."),
            @ApiResponse(code = 201, message = "Report not ready."),
            @ApiResponse(code = 404, message = "Report id not found."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    Map<String, String> checkValidation(
            @ApiParam(
                    value = "Test identifier",
                    required = true)
            @PathVariable
            String testId,
            HttpServletRequest request,
            @ApiParam(hidden = true)
            @ApiIgnore
            HttpServletResponse response,
            @ApiParam(hidden = true)
            @ApiIgnore
            HttpSession session
            ) throws Exception {

        ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        String URL = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);

        try {
            if (InspireValidatorUtils.isReady(URL, testId, null)) {
                Map<String, String> values = new HashMap<>();

                values.put("status", InspireValidatorUtils.isPassed(URL, testId, null));
                values.put("report", InspireValidatorUtils.getReportUrl(URL, testId));
                response.setStatus(HttpStatus.SC_OK);

                return values;
            }
        } catch (NotFoundException e) {
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
