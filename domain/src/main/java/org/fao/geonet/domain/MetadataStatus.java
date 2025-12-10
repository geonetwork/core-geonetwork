/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.annotation.*;
import org.fao.geonet.domain.converter.JpaConverterJson;
import org.fao.geonet.entitylistener.AbstractEntityListenerManager;
import org.hibernate.annotations.JdbcTypeCode;
import org.jdom.Element;
import java.sql.Types;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import java.util.LinkedHashMap;

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
@Table(name = MetadataStatus.TABLE_NAME,
    indexes = {
        @Index(name="idx_metadatastatus_metadataid", columnList = "metadataid"),
        @Index(name="idx_metadatastatus_statusid", columnList = "statusid"),
        @Index(name="idx_metadatastatus_userid", columnList = "userid"),
        @Index(name="idx_metadatastatus_owner", columnList = "owner"),
        @Index(name="idx_metadatastatus_changedate", columnList = "changedate")
    }
)
@EntityListeners(MetadataStatus.EntityListener.class)
@SequenceGenerator(name = MetadataStatus.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)

public class MetadataStatus extends GeonetEntity {
    public static final String ID_SEQ_NAME = "metadataStatus_id_seq";

    public static final String TABLE_NAME = "MetadataStatus";
    public static final String ID_COLUMN_NAME = "id";
    public static final String CHANGE_DATE_COLUMN_NAME = "changeDate";
    public static final String DUE_DATE_COLUMN_NAME = "dueDate";
    public static final String CLOSE_DATE_COLUMN_NAME = "closeDate";
    public static final String METADATA_ID_COLUMN_NAME = "metadataId";
    public static final String STATUS_ID_COLUMN_NAME = "statusId";
    public static final String USER_ID_COLUMN_NAME = "userId";

    /**
     * The Root element of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_METADATA_STATUS = "metadataStatus";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_ID = "id";
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
    private int id;
    private ISODate _changedate;
    private int metadataId;
    private int userId;
    private String changeMessage;
    private StatusValue statusValue;
    private int _owner;
    private ISODate _duedate;
    private ISODate _closedate;
    private String previousState;
    private String currentState;
    private MetadataStatus relatedMetadataStatus;
    private String uuid;
    private LinkedHashMap<String, String> titles;

    /**
     * The id of the metadata status. This is a generated value and not controlled by the developer.
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    /**
     * Set the id of the metadata status. This is typically set by the JPA entity manager and should only
     * be set by the developer when they want to merge new data with an existing entity or want to
     * perform query by example. But even then it is not generally recommended.
     *
     * @param id the id.
     */
    public void setId(int id) {
        this.id = id;
    }

