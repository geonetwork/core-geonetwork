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

package org.fao.geonet.api.csw;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.JeevesEngine;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Service;
import org.fao.geonet.repository.ServiceRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = {
    "/api/csw/virtual",
    "/api/" + API.VERSION_0_1 +
        "/csw/virtual"
})
@Api(value = "csw",
    tags = "csw",
    description = "CSW operations")
@Controller("cswVirtual")
public class VirtualCswApi {

    @ApiOperation(
        value = "Get virtual CSW services",
        notes = "",
        nickname = "getVirtualCsws")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Service> getVirtualCsw() throws Exception {
        ServiceRepository serviceRepository =
            ApplicationContextHolder.get().getBean(ServiceRepository.class);
        return serviceRepository.findAll();
    }

    @ApiOperation(
        value = "Get a virtual CSW",
        notes = "",
        nickname = "getVirtualCsw")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Service getVirtualCsw(
        @ApiParam(
            name = "Service identifier",
            required = true
        )
        @PathVariable
            int identifier
    ) throws Exception {
        ServiceRepository serviceRepository =
            ApplicationContextHolder.get().getBean(ServiceRepository.class);
        return serviceRepository.findOne(identifier);
    }


    @ApiOperation(
        value = "Add a virtual CSW",
        notes = "",
        nickname = "addVirtualCsw")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity addVirtualCsw(
        @ApiParam(
            name = "Service details",
            required = true
        )
        @RequestBody
            Service service
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceRepository serviceRepository =
            applicationContext.getBean(ServiceRepository.class);

        Service existing = serviceRepository.findOneByName(service.getName());
        if (existing != null) {
            throw new IllegalArgumentException(String.format(
                "A service already exist with this name '%s'. Choose another name.",
                service.getName()));
        }
        serviceRepository.save(service);
        applicationContext.getBean(JeevesEngine.class)
            .loadConfigDB(applicationContext, service.getId());
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Update a virtual CSW",
        notes = "",
        nickname = "updateVirtualCsw")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity updateVirtualCsw(
        @ApiParam(
            name = "Service identifier",
            required = true
        )
        @PathVariable
            int identifier,
        @RequestBody
            Service service
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceRepository serviceRepository =
            applicationContext.getBean(ServiceRepository.class);

        Service existing = serviceRepository.findOne(identifier);
        if (existing != null) {
            serviceRepository.save(service);
        } else {
            throw new ResourceNotFoundException(String.format(
                "Virtual CSW with id '%d' does not exist.",
                identifier
            ));
        }
        applicationContext.getBean(JeevesEngine.class)
            .loadConfigDB(applicationContext, identifier);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Remove a virtual CSW",
        notes = "",
        nickname = "deleteVirtualCsw")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity deleteVirtualCsw(
        @ApiParam(
            name = "Service identifier",
            required = true
        )
        @PathVariable
            int identifier
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceRepository serviceRepository =
            applicationContext.getBean(ServiceRepository.class);
        serviceRepository.delete(identifier);

        applicationContext.getBean(JeevesEngine.class)
            .loadConfigDB(applicationContext, Integer.valueOf(identifier));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
