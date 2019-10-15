//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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
package org.fao.geonet.guiservices.csw.virtual;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.responses.CswVirtualServiceListResponse;
import org.fao.geonet.repository.ServiceRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Get the list of service define in DB configuration
 */
@Controller("admin.config.virtualcsw.list")
@Deprecated
public class List {
    @RequestMapping(value = "/{portal}/{lang}/admin.config.virtualcsw.list", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    CswVirtualServiceListResponse exec() throws Exception {
        ServiceRepository serviceRepository = ApplicationContextHolder.get().getBean(ServiceRepository.class);
        CswVirtualServiceListResponse cswVirtualList =
            new CswVirtualServiceListResponse(serviceRepository.findAll());

        return cswVirtualList;
    }
}
