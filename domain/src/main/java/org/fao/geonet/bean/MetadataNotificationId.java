package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * Id of a MetadataNotification object.
 *
 * @author Jesse
 */
public class MetadataNotificationId implements Serializable {
    private static final long serialVersionUID = 8167301479650105617L;
    private int metadataId;
    private int notifierId;
	public int getMetadataId() {
		return metadataId;
	}
	public void setMetadataId(int metadataId) {
		this.metadataId = metadataId;
	}
	public int getNotifierId() {
		return notifierId;
	}
	public void setNotifierId(int notifierId) {
		this.notifierId = notifierId;
	}

}
