package org.fao.geonet.services.statistics.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Stats response object for finding the number of searches using a particular search term.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class TermFieldStats {
    private final String service;
    private final String termfield;
    private final long total;

    public TermFieldStats(long total, String termfield, String service) {
        this.total = total;
        this.termfield = termfield;
        this.service = service;
    }

    /**
     * The service used to perform searches.
     */
    public String getService() {
        return service;
    }

    /**
     * The number of searches made using this service and search term.
     */
    public long getTotal() {
        return total;
    }

    /**
     * Get the search term used.
     */
    public String getTermfield() {
        return termfield;
    }

    @Override
    public String toString() {
        return "TermFieldStats{" +
               "service='" + service + '\'' +
               ", termfield='" + termfield + '\'' +
               ", total=" + total +
               '}';
    }
}
