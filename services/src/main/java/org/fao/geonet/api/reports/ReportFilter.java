package org.fao.geonet.api.reports;

import com.google.common.collect.ImmutableSet;
import org.fao.geonet.domain.ISODate;

import java.util.Set;

/**
 * Class to represent report filters.
 *
 * @author Jose García
 */
public class ReportFilter {
    /**
     * Report begin date filter.
     */
    private ISODate beginDate;

    /**
     * Report end date filter.
     */
    private ISODate endDate;

    /**
     * Report groups filter.
     */
    private Set<Integer> groups;

    /**
     * Retrieves the begin date filter.
     * @return begin date filter.
     */
    public ISODate getBeginDate() {
        return beginDate;
    }

    /**
     * Retrieves the end date filter.
     * @return begin end filter.
     */
    public ISODate getEndDate() {
        return endDate;
    }

    /**
     * Retrieves the groups filter.
     * @return groups filter.
     */
    public Set<Integer> getGroups() {
        return groups;
    }


    /**
     * Creates a ReportFilter instance.
     *
     * @param beginDateFilter Report begin date filter.
     * @param endDateFilter Report end date filter.
     * @param groupsFilter Report groups to filter.
     */
    public ReportFilter(final String beginDateFilter,
                        final String endDateFilter,
                        final Set<Integer> groupsFilter) {
        String dateFrom = beginDateFilter + "T00:00:00";
        String dateTo = endDateFilter + "T23:59:59";

        this.beginDate = new ISODate(dateFrom);
        this.endDate = new ISODate(dateTo);
        this.groups = ImmutableSet.copyOf(groupsFilter);
    }
}
