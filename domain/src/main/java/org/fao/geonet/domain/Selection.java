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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.fao.geonet.entitylistener.SelectionEntityListenerManager;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.Map;

/**
 * A selection define a set of object to select and
 * saved in database.
 */
@Entity
@Access(AccessType.PROPERTY)
@Cacheable
@Table(name = "Selections")
@EntityListeners(SelectionEntityListenerManager.class)
@SequenceGenerator(name = Selection.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Selection extends Localized implements Serializable {
    static final String ID_SEQ_NAME = "selection_id_seq";
    private int _id;
    private String _name;
    private char watchable = Constants.YN_FALSE;

    /**
     * The id of the category. This is a generated value and not controlled by the developer.
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the category. This is typically set by the JPA entity manager and should only
     * be set by the developer when they want to merge new data with an existing entity or want to
     * perform query by example. But even then it is not generally recommended.
     *
     * @param id the id.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * The name of the selection. This is a required property.
     *
     * @return the name.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the selection. This is a required non-null property.
     *
     * @param name the new name.
     */
    public void setName(String name) {
        this._name = name;
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "SelectionsDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    @JsonProperty(value = "label")
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    /**
     * For backwards compatibility we need the isharvested column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @JsonIgnore
    @Column(name = "isWatchable", length = 1, nullable = false)
    protected char getWatchable_JPAWorkaround() {
        return watchable;
    }

    /**
     * Set the code for the watchable column.
     *
     * @param watchable Constants.YN_ENABLED or Constants.YN_DISABLED
     */
    protected void setWatchable_JPAWorkaround(char watchable) {
        this.watchable = watchable;
    }

    /**
     * Return true if the metadata was watchable.
     *
     * @return true if the metadata was watchable.
     */
    @Transient
    @JsonProperty(value = "watchable")
    public boolean isWatchable() {
        return Constants.toBoolean_fromYNChar(getWatchable_JPAWorkaround());
    }

    /**
     * true if the metadata was watchable, false otherwise.
     *
     * @param watchable true if the metadata was watchable.
     * @return this data info object
     */
    public Selection setWatchable(boolean watchable) {
        setWatchable_JPAWorkaround(Constants.toYN_EnabledChar(watchable));
        return this;
    }

    // CHECKSTYLE:OFF
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Selection category = (Selection) o;

        if (_id != category._id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _id;
    }
}
