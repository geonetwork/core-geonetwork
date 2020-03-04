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

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.api.regions.GeomFormat;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionNotFoundEx;
import org.fao.geonet.kernel.region.RegionsDAO;
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
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MapRenderer {

    private final ServiceContext context;

    private static final double WGS_DIAG = sqrt(pow(360, 2) + pow(180, 2));

    public MapRenderer(ServiceContext context) {
        this.context = context;
    }

    public static AffineTransform worldToScreenTransform(Envelope mapExtent, Dimension screenSize) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

        return new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);
    }

    /**
     * Check if a geometry is in the domain of validity of a projection and if not return the
     * intersection of the geometry with the coordinate system domain of validity.
     */
    public static Geometry computeGeomInDomainOfValidity(Geometry geom, CoordinateReferenceSystem mapCRS) {
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
                            Geometry extentPolygon = JTS.toGeometry(env);
                            adjustedGeom = geom.intersection(extentPolygon);
                        }
                    }
                }
            }
            return adjustedGeom;
        } else {
            return geom;
        }
    }

    public BufferedImage render(String id, String srs, Integer width, Integer height,
                                String background, String geomParam, String geomType, String geomSrs) throws Exception {
        ApplicationContext appContext = context.getApplicationContext();
        Map<String, String> regionGetMapBackgroundLayers = appContext.getBean("regionGetMapBackgroundLayers", Map.class);
        SortedSet<ExpandFactor> regionGetMapExpandFactors = appContext.getBean("regionGetMapExpandFactors", SortedSet.class);
        SettingManager settingManager = appContext.getBean(SettingManager.class);

        Geometry geom = null;
        if (id != null) {
            Collection<RegionsDAO> daos = context.getApplicationContext().getBeansOfType(RegionsDAO.class).values();
            for (RegionsDAO regionsDAO : daos) {

                geom = regionsDAO.getGeom(context, id, false, srs);
                if (geom != null) {
                    break;
                }
            }
            if (geom == null) {
                throw new RegionNotFoundEx(id);
            }
        } else {
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
        double expandFactor = calculateExpandFactor(regionGetMapExpandFactors, bboxOfImage, srs);
        bboxOfImage.expandBy(bboxOfImage.getWidth() * expandFactor, bboxOfImage.getHeight() * expandFactor);
        Dimension imageDimensions = calculateImageSize(bboxOfImage, width, height);

        Exception error = null;
        if (background != null) {
            // 4 cases:
            // * request param is 'settings' and db setting is a full url
            // * request param is 'settings' and db setting is a named bg layer
            // * request param is a named bg layer
            // * request param is a full url
            if (background.equalsIgnoreCase(MetadataExtentApi.SETTING_BACKGROUND)) {
                String bgSetting = settingManager.getValue(Settings.REGION_GETMAP_BACKGROUND);
                if (bgSetting.startsWith("http://") || bgSetting.startsWith("https://")) {
                    background = settingManager.getValue(Settings.REGION_GETMAP_BACKGROUND);
                } else if (regionGetMapBackgroundLayers.containsKey(bgSetting)) {
                    background = regionGetMapBackgroundLayers.get(bgSetting);
                }
            } else if (regionGetMapBackgroundLayers.containsKey(background)) {
                background = regionGetMapBackgroundLayers.get(background);
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
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                // draw each included geometry separately to ensure they are filled correctly
                Shape shape = worldToScreenTransform.createTransformedShape(shapeWriter.toShape(geom.getGeometryN(i)));
                graphics.setColor(Color.yellow);
                graphics.draw(shape);

                graphics.setColor(new Color(255, 255, 0, 100));
                graphics.fill(shape);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private double calculateExpandFactor(SortedSet<ExpandFactor> regionGetMapExpandFactors, Envelope bboxOfImage,
                                         String srs) throws Exception {
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
        if (width != null) {
            return new Dimension(width, (int) Math.round(bboxOfImage.getHeight() / bboxOfImage.getWidth() * width));
        } else {
            return new Dimension((int) Math.round(bboxOfImage.getWidth() / bboxOfImage.getHeight() * height), height);
        }
    }

}
