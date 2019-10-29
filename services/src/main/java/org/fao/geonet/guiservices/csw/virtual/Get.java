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
import org.fao.geonet.domain.Service;
import org.fao.geonet.domain.responses.CswVirtualServiceResponse;
import org.fao.geonet.repository.ServiceRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Retrieves a particular service
 */
@Controller("admin.config.virtualcsw.get")
@Deprecated
public class Get {

    @RequestMapping(value = "/{portal}/{lang}/admin.config.virtualcsw.get", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    CswVirtualServiceResponse exec(@RequestParam String id) throws Exception {

        ServiceRepository serviceRepository = ApplicationContextHolder.get().getBean(ServiceRepository.class);
        final Service service = serviceRepository.findOne(Integer.valueOf(id));

        CswVirtualServiceResponse response = new CswVirtualServiceResponse(service);

        return response;
    }
}
