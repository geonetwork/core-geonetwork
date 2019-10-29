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

package org.fao.geonet.services.system;

import com.vividsolutions.jts.util.Assert;

import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.responses.OkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Update the writable information in {@link org.fao.geonet.SystemInfo}.
 *
 * @author Jesse on 1/23/2015.
 */
@Controller("systeminfo/")
@Deprecated
public class UpdateSystemInfo {
    @Autowired
    private SystemInfo info;

    @RequestMapping(value = "/{portal}/{lang}/systeminfo/staging", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public OkResponse update(@RequestParam(required = true) String newProfile) {
        Assert.isTrue(!newProfile.isEmpty(), "newProfile must not be an empty string");
        this.info.setStagingProfile(newProfile);
        return new OkResponse();
    }
}
