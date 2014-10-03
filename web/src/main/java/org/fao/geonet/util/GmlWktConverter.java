//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import java.io.StringReader;

import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.geotools.geometry.jts.JTS;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class GmlWktConverter {
	private final Parser parser;

	public GmlWktConverter(Parser parser) {
		this.parser = parser;
		parser.setStrict(false);
		parser.setValidating(false);
	}

	private String toWkt(String gml) {
		try {
			Geometry geom = (Geometry) parser.parse(new StringReader(gml));
			WKTWriter writer = new WKTWriter();
			return writer.write(toWgs84(geom));
		} catch (Exception e) {
			Log.error(Geonet.GEONETWORK, "Could not parse " + gml + " " + e.getMessage());
			return "";
		}
	}

	private Geometry toWgs84(Geometry geom) throws Exception {
		// Return input geometry if CRS isn't available
		if (!(geom.getUserData() instanceof CoordinateReferenceSystem)) return geom;

		CoordinateReferenceSystem sourceCrs = (CoordinateReferenceSystem) geom.getUserData();

		// Return the input geometry if input geometry's CRS is already WGS84 
		if (CRS.equalsIgnoreMetadata(sourceCrs, DefaultGeographicCRS.WGS84)) return geom;

		// Otherwise return input geometry transformed to WGS84 CRS
		MathTransform tform = CRS.findMathTransform(sourceCrs, DefaultGeographicCRS.WGS84);
		return JTS.transform(geom, tform);
	}

	static public String gmlToWkt(String gml) {
		Parser parser = new Parser(new GMLConfiguration());
		return toWkt(gml, parser);
	}

	static public String gml32ToWkt(String gml) {
		Parser parser = new Parser(new org.geotools.gml3.v3_2.GMLConfiguration());
		return toWkt(gml, parser);
	}

	private static String toWkt(String gml, Parser parser) {
		GmlWktConverter converter = new GmlWktConverter(parser);
		return converter.toWkt(gml);
	}

}
