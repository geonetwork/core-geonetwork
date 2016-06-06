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

import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Test the logic of calculating the Geonetwork data directories when the node id is the default and
 * the system data directory is set to something other than the default via the general property
 * ({@link GeonetworkDataDirectory#GEONETWORK_DIR_KEY}.
 * <p/>
 * User: Jesse Date: 11/14/13 Time: 8:36 AM
 */
public class GeonetworkDataDirectoryMultiNodeServiceConfigOnlySystemDataDirSetTest extends AbstractGeonetworkDataDirectoryTest {

    @Override
    protected ArrayList<Element> getServiceConfigParameterElements() {
        ArrayList<Element> list = super.getServiceConfigParameterElements();
        list.add(createServiceConfigParam(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, getBaseDir().toString()));
        return list;
    }

    @Override
    protected String getGeonetworkNodeId() {
        return "node1";
    }

    @Override
    protected boolean isDefaultNode() {
        return false;
    }


    /**
     * Get The expected data directory
     */
    @Override
    protected Path getDataDir() {
        final Path baseDir = getBaseDir();
        return baseDir.getParent().resolve(baseDir.getFileName() + "_" + getGeonetworkNodeId());
    }

    private Path getBaseDir() {
        return testFixture.getDataDirContainer().toAbsolutePath().normalize();
    }


}
