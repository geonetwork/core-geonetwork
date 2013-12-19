package org.fao.geonet.bean;

import org.fao.geonet.domain.StatusValue;

/**
 * An entity that represents a status change of a metadata.
 * <p/>
 * Note: I am not the author of metadata status, but it appears that this tracks
 * the history as well since the Id consists of the User, date, metadata and
 * statusvalue of the metadata status change.
 * 
 * @author Jesse
 */
public class MetadataStatus extends GeonetEntity {
	private static final long serialVersionUID = 3913761533850728587L;
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

	public MetadataStatusId getId() {
		return id;
	}

	public void setId(MetadataStatusId id) {
		this.id = id;
	}

	public String getChangeMessage() {
		return changeMessage;
	}

	public void setChangeMessage(String changeMessage) {
		this.changeMessage = changeMessage;
	}

	public StatusValue getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(StatusValue statusValue) {
		this.statusValue = statusValue;
	}

	public static String getElMetadataStatus() {
		return EL_METADATA_STATUS;
	}

	public static String getElStatusId() {
		return EL_STATUS_ID;
	}

	public static String getElUserId() {
		return EL_USER_ID;
	}

	public static String getElChangeDate() {
		return EL_CHANGE_DATE;
	}

	public static String getElChangeMessage() {
		return EL_CHANGE_MESSAGE;
	}

	public static String getElName() {
		return EL_NAME;
	}

}
