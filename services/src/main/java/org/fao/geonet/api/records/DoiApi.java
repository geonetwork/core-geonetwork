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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.doi.client.DoiManager;
import org.fao.geonet.domain.AbstractMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

/**
 * Handle DOI creation.
 */
@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("doi")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class DoiApi {

    @Autowired
    private DoiManager doiManager;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check that a record can be submitted to DataCite for DOI creation. " +
            "DataCite requires some fields to be populated.")
    @RequestMapping(value = "/{metadataUuid:.+}/doi/checkPreConditions",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record can be proposed to DataCite."),
        @ApiResponse(responseCode = "404", description = "Metadata not found."),
        @ApiResponse(responseCode = "400", description = "Record does not meet preconditions. Check error message."),
        @ApiResponse(responseCode = "500", description = "Service unavailable."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    ResponseEntity<Map<String, Boolean>> checkDoiStatus(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        final Map<String, Boolean> reportStatus = doiManager.check(serviceContext, metadata, null);
        return new ResponseEntity<>(reportStatus, HttpStatus.OK);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check the DOI URL created based on current configuration and pattern.")
    @RequestMapping(value = "/{metadataUuid}/doi/checkDoiUrl",
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "DOI URL created."),
        @ApiResponse(responseCode = "404", description = "Metadata not found."),
        @ApiResponse(responseCode = "500", description = "Service unavailable."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    ResponseEntity<String> checkDoiUrl(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        return new ResponseEntity<>(doiManager.checkDoiUrl(metadata), HttpStatus.OK);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Submit a record to the Datacite metadata store in order to create a DOI.")
    @RequestMapping(value = "/{metadataUuid:.+}/doi",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Check status of the report."),
        @ApiResponse(responseCode = "404", description = "Metadata not found."),
        @ApiResponse(responseCode = "500", description = "Service unavailable."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    ResponseEntity<Map<String, String>> createDoi(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        Map<String, String> doiInfo = doiManager.register(serviceContext, metadata);
        return new ResponseEntity<>(doiInfo, HttpStatus.CREATED);
    }



    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a DOI (this is not recommended, DOI are supposed to be persistent once created. This is mainly here for testing).")
    @RequestMapping(value = "/{metadataUuid:.+}/doi",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "DOI unregistered."),
        @ApiResponse(responseCode = "404", description = "Metadata or DOI not found."),
        @ApiResponse(responseCode = "500", description = "Service unavailable."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public
    @ResponseBody
    ResponseEntity unregisterDoi(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
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
