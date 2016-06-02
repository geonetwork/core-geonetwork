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
 * Encapsulates the data calculated by the {@link org.fao.geonet.services.statistics.ContentStatistics#exec()}
 * service.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
public class ContentStats {
    private long totalNonTemplateMetadata;
    private long totalHarvestedNonTemplateMetadata;
    private long totalTemplateMetadata;
    private long totalSubTemplateMetadata;
    private long totalPublicMetadata;

    @XmlElement(name = "nb_metadata")
    public long getTotalNonTemplateMetadata() {
        return totalNonTemplateMetadata;
    }

    public void setTotalNonTemplateMetadata(long totalNonTemplateMetadata) {
        this.totalNonTemplateMetadata = totalNonTemplateMetadata;
    }

    @XmlElement(name = "nb_harvested")
    public long getTotalHarvestedNonTemplateMetadata() {
        return totalHarvestedNonTemplateMetadata;
    }

    public void setTotalHarvestedNonTemplateMetadata(long totalHarvestedNonTemplateMetadata) {
        this.totalHarvestedNonTemplateMetadata = totalHarvestedNonTemplateMetadata;
    }

    @XmlElement(name = "nb_template")
    public long getTotalTemplateMetadata() {
        return totalTemplateMetadata;
    }

    public void setTotalTemplateMetadata(long totalTemplateMetadata) {
        this.totalTemplateMetadata = totalTemplateMetadata;
    }

    @XmlElement(name = "nb_subtemplate")
    public long getTotalSubTemplateMetadata() {
        return totalSubTemplateMetadata;
    }

    public void setTotalSubTemplateMetadata(long totalSubTemplateMetadata) {
        this.totalSubTemplateMetadata = totalSubTemplateMetadata;
    }

    @XmlElement(name = "nb_metadata_public")
    public long getTotalPublicMetadata() {
        return totalPublicMetadata;
    }

    public void setTotalPublicMetadata(long totalPublicMetadata) {
        this.totalPublicMetadata = totalPublicMetadata;
    }
}
