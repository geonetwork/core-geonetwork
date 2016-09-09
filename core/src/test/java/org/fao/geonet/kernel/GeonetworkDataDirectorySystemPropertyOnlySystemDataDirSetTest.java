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

import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;

/**
 * Test the logic of calculating the Geonetwork data directories when the node id is the default and
 * the system data directory is set to something other than the default via the general property
 * ({@link GeonetworkDataDirectory#GEONETWORK_DIR_KEY}.
 * <p/>
 * User: Jesse Date: 11/14/13 Time: 8:36 AM
 */
public class GeonetworkDataDirectorySystemPropertyOnlySystemDataDirSetTest extends AbstractGeonetworkDataDirectoryTest {


    @Before
    public void setSystemProperties() {
        System.setProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, getDataDir().toString());
    }

    @After
    public void resetSystemProperties() {
        System.clearProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY);
    }

    /**
     * Get The expected data directory
     */
    @Override
    protected Path getDataDir() {
        return testFixture.getDataDirContainer().resolve("nonDefaultDataDir").toAbsolutePath().normalize();
    }


}
