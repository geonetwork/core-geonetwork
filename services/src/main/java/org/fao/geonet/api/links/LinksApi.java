//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.links;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Link_;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.kernel.url.UrlChecker;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.LinkStatusRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/records/links",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records/links"
})
@Api(value = "links",
    tags = "links",
    description = "Record link operations")
public class LinksApi {
    @Autowired
    LinkRepository linkRepository;

    @Autowired
    LinkStatusRepository linkStatusRepository;

    @Autowired
    MetadataLinkRepository metadataLinkRepository;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    DataManager dataManager;

    @Autowired
    UrlAnalyzer urlAnalyser;

    @Autowired
    UrlChecker urlChecker;

    @ApiOperation(
        value = "Get record links",
        notes = "",
        nickname = "getRecordLinks")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<Link> getRecordLinks(
        @ApiParam(value = "From page",
            required = false)
        @RequestParam(required = false, defaultValue = "0")
            Integer from,
        @ApiParam(value = "Number of records to return",
            required = false)
        @RequestParam(required = false, defaultValue = "200")
            Integer size,
        @ApiIgnore
            HttpSession httpSession
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);

        Sort sortByStateThenUrl = new Sort(Sort.Direction.ASC, SortUtils.createPath(Link_.lastState), SortUtils.createPath(Link_.url));

        int page = (from / size);
        final PageRequest pageRequest = new PageRequest(page, size, sortByStateThenUrl);

        // TODO: Add filter by URL, UUID, status, failing
        final Page<Link> all = linkRepository.findAll(pageRequest);

        List<Link> response = new ArrayList<>();
        all.forEach(e -> response.add(e));
        return response;
    }


    @ApiOperation(
        value = "Analyze records links",
        notes = "",
        nickname = "analyzeRecordLinks")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity analyzeRecordLinks(
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
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean removeFirst,
        @RequestParam(
            required = false,
            defaultValue = "false")
            boolean analyze,
        @ApiIgnore
            HttpSession httpSession,
        @ApiIgnore
            HttpServletRequest request
    ) throws IOException, JDOMException {
        if (removeFirst) {
            urlAnalyser.deleteAll();
        }

        UserSession session = ApiUtils.getUserSession(httpSession);

        Set<Integer> ids = Sets.newHashSet();

        if (uuids != null || StringUtils.isNotEmpty(bucket)) {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);

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
        } else {
            // Process all
            final List<Metadata> metadataList = metadataRepository.findAll();
            for (Metadata m : metadataList) {
                ids.add(m.getId());
            }
        }

        for (int i : ids) {
            final Metadata metadata = metadataRepository.findOne(i);
            urlAnalyser.processMetadata(metadata.getXmlData(false), metadata);
        }

        if (analyze) {
            linkRepository.findAll().stream().forEach(urlAnalyser::testLink);
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }


    @ApiOperation(
        value = "Remove all links and status history",
        notes = "",
        nickname = "purgeAll")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity purgeAll() throws IOException, JDOMException {
        urlAnalyser.deleteAll();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
