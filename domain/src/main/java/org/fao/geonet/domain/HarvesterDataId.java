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

import com.vividsolutions.jts.util.Assert;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.io.Serializable;

/**
 * An id for {@link org.fao.geonet.domain.HarvesterData}.
 *
 * Created by Jesse on 1/23/14.
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class HarvesterDataId implements Serializable {
    private String harvesterUuid;
    private String key;

    public HarvesterDataId() {
        // needed for JPA
    }

    public HarvesterDataId(String harvesterUuid, String key) {
        setHarvesterUuid(harvesterUuid);
        setKey(key);
    }

    /**
     * The uuid of the harvester that the {@link org.fao.geonet.domain.HarvesterData} belongs to.
     *
     * @return uuid of the harvester that the {@link org.fao.geonet.domain.HarvesterData} belongs
     * to.
     */
    public String getHarvesterUuid() {
        return harvesterUuid;
    }

    /**
     * Set uuid of the harvester that the {@link org.fao.geonet.domain.HarvesterData} belongs to.
     *
     * @param harvesterUuid uuid of the harvester that the {@link org.fao.geonet.domain.HarvesterData}
     *                      belongs to.
     */
    public void setHarvesterUuid(String harvesterUuid) {
        this.harvesterUuid = harvesterUuid;
    }

    /**
     * Get the key identifying the data entity (within the scope of the harvester).
     *
     * @return the key.
     */
    @Column(name = "keyvalue", nullable = false, length = 255)
    public String getKey() {
        return key;
    }

    /**
     * Set the key identifying the data entity (within the scope of the harvester).  Max length is
     * 255
     *
     * @param key identifying the data entity (within the scope of the harvester).
     */
    public void setKey(String key) {
        Assert.isTrue(key.length() <= 255);
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HarvesterDataId that = (HarvesterDataId) o;

        if (!harvesterUuid.equals(that.harvesterUuid)) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = harvesterUuid.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ID {" +
            "harvesterUuid='" + harvesterUuid + '\'' +
            ", key='" + key + '\'' +
            '}';
    }
}
