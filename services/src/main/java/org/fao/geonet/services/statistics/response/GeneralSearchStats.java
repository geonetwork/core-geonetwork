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

package org.fao.geonet.services.statistics.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates the response from the {@link org.fao.geonet.services.statistics.SearchStatistics#generalSearchStats(String)}
 * service.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
public class GeneralSearchStats {
    private int activityDays;
    private int activityMonths;
    private long totalSearches;
    private long avgSearchesPerDay;
    private long avgSearchersPerMonth;
    private int avgViewsPerDay;
    private int avgViewsPerMonth;
    private long searchesWithNoHits;

    @XmlElement(name = "activity_days")
    public int getActivityDays() {
        return activityDays;
    }

    public void setActivityDays(int activityDays) {
        this.activityDays = activityDays;
    }

    @XmlElement(name = "activity_months")
    public int getActivityMonths() {
        return activityMonths;
    }

    public void setActivityMonths(int activityMonths) {
        this.activityMonths = activityMonths;
    }

    @XmlElement(name = "total_searches")
    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    @XmlElement(name = "avg_searches_by_day")
    public long getAvgSearchesPerDay() {
        return avgSearchesPerDay;
    }

    public void setAvgSearchesPerDay(long avgSearchesPerDay) {
        this.avgSearchesPerDay = avgSearchesPerDay;
    }

    public void setAvgSearchersPerMonth(long avgSearchersPerMonth) {
        this.avgSearchersPerMonth = avgSearchersPerMonth;
    }

    @XmlElement(name = "avg_searches_by_month")
    public long getAvgSearchesPerMonth() {
        return avgSearchersPerMonth;
    }

    @XmlElement(name = "avg_views_by_day")
    public int getAvgViewsPerDay() {
        return avgViewsPerDay;
    }

    public void setAvgViewsPerDay(int avgViewsPerDay) {
        this.avgViewsPerDay = avgViewsPerDay;
    }

    @XmlElement(name = "avg_views_by_month")
    public int getAvgViewsPerMonth() {
        return avgViewsPerMonth;
    }

    public void setAvgViewsPerMonth(int avgViewsPerMonth) {
        this.avgViewsPerMonth = avgViewsPerMonth;
    }

    @XmlElement(name = "total_searches_with_no_hits")
    public long getSearchesWithNoHits() {
        return searchesWithNoHits;
    }

    public void setSearchesWithNoHits(long searchesWithNoHits) {
        this.searchesWithNoHits = searchesWithNoHits;
    }
}
