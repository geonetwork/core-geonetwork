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

import com.google.common.base.Optional;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.springframework.data.jpa.domain.Specification;

import java.sql.SQLException;

import static org.fao.geonet.repository.specification.SearchRequestSpecs.hasHits;
import static org.fao.geonet.repository.specification.SearchRequestSpecs.hasService;
import static org.fao.geonet.repository.statistic.MetadataStatisticSpec.StandardSpecs.popularitySum;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Service to get core metrics for a service:
 * <ul>
 * <li>total number of searches</li>
 * <li>Average searches by day</li>
 * <li>Average searches by month</li>
 * <li>Number of record views</li>
 * </ul>
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
        Element response = new Element("response");

        final SearchRequestRepository requestRepository = context.getBean(SearchRequestRepository.class);

        ISODate begin = requestRepository.getOldestRequestDate();
        ISODate end = requestRepository.getMostRecentRequestDate();
        if (begin == null || end == null) {
            return response;    // No stats available.
        }
        DateTime beginDate = ISODate.parseBasicOrFullDateTime(begin.getDateAndTime());
        DateTime endDate = ISODate.parseBasicOrFullDateTime(end.getDateAndTime());
        int days = Days.daysBetween(beginDate, endDate).getDays();
        int nonZeroDays = days == 0 ? 1 : days;
        int months = Months.monthsBetween(beginDate, endDate).getMonths();
        int nonZeroMonths = months == 0 ? 1 : months;
        response.addContent(new Element("activity_days").setText(days + ""));
        response.addContent(new Element("activity_months").setText(months + ""));


        // Total number of searches
        long total = requestRepository.count(hasService(service));
        addSingleDBValueToElement(response, total, "total_searches", "total");

        // Average searches by day
        long avgPerDay = total / nonZeroDays;
        addSingleDBValueToElement(response, avgPerDay, "avg_searches_by_day", "avg");

        // Average searches by month
        long avgPerMonth = total / nonZeroMonths;
        addSingleDBValueToElement(response, avgPerMonth, "avg_searches_by_month", "avg");

        // Average views by day
        final int views = context.getBean(MetadataRepository.class).getMetadataStatistics().getTotalStat(popularitySum(),
                Optional.<Specification<Metadata>>absent());
        int viewsByDay = views / nonZeroDays;
        addSingleDBValueToElement(response, viewsByDay, "avg_views_by_day", "avg");

        // Average views by month
        int viewsByMonth = views / nonZeroMonths;
        addSingleDBValueToElement(response, viewsByMonth, "avg_views_by_month", "avg");

        // Number of search with no hits
        long noHits = requestRepository.count(where(hasService(service)).and(hasHits(0)));
        addSingleDBValueToElement(response, noHits, "total_searches_with_no_hits", "total");

        return response;
    }

    /**
     * Add a new element with the value found.
     *
     * @param response
     * @param elementName
     * @param queryFieldName
     * @throws SQLException
     */
    protected static void addSingleDBValueToElement(Element response, Object value,
                                                    String elementName, String queryFieldName)
            throws SQLException {
        response.addContent(new Element(elementName).setText("" + value));
    }
}