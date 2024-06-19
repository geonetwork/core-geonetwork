/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.regions.GeomFormat;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionNotFoundEx;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.awt.IdentityPointTransformation;
import org.locationtech.jts.awt.PointShapeFactory;
import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.metadata.extent.Extent;
import org.geotools.api.metadata.extent.GeographicBoundingBox;
import org.geotools.api.metadata.extent.GeographicExtent;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.springframework.context.ApplicationContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.locationtech.jts.geom.Geometry.TYPENAME_GEOMETRYCOLLECTION;
import static org.locationtech.jts.geom.Geometry.TYPENAME_POINT;

public class MapRenderer {

    private static final double WGS_DIAG = sqrt(pow(360, 2) + pow(180, 2));
    private final ServiceContext context;

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
     * Returns a bounding box geometry.
     *
     * @param geom                  Bounding box geometry.
     * @param srs                   Bounding box geometry srs.
     * @param useGeodesicExtents    false: returns the bounding box geometry as a rectangle (using min / max bounds).
     *                              true: returns the bounding box geometry.
     * @return bounding box geometry.
     */
    public static Geometry getGeometryExtent(Geometry geom, String srs, boolean useGeodesicExtents) {
        boolean isGlobalSrs = srs.equals("EPSG:4326") || srs.equals("EPSG:3857");

        return (!isGlobalSrs && !useGeodesicExtents ?  geom.getEnvelope() : geom);
    }

    /**
     * Check if a geometry is in the domain of validity of a projection and if not return the
     * intersection of the geometry with the coordinate system domain of validity.
     */
    public static Geometry computeGeomInDomainOfValidity(Geometry geom, CoordinateReferenceSystem mapCRS) {
        final Extent domainOfValidity = mapCRS.getDomainOfValidity();
        Geometry adjustedGeom = geom;
        if (domainOfValidity != null) {
            for (final GeographicExtent extent :
                domainOfValidity.getGeographicElements()) {
                if ((extent == null) || (Boolean.FALSE.equals(extent.getInclusion()))) {
                    continue;
                }

                if (extent instanceof GeographicBoundingBox) {
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
            return adjustedGeom;
        } else {
            return geom;
        }
    }

    public BufferedImage render(String id, String srs, Integer width, Integer height,
                                String background, String geomParam, String geomType,
                                String geomSrs, String fillColor, String strokeColor) throws Exception {
        ApplicationContext appContext = context.getApplicationContext();
        Map<String, String> regionGetMapBackgroundLayers = appContext.getBean("regionGetMapBackgroundLayers", Map.class);
        SortedSet<ExpandFactor> regionGetMapExpandFactors = appContext.getBean("regionGetMapExpandFactors", SortedSet.class);
        SettingManager settingManager = appContext.getBean(SettingManager.class);

        boolean useGeodesicExtents = settingManager.getValueAsBool(Settings.REGION_GETMAP_GEODESIC_EXTENTS, false);

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
        boolean isPoint = geom.getGeometryType().equals(TYPENAME_POINT)
                || (geom.getGeometryType().equals(TYPENAME_GEOMETRYCOLLECTION)
                    && geom.getNumGeometries() == 1
                    && geom.getGeometryN(0).getGeometryType().equals(TYPENAME_POINT));
        int pointBufferSize = 150;

        Envelope bboxOfImage = new Envelope(isPoint ?
                geom.buffer(pointBufferSize).getEnvelopeInternal() : geom.getEnvelopeInternal());
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
                if (bgSetting.startsWith("http://") || bgSetting.startsWith("https://") || bgSetting.startsWith("{")) {
                    background = settingManager.getValue(Settings.REGION_GETMAP_BACKGROUND);
                } else if (regionGetMapBackgroundLayers.containsKey(bgSetting)) {
                    background = regionGetMapBackgroundLayers.get(bgSetting);
                }
            } else if (regionGetMapBackgroundLayers.containsKey(background)) {
                background = regionGetMapBackgroundLayers.get(background);
            }

            BaseMapRenderer baseMapRenderer = new BaseMapRenderer(background);
            baseMapRenderer
                .srs(srs)
                .bbox(bboxOfImage)
                .imageDimensions(imageDimensions)
                .context(context)
                ;

            BufferedImage baseMapImage = baseMapRenderer.render();

            image = baseMapImage;
        } else {
            image = new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D graphics = image.createGraphics();
        try {
            if (error != null) {
                graphics.drawString(error.getMessage(), 0, imageDimensions.height / 2);
            }

            Color geomFillColor = getColor(fillColor, new Color(0, 0, 0, 30));
            Color geomStrokeColor = getColor(strokeColor, new Color(0, 0, 0, 255));
            AffineTransform worldToScreenTransform = worldToScreenTransform(bboxOfImage, imageDimensions);
            int pointSize = 5;
            ShapeWriter shapeWriter = new ShapeWriter(new IdentityPointTransformation(),
                    new PointShapeFactory.Circle(pointSize * bboxOfImage.getWidth() / imageDimensions.getWidth()));

            for (int i = 0; i < geom.getNumGeometries(); i++) {
                Geometry geomExtent = MapRenderer.getGeometryExtent(geom.getGeometryN(i), srs, useGeodesicExtents);
                // draw each included geometry separately to ensure they are filled correctly
                Shape shape = worldToScreenTransform.createTransformedShape(shapeWriter.toShape(geomExtent));
                graphics.setColor(geomFillColor);
                graphics.fill(shape);

                graphics.setColor(geomStrokeColor);
                graphics.setStroke(new BasicStroke(2));
                graphics.draw(shape);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private Color getColor(String color, Color defaultColor) {
        if (StringUtils.isNotEmpty(color)) {
            String[] colorsConfig = color.split(",");
            if (colorsConfig.length == 4) {
                try {
                    return new Color(
                        Integer.parseInt(colorsConfig[0]),
                        Integer.parseInt(colorsConfig[1]),
                        Integer.parseInt(colorsConfig[2]),
                        Integer.parseInt(colorsConfig[3]));
                } catch (Exception e) {
                    throw new BadParameterEx(String.format(
                        "Invalid color configuration '%s'. Error is '%s'. Format must be 'RED,GREEN,BLUE,ALPHA' with integer value from 0 to 255.",
                        color, e.getMessage()));
                }
            } else {
                throw new BadParameterEx(String.format(
                    "Invalid color configuration '%s'. Format must be 'RED,GREEN,BLUE,ALPHA' with integer value from 0 to 255.",
                    color));
            }
        }
        return defaultColor;
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
