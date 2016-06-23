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

package org.fao.geonet.api.mapservers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Util;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.standards.StandardsUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.MapServer;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.editorconfig.BatchEditing;
import org.fao.geonet.kernel.schema.editorconfig.Editor;
import org.fao.geonet.kernel.schema.labels.Codelists;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.MapServerRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 *
 */

@RequestMapping(value = {
    "/api/mapservers",
    "/api/" + API.VERSION_0_1 +
        "/mapservers"
})
@Api(value = "mapservers",
    tags = "mapservers",
    description = "Mapservers related operations")
@Controller("mapservers")
public class MapServersApi {

    public static final String API_PARAM_MAPSERVER_IDENTIFIER = "Mapserver identifier";
    @Autowired
    LanguageUtils languageUtils;

    @ApiOperation(value = "Get mapservers",
        nickname = "getMapservers")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<MapServer> getMapservers() throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();

        List<MapServer> mapServers = applicationContext.getBean(MapServerRepository.class)
            .findAll();
        mapServers.stream().forEach(e -> anonymize(e));
        return mapServers;
    }

    @ApiOperation(value = "Get mapserver ",
        nickname = "getMapserver")
    @RequestMapping(value = "/{mapserverId}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public MapServer getMapserver(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String mapserverId
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        MapServer mapserver =
            applicationContext.getBean(MapServerRepository.class)
                .findOneById(mapserverId);
        return anonymize(mapserver);
    }

    private MapServer anonymize(MapServer mapserver) {
        return mapserver
            .setPassword("****")
            .setUsername("****");
    }


    @ApiOperation(value = "Add a mapserver",
        nickname = "addMapserver")
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public ResponseEntity<Integer> addMapserver(
        @ApiParam(value = "Mapserver details",
            required = true)
        @RequestBody
            MapServer mapserver
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        MapServerRepository repo =
            applicationContext.getBean(MapServerRepository.class);

        MapServer existingMapserver = repo.findOneById(mapserver.getId());
        if (existingMapserver != null) {

        } else {
            MapServer m = repo.save(mapserver);
        }
        return new ResponseEntity<>(mapserver.getId(), HttpStatus.CREATED);
    }


    @ApiOperation(value = "Update a mapserver",
        nickname = "updateMapserver")
    @RequestMapping(
        value = "/{mapserverId}",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public ResponseEntity<Integer> updateMapserver(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable Integer mapserverId,
        @ApiParam(value = "Mapserver details",
            required = true)
        @RequestBody
            MapServer mapserver
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        MapServerRepository repo =
            applicationContext.getBean(MapServerRepository.class);

        MapServer existingMapserver = repo.findOneById(mapserverId);
        if (existingMapserver != null) {
            updateMapserver(mapserverId, mapserver, repo);
        } else {
            throw new ResourceNotFoundException(String.format(
                "Mapserver with id '%d' does not exist.",
                mapserverId
            ));
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(value = "Update a mapserver authentication",
        nickname = "updateMapserverAuth")
    @RequestMapping(
        value = "/{mapserverId}/auth",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public ResponseEntity updateMapserver(
        @ApiParam(
            value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable Integer mapserverId,
        @ApiParam(
            value = "User name",
            required = true)
        @RequestParam
            String username,
        @ApiParam(
            value = "User password",
            required = true)
        @RequestParam
            String password
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        MapServerRepository repository =
            applicationContext.getBean(MapServerRepository.class);

        MapServer existingMapserver = repository.findOneById(mapserverId);
        if (existingMapserver != null) {
            repository.update(mapserverId, entity -> {
            entity.setUsername(username);
            entity.setPassword(password);
            });
        } else {
            throw new ResourceNotFoundException(String.format(
                "Mapserver with id '%d' does not exist.",
                mapserverId
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    private void updateMapserver(
        int mapserverId,
        final MapServer mapServer,
        MapServerRepository repository) {
        repository.update(mapserverId, entity -> {
            entity.setName(mapServer.getName());
            entity.setConfigurl(mapServer.getConfigurl());
            entity.setWmsurl(mapServer.getWmsurl());
            entity.setWfsurl(mapServer.getWfsurl());
            entity.setWcsurl(mapServer.getWcsurl());
            entity.setStylerurl(mapServer.getStylerurl());
            entity.setDescription(mapServer.getDescription());
            entity.setNamespace(mapServer.getNamespace());
            entity.setNamespacePrefix(mapServer.getNamespacePrefix());
            entity.setPushStyleInWorkspace(mapServer.pushStyleInWorkspace());
        });
    }


    @ApiOperation(value = "Delete a mapserver",
        nickname = "deleteMapserver")
    @RequestMapping(
        value = "/{mapserverId}",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public ResponseEntity deleteMapserver(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable Integer mapserverId
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        MapServerRepository repo =
            applicationContext.getBean(MapServerRepository.class);
        MapServer m = repo.findOneById(mapserverId);
        if (m != null) {
            repo.delete(m);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }




    @ApiOperation(value = "Get mapserver resource",
        nickname = "getMapserverResource")
    @RequestMapping(value = "/{mapserverId}/resources/{resource}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
        })
    @ResponseBody
    public boolean getMapserverResource(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String mapserverId,
        @ApiParam(
            value = "Layer name"
        )
        @PathVariable String resource,
        HttpServletRequest request
    ) throws Exception {

        return true;
    }

}
