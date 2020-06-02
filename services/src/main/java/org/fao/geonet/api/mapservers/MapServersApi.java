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

import io.swagger.annotations.*;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.mapservers.model.AnonymousMapserver;
import org.fao.geonet.api.records.attachments.FilesystemStore;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.MapServer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MapServerRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.api.mapservers.MapServersUtils.*;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/mapservers",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/mapservers"
})
@Api(value = "mapservers",
    tags = "mapservers",
    description = "Mapservers related operations")
@Controller("mapservers")
public class MapServersApi {

    public static final String API_PARAM_MAPSERVER_IDENTIFIER = "Mapserver identifier";
    public static final String API_PARAM_MAPSERVER_DETAILS = "Mapserver details";
    public static final String MSG_MAPSERVER_WITH_ID_NOT_FOUND = "Mapserver with id '%s' not found.";
    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    MapServerRepository mapServerRepository;

    @Autowired
    @Qualifier("resourceStore")
    Store store;

    @Autowired
    SettingManager settingManager;

    @Autowired
    GeonetHttpRequestFactory requestFactory;

    @ApiOperation(
        value = "Get mapservers",
        notes = "Mapservers are used by the catalog to publish record attachements " +
            "(eg. ZIP file with shape) or record associated resources (eg. " +
            "database table, file on the local network) in a remote mapserver like " +
            "GeoServer or MapServer. The catalog communicate with the mapserver using " +
            "GeoServer REST API.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "getMapservers"
    )
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    List<AnonymousMapserver> getMapservers() throws Exception {
        List<MapServer> mapServers = mapServerRepository.findAll();
        List<AnonymousMapserver> list = new ArrayList<>(mapServers.size());
        mapServers.stream().forEach(e -> list.add(new AnonymousMapserver(e)));
        return list;
    }

