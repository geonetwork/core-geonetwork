package org.fao.geonet.util;

import org.geotools.gml3.GMLConfiguration;
import org.geotools.xsd.Parser;

public final class GMLParsers {


	public static final GMLConfiguration GML_CONFIGURATION = new GMLConfiguration();
	public static final org.geotools.gml3.v3_2.GMLConfiguration GML_32_CONFIGURATION = new org.geotools.gml3.v3_2.GMLConfiguration();

	public static Parser[] create() {
	  Parser[] parsers = {
			  new Parser(GML_CONFIGURATION),
			  new Parser(GML_32_CONFIGURATION)
	  };
	  for (Parser _parser : parsers) {
		_parser.setStrict(false);
		_parser.setValidating(false);
	  }
	return parsers;
  }
   
  private GMLParsers() {}
}
