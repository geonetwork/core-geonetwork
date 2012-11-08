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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;

import org.fao.geonet.constants.Params;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.jdom.Element;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

//=============================================================================

/**
 * Returns a specific region and coordinates given its id
 */

public class GetMap implements Service {
    public static final String SRS_PARAM = "SRS";
    public static final String WIDTH_PARAM = "WIDTH";
    public static final String HEIGHT_PARAM = "HEIGHT";
    public static final String BACKGROUND_PARAM = "BACKGROUND";
    private String format;

    public void init(String appPath, ServiceConfig params) throws Exception {
        this.format = params.getMandatoryValue("format");
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        String id = params.getChildText(Params.ID);
        String srs = Util.getParam(params, SRS_PARAM, "EPSG:4326");
        int width = Util.getParamAsInt(params, WIDTH_PARAM);
        int height = Util.getParamAsInt(params, HEIGHT_PARAM);
        String background = params.getChildText(BACKGROUND_PARAM);

        if (id == null)
            return new Element(Jeeves.Elem.RESPONSE);

        RegionsDAO dao = context.getApplicationContext().getBean(RegionsDAO.class);
        Geometry region = dao.getGeom(context, id, false);
        CoordinateReferenceSystem geometryCRS = (CoordinateReferenceSystem) region.getUserData();
        CoordinateReferenceSystem desiredCRS = CRS.decode(srs);
        if (!CRS.equalsIgnoreMetadata(geometryCRS, desiredCRS)) {
            MathTransform transform = CRS.findMathTransform(geometryCRS, desiredCRS, true);
            region = JTS.transform(region, transform);
        }
        if (region == null) {
            throw new RegionNotFoundEx(id);
        }

        BufferedImage image;
        Envelope bboxOfImage = new Envelope(region.getEnvelopeInternal());
        double expandFactor = 0.2;
        bboxOfImage.expandBy(bboxOfImage.getWidth() * expandFactor, bboxOfImage.getHeight() * expandFactor);
        if (background != null) {

            String minx = Double.toString(bboxOfImage.getMinX());
            String maxx = Double.toString(bboxOfImage.getMaxX());
            String miny = Double.toString(bboxOfImage.getMinY());
            String maxy = Double.toString(bboxOfImage.getMaxY());
            background.replace("{minx}", minx).replace("{maxx}", maxx).replace("{miny}", miny).replace("{maxy}", maxy)
                    .replace("{width}", Integer.toString(width)).replace("{height}", Integer.toString(height));

            URL imageUrl = new URL(background);
            InputStream in = imageUrl.openStream();
            try {
                image = ImageIO.read(in);
            } finally {
                in.close();

            }
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D graphics = image.createGraphics();
        try {
            ShapeWriter shapeWriter = new ShapeWriter();
            AffineTransform worldToScreenTransform = worldToScreenTransform(bboxOfImage, new Dimension(width, height));
            MathTransform mathTransform = new AffineTransform2D(worldToScreenTransform);
            Geometry screenGeom = JTS.transform(region, mathTransform);
            Shape shape = shapeWriter.toShape(screenGeom);
            graphics.setColor(Color.yellow);
            graphics.draw(shape);
            
            graphics.setColor(new Color(255,255,0,100));
            graphics.fill(shape);
            
        } finally {
            graphics.dispose();
        }

        File tmpFile = File.createTempFile("GetMap", "."+format);
        ImageIO.write(image, format, tmpFile);
        return BinaryFile.encode(200, tmpFile.getAbsolutePath(), true);
    }

    public static AffineTransform worldToScreenTransform(Envelope mapExtent, Dimension screenSize) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);

        return at;
    }

}

// =============================================================================

