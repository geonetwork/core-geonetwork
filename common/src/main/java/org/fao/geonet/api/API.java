/*
 * =============================================================================
 * ===	Copyright (C) 2016-2022 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * GeoNetwork Application Programming Interface constants.
 *
 * Created by francois on 08/01/16.
 * @author Francois
 */
public class API {

    /**
     * @deprecated unused
     */
    public static final String CONTACT_EMAIL = "geonetwork@osgeo.org";
    /**
     * Logging {@code api} module name.
     *
     * @deprecated Use {@link #LOG_MARKER}
     */
    public static final String LOG_MODULE_NAME = "geonetwork.api" ;
    /**
     * Marker for {@code api} log messages.
     */
    public static final Marker LOG_MARKER = MarkerManager.getMarker("api").addParents(MarkerManager.getMarker("geonetwork"));
}
