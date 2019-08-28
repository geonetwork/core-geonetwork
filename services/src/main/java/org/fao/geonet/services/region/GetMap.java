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

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionNotFoundEx;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

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
    private static final double WGS_DIAG = sqrt(pow(360, 2) + pow(180, 2));

    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private SettingManager settingManager;
    @Autowired
    private ApplicationContext context;
    private Map<String, String> regionGetMapBackgroundLayers;
    private SortedSet<ExpandFactor> regionGetMapExpandFactors;

    /**
     * Check if a geometry is in the domain of validity of a projection and if not return the
     * intersection of the geometry with the coordinate system domain of validity.
     */
    private static Geometry computeGeomInDomainOfValidity(Geometry geom, CoordinateReferenceSystem mapCRS) {
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        final Extent domainOfValidity = mapCRS.getDomainOfValidity();
        Geometry adjustedGeom = geom;
        if (domainOfValidity != null) {
            for (final GeographicExtent extent :
                domainOfValidity.getGeographicElements()) {
                if (Boolean.FALSE.equals(extent.getInclusion())) {
                    continue;
                }
                if (extent instanceof GeographicBoundingBox) {
                    if (extent != null) {
                        GeographicBoundingBox box = (GeographicBoundingBox) extent;

                        Envelope env = new Envelope(
                            box.getWestBoundLongitude(),
                            box.getEastBoundLongitude(),
                            box.getSouthBoundLatitude(),
                            box.getNorthBoundLatitude());
                        if (env.contains(geom.getEnvelopeInternal())) {
                            return geom;
                        } else {
                            adjustedGeom = geometryFactory.toGeometry(
                                env.intersection(geom.getEnvelopeInternal())
                            );
                        }
                    }
                }
            }
            return adjustedGeom;
        } else {
            return geom;
        }
    }

    public static AffineTransform worldToScreenTransform(Envelope mapExtent, Dimension screenSize) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

        return new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        this.regionGetMapBackgroundLayers = context.getBean("regionGetMapBackgroundLayers", Map.class);
        this.regionGetMapExpandFactors = context.getBean("regionGetMapExpandFactors", SortedSet.class);
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

        if (outputFileName == null) {
            outputFileName = "region.getmap." + imageFormat;
        }

        // see calculateImageSize for more parameter checks

        Geometry geom = null;
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

                geom = regionsDAO.getGeom(context, id, false, srs);
                if (geom != null) {
                    break;
                }
            }
            if (geom == null) {
                throw new RegionNotFoundEx(id);
            }
        } else {
            if (request.checkNotModified(geomParam + srs + background)) {
                return null;
            }
            GeomFormat format = GeomFormat.find(geomType);
            geom = format.parse(geomParam);
            if (!geomSrs.equals(srs)) {
                CoordinateReferenceSystem mapCRS = Region.decodeCRS(srs);
                CoordinateReferenceSystem geomCRS = Region.decodeCRS(geomSrs);

                // Check if coordinates provided
                // are in projection scope to avoid.
                // eg. java.lang.RuntimeException:
                // org.geotools.referencing.operation.projection.ProjectionException:
                // Latitude 90°00.0'S is too close to a pole.
                geom = computeGeomInDomainOfValidity(geom, mapCRS);
                MathTransform transform = CRS.findMathTransform(geomCRS, mapCRS, true);
                geom = JTS.transform(geom, transform);
            }
        }
        BufferedImage image;
        Envelope bboxOfImage = new Envelope(geom.getEnvelopeInternal());
        double expandFactor = calculateExpandFactor(bboxOfImage, srs);
        bboxOfImage.expandBy(bboxOfImage.getWidth() * expandFactor, bboxOfImage.getHeight() * expandFactor);
        Dimension imageDimensions = calculateImageSize(bboxOfImage, width, height);


        Exception error = null;
        if (background != null) {
            // 4 cases:
            // * request param is 'settings' and db setting is a full url
            // * request param is 'settings' and db setting is a named bg layer
            // * request param is a named bg layer
            // * request param is a full url
            if (background.equalsIgnoreCase(SETTING_BACKGROUND)) {
                String bgSetting = settingManager.getValue(Settings.REGION_GETMAP_BACKGROUND);
                if (bgSetting.startsWith("http://") || bgSetting.startsWith("https://")) {
                    background = settingManager.getValue(Settings.REGION_GETMAP_BACKGROUND);
                } else if (this.regionGetMapBackgroundLayers.containsKey(bgSetting)) {
                    background = this.regionGetMapBackgroundLayers.get(bgSetting);
                }
            } else if (this.regionGetMapBackgroundLayers.containsKey(background)) {
                background = this.regionGetMapBackgroundLayers.get(background);
            }

            String minx = Double.toString(bboxOfImage.getMinX());
            String maxx = Double.toString(bboxOfImage.getMaxX());
            String miny = Double.toString(bboxOfImage.getMinY());
            String maxy = Double.toString(bboxOfImage.getMaxY());
            background = background.replace("{minx}", minx).replace("{maxx}", maxx).replace("{miny}", miny)
                .replace("{maxy}", maxy).replace("{srs}", srs)
                .replace("{width}", Integer.toString(imageDimensions.width))
                .replace("{height}", Integer.toString(imageDimensions.height)).replace("{MINX}", minx)
                .replace("{MAXX}", maxx).replace("{MINY}", miny).replace("{MAXY}", maxy).replace("{SRS}", srs)
                .replace("{WIDTH}", Integer.toString(imageDimensions.width))
                .replace("{HEIGHT}", Integer.toString(imageDimensions.height));

            InputStream in = null;
            try {
                URL imageUrl = new URL(background);
                // Setup the proxy for the request if required
                URLConnection conn = Lib.net.setupProxy(context, imageUrl);
                in = conn.getInputStream();
                image = ImageIO.read(in);
            } catch (IOException e) {
                image = new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_ARGB);
                error = e;
            } finally {
                if (in != null) {
                    IOUtils.closeQuietly(in);
                }

            }
        } else {
            image = new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D graphics = image.createGraphics();
        try {
            if (error != null) {
                graphics.drawString(error.getMessage(), 0, imageDimensions.height / 2);
            }
            ShapeWriter shapeWriter = new ShapeWriter();
            AffineTransform worldToScreenTransform = worldToScreenTransform(bboxOfImage, imageDimensions);
            Shape shape = worldToScreenTransform.createTransformedShape(shapeWriter.toShape(geom));
            graphics.setColor(Color.yellow);
            graphics.draw(shape);

            graphics.setColor(new Color(255, 255, 0, 100));
            graphics.fill(shape);

        } finally {
            graphics.dispose();
        }


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

    private double calculateExpandFactor(Envelope bboxOfImage, String srs) throws Exception {
        CoordinateReferenceSystem crs = Region.decodeCRS(srs);
        ReferencedEnvelope env = new ReferencedEnvelope(bboxOfImage, crs);
        env = env.transform(Region.WGS84, true);

        double diag = sqrt(pow(env.getWidth(), 2) + pow(env.getHeight(), 2));

        double scale = diag / WGS_DIAG;

        for (ExpandFactor factor : regionGetMapExpandFactors) {
            if (scale < factor.proportion) {
                return factor.factor;
            }
        }
        return regionGetMapExpandFactors.last().factor;
    }

    private Dimension calculateImageSize(Envelope bboxOfImage, Integer width, Integer height) {

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

        if (width != null) {
            return new Dimension(width, (int) Math.round(bboxOfImage.getHeight() / bboxOfImage.getWidth() * width));
        } else {
            return new Dimension((int) Math.round(bboxOfImage.getWidth() / bboxOfImage.getHeight() * height), height);
        }
    }

}

// =============================================================================

