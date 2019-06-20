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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.doi.client.DoiManager;
import org.fao.geonet.domain.AbstractMetadata;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Map;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

/**
 * Handle DOI creation.
 */
@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG)
@Controller("doi")
@PreAuthorize("hasRole('Editor')")
@ReadWriteController
public class DoiApi {

    @Autowired
    private DoiManager doiManager;

    @ApiOperation(
        value = "Check that a record can be submitted to DataCite for DOI creation. " +
            "DataCite requires some fields to be populated.",
        nickname = "checkDoiStatus")
    @RequestMapping(value = "/{metadataUuid}/doi/checkPreConditions",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Record can be proposed to DataCite."),
        @ApiResponse(code = 404, message = "Metadata not found."),
        @ApiResponse(code = 400, message = "Record does not meet preconditions. Check error message."),
        @ApiResponse(code = 500, message = "Service unavailable."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    ResponseEntity<Map<String, Boolean>> checkDoiStatus(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        final Map<String, Boolean> reportStatus = doiManager.check(serviceContext, metadata, null);
        return new ResponseEntity<>(reportStatus, HttpStatus.OK);
    }


    @ApiOperation(
        value = "Submit a record to the Datacite metadata store in order to create a DOI.",
        nickname = "createDoi")
    @RequestMapping(value = "/{metadataUuid}/doi",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Check status of the report."),
        @ApiResponse(code = 404, message = "Metadata not found."),
        @ApiResponse(code = 500, message = "Service unavailable."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    ResponseEntity<Map<String, String>> createDoi(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpServletRequest request,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpSession session
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        Map<String, String> doiInfo = doiManager.register(serviceContext, metadata);
        return new ResponseEntity<>(doiInfo, HttpStatus.CREATED);
    }

//    Do not provide support for DOI removal ?
    @ApiOperation(
        value = "Remove a DOI (this is not recommended, DOI are supposed to be persistent once created. This is mainly here for testing).",
        nickname = "deleteDoi")
    @RequestMapping(value = "/{metadataUuid}/doi",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "DOI unregistered."),
        @ApiResponse(code = 404, message = "Metadata or DOI not found."),
        @ApiResponse(code = 500, message = "Service unavailable."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public
    ResponseEntity deleteDoi(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpServletRequest request,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpSession session
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        doiManager.unregisterDoi(metadata, serviceContext);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


//    TODO: At some point we may add support for DOI States management
//    https://support.datacite.org/docs/mds-api-guide#section-doi-states
}
