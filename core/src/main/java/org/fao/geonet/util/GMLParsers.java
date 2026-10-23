/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.NoOpEntityResolver;
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
		parser.setEntityResolver(NoOpEntityResolver.INSTANCE);
		parser.setStrict(false);
		parser.setValidating(false);
		return parser;
	}
}
