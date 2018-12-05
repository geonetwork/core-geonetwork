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

package org.fao.geonet.api.status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(value = {
    "/api/status",
    "/api/" + API.VERSION_0_1 +
        "/status"
})
@Api(value = "status",
    tags = "status",
    description = "Workflow status operations")
@Controller("status")
public class StatusApi {

    @ApiOperation(
        value = "Get status",
        notes = "",
        nickname = "getStatus")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<StatusValue> getStatus(HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return context.getBean(StatusValueRepository.class).findAll();
    }


    @ApiOperation(
        value = "Search status",
        notes = "",
        nickname = "searchStatusByType")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET,
        path = "/search")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatusDTO> getStatusByType(
        @ApiParam(
            value = "One or more types to retrieve (ie. worflow, event, task). Default is all.",
            required = false)
        @RequestParam(
            required = false
        )
            StatusValueType[] type,
        @ApiParam(
            value = "One or more event author. Default is all.",
            required = false)
        @RequestParam(
            required = false
        )
        Integer[] author,
        @ApiParam(
        value = "One or more event owners. Default is all.",
        required = false)
        @RequestParam(
            required = false
        )
            Integer[] owner,
        @ApiParam(
        value = "One or more record identifier. Default is all.",
        required = false)
        @RequestParam(
            required = false
        )
            Integer[] record,
        // TODO: Add parameters for dates
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        MetadataStatusRepository statusRepository = context.getBean(MetadataStatusRepository.class);

        List<MetadataStatus> metadataStatuses;
        if ((type != null && type.length > 0) ||
            (author != null && author.length > 0) ||
            (owner != null && owner.length > 0) ||
            (record != null && record.length > 0)) {
            metadataStatuses = statusRepository.searchStatus(
                type != null && type.length > 0 ? Arrays.asList(type) : null,
                author != null && author.length > 0 ? Arrays.asList(author) : null,
                owner != null && owner.length > 0 ? Arrays.asList(owner) : null,
                record != null && record.length > 0 ? Arrays.asList(record) : null
            );
        } else {
             metadataStatuses = statusRepository.findAll();
        }

        Map<Integer, String> titles = new HashMap<>();
        List<MetadataStatusDTO> response = new ArrayList<>(metadataStatuses.size());
        metadataStatuses.forEach(e -> {
            String title = titles.get(e.getId().getMetadataId());
            if (title == null) {
                try {
                    // Collect metadata titles. For now we use Lucene
                    title = LuceneSearcher.getMetadataFromIndexById(
                        context.getLanguage(), e.getId().getMetadataId() + "",
                        "title");
                    titles.put(e.getId().getMetadataId(), title);
                } catch (Exception e1) {
                }
            }
            response.add(new MetadataStatusDTO(e, title));
        });
        return response;
    }

    @ApiOperation(
        value = "Get status by type",
        notes = "",
        nickname = "getStatusByType")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET,
        path = "/{type}")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<StatusValue> getStatusByType(
        @ApiParam(value = "Type",
            required = true)
        @PathVariable
            StatusValueType type,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return context.getBean(StatusValueRepository.class).findAllByType(type);
    }
}
