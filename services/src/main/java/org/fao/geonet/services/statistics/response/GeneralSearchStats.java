package org.fao.geonet.services.statistics.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates the response from the {@link org.fao.geonet.services.statistics.SearchStatistics#generalSearchStats(String)} service.
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

    public void setActivityDays(int activityDays) {
        this.activityDays = activityDays;
    }

    @XmlElement(name = "activity_days")
    public int getActivityDays() {
        return activityDays;
    }

    public void setActivityMonths(int activityMonths) {
        this.activityMonths = activityMonths;
    }

    @XmlElement(name = "activity_months")
    public int getActivityMonths() {
        return activityMonths;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    @XmlElement(name = "total_searches")
    public long getTotalSearches() {
        return totalSearches;
    }

    public void setAvgSearchesPerDay(long avgSearchesPerDay) {
        this.avgSearchesPerDay = avgSearchesPerDay;
    }

    @XmlElement(name = "avg_searches_by_day")
    public long getAvgSearchesPerDay() {
        return avgSearchesPerDay;
    }

    public void setAvgSearchersPerMonth(long avgSearchersPerMonth) {
        this.avgSearchersPerMonth = avgSearchersPerMonth;
    }

    @XmlElement(name = "avg_searches_by_month")
    public long getAvgSearchesPerMonth() {
        return avgSearchersPerMonth;
    }

    public void setAvgViewsPerDay(int avgViewsPerDay) {
        this.avgViewsPerDay = avgViewsPerDay;
    }

    @XmlElement(name = "avg_views_by_day")
    public int getAvgViewsPerDay() {
        return avgViewsPerDay;
    }

    public void setAvgViewsPerMonth(int avgViewsPerMonth) {
        this.avgViewsPerMonth = avgViewsPerMonth;
    }

    @XmlElement(name = "avg_views_by_month")
    public int getAvgViewsPerMonth() {
        return avgViewsPerMonth;
    }

    public void setSearchesWithNoHits(long searchesWithNoHits) {
        this.searchesWithNoHits = searchesWithNoHits;
    }

    @XmlElement(name = "total_searches_with_no_hits")
    public long getSearchesWithNoHits() {
        return searchesWithNoHits;
    }
}