  /**
     * Get the date of the status change in string form.
     *
     * @return the date of the status change in string form.
     */
    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = CHANGE_DATE_COLUMN_NAME, nullable = false, length = 30))
    public ISODate getChangeDate() {
        return _changedate;
    }

    /**
     * Set the date of the status change in string form.
     *
     * @param changedate the date of the status change in string form.
     */
    public void setChangeDate(ISODate changedate) {
        this._changedate = changedate;
    }

    /**
     * Get the id of the metadata the status is related to.
     *
     * @return the id of the metadata the status is related to.
     */
    @Column(name = METADATA_ID_COLUMN_NAME, nullable = false)
    public int getMetadataId() {
        return metadataId;
    }

    /**
     * Set the id of the metadata the status is related to.
     *
     * @param metadataId the id of the metadata the status is related to.
     */
    public void setMetadataId(int metadataId) {
        this.metadataId = metadataId;
    }

    /**
     * Get the user who is responsible for changing the status.
     *
     * @return the user who is responsible for changing the status.
     */
    @Column(name = USER_ID_COLUMN_NAME, nullable = false)
    public int getUserId() {
        return userId;
    }

    /**
     * Set the user who changed the status.
     *
     * @param userId the user who changed the status.
     */
    public void setUserId(int userId) {
        this.userId = userId;
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
     * of user is notified when the status changes.
     * <p/>
     * This is defined when a task is delegated to someone in particular and the
     * type is {@link StatusValueType#task}.
     * <p/>
     * Note the author of the status is set in {@link#setId(Id)}.
     *
     * @return the user responsible for this task.
     */
    // TODO: set FK to user table
    // @MapsId("ownerId")
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "ownerId", referencedColumnName = "id")
    @Column(nullable = false)
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
    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = DUE_DATE_COLUMN_NAME, nullable = true, length = 30))
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
    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = CLOSE_DATE_COLUMN_NAME, nullable = true, length = 30))
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = STATUS_ID_COLUMN_NAME, columnDefinition="integer", nullable = false, referencedColumnName = "id")
    public StatusValue getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(StatusValue statusValue) {
        this.statusValue = statusValue;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "relatedMetadataStatusId", columnDefinition="integer", nullable = true, referencedColumnName = "id")
    @JsonProperty(value = "relatedMetadataStatusId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public MetadataStatus getRelatedMetadataStatus() {
        return relatedMetadataStatus;
    }

    public void setRelatedMetadataStatus(MetadataStatus relatedMetadataStatus) {
        this.relatedMetadataStatus = relatedMetadataStatus;
    }

    /**
     * Get the uuid of the metadata.
     *
     * @return the uuid of the metadata.
     */
    @Column(nullable = false)
    public String getUuid() {
        return uuid;
    }

    /**
     * Set the metadata uuid.
     *
     * @param uuid the new uuid of the metadata
     */
    public void setUuid(@Nonnull String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the multilingual titles of the metadata as a json string.
     *
     * @return the multilingual titles of the metadata.
     */
    // Todo apply this once jpa is upgraded - cannot be applied due to the following bug.
    //      Also remove getTitlesString and setTitlesString - they were only created for JPA
    // https://hibernate.atlassian.net/browse/HHH-10818
    //@Lob
    //@JdbcTypeCode(Types.LONGVARCHAR)
    //@Basic(fetch = FetchType.LAZY)
    //@Convert(converter = JpaConverterJson.class)
    @Transient
    public LinkedHashMap<String, String> getTitles() {
        return titles;
    }

    @Column(name = "titles")
    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    @Basic(fetch = FetchType.LAZY)
    @JsonProperty(value = "titles")
    @JsonRawValue
    @Deprecated // Use getTitles() when possible as this will be depreciated once jpa is upgraded
    public String getTitlesString() {
        JpaConverterJson jpaConverterJson = new JpaConverterJson();
        return jpaConverterJson.convertToDatabaseColumn(getTitles());
    }

    @Deprecated // Use setTitles() when possible as this will be depreciated once jpa is upgraded
    public void setTitlesString(String titles) {
        setTitles(titles);
    }

    /**
     * Set the metadata multilingual titles as json string.
     *
     * @param titles the new titles related to the metadata record at the time the status was created.
     */
    public void setTitles(LinkedHashMap<String, String> titles) {
        this.titles = titles;
    }

    /**
     * Set the metadata multilingual titles as json string.
     *
     * @param titles the new titles related to the metadata record at the time the status was created.
     */
    public void setTitles(String titles) {
        if (titles != null) {
            JpaConverterJson jpaConverterJson = new JpaConverterJson();
            this.titles = (LinkedHashMap<String, String>) jpaConverterJson.convertToEntityAttribute(titles);
        }
    }

    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    @Basic(fetch = FetchType.LAZY)
    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
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
                .addContent(new Element(EL_ID).setText(String.valueOf(getId())))
                .addContent(new Element(EL_STATUS_ID).setText(String.valueOf(getStatusValue().getId())))
                .addContent(new Element(EL_USER_ID).setText(String.valueOf(getUserId())))
                .addContent(new Element(EL_CHANGE_DATE).setText(getChangeDate().getDateAndTime()))
                .addContent(new Element(EL_CHANGE_MESSAGE).setText(getChangeMessage()))
                .addContent(new Element(EL_NAME).setText(getStatusValue().getName()));
    }

    public static class EntityListener extends AbstractEntityListenerManager<MetadataStatus> {
    }

	@Override
	public String toString() {
		return "MetadataStatus [" + "id=" + id + ", "
                + "_metadataId=" + metadataId + ", "
                + "_statusId=" + statusValue.getId() + ", "
                + "_userId=" + userId + ", "
                + (_changedate != null ? "changeDate=" + getChangeDate().getDateAndTime() + ", " : "")
                + (uuid != null ? "uuid=" + uuid + ", " : "")
				+ (changeMessage != null ? "changeMessage=" + changeMessage + ", " : "")
				+ (statusValue != null ? "statusValue=" + statusValue + ", " : "")
                + "_owner=" + _owner + ", "
				+ (_duedate != null ? "_duedate=" + _duedate + ", " : "")
				+ (_closedate != null ? "_closedate=" + _closedate + ", " : "")
				+ (previousState != null ? "previousState=" + previousState + ", " : "")
				+ (currentState != null ? "currentState=" + currentState : "")
                + (relatedMetadataStatus != null ? "relatedMetadataStatusId=" + relatedMetadataStatus.getId() : "")
                + "]";
	}
}
