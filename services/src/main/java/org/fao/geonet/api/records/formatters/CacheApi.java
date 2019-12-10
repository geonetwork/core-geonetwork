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

package org.fao.geonet.api.records.formatters;

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jeeves.services.ReadWriteController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static org.fao.geonet.api.records.formatters.cache.FormatterCache.initializeFormatters;

@RequestMapping(value = {
    "/{portal}/api/formatters",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/formatters"
})
@Api(value = "formatters",
    tags = "formatters",
    description = "Formatter operations")
@Controller("formatters")
@ReadWriteController
public class CacheApi {
    @Autowired
    FormatterCache formatterCache;

    @ApiOperation(
        value = "Get formatter cache info",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "getFormatterCacheInfo")
    @RequestMapping(
        value = "/cache",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cache info returned."),
        @ApiResponse(code = 403, message = "Operation not allowed. Only Administrator can access it.")
    })
    @ResponseBody
    public Map<String, String> getFormatterCacheInfo() {
        return formatterCache.getInfo();
    }

    @ApiOperation(
        value = "Clear formatter cache",
        notes = "Formatters are used to render records in various format (HTML, PDF, ...). " +
            "When a record is rendered a cache is populated for better performance. " +
            "By default the cache is an H2 database with files on the filesystems " +
            "(See <dataDirectory>/resources/htmlcache/formatter-cache folder).",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "clearFormatterCache")
    @RequestMapping(
        value = "/cache",
        method = RequestMethod.DELETE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Cache cleared."),
        @ApiResponse(code = 403, message = "Operation not allowed. Only Administrator can access it.")
    })
    public void clearFormatterCache() throws Exception {
        formatterCache.clear();
    }


    @ApiOperation(
        value = "Rebuild landing page cache",
        notes = "Build cache for public metadata records",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "rebuildLandingPageCache")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Formatter cache builder process started."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity rebuildLandingPageCache(
        @ApiIgnore
            HttpServletRequest request
    ) throws Exception {
        formatterCache.fillLandingPageCache(ApiUtils.createServiceContext(request));
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
