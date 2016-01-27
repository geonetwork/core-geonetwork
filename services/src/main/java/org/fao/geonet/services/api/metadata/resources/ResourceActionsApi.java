/*
 * =============================================================================
 * ===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.services.api.metadata.resources;

import io.swagger.annotations.*;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.thumbnail.ThumbnailMaker;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.api.API;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

@EnableWebMvc
@Controller
@Service
@Api(value = "metadata/resources/actions",
     tags= "metadata/resources/actions",
     description = "Generate metadata resource from various sources")
public class ResourceActionsApi {
    public ResourceActionsApi() {
    }
    public ResourceActionsApi(Store store) {
        this.store = store;
    }

    private Store store;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    private final ApplicationContext appContext = ApplicationContextHolder.get();

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }
    }


    @ApiOperation(value = "Create a metadata overview using the mapprint module",
                  notes = "Notes",
                  response = MetadataResource.class,
                  nickname = "createMetadataOverview")
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful retrieval of user detail", response = User.class),
//            @ApiResponse(code = 404, message = "User with given username does not exist"),
//            @ApiResponse(code = 500, message = "Internal server error")}
//    )
    @RequestMapping(value = "/api/" + API.VERSION_0_1 +
                                "/metadata/{metadataUuid}/resources/actions/save-thumbnail",
                    method = RequestMethod.PUT)
    @ResponseBody
    public MetadataResource saveThumbnail(
            @ApiParam(value = "The metadata UUID",
                      required = true,
                      examples = @Example(value = {
                              @ExampleProperty(
                                      mediaType = "string",
                                      value = "43d7c186-2187-4bcd-8843-41e575a5ef56")
                      })
            )
            @PathVariable
            String metadataUuid,
            @ApiParam(value = "The mapprint module JSON configuration",
                    required = true)
            @RequestParam()
            String jsonConfig,
            @ApiParam(value = "The rotation angle of the map")
            @RequestParam(required = false, defaultValue = "0") int rotationAngle
            )
            throws Exception {
        ServiceContext context = ServiceContext.get();
        DataManager dataMan = appContext.getBean(DataManager.class);

        String metadataId = dataMan.getMetadataId(metadataUuid);
        Lib.resource.checkEditPrivilege(context, metadataId);

        ThumbnailMaker thumbnailMaker = appContext.getBean(ThumbnailMaker.class);

        Path thumbnailFile = thumbnailMaker.generateThumbnail(
                jsonConfig,
                rotationAngle);

        return store.putResource(metadataUuid, thumbnailFile, MetadataResourceVisibility.PUBLIC);
    }
}