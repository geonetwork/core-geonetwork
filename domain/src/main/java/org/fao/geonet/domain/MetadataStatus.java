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

import org.fao.geonet.entitylistener.AbstractEntityListenerManager;
import org.jdom.Element;

import javax.persistence.*;

/**
 * An entity that represents a status change of a metadata.
 *
 * @link StatusValue
 *
 *       <p/>
 *       This tracks the history as well since the Id consists of the User,
 *       date, metadata and statusvalue of the metadata status change.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "MetadataStatus",
    indexes = {
        @Index(name="idx_metadatastatus_metadataid", columnList = "metadataid"),
        @Index(name="idx_metadatastatus_statusid", columnList = "statusid"),
        @Index(name="idx_metadatastatus_userid", columnList = "userid"),
        @Index(name="idx_metadatastatus_owner", columnList = "owner"),
        @Index(name="idx_metadatastatus_changedate", columnList = "changedate")
    }
)
@EntityListeners(MetadataStatus.EntityListener.class)
public class MetadataStatus extends GeonetEntity {
    /**
     * The Root element of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_METADATA_STATUS = "metadataStatus";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_STATUS_ID = "statusId";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_USER_ID = "userId";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_CHANGE_DATE = "changeDate";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_CHANGE_MESSAGE = "changeMessage";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_NAME = "name";
    private MetadataStatusId id = new MetadataStatusId();
    private String changeMessage;
    private StatusValue statusValue;
    private int _owner;
    private ISODate _duedate;
    private ISODate _closedate;
    private String previousState;
    private String currentState;

    /**
     * Get the id object of this metadata status object.
     *
     * @return the id object of this metadata status object.
     */
    @EmbeddedId
    public MetadataStatusId getId() {
        return id;
    }

    /**
     * Set the id object of this metadata status object.
     *
     * @param id the id object of this metadata status object.
     */
    public void setId(MetadataStatusId id) {
        this.id = id;
    }

    /**
     * Get the change message, the message that describes the change in status. It
     * is application specific.
     *
     * @return the change message
     */
    @Column(length = 2048, nullable = false)
    public String getChangeMessage() {
        return changeMessage;
    }

    /**
     * Set the change message, the message that describes the change in status. It
     * is application specific.
     *
     * @param changeMessage the change message
     */
    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
    }

    /**
     * Return the owner of the metadata status.
     * <p/>
     *
     * This may be null for {@link StatusValueType#workflow} type as usually a list
     * of user is notified when the status change.
     *
     * This is defined when a task is delegated to someone in particular and the
     * type is {@link StatusValueType#task}.
     *
     *
     * Note the author of the status is set in {@link #setId(MetadataStatusId)}.
     *
     * @return the user responsible for this task.
     */
    // TODO: set FK to user table
    // @MapsId("ownerId")
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "ownerId", referencedColumnName = "id")
    public Integer getOwner() {
        return _owner;
    }

    /**
     * Set the owner of the metadata status.
     *
     * @param ownerId the owner of the metadata status.
     * @return this entity object
     */
    public MetadataStatus setOwner(Integer ownerId) {
        if (ownerId != null) {
            this._owner = ownerId;
        }
        return this;
    }

    /**
     * Get the due date of the status in string form.
     * <p/>
     * This date is set when the action needs to be addressed before a certain time.
     *
     * @return the due date of the status change in string form.
     */
    @AttributeOverride(name = "dateAndTime", column = @Column(name = "dueDate", nullable = true, length = 30))
    public ISODate getDueDate() {
        return _duedate;
    }

    /**
     * Set the due date of the status change in string form.
     *
     * @param duedate the due date of the status change in string form.
     */
    public MetadataStatus setDueDate(ISODate duedate) {
        this._duedate = duedate;
        return this;
    }

    /**
     * Get the close date of the status in string form.
     * <p/>
     * Indicate if a task is finished. It is set when the owner did his/her work.
     *
     * @return the close date of the status change in string form.
     */
    @AttributeOverride(name = "dateAndTime", column = @Column(name = "closeDate", nullable = true, length = 30))
    public ISODate getCloseDate() {
        return _closedate;
    }

    /**
     * Set the close date of the status change in string form.
     *
     * @param closedate the close date of the status change in string form.
     */
    public MetadataStatus setCloseDate(ISODate closedate) {
        this._closedate = closedate;
        return this;
    }

    @ManyToOne
    @JoinColumn(name = "statusId", nullable = false, insertable = false, updatable = false)
    @MapsId("statusId")
    public StatusValue getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(StatusValue statusValue) {
        this.statusValue = statusValue;
        this.getId().setStatusId(statusValue.getId());
    }

    @Column
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    @Column
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    @Transient
    public Element getAsXml() {
        return new Element(EL_METADATA_STATUS)
                .addContent(new Element(EL_STATUS_ID).setText(String.valueOf(getId().getStatusId())))
                .addContent(new Element(EL_USER_ID).setText(String.valueOf(getId().getUserId())))
                .addContent(new Element(EL_CHANGE_DATE).setText(getId().getChangeDate().getDateAndTime()))
                .addContent(new Element(EL_CHANGE_MESSAGE).setText(getChangeMessage()))
                .addContent(new Element(EL_NAME).setText(getStatusValue().getName()));
    }

    public static class EntityListener extends AbstractEntityListenerManager<MetadataStatus> {
    }

	@Override
	public String toString() {
		return "MetadataStatus [" + (id != null ? "id=" + id + ", " : "")
				+ (changeMessage != null ? "changeMessage=" + changeMessage + ", " : "")
				+ (statusValue != null ? "statusValue=" + statusValue + ", " : "") + "_owner=" + _owner + ", "
				+ (_duedate != null ? "_duedate=" + _duedate + ", " : "")
				+ (_closedate != null ? "_closedate=" + _closedate + ", " : "")
				+ (previousState != null ? "previousState=" + previousState + ", " : "")
				+ (currentState != null ? "currentState=" + currentState : "") + "]";
	}
}
