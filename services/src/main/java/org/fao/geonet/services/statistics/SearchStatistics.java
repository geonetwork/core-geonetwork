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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.fao.geonet.services.statistics.response.GeneralSearchStats;
import org.fao.geonet.services.statistics.response.IpStats;
import org.fao.geonet.services.statistics.response.SearchTypeStats;
import org.fao.geonet.services.statistics.response.TermFieldStats;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static java.lang.Math.max;
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
@Controller("search.statistics")
public class SearchStatistics {
    private static final String SERVICE_PARAM = "service";

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SearchRequestRepository requestRepository;
    @Autowired
    private MetadataRepository metadataRepository;

    @RequestMapping(value = {"/{lang}/statistics-search"}, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public GeneralSearchStats generalSearchStats(@RequestParam(SERVICE_PARAM) String service) throws Exception {
        final GeneralSearchStats stats = new GeneralSearchStats();

        ISODate begin = requestRepository.getOldestRequestDate();
        ISODate end = requestRepository.getMostRecentRequestDate();
        if (begin == null || end == null) {
            return stats;
        }
        DateTime beginDate = ISODate.parseBasicOrFullDateTime(begin.getDateAndTime());
        DateTime endDate = ISODate.parseBasicOrFullDateTime(end.getDateAndTime());
        int days = Days.daysBetween(beginDate, endDate).getDays();
        int nonZeroDays = max(1, days);
        int months = Months.monthsBetween(beginDate, endDate).getMonths();
        int nonZeroMonths = max(1, months);

        stats.setActivityDays(days);
        stats.setActivityMonths(months);

        // Total number of searches
        long total = requestRepository.count(hasService(service));
        stats.setTotalSearches(total);

        // Average searches by day
        long avgPerDay = total / nonZeroDays;
        stats.setAvgSearchesPerDay(avgPerDay);

        // Average searches by month
        long avgPerMonth = total / nonZeroMonths;
        stats.setAvgSearchersPerMonth(avgPerMonth);

        // Average views by day
        final int views = metadataRepository.getMetadataStatistics().getTotalStat(popularitySum(), Optional.<Specification<Metadata>>absent());
        int viewsByDay = views / nonZeroDays;
        stats.setAvgViewsPerDay(viewsByDay);

        // Average views by month
        int viewsByMonth = views / nonZeroMonths;
        stats.setAvgViewsPerMonth(viewsByMonth);

        // Number of search with no hits
        long noHits = requestRepository.count(where(hasService(service)).and(hasHits(0)));
        stats.setSearchesWithNoHits(noHits);

        return stats;
    }

    @RequestMapping(value = {"/{lang}/statistics-search-ip"}, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public List<IpStats> searchIpStats() {
        final String queryString =
                "select ipAddress, sum(hits) as sumhit from SearchRequest where autogenerated = FALSE group " +
                                   "by ipAddress order by sumhit desc";
        final Query query = entityManager.createQuery(queryString);
        @SuppressWarnings("unchecked")
        final List<Object[]> resultList = query.getResultList();
        return Lists.transform(resultList, new Function<Object[], IpStats>() {
            @Nonnull
            @Override
            public IpStats apply(@Nonnull Object[] input) {
                return new IpStats((String)input[0], (Long)input[1]);
            }
        });
    }

    @RequestMapping(value = {"/{lang}/statistics-search-by-service-type"}, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public List<SearchTypeStats> searchServiceTypeStats() {
        final String queryString =
                "select service, count(id) as nbsearch from SearchRequest group by service order by nbsearch desc";
        final Query query = entityManager.createQuery(queryString);
        @SuppressWarnings("unchecked")
        final List<Object[]> resultList = query.getResultList();
        return Lists.transform(resultList, new Function<Object[], SearchTypeStats>() {
            @Nonnull
            @Override
            public SearchTypeStats apply(@Nonnull Object[] input) {
                return new SearchTypeStats((String) input[0], (Long) input[1]);
            }
        });
    }

    @RequestMapping(value = {"/{lang}/statistics-search-fields"}, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public List<TermFieldStats> searchFieldsStats() {
        final String queryString = "SELECT COUNT(r.id) AS total, p.termField, r.service "
                          + "                    FROM SearchRequest r JOIN r.params p "
                          + "                    group by r.service, p.termField order by r.service, total desc";
        final Query query = entityManager.createQuery(queryString);
        @SuppressWarnings("unchecked")
        final List<Object[]> resultList = query.getResultList();
        return Lists.transform(resultList, new Function<Object[], TermFieldStats>() {
            @Nonnull
            @Override
            public TermFieldStats apply(@Nonnull Object[] input) {
                return new TermFieldStats((Long) input[0], (String) input[1], (String) input[2]);
            }
        });
    }
}