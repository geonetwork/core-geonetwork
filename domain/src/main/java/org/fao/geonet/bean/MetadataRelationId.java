package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * Id class for Metadata relation.
 * 
 * @author Jesse
 */
public class MetadataRelationId implements Serializable {
	private static final long serialVersionUID = -2705273953015744638L;

	private int _metadataId;
	private int _relatedId;

	public int get_metadataId() {
		return _metadataId;
	}

	public void set_metadataId(int _metadataId) {
		this._metadataId = _metadataId;
	}

	public int get_relatedId() {
		return _relatedId;
	}

	public void set_relatedId(int _relatedId) {
		this._relatedId = _relatedId;
	}

}
