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

import org.fao.geonet.entitylistener.MetadataRelationEntityListenerManager;

import javax.persistence.*;

/**
 * Tables that links related metadata. <p> Object is its own entity so that it is easier to add
 * relations without having to load the related metadata. </p> <p> Note: It is important that both
 * Metadata are managed (have been saved or loaded from the MetadataRepository.) For example:
 * <pre><code>
 *      Metadata metadata1 = _metadataRepo.findOne(id);
 *      Metadata metadata2 = _metadataRepo.findOne(id2);
 *      new MetadataRelation(metadata1, metadata2);
 *     </code></pre>
 * </p>
 *
 * @author Jesse
 */
@Entity
@Table(name = "Relations")
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataRelationEntityListenerManager.class)
public class MetadataRelation extends GeonetEntity {
    private MetadataRelationId _id = new MetadataRelationId();

    /**
     * Default constructor, required by JPA.
     */
    public MetadataRelation() {

    }

    /**
     * Get the metadata relation id object.
     *
     * @return the metadata relation id object.
     */
    @EmbeddedId
    public MetadataRelationId getId() {
        return _id;
    }

    /**
     * Set the metadata relation id object.
     *
     * @param id the metadata relation id object.
     */
    public MetadataRelation setId(final MetadataRelationId id) {
        this._id = id;
        return this;
    }
}
