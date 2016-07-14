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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.fao.geonet.entitylistener.SettingEntityListenerManager;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * An entity representing a system configuration setting.
 * <p/>
 * Settings are represented by a tree. One should use the {@link org.fao.geonet.repository.HarvesterSettingRepository}
 * to traverse the hierarchy.
 *
 * @author Jesse
 */
@Entity
@Table(name = "Settings")
@Cacheable
@Access(AccessType.PROPERTY)
@EntityListeners(SettingEntityListenerManager.class)
@JsonSerialize(using = SettingToObjectSerializer.class)
public class Setting extends GeonetEntity {
    private String name;
    private String value;
    private SettingDataType dataType = SettingDataType.STRING;
    private int position = 0;
    private char internal = Constants.YN_TRUE;

    @Id
    @Column(name = "name", nullable = false, length = 255/* mysql cannot accept it any bigger if it is to be the id */)
    public String getName() {
        return name;
    }

    public Setting setName(String name) {
        this.name = name;
        return this;
    }

    @Lob
    @Column(name = "value", nullable = true)
    @Type(type = "org.hibernate.type.StringClobType")
    // this is a work around for postgres so postgres can correctly load clobs
    public String getValue() {
        return value;
    }

    public Setting setValue(String value) {
        this.value = value;
        return this;
    }

    @Column(name = "datatype")
    public SettingDataType getDataType() {
        return dataType;
    }

    public Setting setDataType(SettingDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    @Column(name = "position", nullable = false, columnDefinition = "int default 0")
    public int getPosition() {
        return position;
    }

    public Setting setPosition(int position) {
        this.position = position;
        return this;
    }

    /**
     * For backwards compatibility we need the activated column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "internal", nullable = false, length = 1, columnDefinition = "char default 'y'")
    protected char getInternal_JpaWorkaround() {
        return internal;
    }

    /**
     * Set the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     *
     * @param internalValue the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED
     *                      for false.
     */
    protected void setInternal_JpaWorkaround(char internalValue) {
        internal = internalValue;
    }

    /**
     * Return true if the setting is public.
     *
     * @return true if the setting is public.
     */
    @Transient
    public boolean isInternal() {
        return Constants.toBoolean_fromYNChar(getInternal_JpaWorkaround());
    }

    /**
     * Set true if the setting is private.
     *
     * @param internal true if the setting is private.
     */
    public Setting setInternal(boolean internal) {
        setInternal_JpaWorkaround(Constants.toYN_EnabledChar(internal));
        return this;
    }

    @Override
    public String toString() {
        return "Setting{'" + name + "' = '" + value + "'}";
    }

}