    @ApiOperation(
        value = "Get a mapserver",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "getMapserver"
    )
    @RequestMapping(value = "/{mapserverId}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public AnonymousMapserver getMapserver(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String mapserverId
    ) throws Exception {
        MapServer mapserver = mapServerRepository.findOneById(mapserverId);
        if (mapserver == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_MAPSERVER_WITH_ID_NOT_FOUND,
                mapserverId
            ));
        } else {
            return new AnonymousMapserver(mapserver);
        }
    }


    @ApiOperation(
        value = "Add a mapserver",
        notes = "Return the id of the newly created mapserver.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "addMapserver"
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Mapserver created."),
        @ApiResponse(code = 400, message = "Bad parameters.") ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<Integer> addMapserver(
        @ApiParam(
            value = API_PARAM_MAPSERVER_DETAILS,
            required = true
        )
        @RequestBody
            MapServer mapserver
    ) throws Exception {
        MapServer existingMapserver = mapServerRepository.findOneById(mapserver.getId());
        if (existingMapserver != null) {
            throw new IllegalArgumentException(String.format(
                "Mapserver with id '%d' already exists.",
                mapserver.getId()
            ));
        } else {
            mapServerRepository.save(mapserver);
        }
        return new ResponseEntity<>(mapserver.getId(), HttpStatus.CREATED);
    }


    @ApiOperation(
        value = "Update a mapserver",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "updateMapserver"
    )
    @RequestMapping(
        value = "/{mapserverId}",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Mapserver updated."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateMapserver(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable Integer mapserverId,
        @ApiParam(value = API_PARAM_MAPSERVER_DETAILS,
            required = true)
        @RequestBody
            MapServer mapserver
    ) throws Exception {
        MapServer existingMapserver = mapServerRepository.findOneById(mapserverId);
        if (existingMapserver != null) {
            updateMapserver(mapserverId, mapserver, mapServerRepository);
        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_MAPSERVER_WITH_ID_NOT_FOUND,
                mapserverId
            ));
        }
    }


    @ApiOperation(
        value = "Update a mapserver authentication",
        notes = "The remote mapserver REST API may require basic authentication. " +
            "This operation set the username and password.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "updateMapserverAuth")
    @RequestMapping(
        value = "/{mapserverId}/auth",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Mapserver updated."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMapserver(
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
            value = "Password",
            required = true)
        @RequestParam
            String password
    ) throws Exception {
        MapServer existingMapserver = mapServerRepository.findOneById(mapserverId);
        if (existingMapserver != null) {
            mapServerRepository.update(mapserverId, entity -> {
                entity.setUsername(username);
                entity.setPassword(password);
            });
        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_MAPSERVER_WITH_ID_NOT_FOUND,
                mapserverId
            ));
        }
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


    @ApiOperation(
        value = "Remove a mapserver",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "deleteMapserver")
    @RequestMapping(
        value = "/{mapserverId}",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Mapserver removed."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMapserver(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true
        )
        @PathVariable Integer mapserverId
    ) throws Exception {
        MapServer m = mapServerRepository.findOneById(mapserverId);
        if (m != null) {
            mapServerRepository.delete(m);
        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_MAPSERVER_WITH_ID_NOT_FOUND,
                mapserverId
            ));
        }
    }


    @ApiOperation(
        value = "Check metadata mapserver resource is published ",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "getMapserverResource"
    )
    @RequestMapping(value = "/{mapserverId}/records/{metadataUuid}",
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @ResponseBody
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public String getMapserverResource(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String mapserverId,
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true
        )
        @PathVariable String metadataUuid,
        @ApiParam(
            value = ApiParams.API_PARAM_MAPSERVER_RESOURCE,
            required = true
        )
        @RequestParam String resource,
        @ApiParam(
            value = ApiParams.API_PARAM_METADATA_TITLE
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String metadataTitle,
        @ApiParam(
            value = ApiParams.API_PARAM_METADATA_ABSTRACT
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String metadataAbstract,
        HttpServletRequest request
    ) throws Exception {
        final MapServersUtils.ACTION action = MapServersUtils.ACTION.GET;
        return publishResource(mapserverId, metadataUuid, resource, metadataTitle, metadataAbstract, request, action);
    }


    @ApiOperation(
        value = "Publish a metadata resource in a mapserver",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "publishMapserverResource"
    )
    @RequestMapping(value = "/{mapserverId}/records/{metadataUuid}",
        method = RequestMethod.PUT,
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @ResponseBody
    public String publishMapserverResource(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String mapserverId,
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true
        )
        @PathVariable String metadataUuid,
        @ApiParam(
            value = ApiParams.API_PARAM_MAPSERVER_RESOURCE,
            required = true
        )
        @RequestParam String resource,
        @ApiParam(
            value = ApiParams.API_PARAM_METADATA_TITLE
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String metadataTitle,
        @ApiParam(
            value = ApiParams.API_PARAM_METADATA_ABSTRACT
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String metadataAbstract,
        HttpServletRequest request
    ) throws Exception {
        final MapServersUtils.ACTION action = MapServersUtils.ACTION.CREATE;
        return publishResource(mapserverId, metadataUuid, resource, metadataTitle, metadataAbstract, request, action);
    }


    @ApiOperation(
        value = "Remove a metadata mapserver resource",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "deleteMapserverResource")
    @RequestMapping(
        value = "/{mapserverId}/records/{metadataUuid}",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @ResponseBody
    public String deleteMapserverResource(
        @ApiParam(value = API_PARAM_MAPSERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String mapserverId,
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true
        )
        @PathVariable String metadataUuid,
        @ApiParam(
            value = ApiParams.API_PARAM_MAPSERVER_RESOURCE,
            required = true
        )
        @RequestParam String resource,
        @ApiParam(
            value = ApiParams.API_PARAM_METADATA_TITLE
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String metadataTitle,
        @ApiParam(
            value = ApiParams.API_PARAM_METADATA_ABSTRACT
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String metadataAbstract,
        HttpServletRequest request
    ) throws Exception {
        final MapServersUtils.ACTION action = MapServersUtils.ACTION.DELETE;
        return publishResource(mapserverId, metadataUuid, resource, metadataTitle, metadataAbstract, request, action);
    }


    private String publishResource(String mapserverId, String metadataUuid,
                                   String resource,
                                   String metadataTitle,
                                   String metadataAbstract,
                                   HttpServletRequest request,
                                   MapServersUtils.ACTION action) throws Exception {
        // purge \\n from metadataTitle - geoserver prefers layer titles on a single line
        metadataTitle = metadataTitle.replace("\\n", "");
        metadataAbstract = metadataAbstract.replace("\\n", "");

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        MapServer m = mapServerRepository.findOneById(mapserverId);
        GeoServerNode g = new GeoServerNode(m);


        ServiceContext context = ApiUtils.createServiceContext(request);

        String baseUrl = settingManager.getSiteURL(context);
        GeoServerRest gs = new GeoServerRest(requestFactory, g.getUrl(),
            g.getUsername(), g.getUserpassword(),
            g.getNamespacePrefix(), baseUrl, settingManager.getNodeURL(),
            m.pushStyleInWorkspace());

//        String access = Util.getParam(params, "access");

        //jdbc:postgresql://host:port/user:password@database#table
        if (resource.startsWith("jdbc:postgresql")) {
            String[] values = resource.split("/");

            String[] serverInfo = values[2].split(":");
            String host = serverInfo[0];
            String port = serverInfo[1];

            String[] dbUserInfo = values[3].split("@");

            String[] userInfo = dbUserInfo[0].split(":");
            String user = userInfo[0];
            String password = userInfo[1];

            String[] dbInfo = dbUserInfo[1].split("#");
            String db = dbInfo[0];
            String table = dbInfo[1];

            return publishDbTable(action, gs,
                "postgis", host, port, user, password, db, table, "postgis",
                g.getNamespaceUrl(), metadataUuid, metadataTitle, metadataAbstract);
        } else {
            if (resource.startsWith("file://") || resource.startsWith("http://")) {
                return addExternalFile(action, gs,
                    resource,
                    metadataUuid, metadataTitle, metadataAbstract);
            } else {
                // Get ZIP file from data directory
                try (Store.ResourceHolder f = store.getResource(context, metadataUuid, resource)) {
                    return addZipFile(action, gs,
                                      f.getPath(), resource,
                                      metadataUuid, metadataTitle, metadataAbstract);
                }
            }
        }
    }
}
