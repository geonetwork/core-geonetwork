/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.extent.MapRenderer;
import org.fao.geonet.api.regions.model.Category;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.LabelNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.fao.geonet.api.records.extent.MetadataExtentApi.*;

@RequestMapping(value = {
    "/{portal}/api/regions"
})
@Tag(name = "regions",
    description = "Regions operations")
@Controller("regions")
public class RegionsApi {

    @Autowired
    private LanguageUtils languageUtils;
    @Autowired
    private SettingManager settingManager;

    @Value("${metadata.extentApi.disableFullUrlBackgroundMapServices:true}")
    private boolean disableFullUrlBackgroundMapServices;

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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get list of regions"
    )
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of regions.")
    })
    @ResponseBody
    public ListRegionsResponse getRegions(
        @RequestParam(required = false)
            String label,
        @RequestParam(required = false)
            String categoryId,
        @RequestParam(defaultValue = "-1")
            int maxRecords,
        @Parameter(hidden = true)
            NativeWebRequest webRequest) throws Exception {
        final HttpServletRequest nativeRequest =
            webRequest.getNativeRequest(HttpServletRequest.class);

        ServiceContext context = ApiUtils.createServiceContext(nativeRequest);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        Collection<RegionsDAO> daos =
            applicationContext.getBeansOfType(RegionsDAO.class).values();

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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get list of region types"
    )
    @RequestMapping(
        path = "/types",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of region types.")
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
                        String label = "";
                        try {
                            label = k.getPreferredLabel(language);
                        } catch (LabelNotFoundException ex) {
                            label = k.getDefaultValue();
                        }

                        Category c = new Category(
                            k.getUriCode(),
                            label
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get geometry as image",
        description = "A rendering of the geometry as a `png`.\n " +
            "\n " +
            "The coverage of the image is computed from the geometry envelope and size using scale factor configuration " +
            "(See `regionGetMapExpandFactors` bean in `config-spring-geonetwork.xml`) " +
            "to give enough context on where the geometry is. The smaller the geometry, the bigger the expand factor.\n" +
            "\n " +
            "If needed, when the factor is high, square image mode can be enabled (instead of proportional geometry size):\n" +
            "\n " +
            "```xml\n" +
            " <util:set id=\"regionGetMapExpandFactors\" set-class=\"java.util.TreeSet\">\n" +
            "    <bean class=\"org.fao.geonet.api.records.extent.ExpandFactor\"\n" +
            "          p:proportion=\".00005\" p:factor=\"256\" p:squareImage=\"true\"/>\n" +
            "```\n")
    @RequestMapping(
        value = "/geom.png",
        produces = {
            MediaType.IMAGE_PNG_VALUE
        },
        method = RequestMethod.GET)
    public HttpEntity<byte[]> getGeomAsImage(
        @Parameter(description = "(optional) the background map projection. If not passed uses the region/getmap/mapproj"
            + " setting. If the setting is not set defaults to EPSG:4326")
        @RequestParam(value = MAP_SRS_PARAM, required = false) String mapSrs,
        @Parameter(description = "(optional) width of the image that is created. Only one of width and height are permitted")
        @RequestParam(value = WIDTH_PARAM, required = false, defaultValue = "300") Integer width,
        @Parameter(description = "(optional) height of the image that is created. Only one of width and height are permitted")
        @RequestParam(value = HEIGHT_PARAM, required = false) Integer height,
        @Parameter(description = "(optional) URL for loading a background image for regions or a key that references the namedBackgrounds (configured in config-spring-geonetwork.xml). A WMS Getmap request is the typical example. The URL must be parameterized with the following parameters: minx, maxx, miny, maxy, width, height")
        @RequestParam(value = BACKGROUND_PARAM, required = false, defaultValue = "settings") String background,
        @Parameter(description = "(optional) a wkt or gml encoded geometry.")
        @RequestParam(value = GEOM_PARAM, required = false) String geomParam,
        @Parameter(description = "(optional) defines if geom is wkt or gml. Allowed values are wkt and gml. if not specified the it is assumed the geometry is wkt")
        @RequestParam(value = GEOM_TYPE_PARAM, defaultValue = "WKT") String geomType,
        @Parameter(description = "")
        @RequestParam(value = GEOM_SRS_PARAM, defaultValue = "EPSG:4326") String geomSrs,
        @Parameter(description = API_PARAM_FILL_DESCRIPTION)
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,30")
        String fillColor,
        @Parameter(description = API_PARAM_STROKE_DESCRIPTION)
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,255")
        String strokeColor,
        @Parameter(hidden = true)
            NativeWebRequest nativeWebRequest,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {
        final ServiceContext context = ApiUtils.createServiceContext(request);
        String srs = mapSrs;
        if (StringUtils.isBlank(srs)) {
            // If no map srs parameter is provided use the `region/getmap/mapproj` setting and if this is not set defaults
            // to EPSG:4326
             srs = StringUtils.defaultString(settingManager.getValue(Settings.REGION_GETMAP_MAPPROJ, true),
                "EPSG:4326");
        }

        if (width != null && height != null) {
            throw new BadParameterEx(
                WIDTH_PARAM,
                "Only one of "
                    + WIDTH_PARAM
                    + " and "
                    + HEIGHT_PARAM
                    + " can be defined currently.  Future versions may support this but it is not supported at the moment");
        }

        if (width == null && height == null) {
            throw new BadParameterEx(WIDTH_PARAM, "One of " + WIDTH_PARAM + " or " + HEIGHT_PARAM
                + " parameters must be included in the request");

        }

        if ((background != null) && (background.startsWith("http")) && (disableFullUrlBackgroundMapServices)) {
            throw new BadParameterEx(BACKGROUND_PARAM, "Background layers from provided are not supported, " +
                "use a preconfigured background layers map service.");
        }

        String outputFileName = "geom.png";
        String regionId = null;

        if (nativeWebRequest.checkNotModified(geomParam + srs + background)) {
            return null;
        }

        MapRenderer renderer = new MapRenderer(context);
        BufferedImage image = renderer.render(regionId, srs, width, height, background, geomParam, geomType, geomSrs, fillColor, strokeColor);

        if (image == null) return null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + outputFileName + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "public, max-age: " + TimeUnit.DAYS.toSeconds(5));
            headers.add(HttpHeaders.CONTENT_TYPE, "image/png");
            return new HttpEntity<>(out.toByteArray(), headers);
        }
    }
}
