package org.fao.geonet.services.statistics.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Stats response object for finding the number of searches done with a particular search service.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SearchTypeStats {
    private final String service;
    private final long nbsearch;

    public SearchTypeStats(String service, long nbsearch) {
        this.service = service;
        this.nbsearch = nbsearch;
    }

    /**
     * The service used to perform searches.
     */
    public String getService() {
        return service;
    }

    /**
     * The number of searches made using this service.
     */
    public long getNbsearch() {
        return nbsearch;
    }

    @Override
    public String toString() {
        return "SearchTypeStats{" +
               "service='" + service + '\'' +
               ", nbsearch=" + nbsearch +
               '}';
    }
}
