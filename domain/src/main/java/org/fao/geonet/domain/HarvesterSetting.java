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

import com.google.common.collect.Sets;

import org.fao.geonet.entitylistener.HarvesterSettingEntityListenerManager;
import org.hibernate.annotations.Type;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.fao.geonet.entitylistener.HarvesterSettingEntityListenerManager;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.engine.internal.Cascade;

import com.google.common.collect.Sets;

/**
 * An entity representing a harvester configuration setting.
 * <p/>
 * Harvester settings are represented by a tree. One should use the {@link
 * org.fao.geonet.repository.HarvesterSettingRepository} to traverse the hierarchy.
 *
 * @author Jesse
 */
@Entity
@Table(name = "HarvesterSettings")
@Access(AccessType.PROPERTY)
@EntityListeners(HarvesterSettingEntityListenerManager.class)
@SequenceGenerator(name = HarvesterSetting.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class HarvesterSetting extends GeonetEntity {
    static final String ID_SEQ_NAME = "harvester_setting_id_seq";
    private static final HashSet<String> EXCLUDE_FROM_XML = Sets.newHashSet("valueAsBool", "valueAsInt");

    private int _id;
    private HarvesterSetting _parent;
    private String _name;
    private String _value;

    /**
     * Get the setting id. This is a generated value and as such new instances should not have this
     * set as it will simply be ignored and could result in reduced performance.
     *
     * @return the setting id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = "id", nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the setting id. This is a generated value and as such new instances should not have this
     * set as it will simply be ignored and could result in reduced performance.
     *
     * @param id the setting id
     * @return this setting object
     */
    public HarvesterSetting setId(int id) {
        this._id = id;
        return this;
    }

    /**
     * Get the parent setting object. This is a nullable property.
     */
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "parentid")
    @OnDelete(action=OnDeleteAction.CASCADE)
    public
    @Nullable
    HarvesterSetting getParent() {
        return _parent;
    }                         

    /**
     * Set the parent setting object for this setting. The may be null.
     *
     * @param parent the parent setting object
     * @return this setting object
     */
    public
    @Nonnull
    HarvesterSetting setParent(@Nullable HarvesterSetting parent) {
        this._parent = parent;
        return this;
    }

    /**
     * Get the setting name. This is a required property.
     *
     * @return the setting name.
     */
    @Column(name = "name", nullable = false)
    public
    @Nonnull
    String getName() {
        return _name;
    }

    /**
     * Set the setting name. This is a required property.
     *
     * @param name the setting name. This is a required property.
     * @return this setting object
     */
    public
    @Nonnull
    HarvesterSetting setName(@Nonnull String name) {
        this._name = name;
        return this;
    }

    /**
     * Get the setting value. This is a nullable property.
     */
    @Lob
    @Column(name = "value", nullable = true)
    @Type(type = "org.hibernate.type.StringClobType")
    // this is a work around for postgres so postgres can correctly load clobs
    public
    @Nullable
    String getValue() {
        return _value;
    }

    /**
     * Set the value of setting with a boolean.
     *
     * @param value the new value
     * @return this setting object
     */
    public HarvesterSetting setValue(boolean value) {
        return setValue(String.valueOf(value));
    }

    public HarvesterSetting setValue(@Nullable String value) {
        this._value = value;
        return this;
    }

    /**
     * Get the value as an integer. This may throw {@link NullPointerException} if the value is null
     * or {@link NumberFormatException} if the value is not a valid number.
     *
     * @return the value as an integer
     */
    @Transient
    public int getValueAsInt() throws NullPointerException, NumberFormatException {
        if (getValue() == null) {
            throw new NullPointerException("Setting value of " + getName() + " is null");
        }
        return Integer.parseInt(getValue());
    }

    /**
     * Set the value of setting with an integer.
     *
     * @param value the new value
     * @return this setting object
     */
    public HarvesterSetting setValue(int value) {
        return setValue(String.valueOf(value));
    }

    /**
     * Get the values as a boolean. Returns false if the values is not a boolean.
     *
     * @return the values as a boolean
     * @throws NullPointerException if the value is null.
     */
    @Transient
    public boolean getValueAsBool() throws NullPointerException {
        if (getValue() == null) {
            throw new NullPointerException("Setting value of " + getName() + " is null");
        }
        return Boolean.parseBoolean(_value);
    }

    @Override
    protected Set<String> propertiesToExcludeFromXml() {
        return EXCLUDE_FROM_XML;
    }

    @Override
    public String toString() {
        return "Setting [id=" + _id + ", name=" + _name + ", value=" + _value + "]";
    }
}
