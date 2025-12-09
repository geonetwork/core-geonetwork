/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    private int id;
    private HarvesterSetting parent;
    private String name;
    /**
     * If the setting is not encrypted: value = storedValue, otherwise value contains the unencrypted value.
     *
     * Should be used the methods for value property. storedValue is managed in
     * {@link org.fao.geonet.entitylistener.HarvesterSettingValueSetter}.
     */
    private String storedValue;
    private String value;
    private char encrypted = Constants.YN_FALSE;

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
        return id;
    }

    /**
     * Set the setting id. This is a generated value and as such new instances should not have this
     * set as it will simply be ignored and could result in reduced performance.
     *
     * @param id the setting id
     * @return this setting object
     */
    public HarvesterSetting setId(int id) {
        this.id = id;
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
        return parent;
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
        this.parent = parent;
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
        return name;
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
        this.name = name;
        return this;
    }

    /**
     * Get the setting value. This is a nullable property.
     */
    @Lob
    @Column(name = "value", nullable = true)
    @Type(type = "org.hibernate.type.TextType")
    // this is a work around for postgres so postgres can correctly load clobs
    public
    @Nullable
    String getStoredValue() {
        return storedValue;
    }

    /**
     * Set the value of setting.
     *
     * @param value the new value
     * @return this setting object
     */
    public HarvesterSetting setStoredValue(@Nullable String value) {
        this.storedValue = value;
        return this;
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
        return Boolean.parseBoolean(value);
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
     * For backwards compatibility we need the activated column to be either 'n' or 'y'.
     * This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "encrypted", nullable = false, length = 1, columnDefinition="char default 'n'")
    protected char getEncrypted_JpaWorkaround() {
        return encrypted;
    }

    /**
     * Set the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     *
     * @param encryptedValue the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     * @return
     */
    protected void setEncrypted_JpaWorkaround(char encryptedValue) {
        encrypted = encryptedValue;
    }

    /**
     * Return true if the setting is public.
     *
     * @return true if the setting is public.
     */
    @Transient
    public boolean isEncrypted() {
        return Constants.toBoolean_fromYNChar(getEncrypted_JpaWorkaround());
    }

    /**
     * Set true if the setting is private.
     *
     * @param encrypted true if the setting is private.
     */
    public HarvesterSetting setEncrypted(boolean encrypted) {
        setEncrypted_JpaWorkaround(Constants.toYN_EnabledChar(encrypted));
        return this;
    }

    @Transient
    public String getValue() {
        return value;
    }

    public HarvesterSetting setValue(@Nullable String value) {
        this.value = value;
        // Required to trigger PreUpdate event in  {@link org.fao.geonet.entitylistener.SettingEntityListenerManager},
        // otherwise doesn't work with transient properties
        this.setStoredValue(value);
        return this;
    }

    @Override
    protected Set<String> propertiesToExcludeFromXml() {
        return EXCLUDE_FROM_XML;
    }

    @Override
    public String toString() {
        return "Setting [id=" + id + ", name=" + name + ", value=" + value + "]";
    }
}
