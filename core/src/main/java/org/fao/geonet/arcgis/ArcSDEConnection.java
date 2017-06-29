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
package org.fao.geonet.arcgis;

import org.fao.geonet.constants.Geonet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Class for retrieving ISO metadata from an ArcSDE server. The metadata in ArcSDE is scanned for
 * "MD_Metadata" and those that match are included in the result unprocessed, so including any
 * non-ISO ESRI elements they may contain.
 */
public interface ArcSDEConnection {
    String ARCSDE_LOG_MODULE_NAME = Geonet.HARVESTER + ".arcsde";
    String ISO_METADATA_IDENTIFIER = "MD_Metadata";



    /**
     * Retrieves all metadata records found in the ArcSDE database.
     *
     * @param cancelMonitor if true stops the current metadata processing.
     * @param arcSDEVersion ArcSDE version.
     *
     * @return results the list with all the XML metadata records found.
     * @throws Exception
     */
    Map<String, String> retrieveMetadata(AtomicBoolean cancelMonitor, String arcSDEVersion) throws Exception;

    /**
     * Closes the connection to the ArcSDE server.
     */
    public void close() throws ArcSDEConnectionException;
}
