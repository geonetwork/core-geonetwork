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

import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.entitylistener.MetadataEntityListenerManager;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.Set;


/**
 * @See {@link AbstractMetadata}
 * @author Jesse
 */
@Entity
@Table(name = Metadata.TABLENAME)
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataEntityListenerManager.class)
public class Metadata extends AbstractMetadata implements Serializable {

    private static final long serialVersionUID = -5557599895424227101L;
    public static final String TABLENAME = "Metadata";
    public static final String METADATA_CATEG_JOIN_TABLE_NAME = "MetadataCateg";
    private List<UserFeedback> userFeedbacks;

    public Metadata() {
    }

    /**
     * Get the set of metadata categories this metadata is part of. This is lazily
     * loaded and all operations are cascaded
     *
     * @return the metadata categories
     */
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.REFRESH},
            fetch = FetchType.EAGER)
    @JoinTable(name = METADATA_CATEG_JOIN_TABLE_NAME,
            joinColumns = @JoinColumn(name = "metadataId"),
            inverseJoinColumns = @JoinColumn(name =
            METADATA_CATEG_JOIN_TABLE_CATEGORY_ID))
    @Nonnull
    public Set<MetadataCategory> getMetadataCategories() {
        return metadataCategories;
    }

    /**
     * Set the metadata category
     *
     * @param categories
     */
    protected void setMetadataCategories(@Nonnull Set<MetadataCategory> categories) {
        this.metadataCategories = categories;
    }

    @OneToMany(mappedBy = "metadata", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<UserFeedback> getUserFeedbacks() {
        return userFeedbacks;
    }

    public void setUserFeedbacks(@Nonnull List<UserFeedback> userFeedbacks) {
        this.userFeedbacks = userFeedbacks;
    }
}
