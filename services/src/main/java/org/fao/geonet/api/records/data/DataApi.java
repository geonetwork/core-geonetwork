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
package org.fao.geonet.api.records.data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.data.GdalMetadataExtractor;
import org.fao.geonet.data.model.gdal.GdalDataset;
import org.fao.geonet.domain.AbstractMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.fao.geonet.api.ApiParams.*;

/**
 * Data analysis
 */
@RequestMapping(value = {
    "/{portal}/api/data"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("data")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class DataApi {

    @Autowired
    private GdalMetadataExtractor gdalMetadataExtractor;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Return status of analyzer.")
    @RequestMapping(value = "/analyzer/status",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analyzer status.")
    })
    public
    @ResponseBody
    ResponseEntity<Map<String, String>> analyzerStatus() {
        Map<String, String> status = new HashMap<>();
        try {
            status.put("gdalMetadataExtractor", gdalMetadataExtractor.getVersion());
        } catch (IOException e) {
            status.put("gdalMetadataExtractor", null);
        }
        return new ResponseEntity<>(status, HttpStatus.OK);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Analyze a file or datasource related to that record.")
    @RequestMapping(value = "/{metadataUuid}/data/analyze",
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
    ResponseEntity<GdalDataset> analyze(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Datasource",
            required = true)
        @RequestParam(name = "datasource")
            String datasource,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        return new ResponseEntity<>(gdalMetadataExtractor.analyze(datasource), HttpStatus.OK);
    }
}
