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

package org.fao.geonet.api.oaipmh;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.oaipmh.OaiPmhParams;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping(value = {
    "/{portal}/api/oaipmh"
})
@Tag(name = "oaipmh",
    description = "OAI-PMH server")
@RestController("oaipmh")
public class OaiPmhApi {

    @Autowired
    private OaiPmhDispatcher oaiPmhDispatcher;

    @Operation(
        summary = "OAI-PMH server endpoint",
        description = "[More information](https://docs.geonetwork-opensource.org/4.4/api/oai-pmh/)")
    @GetMapping(
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OAI-PMH server response.")
    })
    @ResponseBody
    public Element dispatch(
        final OaiPmhParams oaiPmhParams,
        final HttpServletRequest request
    ) {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        // Set the service name, used in OaiPmhDispatcher to build the oaiphm endpoint URL
        serviceContext.setService("api/oaipmh");

        return oaiPmhDispatcher.dispatch(oaiPmhParams, serviceContext);
    }
}
