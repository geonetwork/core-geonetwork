/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

import org.fao.geonet.entitylistener.FavouriteMetadataListItemListenerManager;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Access(AccessType.PROPERTY)
@Table(
    name = "FavouriteMetadataListItem"
    // ,indexes = {@Index(name = "idx_usersavedselections_metadatauuid", columnList = "metadataUuid")}
)
@SequenceGenerator(name = FavouriteMetadataListItem.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
@EntityListeners(FavouriteMetadataListItemListenerManager.class)
public class FavouriteMetadataListItem extends GeonetEntity implements Serializable {
    static final String ID_SEQ_NAME = "fav_md_item_id_seq";

    /**
     * id - managed by hibernate/JPA
     */
    private int id;
    /**
     * UUID ("id") of a metadata document.  cf. `metadata` table in the DB.
     */
    private String metadataUuid;

    /**
     *  do not modify.  This is a back link (via id) to the FavouriteMetadataList this belongs to.
     *  This is managed by hibernate/JPA by the one-to-many relationship.  Its exposed because it's need
     *  by the repository.
     */
    private int selection_id;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    public FavouriteMetadataListItem setId(int id) {
        this.id = id;
        return this;
    }


    public String getMetadataUuid() {
        return metadataUuid;
    }

    public void setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
    }

    @Column(name = "selection_id", insertable = false, updatable = false)
    public int getSelection_id() {
        return selection_id;
    }

    public void setSelection_id(int selection_id) {
        this.selection_id = selection_id;
    }
}