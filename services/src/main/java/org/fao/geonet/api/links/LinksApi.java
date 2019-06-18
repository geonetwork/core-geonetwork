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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.links.model.PaginatedLinksResponse;
import org.fao.geonet.api.links.model.LinkDto;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkType;
import org.fao.geonet.domain.Link_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserSearch;
import org.fao.geonet.domain.UserSearchFeaturedType;
import org.fao.geonet.domain.UserSearch_;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserSearchRepository;
import org.fao.geonet.repository.specification.UserSearchSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/api/records/links",
    "/api/" + API.VERSION_0_1 +
        "/records/links"
})
@Api(value = "links",
    tags = "links",
    description = "Record link operations")
public class LinksApi {

    public static final String API_PARAM_LINKS_DETAILS = "User search details";

//
//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        binder.registerCustomEditor(UserSearchFeaturedType.class, new UserSearchFeaturedTypeConverter());
//    }

    @Autowired
    LinkRepository linkRepository;

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
            Integer offset,
        @ApiParam(value = "Number of records to return",
            required = false)
        @RequestParam(required = false, defaultValue = "10")
            Integer limit,
        @ApiIgnore
            HttpSession httpSession
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);

        Sort sortBy = SortUtils.createSort(Sort.Direction.ASC,
            Link_.url);

        int page = (offset / limit);
        final PageRequest pageRequest = new PageRequest(page, limit, sortBy);

        // TODO: Add filter by URL, UUID, status, failing
        final Page<Link> all = linkRepository.findAll(pageRequest);

        List<Link> response = new ArrayList<>();
        all.forEach(e -> response.add(e));
        return response;
    }
}
