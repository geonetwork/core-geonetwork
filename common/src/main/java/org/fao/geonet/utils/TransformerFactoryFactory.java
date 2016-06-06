/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.utils;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import java.util.Properties;

/**
 * This class is to prevent the situation that rogue applications running in the same JVM as does
 * GeoNetwork can set the System property about XSLT TransformerFactory, and so override
 * GeoNetwork's own definition in a classpath file, causing GeoNetwork to not function.
 *
 * Class holds a static TransformerFactory that is instantiated from the implementation name in the
 * classpath file "META-INF/services/javax.xml.transform.TransformerFactory" (or if reading that had
 * failed, from whatever is the current System property).
 *
 * @author heikki doeleman
 */
public class TransformerFactoryFactory {
    public final static String SYSTEM_PROPERTY_NAME = "javax.xml.transform.TransformerFactory";
    public static final String TRANSFORMER_PATH = "/WEB-INF/classes/META-INF/services/" + SYSTEM_PROPERTY_NAME;

    private static TransformerFactory factory;

    public static void init(String implementationName) {
        debug("Implementation name: " + implementationName);
        if (implementationName != null && implementationName.length() > 0) {
            factory = TransformerFactory.newInstance(implementationName, null);
        } else {
            factory = TransformerFactory.newInstance();
        }
    }

    public static TransformerFactory getTransformerFactory() throws TransformerConfigurationException {
        if (factory == null) {
            debug("TransformerFactoryFactory is null. Initializing ...");
            init(null);
        }
        debug("TransformerFactoryFactory: "
            + factory.getClass().getName()
            + " produces transformer implementation "
            + factory.newTransformer().getClass().getName());
        return factory;
    }

    // used for JUnit testing into Eclipse (which selects an incompatible TransformerFactory)
    public static void setTransformerFactory(TransformerFactory _factory) {
        factory = _factory;
    }

    private static void debug(String message) {
        Log.debug(Log.TRANSFORMER_FACTORY, message);
    }
}
