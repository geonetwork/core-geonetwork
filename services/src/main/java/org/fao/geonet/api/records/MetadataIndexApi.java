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

import com.google.common.collect.Sets;
import io.swagger.annotations.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import net.sf.json.JSONObject;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.api.processing.XslProcessUtils;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.api.records.model.suggestion.SuggestionType;
import org.fao.geonet.api.records.model.suggestion.SuggestionsType;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.metadata.BatchOpsMetadataReindexer;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerConfigurationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordIndexing")
@ReadWriteController
public class MetadataIndexApi {

    @Autowired
    DataManager dataManager;

    @ApiOperation(
        value = "Index a set of records",
        notes = "Index a set of records provided either by a bucket or a list of uuids",
        nickname = "indexSelection")
    @RequestMapping(
        value = "/index",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('Administrator')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Record indexed."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public
    @ResponseBody
    JSONObject index(
            @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
                    required = false,
                    example = "")
            @RequestParam(required = false)
                    String[] uuids,
            @ApiParam(
                    value = ApiParams.API_PARAM_BUCKET_NAME,
                    required = false)
            @RequestParam(
                    required = false
            )
                    String bucket,
            @ApiIgnore
                    HttpSession httpSession,
            @ApiIgnore
                    HttpServletRequest request
    )
        throws Exception {

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserSession session = ApiUtils.getUserSession(httpSession);

        SelectionManager selectionManager =
                SelectionManager.getManager(serviceContext.getUserSession());

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
        Set<Integer> ids = Sets.newHashSet();
        int index = 0;

        for (String uuid : records) {
            try {
                final String metadataId = dataManager.getMetadataId(uuid);
                if (metadataId != null) {
                    ids.add(Integer.valueOf(metadataId));
                }
            } catch (Exception e) {
                try {
                    ids.add(Integer.valueOf(uuid));
                } catch (NumberFormatException nfe) {
                    // skip
                }
            }
        }
        index = ids.size();
        new BatchOpsMetadataReindexer(dataManager, ids).process(false);

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("count", index);

        return res;
    }

}
