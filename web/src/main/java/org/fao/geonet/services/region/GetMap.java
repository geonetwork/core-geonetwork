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

import jeeves.exceptions.BadParameterEx;
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
 * Return an image of the region as a polygon against an optional background.  If the background is provided it is assumed to
 * be a url with placeholders for width, height, srs, minx,maxx,miny and maxy.  For example:
 * 
 *  http://www2.demis.nl/wms/wms.ashx?WMS=BlueMarble&LAYERS=Earth%20Image%2CBorders%2CCoastlines&FORMAT=image%2Fjpeg&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&SRS=EPSG%3A4326&BBOX={MINX},{MINY},{MAXX},{MAXY}&WIDTH={WIDTH}&HEIGHT={HEIGHT}
 *  
 *  the placeholders must either be all uppercase or all lowercase
 *  
 *  The parameters to the service are:
 *  
 *  id - id of the region to render
 *  srs - (optional) default is EPSG:4326 otherwise it is the project to use when rendering the image
 *  width - (optional) width of the image that is created.  Only one of width and height are permitted
 *  height - (optional) height of the image that is created.  Only one of width and height are permitted
 *  background - URL for loading a background image for regions.   A WMS Getmap request is the typical example.  
 *      the URL must be parameterized with the following parameters: minx, maxx, miny, maxy, width, height and optionally srs
 *      
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
        String widthString = params.getChildText(WIDTH_PARAM);
        String heightString = params.getChildText(HEIGHT_PARAM);
        String background = params.getChildText(BACKGROUND_PARAM);

        if (id == null) {
            throw new BadParameterEx(Params.ID, Params.ID+" cannot be null");
        }

        // see calculateImageSize for more parameter checks
        
        RegionsDAO dao = context.getApplicationContext().getBean(RegionsDAO.class);
        Geometry region = dao.getGeom(context, id, false);
        CoordinateReferenceSystem geometryCRS = (CoordinateReferenceSystem) region.getUserData();
        CoordinateReferenceSystem desiredCRS = CRS.decode(srs, true);
        Integer geomCode = CRS.lookupEpsgCode(geometryCRS, false);
        if (geomCode == null || !srs.equalsIgnoreCase("EPSG:"+geomCode) && !CRS.equalsIgnoreMetadata(geometryCRS, desiredCRS)) {
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
        Dimension imageDimenions = calculateImageSize(bboxOfImage, widthString, heightString);
        if (background != null) {

            String minx = Double.toString(bboxOfImage.getMinX());
            String maxx = Double.toString(bboxOfImage.getMaxX());
            String miny = Double.toString(bboxOfImage.getMinY());
            String maxy = Double.toString(bboxOfImage.getMaxY());
            background = background.
                replace("{minx}", minx).
                replace("{maxx}", maxx).
                replace("{miny}", miny).
                replace("{maxy}", maxy).
                replace("{srs}", srs).
                replace("{width}", Integer.toString(imageDimenions.width)).
                replace("{height}", Integer.toString(imageDimenions.height)).
                replace("{MINX}", minx).
                replace("{MAXX}", maxx).
                replace("{MINY}", miny).
                replace("{MAXY}", maxy).
                replace("{SRS}", srs).
                replace("{WIDTH}", Integer.toString(imageDimenions.width)).
                replace("{HEIGHT}", Integer.toString(imageDimenions.height));

            URL imageUrl = new URL(background);
            InputStream in = imageUrl.openStream();
            try {
                image = ImageIO.read(in);
            } finally {
                in.close();

            }
        } else {
            image = new BufferedImage(imageDimenions.width, imageDimenions.height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D graphics = image.createGraphics();
        try {
            ShapeWriter shapeWriter = new ShapeWriter();
            AffineTransform worldToScreenTransform = worldToScreenTransform(bboxOfImage, imageDimenions);
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

    private Dimension calculateImageSize(Envelope bboxOfImage, String widthString, String heightString) {

        if (widthString != null && heightString != null) {
            throw new BadParameterEx(WIDTH_PARAM, "Only one of " + WIDTH_PARAM + " and " + HEIGHT_PARAM
                    + " can be defined currently.  Future versions may support this but it is not supported at the moment");
        }
        if(widthString==null && heightString==null) {
            throw new BadParameterEx(WIDTH_PARAM, "One of " + WIDTH_PARAM + " or " + HEIGHT_PARAM
                    + " parameters must be included in the request");
            
        }

        int width, height;
        if(widthString != null) {
            width = Integer.parseInt(widthString);
            height = (int) Math.round(bboxOfImage.getWidth()/bboxOfImage.getHeight()/width);
        } else {
            height = Integer.parseInt(heightString);
            width = (int) Math.round(bboxOfImage.getWidth()/bboxOfImage.getHeight()*height);
        }
        return new Dimension(width, height);
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

