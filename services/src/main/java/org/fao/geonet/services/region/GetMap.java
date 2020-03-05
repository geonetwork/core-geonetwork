//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.region;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Envelope;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.api.records.extent.MapRenderer;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

//=============================================================================

/**
 * Return an image of the region as a polygon against an optional background. If the background is
 * provided it is assumed to be a url with placeholders for width, height, srs, minx,maxx,miny and
 * maxy. For example:
 *
 * http://www2.demis.nl/wms/wms.ashx?WMS=BlueMarble&LAYERS=Earth%20Image%2 CBorders
 * %2CCoastlines&FORMAT=image%2Fjpeg&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap &STYLES
 * =&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A4326&BBOX={MINX
 * },{MINY},{MAXX},{MAXY}&WIDTH={WIDTH}&HEIGHT={HEIGHT}
 *
 * the placeholders must either be all uppercase or all lowercase
 *
 * The parameters to the service are:
 *
 * id - id of the region to render srs - (optional) default is EPSG:4326 otherwise it is the project
 * to use when rendering the image width - (optional) width of the image that is created. Only one
 * of width and height are permitted height - (optional) height of the image that is created. Only
 * one of width and height are permitted background - URL for loading a background image for
 * regions. A WMS Getmap request is the typical example. the URL must be parameterized with the
 * following parameters: minx, maxx, miny, maxy, width, height and optionally srs
 */
@Controller
@Deprecated
public class GetMap {
    public static final String MAP_SRS_PARAM = "mapsrs";
    public static final String GEOM_SRS_PARAM = "geomsrs";
    public static final String WIDTH_PARAM = "width";
    public static final String GEOM_PARAM = "geom";
    public static final String GEOM_TYPE_PARAM = "geomtype";
    public static final String HEIGHT_PARAM = "height";
    public static final String BACKGROUND_PARAM = "background";
    public static final String OUTPUT_FILE_NAME = "outputFileName";
    public static final String SETTING_BACKGROUND = "settings";

    @Autowired
    private ServiceManager serviceManager;

    public static AffineTransform worldToScreenTransform(Envelope mapExtent, Dimension screenSize) {
        return MapRenderer.worldToScreenTransform(mapExtent, screenSize);
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
    }

    /**
     * A rendering of the geometry as a png. If no background is specified the image will be
     * transparent. In getMap the envelope of the geometry is calculated then it is expanded by a
     * factor.  That factor is the size of the map.  This allows the map to be slightly bigger than
     * the geometry allowing some context to be shown. This parameter allows different factors to be
     * chosen per scale level
     *
     * Proportion is the proportion of the world that the geometry covers (bounds of WGS84)/(bounds
     * of geometry in WGS84)
     *
     * Named backgrounds allow the background parameter to be a simple key and the complete URL will
     * be looked up from this list of named backgrounds
     *
     * The name of the child elements is the key and the text is the url
     *
     * @param lang           UI lang
     * @param imageFormat    output image type.  eg. png/gif/etc...
     * @param id             required
     * @param srs            optional
     * @param width          (optional) width of the image that is created. Only one of width and
     *                       height are permitted
     * @param height         (optional) height of the image that is created. Only one of width and
     *                       height are permitted
     * @param background     (optional) URL for loading a background image for regions or a key that
     *                       references the namedBackgrounds (configured in config-spring-geonetwork.xml).
     *                       A WMS Getmap request is the typical example. The URL must be
     *                       parameterized with the following parameters: minx, maxx, miny, maxy,
     *                       width, height
     * @param geomParam      (optional) a wkt or gml encoded geometry.
     * @param geomType       (optional) defines if geom is wkt or gml. Allowed values are wkt and
     *                       gml. if not specified the it is assumed the geometry is wkt
     * @param geomSrs        (optional)
     * @param outputFileName the filename if the image is downloaded
     */
    @RequestMapping(value = "/{portal}/{lang:[a-z]{3}}/region.getmap.{imageFormat}")
    public HttpEntity<byte[]> exec(@PathVariable String lang,
                                   @PathVariable String imageFormat,
                                   @RequestParam(value = Params.ID, required = false) String id,
                                   @RequestParam(value = MAP_SRS_PARAM, defaultValue = "EPSG:4326") String srs,
                                   @RequestParam(value = WIDTH_PARAM, required = false) Integer width,
                                   @RequestParam(value = HEIGHT_PARAM, required = false) Integer height,
                                   @RequestParam(value = BACKGROUND_PARAM, required = false) String background,
                                   @RequestParam(value = GEOM_PARAM, required = false) String geomParam,
                                   @RequestParam(value = GEOM_TYPE_PARAM, defaultValue = "WKT") String geomType,
                                   @RequestParam(value = GEOM_SRS_PARAM, defaultValue = "EPSG:4326") String geomSrs,
                                   @RequestParam(value = OUTPUT_FILE_NAME, required = false) String outputFileName,
                                   NativeWebRequest request) throws Exception {

        ServiceContext context = serviceManager.createServiceContext("region.getmap." + imageFormat, lang,
            request.getNativeRequest(HttpServletRequest.class));

        if (id == null && geomParam == null) {
            throw new BadParameterEx(Params.ID, "Either " + GEOM_PARAM + " or " + Params.ID + " is required");
        }
        if (id != null && geomParam != null) {
            throw new BadParameterEx(Params.ID, "Only one of " + GEOM_PARAM + " or " + Params.ID + " is permitted");
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

        if (outputFileName == null) {
            outputFileName = "region.getmap." + imageFormat;
        }

        // resource modified?

        if (id != null) {
            Collection<RegionsDAO> daos = context.getApplicationContext().getBeansOfType(RegionsDAO.class).values();

            for (RegionsDAO regionsDAO : daos) {
                final Request searchRequest = regionsDAO.createSearchRequest(context);
                searchRequest.id(id);
                Optional<Long> lastModifiedOption = searchRequest.getLastModified();
                if (lastModifiedOption.isPresent()) {
                    final Long lastModified = lastModifiedOption.get();
                    if (lastModified != null && request.checkNotModified(lastModified)) {
                        return null;
                    }
                }
            }
        } else {
            if (request.checkNotModified(geomParam + srs + background)) {
                return null;
            }
        }

        MapRenderer renderer = new MapRenderer(context);
        BufferedImage image = renderer.render(id, srs, width, height, background, geomParam, geomType, geomSrs);

        if (image == null) return null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            ImageIO.write(image, imageFormat, out);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=\"" + outputFileName + "\"");
            if (id != null) {
                headers.add("Cache-Control", "no-cache");
            } else {
                headers.add("Cache-Control", "public, max-age: " + TimeUnit.DAYS.toSeconds(5));

            }
            headers.add("Content-Type", "image/" + imageFormat);
            return new HttpEntity<>(out.toByteArray(), headers);
        }
    }

}


// =============================================================================

