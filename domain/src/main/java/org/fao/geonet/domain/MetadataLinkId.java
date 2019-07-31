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

package org.fao.geonet.domain;

import java.io.Serializable;

/**
 * The id object of {@link MetadataLink}
 */

public class MetadataLinkId implements Serializable {
    private static final long serialVersionUID = -5759713154514715316L;

    private Link link;

    private Integer metadataId;

    public MetadataLinkId() {
    }

    public Link getLink() {
        return link;
    }

    public Integer getMetadataId() {
        return metadataId;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public void setMetadataId(Integer metadataId) {
        this.metadataId = metadataId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + link.getId();
        result = prime * result + metadataId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetadataLinkId other = (MetadataLinkId) obj;
        if (link.getId() != other.link.getId())
            return false;
        if (metadataId.intValue() != other.metadataId.intValue())
            return false;
        return true;
    }

}
