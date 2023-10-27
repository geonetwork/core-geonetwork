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

package org.fao.geonet.kernel.csw.services.getrecords;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Parser;
import org.jdom.Element;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.capability.FilterCapabilities;
import org.xml.sax.SAXException;

public class FilterParser {

    private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();
    private static final Configuration FILTER_2_0_0 = new org.geotools.filter.v2_0.FESConfiguration();

    private static Parser createFilterParser(String filterVersion) {
        Configuration config;
        if (filterVersion.equals(FilterCapabilities.VERSION_100)) {
            config = FILTER_1_0_0;
        } else if (filterVersion.equals(FilterCapabilities.VERSION_200)) {
            config = FILTER_2_0_0;
        } else if (filterVersion.equals(FilterCapabilities.VERSION_110)) {
            config = FILTER_1_1_0;
        } else {
            throw new IllegalArgumentException("UnsupportFilterVersion: " + filterVersion);
        }
        return new Parser(config);
    }

    /**
     * Converts a XML representation of an OGC Filter into a OpenGIS Filter.
     *
     * @param xml           XML representation of Filter.
     * @param filterVersion OGC Filter API version to use.
     * @return Parsed filter or null if parsed object is not of class Filter or xml
     *         input was null.
     */

    public static Filter parseFilter(Element xml, String filterVersion) {
        if (xml == null) {
            return null;
        }
        return parseFilter(Xml.getString(xml), filterVersion);
    }

    /**
     * Converts a XML representation of an OGC Filter into a OpenGIS Filter.
     *
     * @param xml           XML-String representing the Filter.
     * @param filterVersion OGC Filter API version to use.
     * @return Parsed filter or null if parsed object is not of class Filter or
     *         input was null.
     */
    public static Filter parseFilter(String string, String filterVersion) {
        if (string == null) {
            return null;
        }

        final Parser parser = createFilterParser(filterVersion);
        parser.setValidating(true);
        parser.setFailOnValidationError(true);

        try {
            final Object parseResult = parser.parse(new StringReader(string));
            if (parseResult instanceof Filter) {
                return (Filter) parseResult;
            } else {
                return null;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Log.error(Geonet.CSW_SEARCH, "Errors occurred when trying to parse a filter", e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
