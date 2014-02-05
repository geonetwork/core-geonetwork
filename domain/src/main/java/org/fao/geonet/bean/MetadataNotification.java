package org.fao.geonet.bean;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.MetadataNotificationAction;

/**
 * An entity representing a metadata related notification that has been made or is pending.
 *
 * @author Jesse
 */
public class MetadataNotification extends GeonetEntity {
	private static final long serialVersionUID = 2320937039405670248L;
	private MetadataNotificationId _id;
    private char _notified = Constants.YN_FALSE;
    private String _metadataUuid;
    private MetadataNotificationAction _action;
    private String _errorMessage;
	public MetadataNotificationId get_id() {
		return _id;
	}
	public void set_id(MetadataNotificationId _id) {
		this._id = _id;
	}
	public char get_notified() {
		return _notified;
	}
	public void set_notified(char _notified) {
		this._notified = _notified;
	}
	public String get_metadataUuid() {
		return _metadataUuid;
	}
	public void set_metadataUuid(String _metadataUuid) {
		this._metadataUuid = _metadataUuid;
	}
	public MetadataNotificationAction get_action() {
		return _action;
	}
	public void set_action(MetadataNotificationAction _action) {
		this._action = _action;
	}
	public String get_errorMessage() {
		return _errorMessage;
	}
	public void set_errorMessage(String _errorMessage) {
		this._errorMessage = _errorMessage;
	}

}
