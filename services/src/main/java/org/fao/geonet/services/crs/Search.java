//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.crs;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.jdom.Element;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.nio.file.Path;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Get all Coordinate Reference System defined in GeoTools Referencing database and return them as a
 * Jeeves XML response element.
 *
 * @author francois
 */
@Deprecated
public class Search implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * Search for CRS
     *
     * @param params Parameter "name" is a list of word separated by spaces. Parameter "maxResults"
     *               is the max number of CRS returned. Parameter "type" is the type of CRS. Default
     *               value is CoordinateReferenceSystem.
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {

        int maxResults = Integer.valueOf(Util.getParam(params, "maxResults",
            "50"));

        // List of word space separated.
        String searchText = Util.getParam(params, Params.NAME, "");
        String filter[] = searchText.toUpperCase().split(" ");

        // CRS type could be one of CoordinateReferenceSystem,
        // VerticalCRS, ProjectedCRS, GeographicCRS.
        // Additional filter could be added to Constant.CRSType
        String crsType = Util.getParam(params, Params.TYPE, "");

        Class<? extends IdentifiedObject> crsTypeClass = CoordinateReferenceSystem.class;
        if (Constant.CRSType.containsKey(crsType))
            crsTypeClass = Constant.CRSType.get(crsType);

        // Search in all factories
        Element crs = filterCRS(filter, crsTypeClass, maxResults);

        return crs;
    }

    /**
     * filters all CRS Names from all available CRS authorities
     *
     * @param filter       array of keywords
     * @param crsTypeClass type of CRS to search for
     * @param maxResults   maximum number of results
     * @return XML with all CRS Names which contain all the filter keywords
     */
    private Element filterCRS(String[] filter,
                              Class<? extends IdentifiedObject> crsTypeClass,
                              int maxResults) {
        return null;
    }


    /**
     * checks if all keywords in filter array are in input
     *
     * @param input  test string
     * @param filter array of keywords
     * @return true, if all keywords in filter are in the input, false otherwise
     */
    protected boolean matchesFilter(String input, String[] filter) {
        for (String match : filter) {
            if (!input.contains(match))
                return false;
        }
        return true;
    }
}
