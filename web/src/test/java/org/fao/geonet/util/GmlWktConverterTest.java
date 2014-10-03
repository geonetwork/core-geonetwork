package org.fao.geonet.util;

import static org.junit.Assert.*;

import org.fao.geonet.constants.Geonet;
import org.junit.Test;

public class GmlWktConverterTest {

	private static final String GML_NAMESPACE_URI = Geonet.Namespaces.GML.getURI();
	private static final String GML32_NAMESPACE_URI = Geonet.Namespaces.GML32.getURI();

	private static final String EPSG_4326_GML_TO_WKT_RESULT = 
		"POLYGON ((146 -42, 146 -41, 145 -41, 145 -40, 144 -40, 144 -39, 144 -38, 144 -37, 145 -37, 145 -38, 146 -38, 146 -39, 146 -40, 147 -40, 147 -41, 147 -42, 146 -42))";

	@Test
	public void testGmlToWktEpsg4326Input() {
		String epsg4326Gml = getEpsg4326Gml(GML_NAMESPACE_URI);
		String result = GmlWktConverter.gmlToWkt(epsg4326Gml);
		assertEquals(EPSG_4326_GML_TO_WKT_RESULT, result);
	}

	@Test
	public void testGml32WktForEpsg4326Input() {
		String epsg4326Gml = getEpsg4326Gml(GML32_NAMESPACE_URI);
		String result = GmlWktConverter.gml32ToWkt(epsg4326Gml);
		assertEquals(EPSG_4326_GML_TO_WKT_RESULT, result);
	}

	private String getEpsg4326Gml(String gmlUri) {
		return 
			"<gml:Polygon  xmlns:gml=\"" + gmlUri + "\" srsName=\"EPSG:4326\">" +
				"<gml:exterior>" +
					"<gml:LinearRing>" +
						"<gml:posList srsDimension=\"2\">-42 146 -41 146 -41 145 -40 145 -40 144 -39 144 -38 144 -37 144 -37 145 -38 145 -38 146 -39 146 -40 146 -40 147 -41 147 -42 147 -42 146</gml:posList>" +
					"</gml:LinearRing>" +
				"</gml:exterior>" +
			"</gml:Polygon>";
	}

}
