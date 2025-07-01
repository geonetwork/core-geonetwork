/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.regions.MetadataRegionDAO;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.XPath;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.fao.geonet.api.ApiParams.*;


@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@RestController("recordExtent")
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
        String.format("One of %s or %s parameters must be included in the request", WIDTH_PARAM, HEIGHT_PARAM);

    private static final String API_EXTENT_DESCRIPTION = "A rendering of the geometry as a png. If no background is specified the image will be " +
        "transparent. In getMap the envelope of the geometry is calculated then it is expanded by a " +
        "factor.  That factor is the size of the map.  This allows the map to be slightly bigger than " +
        "the geometry allowing some context to be shown. This parameter allows different factors to be " +
        "chosen per scale level." +
        "\n" +
        "Proportion is the proportion of the world that the geometry covers (bounds of WGS84)/(bounds " +
        "of geometry in WGS84)\n" +
        "\n" +
        "Named backgrounds allow the background parameter to be a simple key and the complete URL will " +
        "be looked up from this list of named backgrounds\n";

    private static final String API_PARAM_MAP_SRS_DESCRIPTION = "(optional) the background map projection. "
        + "If not passed uses the region/getmap/mapproj setting. If the setting is not set defaults to EPSG:4326";
    private static final String API_PARAM_WIDTH_DESCRIPTION = "(optional) width of the image that is created. Only one of width and height are permitted";
    private static final String API_PARAM_HEIGHT_DESCRIPTION = "(optional) height of the image that is created. Only one of width and height are permitted";
    private static final String API_PARAM_BG_DESCRIPTION = "(optional) URL for loading a background image for regions or a key that references the namedBackgrounds (configured in config-spring-geonetwork.xml). A WMS GetMap request is the typical example. The URL must be parameterized with the following parameters: minx, maxx, miny, maxy, width, height";
    public static final String API_PARAM_FILL_DESCRIPTION = "(optional) Fill color with format RED,GREEN,BLUE,ALPHA";
    public static final String API_PARAM_STROKE_DESCRIPTION = "(optional) Stroke color with format RED,GREEN,BLUE,ALPHA";

    private static final String EXTENT_XPATH = ".//*[local-name() ='extent']/*/*[local-name() = 'geographicElement']/*";
    private static final String EXTENT_DESCRIPTION_XPATH = "ancestor::*[local-name() = 'EX_Extent']/*[local-name() = 'description']/*/text()";

    @Autowired
    private MetadataRegionDAO metadataRegionDAO;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private SettingManager settingManager;

    @Value("${metadata.extentApi.disableFullUrlBackgroundMapServices:true}")
    private boolean disableFullUrlBackgroundMapServices;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record extents as image",
        description = API_EXTENT_DESCRIPTION)
    @GetMapping(
        value = "/{metadataUuid:.+}/extents.png",
        produces = {
            MediaType.IMAGE_PNG_VALUE
        })
    public HttpEntity<byte[]> getAllRecordExtentAsImage(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable(value = "metadataUuid")
            String metadataUuid,
        @Parameter(description = API_PARAM_MAP_SRS_DESCRIPTION)
        @RequestParam(value = MAP_SRS_PARAM, required = false) String mapSrs,
        @Parameter(description = API_PARAM_WIDTH_DESCRIPTION)
        @RequestParam(value = WIDTH_PARAM, required = false, defaultValue = "300") Integer width,
        @Parameter(description = API_PARAM_HEIGHT_DESCRIPTION)
        @RequestParam(value = HEIGHT_PARAM, required = false) Integer height,
        @Parameter(description = API_PARAM_BG_DESCRIPTION)
        @RequestParam(value = BACKGROUND_PARAM, required = false, defaultValue = "settings") String background,
        @Parameter(description = API_PARAM_FILL_DESCRIPTION)
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,30")
        String fillColor,
        @Parameter(description = API_PARAM_STROKE_DESCRIPTION)
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,255")
        String strokeColor,
        @RequestParam(required = false, defaultValue = "true")
        Boolean approved,
        @Parameter(hidden = true)
            NativeWebRequest nativeWebRequest,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {

        String srs = mapSrs;
        if (StringUtils.isBlank(srs)) {
            // If no map srs parameter is provided use the `region/getmap/mapproj` setting and if this is not set defaults
            // to EPSG:4326
            srs = StringUtils.defaultIfBlank(settingManager.getValue(Settings.REGION_GETMAP_MAPPROJ, true),
                "EPSG:4326");
        }

        return getExtent(metadataUuid, srs, width, height, background, fillColor, strokeColor, null, approved, nativeWebRequest, request);
    }



    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get list of record extents",
        description = API_EXTENT_DESCRIPTION)
    @GetMapping(
        value = "/{metadataUuid:.+}/extents.json",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public List<ExtentDto> getAllRecordExtentAsJson(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable(value = "metadataUuid")
            String metadataUuid,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);

        String schemaId = metadata.getDataInfo().getSchemaId();
        MetadataSchema schema = schemaManager.getSchema(schemaId);

        Element xmlData = metadata.getXmlData(false);
        List<?> extentList = Xml.selectNodes(
            xmlData,
            EXTENT_XPATH, schema.getNamespaces());

        List<ExtentDto> response = new ArrayList<>(extentList.size() + 1);
        if (!"iso19110".equals(schemaId)) {
            response.add(new ExtentDto(
                String.format("%sapi/records/%s/extents.png",
                    settingManager.getNodeURL(), metadataUuid),
                "ALL",
                "",
                ""));
        }

        int index = 1;

        for (Object extent : extentList) {
            if (extent instanceof Element) {
                Element extentElement = (Element) extent;

                String description =
                    Xml.selectString(extentElement,
                        EXTENT_DESCRIPTION_XPATH,
                        schema.getNamespaces());

                response.add(new ExtentDto(
                    String.format("%sapi/records/%s/extents/%d.png",
                        settingManager.getNodeURL(), metadataUuid, index),
                    extentElement.getName(),
                    XPath.getXPath(xmlData, extent),
                    description));
                index ++;
            }
        }
        return response;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get one record extent as image",
        description = API_EXTENT_DESCRIPTION)
    @GetMapping(
        value = "/{metadataUuid:.+}/extents/{geometryIndex}.png",
        produces = {
            MediaType.IMAGE_PNG_VALUE
        })
    public HttpEntity<byte[]> getOneRecordExtentAsImage(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable(value = "metadataUuid")
            String metadataUuid,
        @Parameter(description = "Index of the geometry or bounding box to display. Starts at 1.")
        @PathVariable(value = "geometryIndex")
            Integer geometryIndex,
        @Parameter(description = API_PARAM_MAP_SRS_DESCRIPTION)
        @RequestParam(value = MAP_SRS_PARAM, required = false) String mapSrs,
        @Parameter(description = API_PARAM_WIDTH_DESCRIPTION)
        @RequestParam(value = WIDTH_PARAM, required = false, defaultValue = "300") Integer width,
        @Parameter(description = API_PARAM_HEIGHT_DESCRIPTION)
        @RequestParam(value = HEIGHT_PARAM, required = false) Integer height,
        @Parameter(description = API_PARAM_BG_DESCRIPTION)
        @RequestParam(value = BACKGROUND_PARAM, required = false, defaultValue = "settings") String background,
        @Parameter(description = API_PARAM_FILL_DESCRIPTION)
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,30")
            String fillColor,
        @Parameter(description = API_PARAM_STROKE_DESCRIPTION)
        @RequestParam(value = "", required = false, defaultValue = "0,0,0,255")
            String strokeColor,
        @RequestParam(required = false, defaultValue = "true")
        Boolean approved,
        @Parameter(hidden = true)
            NativeWebRequest nativeWebRequest,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {

        String srs = mapSrs;
        if (StringUtils.isBlank(srs)) {
            // If no map srs parameter is provided use the `region/getmap/mapproj` setting and if this is not set defaults
            // to EPSG:4326
            srs = StringUtils.defaultIfBlank(settingManager.getValue(Settings.REGION_GETMAP_MAPPROJ, true),
                "EPSG:4326");
        }

        return getExtent(metadataUuid, srs, width, height, background, fillColor, strokeColor, geometryIndex, approved, nativeWebRequest, request);
    }

    private HttpEntity<byte[]> getExtent(String metadataUuid, String srs, Integer width, Integer height, String background, String fillColor, String strokeColor,
                                         Integer extentOrderOfAppearance, Boolean approved,
                                         NativeWebRequest nativeWebRequest, HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, approved, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        if (width != null && height != null) {
            throw new BadParameterEx(WIDTH_PARAM, WIDTH_AND_HEIGHT_BOTH_DEFINED_MESSAGE);
        }

        if (width == null && height == null) {
            throw new BadParameterEx(WIDTH_PARAM, WIDTH_AND_HEIGHT_BOTH_MISSING_MESSAGE);
        }

        if ((background != null) && (background.startsWith("http")) && (disableFullUrlBackgroundMapServices)) {
           throw new BadParameterEx(BACKGROUND_PARAM, "Background layers from provided are not supported, " +
                "use a preconfigured background layers map service.");
        }

        String regionId;
        if (extentOrderOfAppearance == null) {
            regionId = String.format("metadata:@id%s", metadata.getId());
        } else {
            regionId = String.format("metadata:@id%s:@xpath(%s)[%d]",
                metadata.getId(), EXTENT_XPATH, extentOrderOfAppearance);
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
            headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s-extent.png\"", metadataUuid));
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
            headers.add(HttpHeaders.CONTENT_TYPE, "image/png");
            return new HttpEntity<>(out.toByteArray(), headers);
        }
    }
}
