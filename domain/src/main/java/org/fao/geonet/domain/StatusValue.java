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
import org.springframework.http.MediaType;

import javax.persistence.*;

import java.util.Arrays;
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
    public enum Events {
        RECORDCREATED(50, false, MediaType.APPLICATION_JSON, null),
        RECORDUPDATED(51, true, MediaType.APPLICATION_XML, MediaType.APPLICATION_XML),
        ATTACHMENTADDED(52, false, MediaType.TEXT_PLAIN, null),
        ATTACHMENTDELETED(53, false, null, MediaType.TEXT_PLAIN),
        RECORDOWNERCHANGE(54, false, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON),
        RECORDGROUPOWNERCHANGE(55, false, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON),
        RECORDPRIVILEGESCHANGE(56, false, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON),
        RECORDCATEGORYCHANGE(57, false, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON),
        RECORDVALIDATIONTRIGGERED(58, false, MediaType.TEXT_PLAIN, null),
        RECORDSTATUSCHANGE(59, false, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON),
        RECORDPROCESSINGCHANGE(60, true, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML),
        RECORDDELETED(61, true, null, MediaType.APPLICATION_XML),
        RECORDIMPORTED(62, false, MediaType.APPLICATION_JSON, null),
        RECORDRESTORED(63, true, MediaType.APPLICATION_XML, MediaType.APPLICATION_XML);

        /**
         * The id of the event.
         */
        private final Integer id;
        /**
         * The restore function currently supports these states
         */
        private final boolean isSupportedRestoreStatus;

        /**
         * Indicates mime type of the current state will be stored in.
         * This is generally json or xml and if it is null then the state is not supported.
         */
        private final MediaType currentStateFormat;

        /**
         * Indicates mime type of the previous state will be stored in.
         * This is generally json or xml and if it is null then the state is not supported.
         */
        private final MediaType previousStateFormat;

        /**
         * Constructor.
         *
         * @param id the id of the event.
         * @param isSupportedRestoreStatus the restore function currently supports these states.
         * @param currentStateFormat indicates mime type of the current state will be stored in.
         * @param previousStateFormat indicates mime type of the current state will be stored in.
         */
        Events(Integer id, boolean isSupportedRestoreStatus, MediaType currentStateFormat, MediaType previousStateFormat) {
            this.id = id;
            this.isSupportedRestoreStatus = isSupportedRestoreStatus;
            this.currentStateFormat = currentStateFormat;
            this.previousStateFormat = previousStateFormat;
        }

        /**
         * Get the id of the event.
         *
         * @return the id of the event.
         */
        public Integer getId() {
            return id;
        }

        /**
         * Get the event from the id.
         *
         * @param id the id of the event.
         * @return the event.
         */
        public static Events fromId(Integer id) {
            return Arrays.stream(values())
                .filter(event -> event.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No event found with id: " + id));
        }

        /**
         * Get the code of the event.
         * The code currently is the string representation of the id.
         *
         * @return the code of the event.
         */
        public String getCode() {
            return String.valueOf(id);
        }

        /**
         * Get the event from the code.
         *
         * @param code the code of the event.
         * @return the event.
         */
        public static Events fromCode(String code) {
            return Arrays.stream(values())
                .filter(event -> event.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No event found with code: " + code));
        }

        /**
         * Get the mime type of the current state will be stored in.
         *
         * @return the mime type of the current state will be stored in.
         */
        public MediaType getCurrentStateFormat() {
            return currentStateFormat;
        }

        /**
         * Get the mime type of the previous state will be stored in.
         *
         * @return the mime type of the previous state will be stored in.
         */
        public MediaType getPreviousStateFormat() {
            return previousStateFormat;
        }

        /**
         * Identify if the current status supports restoring the values.
         * This is mostly for restoring xml metadata records.
         *
         * @return true if supported.
         */
        public boolean isSupportedRestoreStatus() {
            return isSupportedRestoreStatus;
        }

        /**
         * Get an array of the event that support restoring statuses.
         * This is mostly for restoring xml metadata records.
         *
         * @return list of events with isSupportedRestoreStatus set to true.
         */
        public static Events[] getSupportedRestoreStatuses() {
            return Arrays.stream(values())
                .filter(Events::isSupportedRestoreStatus)
                .toArray(Events[]::new);
        }
    }
}
