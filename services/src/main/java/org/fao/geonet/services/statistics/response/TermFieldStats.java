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
