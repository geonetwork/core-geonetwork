/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.sld;

import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_TAG;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.constants.Geonet;
import org.geonetwork.map.wms.SLDUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping(value = {
    "/{portal}/api/tools/ogc"
})
@Tag(name = API_CLASS_TOOLS_TAG,
    description = API_CLASS_TOOLS_OPS)
@Controller("tools")
public class SldApi {

    public static final String LOGGER = Geonet.GEONETWORK + ".api.sld";

    @io.swagger.v3.oas.annotations.Operation(summary = "Generate an OGC filter",
        description = "From a JSON filter, return an OGC filter expression.")
    @PostMapping(value = "/filter",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public
    @ResponseBody
    String buildFilter(
        @Parameter(description = "The filters in JSON",
            required = true)
        @RequestParam("filters") String filters) throws JSONException, IOException {

        Filter customFilter = SLDUtil.generateCustomFilter(new JSONObject(filters));
        return SLDUtil.encodeFilter(customFilter);
    }
}
