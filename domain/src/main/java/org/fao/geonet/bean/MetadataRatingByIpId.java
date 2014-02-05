package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * An Id object for {@link MetadataRatingByIp}
 * 
 * @author Jesse
 */
public class MetadataRatingByIpId implements Serializable {

	private static final long serialVersionUID = 2793801901676171677L;
	private int _metadataId;
	private String _ipAddress;

	public int get_metadataId() {
		return _metadataId;
	}

	public void set_metadataId(int _metadataId) {
		this._metadataId = _metadataId;
	}

	public String get_ipAddress() {
		return _ipAddress;
	}

	public void set_ipAddress(String _ipAddress) {
		this._ipAddress = _ipAddress;
	}

}
