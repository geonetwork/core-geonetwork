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
 * IPStats response object.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class IpStats {
    private final String ip;
    private final long sumhit;

    public IpStats(String ip, long sumhit) {
        this.ip = ip;
        this.sumhit = sumhit;
    }

    /**
     * Get the ip address of the user performing the searches
     */
    public String getIp() {
        return ip;
    }

    /**
     * Get the total number of hits (the sum of all found records by this ip address).
     */
    public long getSumhit() {
        return sumhit;
    }

    @Override
    public String toString() {
        return "IpStats{" +
            "ip='" + ip + '\'' +
            ", sumhit=" + sumhit +
            '}';
    }
}
