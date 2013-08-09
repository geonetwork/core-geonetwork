//=============================================================================
//===   Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.statistics;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Service to get the search terms for a field and optionnally a specific service
 */
public class SearchTermsStatistics extends NotInReadOnlyModeService {
    
    private static final String FIELD_QUERY = "SELECT termtext, COUNT(*) AS total " +
                                            "FROM params WHERE termfield = ? " + 
                                            "GROUP BY termtext ORDER BY total DESC";
    private static final String FIELD_AND_SERVICE_QUERY = "SELECT termtext, COUNT(*) AS total " +
                                            "FROM params p, requests r WHERE p.requestid = r.id AND termfield = ? AND service = ?" + 
                                            "GROUP BY termtext ORDER BY total DESC";
    private static final String FIELD_PARAM = "field";
    private static final String SERVICE_PARAM = "service";
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }
    
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String field = Util.getParam(params, FIELD_PARAM);
        String service = Util.getParam(params, SERVICE_PARAM, "");
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        if (service.equals("")) {
            return dbms.select(FIELD_QUERY, field);
        } else {
            return dbms.select(FIELD_AND_SERVICE_QUERY, field, service);
        }
    }
}