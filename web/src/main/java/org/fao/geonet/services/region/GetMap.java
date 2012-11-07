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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;

import org.fao.geonet.constants.Params;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.jdom.Element;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.awt.ShapeWriter;
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

    public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id = params.getChildText(Params.ID);
		String srs = Util.getParam(params, SRS_PARAM, "EPSG:4326");
		int width = Util.getParamAsInt(params, WIDTH_PARAM);
		int height = Util.getParamAsInt(params, HEIGHT_PARAM);
		String background = params.getChildText(BACKGROUND_PARAM);

		if (id == null)
			return new Element(Jeeves.Elem.RESPONSE);

		RegionsDAO dao = context.getApplicationContext().getBean(RegionsDAO.class);
		Geometry region = dao.getGeom(context, id, false);
		if (!srs.equalsIgnoreCase((String) region.getUserData())) {
		    CoordinateReferenceSystem geometryCRS = CRS.decode((String) region.getUserData());
		    CoordinateReferenceSystem desiredCRS = CRS.decode(srs);
		    if (!CRS.equalsIgnoreMetadata(geometryCRS, desiredCRS)) {
		        MathTransform transform = CRS.findMathTransform(geometryCRS, desiredCRS, true);
		        region = JTS.transform(region, transform );
		    }
		}
		if (region == null) {
		    throw  new RegionNotFoundEx(id);
		}
		
		BufferedImage image;
		if (background != null) {
		    URL imageUrl = new URL(background);
		    InputStream in = imageUrl.openStream();
		    try {
		        image = ImageIO.read(in);
		    } finally {
		        in.close();
		        
		    }
		} else {
		    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		
		Graphics2D graphics = image.createGraphics();
		try {
		   ShapeWriter shapeWriter = new ShapeWriter();
	       graphics.draw(shapeWriter.toShape(region));
		} finally {
		    graphics.dispose();
		}
		File tmpFile = File.createTempFile("GetMap", format);
        ImageIO.write(image, "image/"+format, tmpFile );
		return BinaryFile.encode(200, tmpFile.getAbsolutePath(), true);
	}
}

// =============================================================================

