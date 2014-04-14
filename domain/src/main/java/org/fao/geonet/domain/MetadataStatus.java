package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.AbstractEntityListenerManager;
import org.jdom.Element;

import javax.persistence.*;

/**
 * An entity that represents a status change of a metadata.
 * <p/>
 * Note: I am not the author of metadata status, but it appears that this tracks the history as well since the Id consists of the User,
 * date, metadata and statusvalue of the metadata status change.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "MetadataStatus")
@EntityListeners(MetadataStatus.EntityListener.class)
public class MetadataStatus extends GeonetEntity {
    public static class EntityListener extends AbstractEntityListenerManager<MetadataStatus> {}
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
     * Get the change message, the message that describes the change in status. It is application specific.
     *
     * @return the change message
     */
    @Column(length = 2048, nullable = false)
    public String getChangeMessage() {
        return changeMessage;
    }

    /**
     * Set the change message, the message that describes the change in status. It is application specific.
     *
     * @param changeMessage the change message
     */
    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
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

    @Transient
    public Element getAsXml() {
        return new Element(EL_METADATA_STATUS)
                .addContent(new Element(EL_STATUS_ID).setText(String.valueOf(getId().getStatusId())))
                .addContent(new Element(EL_USER_ID).setText(String.valueOf(getId().getUserId())))
                .addContent(new Element(EL_CHANGE_DATE).setText(getId().getChangeDate().getDateAndTime()))
                .addContent(new Element(EL_CHANGE_MESSAGE).setText(getChangeMessage()))
                .addContent(new Element(EL_NAME).setText(getStatusValue().getName()));
    }
}
