//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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

package org.fao.geonet.services.thumbnail;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.thumbnail.ThumbnailMaker;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.resources.Resource;
import org.fao.geonet.services.metadata.resources.ResourceType;
import org.fao.geonet.services.metadata.resources.Store;
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
@Api(value = "metadata/resources",
     tags= "metadata/resources")
public class Generate {
    public Generate() {
    }
    public Generate(Store store) {
        this.store = store;
    }

    private Store store;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    ApplicationContext appContext = ApplicationContextHolder.get();

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }
    }

    @RequestMapping(value = "/api/metadata/{metadataUuid}/resources/actions/save-thumbnail",
                    method = RequestMethod.PUT)
    @ResponseBody
    public Resource saveThumbnail(
            @ApiParam(value = "The metadata UUID",
                    example = "43d7c186-2187-4bcd-8843-41e575a5ef56")
            @PathVariable String metadataUuid,
            @RequestParam() String jsonConfig,
            @RequestParam(required = false, defaultValue = "0") String rotation
            )
            throws Exception {
        Integer rotationAngle = null;
        try {
            rotationAngle = Integer.valueOf(rotation);
        } catch (NumberFormatException e) {
        }

        ServiceContext context = ServiceContext.get();
        DataManager dataMan = appContext.getBean(DataManager.class);

        String metadataId = dataMan.getMetadataId(metadataUuid);
        Lib.resource.checkEditPrivilege(context, metadataId);

        ThumbnailMaker thumbnailMaker = appContext.getBean(ThumbnailMaker.class);

        Path thumbnailFile = thumbnailMaker.generateThumbnail(
                jsonConfig,
                rotationAngle);

        return store.putResource(metadataUuid, thumbnailFile, ResourceType.PUBLIC);
    }
}