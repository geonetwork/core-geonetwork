//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.wro4j;

import org.fao.geonet.utils.Log;
import ro.isdc.wro.http.WroFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketException;

/**
 * @author Jesse on 2/1/2015.
 */
public class GeonetWro4jFilter extends WroFilter {

    public static final String GEONET_WRO4J_FILTER_KEY = "GEONET_WRO4J_FILTER_KEY";

    @Override
    protected void doInit(FilterConfig config) throws ServletException {
        super.doInit(config);
        config.getServletContext().setAttribute(GEONET_WRO4J_FILTER_KEY, this);
    }

    @Override
    protected void onException(Exception e, HttpServletResponse response, FilterChain chain) {
        if (e.getCause() instanceof SocketException) {
            // ignore this because it means that a client closed the socket while data was being written to it.
        } else {
            Log.error(GeonetworkWrojManagerFactory.WRO4J_LOG, "Error occurred during a wro4j request handling", e);
            // Do not call super to avoid having WRO4J chaining
            // to other filters.
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
}
