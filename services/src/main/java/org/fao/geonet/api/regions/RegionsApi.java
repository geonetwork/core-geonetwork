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

package org.fao.geonet.api.regions;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.regions.model.Category;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.services.region.ThesaurusBasedRegionsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/regions",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/regions"
})
@Api(value = "regions",
    tags = "regions",
    description = "Regions operations")
@Controller("regions")
public class RegionsApi {

    @Autowired
    LanguageUtils languageUtils;

    @ApiOperation(
        value = "Get list of regions",
        nickname = "getRegions"
    )
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of regions.")
    })
    @ResponseBody
    public ListRegionsResponse getRegions(
        @RequestParam(required = false)
            String label,
        @RequestParam(required = false)
            String categoryId,
        @RequestParam(defaultValue = "-1")
            int maxRecords,
        @ApiIgnore
            @ApiParam(hidden = true)
        NativeWebRequest webRequest) throws Exception {
        final HttpServletRequest nativeRequest =
            webRequest.getNativeRequest(HttpServletRequest.class);

        ServiceContext context = ApiUtils.createServiceContext(nativeRequest);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        Collection<RegionsDAO> daos =
            applicationContext.getBeansOfType(RegionsDAO.class).values();

        long lastModified = -1;
        for (RegionsDAO dao : daos) {
            if (dao.includeInListing()) {
                if (lastModified < Long.MAX_VALUE) {
                    Request request = createRequest(label, categoryId, maxRecords, context, dao);

                    Optional<Long> currentLastModified = request.getLastModified();
                    if (currentLastModified.isPresent() && lastModified < currentLastModified.get()) {
                        lastModified = currentLastModified.get();
                    }
                }
            }
        }

        if (lastModified < Long.MAX_VALUE && webRequest.checkNotModified(lastModified)) {
            return null;
        }

        Collection<Region> regions = Lists.newArrayList();
        for (RegionsDAO dao : daos) {
            if (dao.includeInListing()) {
                Request request = createRequest(label, categoryId, maxRecords, context, dao);
                regions.addAll(request.execute());
            }
        }

        final HttpServletResponse nativeResponse = webRequest.getNativeResponse(HttpServletResponse.class);

        nativeResponse.setHeader("Cache-Control", "no-cache");
        return new ListRegionsResponse(regions);
    }

    @ApiOperation(
        value = "Get list of region types",
        nickname = "getRegionTypes"
    )
    @RequestMapping(
        path = "/types",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of region types.")
    })
    @ResponseBody
    public List<Category> getRegionTypes(
        HttpServletRequest request) throws Exception {

        String language = languageUtils.getIso3langCode(request.getLocales());
        ServiceContext context = ApiUtils.createServiceContext(request);
        Collection<RegionsDAO> daos = ApplicationContextHolder.get().getBeansOfType(RegionsDAO.class).values();
        List<Category> response = new ArrayList<>();
        for (RegionsDAO dao : daos) {
            if (dao instanceof ThesaurusBasedRegionsDAO) {
                java.util.List<KeywordBean> keywords =
                    ((ThesaurusBasedRegionsDAO) dao).getRegionTopConcepts(context);
                if (keywords != null) {
                    for (KeywordBean k : keywords) {
                        Category c = new Category(
                            k.getUriCode(),
                            k.getPreferredLabel(language)
                        );
                        response.add(c);
                    }
                }
            } else {
                Collection<String> ids = dao.getRegionCategoryIds(context);
                if (ids != null) {
                    for (String id : ids) {
                        Category c = new Category(
                            id,
                            null
                        );
                        response.add(c);
                    }
                }
            }
        }
        return response;
    }

    public static Request createRequest(String label, String categoryId,
                                  int maxRecords, ServiceContext context,
                                  RegionsDAO dao) throws Exception {
        Request request = dao.createSearchRequest(context);
        if (label != null) {
            request.label(label);
        }
        if (categoryId != null) {
            request.categoryId(categoryId);
        }
        if (maxRecords > 0) {
            request.maxRecords(maxRecords);
        }
        return request;
    }
}
