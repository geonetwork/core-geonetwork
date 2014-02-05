package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * Id object for the {@link MetadataValidation} entity.
 * 
 * @author Jesse
 */
public class MetadataValidationId implements Serializable {
	private static final long serialVersionUID = -7162983572434017017L;
	private int _metadataId;
	private String _validationType;

	public int get_metadataId() {
		return _metadataId;
	}

	public void set_metadataId(int _metadataId) {
		this._metadataId = _metadataId;
	}

	public String get_validationType() {
		return _validationType;
	}

	public void set_validationType(String _validationType) {
		this._validationType = _validationType;
	}

}
