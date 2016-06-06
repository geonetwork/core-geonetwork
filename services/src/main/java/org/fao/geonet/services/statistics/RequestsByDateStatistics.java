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

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.fao.geonet.repository.specification.SearchRequestSpecs;
import org.fao.geonet.repository.statistic.DateInterval;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;

/**
 * Service to get the db-stored requests information group by a date (year, month, day)
 *
 * @author nicolas Ribot
 */
public class RequestsByDateStatistics extends NotInReadOnlyModeService {

    static final String BY_YEAR = "YEAR";
    static final String BY_MONTH = "MONTH";
    static final String BY_DAY = "DAY";
    static final String BY_HOUR = "HOUR";
    /**
     * the custom part of the date query; according to user choice for graphic
     */
    public Hashtable<String, DateInterval> queryFragments;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);

        queryFragments = new Hashtable<>(4);

        queryFragments.put(BY_HOUR, new DateInterval.Hour());
        queryFragments.put(BY_DAY, new DateInterval.Day());
        queryFragments.put(BY_MONTH, new DateInterval.Month());
        queryFragments.put(BY_YEAR, new DateInterval.Year());
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String dateFromParam = Util.getParam(params, "dateFrom");
        String dateToParam = Util.getParam(params, "dateTo");
        String graphicType = Util.getParam(params, "graphicType");
        boolean byType = Util.getParam(params, "byType", false);

        ISODate dateFrom = null;
        ISODate dateTo = null;

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        final SearchRequestRepository requestRepository = context.getBean(SearchRequestRepository.class);

        try {
            dateFrom = new ISODate(dateFromParam);
            dateTo = new ISODate(dateToParam);

            // TODO : if ByServiceType
            if (byType) {
                final List<String> serviceTypes = requestRepository.selectAllDistinctAttributes(SearchRequest_.service);
                for (String serviceType : serviceTypes) {
                    Element results = buildQuery(requestRepository, serviceType, dateFrom, dateTo, graphicType);

                    results.setAttribute("service", serviceType);
                    elResp.addContent(results);
                }
            } else {
                Element results = buildQuery(requestRepository, null, dateFrom, dateTo, graphicType);
                elResp.addContent(results);
            }

            elResp.addContent(new Element("dateFrom").setText(dateFrom.getDateAndTime()));
            elResp.addContent(new Element("dateTo").setText(dateTo.getDateAndTime()));
        } catch (Exception e) {
            elResp.setAttribute("error", e.getMessage());
        }

        final ISODate oldestRequestDate = requestRepository.getOldestRequestDate();
        elResp.addContent(new Element("dateMin").setText(oldestRequestDate.getDateAndTime()));
        final ISODate mostRecentRequestDate = requestRepository.getMostRecentRequestDate();
        elResp.addContent(new Element("dateMax").setText(mostRecentRequestDate.getDateAndTime()));

        return elResp;
    }

    public Element buildQuery(SearchRequestRepository requestRepository, String service, ISODate dateFrom,
                              ISODate dateTo, String graphicType) {

        DateInterval dateInterval = this.queryFragments.get(graphicType);
        final List<Pair<DateInterval, Integer>> requestDateToRequestCountBetween = requestRepository
            .getRequestDateToRequestCountBetween(dateInterval, dateFrom, dateTo, SearchRequestSpecs.hasService(service));

        Element results = new Element("requests");
        for (Pair<DateInterval, Integer> entry : requestDateToRequestCountBetween) {
            results.addContent(new Element("record")
                .addContent(new Element("number").setText("" + entry.two()))
                .addContent(new Element("reqdate").setText(entry.one().getDateString()))
            );
        }

        return results;
    }
}
