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

import org.fao.geonet.entitylistener.StatusValueEntityListenerManager;

import javax.persistence.*;

import java.util.Map;

/**
 * One of the enumerated status options that a metadata can be.
 *
 * By default a metadata has no status ie. UNKNOWN.
 *
 * The first step of the workflow (ie. DRAFT) can be automatically assigned for
 * record created on a group (
 * {@link org.fao.geonet.kernel.datamanager.IMetadataStatus#activateWorkflowIfConfigured}
 *
 * There is 2 types of status value:
 * <ul>
 * <li>One is part of the general workflow (by default)</li>
 * <li>One is independent task that may be triggered on demand</li>
 * </ul>
 *
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "StatusValues")
@Cacheable
@EntityListeners(StatusValueEntityListenerManager.class)
@SequenceGenerator(name = StatusValue.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class StatusValue extends Localized {
    static final String ID_SEQ_NAME = "status_value_id_seq";
    private int _id;
    private String _name;
    private char _reserved = Constants.YN_FALSE;
    private StatusValueType type = StatusValueType.workflow;
    private StatusValueNotificationLevel notificationLevel;
    private int displayOrder;

    /**
     * Get the id of the StatusValue object. This is a generated value and as such
     * new instances should not have this set as it will simply be ignored and could
     * result in reduced performance.
     *
     * @return the id of the StatusValue object
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the StatusValue object. This is a generated value and as such
     * new instances should not have this set as it will simply be ignored and could
     * result in reduced performance.
     *
     * @param id the id of the StatusValue object
     */
    public void setId(final int id) {
        this._id = id;
    }

    /**
     * Get the name of the StatusValue object. This is a required property.
     *
     * @return the name of the StatusValue object.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the StatusValue object. This is a required property.
     *
     * @param name the name of the StatusValue object.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * For backwards compatibility we need the reserved column to be either 'n' or
     * 'y'. This is a workaround to allow this until future versions of JPA that
     * allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "reserved", nullable = false, length = 1)
    protected char getReserved_JpaWorkaround() {
        return _reserved;
    }

    /**
     * Set the column value.
     *
     * @param reserved Constants.YN_ENABLED for true or Constants.YN_DISABLED for
     *                 false.
     */
    protected char setReserved_JpaWorkaround(final char reserved) {
        return _reserved = reserved;
    }

    /**
     * Return true if this is a reserved StatusValue.
     *
     * @return true if this is a reserved StatusValue.
     */
    @Transient
    public boolean isReserved() {
        return Constants.toBoolean_fromYNChar(getReserved_JpaWorkaround());
    }

    /**
     * Set true if this is a reserved StatusValue.
     *
     * @param reserved true if this is a reserved StatusValue.
     */
    public void setReserved(final boolean reserved) {
        setReserved_JpaWorkaround(Constants.toYN_EnabledChar(reserved));
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "StatusValuesDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    /**
     * Get the order to display this value in the UI. This is just a hint to the ui
     * that provides an priority/importance of the status value compared to the
     * others.
     *
     * @return the order to display this value in the UI.
     */
    @Column(name = "displayorder")
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Set the order to display this value in the UI. This is just a hint to the ui
     * that provides an priority/importance of the status value compared to the
     * others.
     *
     * @param displayOrder the order to display this value in the UI
     */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * @return the type
     */
    @Column(nullable = true, name = "type")
    @Enumerated(EnumType.STRING)
    public StatusValueType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(StatusValueType type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    @Column(nullable = true, name = "notificationLevel")
    @Enumerated(EnumType.STRING)
    public StatusValueNotificationLevel getNotificationLevel() {
        return notificationLevel;
    }

    /**
     * @param notificationLevel the notificationLevel to set
     */
    public void setNotificationLevel(StatusValueNotificationLevel notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    /**
     * List of predefined status part of the workflow. Those values are the default
     * one for GeoNetwork and may be modified in the database.
     */
    public static final class Status {
        public static final String DRAFT = "1";
        public static final String APPROVED = "2";
        public static final String RETIRED = "3";
        public static final String SUBMITTED = "4";
    }

    /**
     * List of predefined status part of the events. Those values are the default
     * one for GeoNetwork and may be modified in the database.
     */
    public static final class Events {
        public static final String RECORDCREATED = "50";
        public static final String RECORDUPDATED = "51";
        public static final String ATTACHMENTADDED = "52";
        public static final String ATTACHMENTDELETED = "53";
        public static final String RECORDOWNERCHANGE = "54";
        public static final String RECORDGROUPOWNERCHANGE = "55";
        public static final String RECORDPRIVILEGESCHANGE = "56";
        public static final String RECORDCATEGORYCHANGE = "57";
        public static final String RECORDVALIDATIONTRIGGERED = "58";
        public static final String RECORDSTATUSCHANGE = "59";
        public static final String RECORDPROCESSINGCHANGE = "60";
        public static final String RECORDDELETED = "61";
        public static final String RECORDIMPORTED = "62";
    }
}
