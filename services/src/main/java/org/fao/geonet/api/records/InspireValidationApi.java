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
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.editing.InspireValidatorUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
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
import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
        "/api/inspire",
        "/api/" + API.VERSION_0_1 +
        "/inspire"
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
    @RequestMapping(value = "/{metadataUuid}/validate/submit",
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
            return null;
        }

        String id = String.valueOf(metadata.getId());

        String URL = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);

        try {
            Element md = (Element) ApiUtils.getUserSession(session).getProperty(Geonet.Session.METADATA_EDITING + id);
            if (md == null) {
                throw new ResourceNotFoundException(String.format("Requested metadata with id '%s' is not available in current session. "
                        + "Open an editing session on this record first.", id));
            }
            md.detach();

            InputStream metadataToTest = convertElement2InputStream(md);

            String testId = InspireValidatorUtils.submitFile(URL, metadataToTest, metadata.getUuid());

            return testId;
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return null;
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
    @RequestMapping(value = "/{testId}/validate/check",
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
            return null;
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        response.setStatus(HttpStatus.SC_CREATED);
        return null;
    }




}

