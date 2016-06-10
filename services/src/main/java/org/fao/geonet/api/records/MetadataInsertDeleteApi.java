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

package org.fao.geonet.api.records;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = "records",
    tags = "records",
    description = "Metadata record operations")
@Controller("recordInsertOrDelete")
@ReadWriteController
public class MetadataInsertDeleteApi {

    @ApiOperation(
        value = "Delete a metadata record",
        notes = "",
        nickname = "delete")
    @RequestMapping(value = "/{metadataUuid}",
        method = RequestMethod.DELETE
    )
    public
    @ResponseBody
    void getRecord(
        @ApiParam(
            value = "Record UUID.",
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Backup first the record as MEF in the metadata removed folder.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean withBackup,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        DataManager dataManager = appContext.getBean(DataManager.class);

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE && withBackup) {
            MetadataUtils.backupRecord(metadata, context);
        }

        IO.deleteFileOrDirectory(
            Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class),
                String.valueOf(metadata.getId())));

        dataManager.deleteMetadata(context, metadataUuid);
    }

}
