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
package org.fao.geonet.services.inspireatom;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.inspireatom.harvester.InspireAtomHarvester;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(value = {
    "/{portal}/api/atom"
})
@Tag(name = "atom",
    description = "ATOM")
@RestController
public class AtomHarvester {
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Scan records for ATOM feeds",
        description = "Check in the settings which protocol identify ATOM feeds in your catalogue." +
            "Only applies to ISO19139 records.")
    @GetMapping(
        value = "/scan",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Scan completed."),
        @ApiResponse(responseCode = "204", description = "Not authenticated.", content = {@Content(schema = @Schema(hidden = true))})
    })
    @ResponseStatus(CREATED)
    @ResponseBody
    public Object scan(
        @Parameter(hidden = true)
            HttpServletRequest request) throws IOException {
        ServiceContext context = ApiUtils.createServiceContext(request);
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        InspireAtomHarvester inspireAtomHarvester = new InspireAtomHarvester(gc);
        Element scanReport = inspireAtomHarvester.harvest();
        return Xml.getJSON(scanReport);
    }
}
