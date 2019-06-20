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

package org.fao.geonet.api.registries;

import io.swagger.annotations.*;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.registries.model.Crs;
import org.fao.geonet.api.registries.model.CrsType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/registries",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/registries"
})
@Api(value = ApiParams.API_CLASS_REGISTRIES_TAG,
    tags = ApiParams.API_CLASS_REGISTRIES_TAG,
    description = ApiParams.API_CLASS_REGISTRIES_OPS)
public class CrsApi {

    public static final String DEFAULT_PARAMS_ROWS = "100";

    /**
     * Get list of CRS type.
     */
    @ApiOperation(value = "Get list of CRS type",
        nickname = "getCrsType",
        notes = "")
    @RequestMapping(
        value = "/crs/types",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of CRS types.")
    })
    @ResponseBody
    public List<CrsType> getCrsTypes()
        throws Exception {
        return Arrays.asList(CrsType.values());
    }


    /**
     * Search coordinate reference system.
     */
    @ApiOperation(
        value = "Search coordinate reference system (CRS)",
        nickname = "searchCrs",
        notes = "Based on GeoTools EPSG database. If phrase query, each words " +
            "are searched separately.")
    @RequestMapping(
        value = "/crs",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of CRS.")
    })
    @ResponseBody
    public List<Crs> getCrs(
        @ApiParam(
            value = "Search value",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "")
        final String q,
        @ApiParam(
            value = "Type of CRS",
            required = false)
        @RequestParam(
            required = false)
        final CrsType type,
        @ApiParam(
            value = "Number of results. Default is: " + DEFAULT_PARAMS_ROWS,
            required = false)
        @RequestParam(
            required = false,
            defaultValue = DEFAULT_PARAMS_ROWS)
        final int rows)
        throws Exception {
        List<Crs> crsList = CrsUtils.search(q.split(" "), type, rows);
        return crsList;
    }


    /**
     * Get coordinate reference system.
     */
    @ApiOperation(
        value = "Get CRS",
        nickname = "getCrsById",
        notes = "")
    @RequestMapping(
        value = "/crs/{id}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "CRS details."),
        @ApiResponse(code = 404, message = "CRS not found.")
    })
    @ResponseBody
    public Crs getCrs(
        @ApiParam(
            value = "CRS identifier",
            required = true)
        @PathVariable
        final String id)
        throws Exception {
        Crs crs = CrsUtils.getById(id);
        if (crs != null) {
            return crs;
        } else {
            throw new ResourceNotFoundException(String.format(
                "CRS with id '%s' not found in EPSG database", id));
        }
    }
}
