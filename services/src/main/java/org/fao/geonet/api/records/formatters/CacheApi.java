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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import static org.fao.geonet.api.ApiParams.API_CLASS_FORMATTERS_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_FORMATTERS_TAG;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping(value = {
    "/{portal}/api/formatters"
})
@Tag(name = API_CLASS_FORMATTERS_TAG,
    description = API_CLASS_FORMATTERS_OPS)
@Controller("formatters")
@ReadWriteController
public class CacheApi {


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Clear formatter cache",
        description = "Formatters are used to render records in various format (HTML, PDF, ...). " +
            "When a record is rendered a cache is populated for better performance. " +
            "By default the cache is an H2 database with files on the filesystems " +
            "(See <dataDirectory>/resources/htmlcache/formatter-cache folder)."
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/cache",
        method = RequestMethod.DELETE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cache cleared.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Administrator can access it.")
    })
    public void clearFormatterCache() throws Exception {
        FormatterCache formatterCache = ApplicationContextHolder.get().getBean(FormatterCache.class);
        formatterCache.clear();
    }
}
