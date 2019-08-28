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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fao.geonet.entitylistener.MetadataLinkEntityListenerManager;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * An entity that represents the relationship between a metadata and a metadata link.
 */
@Entity
@Table(name = MetadataLink.TABLE_NAME,
    indexes = {
        @Index(name = "idx_metadatalink_metadataid", columnList = "metadataid"),
        @Index(name = "idx_metadatalink_linkid", columnList = "linkid")})
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataLinkEntityListenerManager.class)
@IdClass(MetadataLinkId.class)
public class MetadataLink extends GeonetEntity {
    public static final String TABLE_NAME = "MetadataLink";

    private Link link;

    private Integer metadataId;

    private String metadataUuid;

    public MetadataLink() {
    }

    @Id
    public Integer getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(Integer metadataId) {
        this.metadataId = metadataId;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linkId", referencedColumnName = "id")
    @Id
    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getMetadataUuid() {
        return metadataUuid;
    }

    public void setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
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
        MetadataLink other = (MetadataLink) obj;
        if (link.getId() != other.link.getId())
            return false;
        if (metadataId.intValue() != metadataId.intValue())
            return false;
        return true;
    }
}
