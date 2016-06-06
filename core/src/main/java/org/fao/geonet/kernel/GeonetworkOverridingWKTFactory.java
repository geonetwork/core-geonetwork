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

package org.fao.geonet.kernel;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import jeeves.server.context.ServiceContext;

import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.FactoryUsingWKT;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;


/**
 * Allow existing CRS definitions to be overridden by geonetwork.
 */
public class GeonetworkOverridingWKTFactory extends FactoryUsingWKT implements CoordinateOperationAuthorityFactory {

    private static final String SYSTEM_DEFAULT_USER_PROJ_FILE = "geonetwork.override.crs.proj.file";

    public GeonetworkOverridingWKTFactory() {
        super(null, MAXIMUM_PRIORITY);
    }

    public GeonetworkOverridingWKTFactory(Hints userHints) {
        super(userHints, MAXIMUM_PRIORITY);
    }

    /**
     * Returns the URL to the property file that contains CRS definitions. The default
     * implementation returns the URL to the {@value #FILENAME} file.
     *
     * @return The URL, or {@code null} if none.
     */
    protected URL getDefinitionsURL() {
        String overrideProjFile = System.getProperty(SYSTEM_DEFAULT_USER_PROJ_FILE);

        if (overrideProjFile == null) {
            ServiceContext srvContext = ServiceContext.get();
            if (srvContext != null) {
                overrideProjFile = srvContext.getServlet().getServletContext().getRealPath("/WEB-INF/override_epsg.properties");
            }

            if (overrideProjFile == null || !new File(overrideProjFile).exists()) {
                overrideProjFile = "override_epsg.properties";
            }
        }

        // Attempt to load user-defined projections
        File proj_file = new File(overrideProjFile);

        if (proj_file.exists()) {
            try {
                return proj_file.toURI().toURL();
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE, "Had troubles converting file name to URL", e);
            }
        }

        // Use the built-in property definitions
        overrideProjFile = "override_epsg.properties";

        return GeonetworkOverridingWKTFactory.class.getResource(overrideProjFile);
    }


}
