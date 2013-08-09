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

import java.util.Hashtable;
import java.util.List;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Service to get the db-stored requests information group by a date (year, month, day)
 * 
 * @author nicolas Ribot
 * 
 */
public class RequestsByDateStatistics extends NotInReadOnlyModeService {
    public static final String BY_YEAR = "YEAR";
    public static final String BY_MONTH = "MONTH";
    public static final String BY_DAY = "DAY";
    public static final String BY_HOUR = "HOUR";

    /** the date to search for from (format MUST be: ) */
    private String dateFrom;
    /** the date to search for too (format MUST be: yyy-MM-ddThh:mm) */
    private String dateTo;
    /** the type of graphic (by year, month or day to display */
    private String graphicType;
    /** to return service statistics by service type (ie. csw, oai, q, ...)*/
    private boolean byType = false;

    /** the custom part of the date query; according to user choice for graphic */
    public Hashtable<String, String> queryFragments;


    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);

        queryFragments = new Hashtable<String, String>(4);

        queryFragments.put(RequestsByDateStatistics.BY_HOUR, "substring(requestDate, 1, 13)");
        queryFragments.put(RequestsByDateStatistics.BY_DAY, "substring(requestDate, 1, 10)");
        queryFragments.put(RequestsByDateStatistics.BY_MONTH, "substring(requestDate, 1, 7)");
        queryFragments.put(RequestsByDateStatistics.BY_YEAR, "substring(requestDate, 1, 4)");
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        this.dateFrom = Util.getParam(params, "dateFrom");
        this.dateTo = Util.getParam(params, "dateTo");
        this.graphicType = Util.getParam(params, "graphicType");
        this.byType = Util.getParam(params, "byType", false);
        
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        
        
        // TODO : if ByServiceType
        if (byType) {
            String serviceTypesQuery = "SELECT DISTINCT(service) as type FROM Requests";
            Element serviceTypes = dbms.select(serviceTypesQuery);
            for (Object o : serviceTypes.getChildren()) {
                Element type = (Element)o;
                String serviceType = type.getChildText("type");
                String query = buildQuery(serviceType);
                if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                    Log.debug(Geonet.SEARCH_LOGGER, "query to get count by date:\n" + query);
                }
                Element results = dbms.select(query, this.dateFrom, this.dateTo, serviceType);
                results.setAttribute("service", serviceType);
                elResp.addContent(results);
            }
        } else {
            String query = buildQuery(null);
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                Log.debug(Geonet.SEARCH_LOGGER, "query to get count by date:\n" + query);
            }
            Element results = dbms.select(query, this.dateFrom, this.dateTo);
            elResp.addContent(results);
        }
        
        
        SearchStatistics.addSingleDBValueToElement(dbms, elResp, 
                "SELECT min(requestdate) AS min FROM Requests", 
                "dateMin", "min", null);
        SearchStatistics.addSingleDBValueToElement(dbms, elResp, 
                "SELECT max(requestdate) AS max FROM Requests", 
                "dateMax", "max", null);
        
        elResp.addContent(new Element("dateFrom").setText(this.dateFrom));
        elResp.addContent(new Element("dateTo").setText(this.dateTo));

        
        return elResp;
    }

    public String buildQuery(String service) {
        String requestDateSubstring = this.queryFragments.get(this.graphicType);

        StringBuilder query = new StringBuilder("SELECT ");
        query.append(requestDateSubstring);
        query.append(" as reqdate, count(*) as number FROM Requests ");
        query.append(" where requestdate >= ?");
        query.append(" and requestdate <= ?");
        if (service != null) {
            query.append(" and service = ?");
        }
        query.append(" GROUP BY ");
        query.append(requestDateSubstring);
        query.append(" ORDER BY ");
        query.append(requestDateSubstring);

        return query.toString();
    }
}
