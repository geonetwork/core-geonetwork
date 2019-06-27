package org.fao.geonet.util;

import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;

public final class GMLParsers {
  public static Parser[] create() {
	  Parser[] parsers = {
			  new Parser(new GMLConfiguration()),
			  new Parser(new org.geotools.gml3.v3_2.GMLConfiguration())
	  };
	  return parsers;
  }
   
  private GMLParsers() {}
}
