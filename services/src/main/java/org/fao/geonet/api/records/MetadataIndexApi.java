/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.services.ReadWriteController;
import jeeves.xlink.Processor;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordIndexing")
@ReadWriteController
public class MetadataIndexApi {

    @Autowired
    DataManager dataManager;

    @Autowired
    SettingManager settingManager;

    @Autowired
    IMetadataUtils metadataUtils;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Index a set of records",
        description = "Index a set of records provided either by a bucket or a list of uuids")
    @RequestMapping(
        value = "/index",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record indexed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    IndexResponse index(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {

        UserSession session = ApiUtils.getUserSession(httpSession);

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
        Set<Integer> ids = Sets.newHashSet();

        for (String uuid : records) {
            ApiUtils.canEditRecord(uuid, request);
            try {
                metadataUtils.findAllByUuid(uuid).forEach(m -> ids.add(m.getId()));
            } catch (Exception e) {
                try {
                    ids.add(Integer.valueOf(uuid));
                } catch (NumberFormatException nfe) {
                    // skip
                }
            }
        }
        int index = ids.size();

        if (index > 0) {
            // clean XLink Cache so that cache and index remain in sync
            Processor.clearCache();

            new BatchOpsMetadataReindexer(dataManager, ids)
                .process(settingManager.getSiteId(), false);
        }

        IndexResponse indexResponse = new IndexResponse();
        indexResponse.setSuccess(true);
        indexResponse.setCount(index);
        return indexResponse;
    }

    private static class IndexResponse {
        private boolean success;
        private int count;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
