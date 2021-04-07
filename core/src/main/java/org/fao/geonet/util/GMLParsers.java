package org.fao.geonet.util;

import org.fao.geonet.constants.Geonet;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Parser;
import org.jdom.Element;

public final class GMLParsers {

	private static final GMLConfiguration GML_CONFIGURATION = new GMLConfiguration();
	private static final org.geotools.gml3.v3_2.GMLConfiguration GML_32_CONFIGURATION = new org.geotools.gml3.v3_2.GMLConfiguration();

	static public Parser create(Element geom) {
		if (geom.getNamespace().equals(Geonet.Namespaces.GML32)) {
			return createGML32();
		} else {
			return createGML();
		}
	}

	static private Parser createGML32() {
		return createParser(GML_32_CONFIGURATION);
	}

	public static Parser createGML() {
		return createParser(GML_CONFIGURATION);
	}

	static private Parser createParser(Configuration configuration) {
		Parser parser = new Parser(configuration);
		parser.setStrict(false);
		parser.setValidating(false);
		return parser;
	}
}
