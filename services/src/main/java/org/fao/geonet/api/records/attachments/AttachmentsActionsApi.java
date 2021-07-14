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

package org.fao.geonet.api.records.attachments;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.thumbnail.ThumbnailMaker;
import org.fao.geonet.lib.Lib;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

@EnableWebMvc
@Controller
@Service
@Tag(name = "records",
    description = "Metadata record operations")
public class AttachmentsActionsApi {
    private final ApplicationContext appContext = ApplicationContextHolder.get();
    @Autowired
    DataManager dataMan;
    @Autowired
    ThumbnailMaker thumbnailMaker;
    private Store store;

    public AttachmentsActionsApi() {
    }

    public AttachmentsActionsApi(Store store) {
        this.store = store;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create an overview using the map print module",
        description = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/linking-thumbnail.html#generating-a-thumbnail-using-wms-layers'>More info</a>"
        //response = MetadataResource.class)
    )
    @RequestMapping(
        value = "/{portal}/api/records/{metadataUuid:.+}/attachments/print-thumbnail",
        method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Thumbnail created."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public MetadataResource saveThumbnail(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true
        )
        @PathVariable
            String metadataUuid,
        @Parameter(description = "The mapprint module JSON configuration",
            required = true)
        @RequestParam()
            String jsonConfig,
        @Parameter(description = "The rotation angle of the map")
        @RequestParam(required = false, defaultValue = "0") int rotationAngle,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        String metadataId = dataMan.getMetadataId(metadataUuid);
        Lib.resource.checkEditPrivilege(context, metadataId);

        Path thumbnailFile = thumbnailMaker.generateThumbnail(
            jsonConfig,
            rotationAngle);

        return store.putResource(context, metadataUuid, thumbnailFile, MetadataResourceVisibility.PUBLIC);
    }
}
