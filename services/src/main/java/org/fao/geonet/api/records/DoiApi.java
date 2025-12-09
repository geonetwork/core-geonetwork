/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.doi.client.DoiManager;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.DoiServer;
import org.fao.geonet.repository.DoiServerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.api.doiservers.DoiServersApi.MSG_DOISERVER_WITH_ID_NOT_FOUND;

/**
 * Handle DOI creation.
 */
@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@RestController("doi")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class DoiApi {

    private final DoiManager doiManager;

    private final DoiServerRepository doiServerRepository;

    DoiApi(final DoiManager doiManager, final DoiServerRepository doiServerRepository) {
        this.doiManager = doiManager;
        this.doiServerRepository = doiServerRepository;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check that a record can be submitted to DataCite for DOI creation. " +
            "DataCite requires some fields to be populated.")
    @GetMapping(value = "/{metadataUuid}/doi/{doiServerId}/checkPreConditions",
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
    ResponseEntity<Map<String, Boolean>> checkDoiStatus(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "DOI server identifier",
            required = true)
        @PathVariable
            Integer doiServerId,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        DoiServer doiServer = retrieveDoiServer(doiServerId);
        final Map<String, Boolean> reportStatus = doiManager.check(serviceContext, doiServer, metadata, null);
        return new ResponseEntity<>(reportStatus, HttpStatus.OK);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check the DOI URL created based on current configuration and pattern.")
    @GetMapping(value = "/{metadataUuid}/doi/{doiServerId}/checkDoiUrl",
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
    ResponseEntity<String> checkDoiUrl(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "DOI server identifier",
            required = true)
        @PathVariable
            Integer doiServerId,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        DoiServer doiServer = retrieveDoiServer(doiServerId);
        return new ResponseEntity<>(doiManager.checkDoiUrl(doiServer, metadata), HttpStatus.OK);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Submit a record to the Datacite metadata store in order to create a DOI.")
    @PutMapping(value = "/{metadataUuid}/doi/{doiServerId}",
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
    ResponseEntity<Map<String, String>> createDoi(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "DOI server identifier",
            required = true)
        @PathVariable
            Integer doiServerId,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        DoiServer doiServer = retrieveDoiServer(doiServerId);
        Map<String, String> doiInfo = doiManager.register(serviceContext, doiServer, metadata);
        return new ResponseEntity<>(doiInfo, HttpStatus.CREATED);
    }



    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a DOI (this is not recommended, DOI are supposed to be persistent once created. This is mainly here for testing).")
    @DeleteMapping(value = "/{metadataUuid}/doi/{doiServerId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "DOI unregistered.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Metadata or DOI not found."),
        @ApiResponse(responseCode = "500", description = "Service unavailable."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public
    ResponseEntity<Void> unregisterDoi(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(
            description = "DOI server identifier",
            required = true)
        @PathVariable
            Integer doiServerId,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        DoiServer doiServer = retrieveDoiServer(doiServerId);
        doiManager.unregisterDoi(doiServer, metadata, serviceContext);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private DoiServer retrieveDoiServer(Integer doiServerId) throws ResourceNotFoundException {
        Optional<DoiServer> doiServerOpt = doiServerRepository.findOneById(doiServerId);
        if (doiServerOpt.isEmpty()) {
            throw new ResourceNotFoundException(String.format(
                MSG_DOISERVER_WITH_ID_NOT_FOUND,
                doiServerId
            ));
        }

        return doiServerOpt.get();
    }

//    TODO: At some point we may add support for DOI States management
//    https://support.datacite.org/docs/mds-api-guide#section-doi-states
}
