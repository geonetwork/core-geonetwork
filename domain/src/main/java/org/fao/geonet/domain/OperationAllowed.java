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

import org.fao.geonet.entitylistener.OperationAllowedEntityListenerManager;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * An entity that represents the relationship between a metadata, group and the operations that
 * group is allowed to perform on the metadata.
 *
 * @author Jesse
 */
@Entity
@Table(name = OperationAllowed.TABLE_NAME,
    indexes = { @Index(name = "idx_operationallowed_metadataid", columnList = "metadataid") })
@Access(AccessType.PROPERTY)
@EntityListeners(OperationAllowedEntityListenerManager.class)
public class OperationAllowed extends GeonetEntity {
    /**
     * Name of the operationallowed table.
     */
    public static final String TABLE_NAME = "OperationAllowed";

    private OperationAllowedId _id = new OperationAllowedId();

    /**
     * Constructor for use by JPA.
     */
    public OperationAllowed() {
    }

    /**
     * Constructor for use by developers to easily create an instance.
     */
    public OperationAllowed(@Nonnull OperationAllowedId id) {
        this._id = id;
    }

    /**
     * Return the Id object.
     */
    @EmbeddedId
    public OperationAllowedId getId() {
        return _id;
    }

    /**
     * Set the Id object.
     *
     * @param id new id
     */
    public void setId(OperationAllowedId id) {
        this._id = id;
    }

    @Override
    public String toString() {
        return "OperationId: [" + _id.toString() + "]";
    }

    public OperationAllowed setId(Metadata metadata, Group group, Operation operation) {
        setId(new OperationAllowedId(metadata.getId(), group.getId(), operation.getId()));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationAllowed that = (OperationAllowed) o;

        if (_id != null ? !_id.equals(that._id) : that._id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _id != null ? _id.hashCode() : 0;
    }
}
