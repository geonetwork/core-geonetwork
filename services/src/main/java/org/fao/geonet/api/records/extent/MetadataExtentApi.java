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

package org.fao.geonet.api.records.extent;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.regions.MetadataRegionDAO;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.region.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordExtent")
@ReadWriteController
public class MetadataExtentApi {

    public static final String MAP_SRS_PARAM = "mapsrs";
    public static final String GEOM_SRS_PARAM = "geomsrs";
    public static final String WIDTH_PARAM = "width";
    public static final String GEOM_PARAM = "geom";
    public static final String GEOM_TYPE_PARAM = "geomtype";
    public static final String HEIGHT_PARAM = "height";
    public static final String BACKGROUND_PARAM = "background";
    public static final String SETTING_BACKGROUND = "settings";
    public static final String WIDTH_AND_HEIGHT_BOTH_DEFINED_MESSAGE =
        String.format("Only one of %s and %s can be defined currently.  Future versions may support this but it is not supported at the moment", WIDTH_PARAM, HEIGHT_PARAM);
    public static final String WIDTH_AND_HEIGHT_BOTH_MISSING_MESSAGE =
        String.format("One of $s or $s parameters must be included in the request", WIDTH_PARAM, HEIGHT_PARAM);

    @Autowired
    private MetadataRegionDAO metadataRegionDAO;

    @ApiOperation(
        value = "Get record extents as image",
        notes = "A rendering of the geometry as a png. If no background is specified the image will be " +
            "transparent. In getMap the envelope of the geometry is calculated then it is expanded by a " +
            "factor.  That factor is the size of the map.  This allows the map to be slightly bigger than " +
            "the geometry allowing some context to be shown. This parameter allows different factors to be " +
            "chosen per scale level." +
            "\n" +
            "Proportion is the proportion of the world that the geometry covers (bounds of WGS84)/(bounds " +
            "of geometry in WGS84)\n" +
            "\n" +
            "Named backgrounds allow the background parameter to be a simple key and the complete URL will " +
            "be looked up from this list of named backgrounds\n",
        nickname = "getRecordExtents")
    @RequestMapping(
        value = "/{metadataUuid}/extents.png",
        produces = {
            MediaType.IMAGE_PNG_VALUE
        },
        method = RequestMethod.GET)
    public HttpEntity<byte[]> getRecordExtentAsImage(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable(value = "metadataUuid")
            String metadataUuid,
        @RequestParam(value = MAP_SRS_PARAM, defaultValue = "EPSG:4326") String srs,
        @ApiParam(value = "(optional) width of the image that is created. Only one of width and height are permitted")
        @RequestParam(value = WIDTH_PARAM, required = false, defaultValue = "300") Integer width,
        @ApiParam(value = "(optional) height of the image that is created. Only one of width and height are permitted")
        @RequestParam(value = HEIGHT_PARAM, required = false) Integer height,
        @ApiParam(value = "(optional) URL for loading a background image for regions or a key that references the namedBackgrounds (configured in config-spring-geonetwork.xml). A WMS Getmap request is the typical example. The URL must be parameterized with the following parameters: minx, maxx, miny, maxy, width, height")
        @RequestParam(value = BACKGROUND_PARAM, required = false, defaultValue = "settings") String background,
        @ApiParam(value = "(optional) Fill color with format RED,GREEN,BLUE,ALPHA")
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,50")
        String fillColor,
        @ApiParam(value = "(optional) Stroke color with format RED,GREEN,BLUE,ALPHA")
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,255")
        String strokeColor,
        @ApiParam(value = "(optional) restrict display to one extent given its order of appearence")
        @RequestParam(value = "", required = false)
        Integer extentOrderOfAppearence,
        @ApiIgnore
            NativeWebRequest nativeWebRequest,
        @ApiIgnore
            HttpServletRequest request) throws Exception {

        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        if (width != null && height != null) {
            throw new BadParameterEx(WIDTH_PARAM, WIDTH_AND_HEIGHT_BOTH_DEFINED_MESSAGE);
        }

        if (width == null && height == null) {
            throw new BadParameterEx(WIDTH_PARAM, WIDTH_AND_HEIGHT_BOTH_MISSING_MESSAGE);
        }

        String regionId;
        if (extentOrderOfAppearence == null) {
            regionId = String.format("metadata:@id%s", metadata.getId());
        } else {
            regionId = String.format(
                "metadata:@id%s:@xpath*//gmd:extent[%d]//*/gmd:EX_BoundingPolygon | *//gmd:extent[%d][not(descendant::*[name() = 'gmd:EX_BoundingPolygon'])]//*/gmd:EX_GeographicBoundingBox",
                metadata.getId(), extentOrderOfAppearence, extentOrderOfAppearence);
        }

        Request searchRequest = metadataRegionDAO.createSearchRequest(context).id(regionId);
        if (searchRequest.getLastModified().isPresent() && nativeWebRequest.checkNotModified(searchRequest.getLastModified().get())) {
            return null;
        }

        MapRenderer renderer = new MapRenderer(context);
        BufferedImage image = renderer.render(
            regionId, srs, width, height, background,
            null, null, null,
            fillColor,
            strokeColor);

        if (image == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("inline; filename=\"%s-extent.png\"", metadataUuid));
            headers.add("Cache-Control", "no-cache");
            headers.add("Content-Type", "image/png");
            return new HttpEntity<>(out.toByteArray(), headers);
        }
    }
}
