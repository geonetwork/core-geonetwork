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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.services.ReadWriteController;

@RequestMapping(value = {
    "/api/formatters",
    "/api/" + API.VERSION_0_1 +
        "/formatters"
})
@Api(value = "formatters",
    tags = "formatters",
    description = "Formatter operations")
@Controller("formatters")
@ReadWriteController
public class CacheApi {


    @ApiOperation(value = "Clear formatter cache",
        nickname = "clearCache")
    @RequestMapping(
        value = "/cache",
        method = RequestMethod.DELETE
    )
    public ResponseEntity clear() throws Exception {
        FormatterCache formatterCache = ApplicationContextHolder.get().getBean(FormatterCache.class);
        formatterCache.clear();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
