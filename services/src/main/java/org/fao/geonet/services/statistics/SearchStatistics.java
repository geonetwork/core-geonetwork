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

import java.sql.SQLException;
import java.util.List;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.JODAISODate;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;

/**
 * Service to get core metrics for a service:
 * <ul>
 * <li>total number of searches</li>
 * <li>Average searches by day</li>
 * <li>Average searches by month</li>
 * <li>Number of record views</li>
 * </ul>
 * 
 */
public class SearchStatistics extends NotInReadOnlyModeService {
    
    private static final String NUMBER_OF_SEARCH_QUERY = "SELECT COUNT(*) AS total " +
                                            "FROM requests WHERE service = ?";
    
    private static final String NUMBER_SEARCH_BY_X_QUERY = "SELECT count(*) / ? AS avg FROM requests " +
                                            "WHERE service = ? ";
    
    private static final String NUMBER_VIEWS_BY_X_QUERY = "SELECT sum(popularity) / ? AS avg FROM metadata";
    
    private static final String NUMBER_OF_SEARCHES_WITH_NO_HITS_QUERY = 
                                "SELECT count(*) AS total FROM requests WHERE hits = 0 AND service = ?";
    
    private static final String SERVICE_PARAM = "service";
    
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }
    
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String service = Util.getParam(params, SERVICE_PARAM);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        Element response = new Element("response");
        

        
        String begin = SearchStatistics.getSingleDBValue(dbms,
                "SELECT min(requestdate) AS min FROM Requests", 
                "min");
        String end = SearchStatistics.getSingleDBValue(dbms, 
                "SELECT max(requestdate) AS max FROM Requests", 
                "max");
        
        DateTime beginDate = JODAISODate.parseBasicOrFullDateTime(begin);
        DateTime endDate = JODAISODate.parseBasicOrFullDateTime(end);
        int days = Days.daysBetween(beginDate, endDate).getDays();
        int months = Months.monthsBetween(beginDate, endDate).getMonths();
        
        response.addContent(new Element("activity_days").setText(days + ""));
        response.addContent(new Element("activity_months").setText(months + ""));
        
        // Total number of searches
        addSingleDBValueToElement(dbms, response, NUMBER_OF_SEARCH_QUERY, "total_searches", "total", service);
        
        // Average searches by day
        addSingleDBValueToElement(dbms, response, NUMBER_SEARCH_BY_X_QUERY, "avg_searches_by_day", "avg", 
                days == 0 ? "1" : String.valueOf(days), service);
        
        // Average searches by month
        addSingleDBValueToElement(dbms, response, NUMBER_SEARCH_BY_X_QUERY, "avg_searches_by_month", "avg", 
                months == 0 ? "1" : String.valueOf(months), service);

        // Average views by day
        addSingleDBValueToElement(dbms, response, NUMBER_VIEWS_BY_X_QUERY, "avg_views_by_day", "avg", 
                days == 0 ? "1" : String.valueOf(days));
        
        // Average views by month
        addSingleDBValueToElement(dbms, response, NUMBER_VIEWS_BY_X_QUERY, "avg_views_by_month", "avg", 
                months == 0 ? "1" : String.valueOf(months));
        
        // Number of search with no hits
        addSingleDBValueToElement(dbms, response, NUMBER_OF_SEARCHES_WITH_NO_HITS_QUERY, "total_searches_with_no_hits", "total", 
                service);
        
        return response;
    }
    
    /**
     * Get a single value returned by a query
     * 
     * @param dbms
     * @param query
     * @param elementName
     * @param queryFieldName
     * @param arg
     * @return
     * @throws SQLException
     */
    protected static String getSingleDBValue(Dbms dbms, String query, String queryFieldName, String... args) 
            throws SQLException {
        List<Element> results;
        if (args != null) {
            results = dbms.select(query, args).getChildren();
        } else {
            results = dbms.select(query).getChildren();
        }
        if (results.size() != 0) {
            Element record = (Element) results.get(0);
            return record.getChildText(queryFieldName);
        }
        return null;
    }
    
    /**
     * Add a new element with the value found.
     * 
     * @param dbms
     * @param response
     * @param query
     * @param elementName
     * @param queryFieldName
     * @param arg
     * @throws SQLException
     */
    protected static void addSingleDBValueToElement(Dbms dbms, Element response, String query, 
            String elementName, String queryFieldName, String... args) 
            throws SQLException {
        String value = getSingleDBValue(dbms, query, queryFieldName, args);
        if (value != null) {
            response.addContent(new Element(elementName).setText(value));
        }
    }
}