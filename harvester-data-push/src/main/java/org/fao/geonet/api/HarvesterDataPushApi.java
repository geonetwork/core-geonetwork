//=============================================================================
//===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
package org.fao.geonet.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.harvester.push.tasks.DatabaseTask;
import org.fao.geonet.harvester.push.tasks.ElasticSearchTask;
import org.fao.geonet.harvester.push.tasks.HarvesterDataTask;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;


@RequestMapping(value = {
    "/{portal}/api/harvesters"
})
@Tag(name = "harvester",
    description = "Harvester data push API")
@Controller("harvesterpush")
public class HarvesterDataPushApi {

    @Autowired
    SettingManager settingManager;

    @Autowired
    HarvestManager harvestManager;


    @Autowired
    DatabaseTask databaseTask;

    @Autowired
    ElasticSearchTask elasticSearchTask;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Push the harvester data to another GeoNetwork catalogue. Configure it in the Settings page the tools path that manage the metadata synchronisation")
    @RequestMapping(value = "/{harvesterUuid}/push",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Harvester data pushed."),
        @ApiResponse(responseCode = "404", description = "Harvester not found."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> pushHarvesterData(
        @Parameter(
            description = "Harvester uuid",
            required = true)
        @PathVariable
            String harvesterUuid
    ) throws Exception {
        String synchToolsPath = settingManager.getValue(HarvesterDataTask.SYSTEM_HARVESTER_SYNCH_TOOLS_PATH);

        if (StringUtils.isEmpty(synchToolsPath)) {
            throw new Exception("Harvester synchronize metadata tools path is not configured. Configure it in the Settings page.");
        }

        AbstractHarvester harvester = harvestManager.getHarvester(harvesterUuid);
        if (harvester == null) {
            throw new ResourceNotFoundException(String.format("Harvester with uuid '%s' not found.", harvesterUuid));
        }

        databaseTask.synch(harvesterUuid);
        elasticSearchTask.synch(harvesterUuid);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
